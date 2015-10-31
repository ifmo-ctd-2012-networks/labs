__author__ = "Polyarnyi Nickolay"

import json
import socket
import asyncio
import logging
import concurrent
import concurrent.futures

from task2.utils.serialization import create_object
from task2.entity.messages import TYPE_TO_CLASS, MessageType
from task2.entity.messages import Message
from task2.utils.support import AsyncExecutor


ENCODING = 'utf-8'
BROADCAST_MESSAGE_SIZE_LIMIT = 4 * 1024


class TCPMessenger:

    def __init__(self, port, io_executor: AsyncExecutor, loop: asyncio.events.AbstractEventLoop=None):
        self._port = port

        self._io_executor = io_executor
        self._loop = loop or asyncio.get_event_loop()

        self._server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._server_socket.bind(('', self._port))  # TODO: '' should be replaced with self._inet_address
        self._server_socket.setblocking(1)

        self._logger = logging.getLogger('TCPMessenger:{}'.format(self._port))

    def start(self):
        self._server_socket.listen(1)
        self._logger.debug('Listening...')

    def stop(self):
        self._server_socket.close()

    @asyncio.coroutine
    def listen_message(self):
        # conn, address = yield from self._loop.sock_accept(self._server_socket)
        conn, address = yield from self._io_executor.map(self._server_socket.accept)
        conn.setblocking(1)
        self._logger.debug('Accepted connection from {}...'.format(address))
        data_bytes = b''
        with conn:
            while True:
                # part = yield from self._loop.sock_recv(conn, 1024)
                part = yield from self._io_executor.map(conn.recv, 1024)
                if not part:
                    break
                else:
                    data_bytes += part
        self._logger.debug('Received {} bytes from {}!'.format(len(data_bytes), address))
        return address, data_bytes

    @asyncio.coroutine
    def send_message(self, host, data_bytes, port=None):
        port = port or self._port
        self._logger.debug('Connecting to {}:{} (to send {} bytes)...'.format(host, port, len(data_bytes)))
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.setblocking(1)
        with client_socket:
            address = yield from self._loop.getaddrinfo(host, port, family=socket.AF_INET, type=socket.SOCK_STREAM)
            assert len(address) == 1
            address = address[0]

            yield from self._io_executor.map(client_socket.connect, address[4])
            # yield from self._loop.sock_connect(client_socket, address[4])

            yield from self._io_executor.map(client_socket.sendall, data_bytes)
            # yield from self._loop.sock_sendall(client_socket, data_bytes)
            client_socket._real_close()
        self._logger.debug('Sent {} bytes to {}!'.format(len(data_bytes), address[4]))


class UDPMessenger:

    def __init__(self, broadcast_address, port, io_executor: AsyncExecutor, loop: asyncio.events.AbstractEventLoop=None):
        self._broadcast_address = broadcast_address
        self._port = port

        self._io_executor = io_executor
        self._loop = loop or asyncio.get_event_loop()

        self._broadcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self._broadcast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        self._broadcast_socket.setblocking(1)

        self._listening_broadcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self._listening_broadcast_socket.bind(('', self._port))  # TODO: '' should be replaced with self._inet_address
        self._listening_broadcast_socket.setblocking(1)

        self._logger = logging.getLogger('UDPMessenger:{}'.format(self._port))

    def stop(self):
        self._broadcast_socket.close()
        self._listening_broadcast_socket.close()

    @asyncio.coroutine
    def listen_message(self):
        self._logger.debug('Receiving...')
        data_bytes, address = yield from self._io_executor.map(
            self._listening_broadcast_socket.recvfrom, BROADCAST_MESSAGE_SIZE_LIMIT)
        self._logger.debug('Received {} bytes from {}!'.format(len(data_bytes), address))
        return address, data_bytes

    @asyncio.coroutine
    def send_message(self, data_bytes):
        assert len(data_bytes) <= BROADCAST_MESSAGE_SIZE_LIMIT
        broadcast_address = (self._broadcast_address, self._port)

        self._logger.debug('Sending {} bytes to broadcast...'.format(len(data_bytes)))
        # self._broadcast_socket.sendto(data_bytes, broadcast_address)
        yield from self._io_executor.map(
            self._broadcast_socket.sendto, data_bytes, broadcast_address)
        self._logger.debug('{} bytes message broadcast!'.format(len(data_bytes)))


class Messenger:

    def __init__(self, mac, broadcast_address, broadcast_port, tcp_port, node_id=None, loop=None):
        self._mac = mac
        self._node_id = node_id
        self._nodes = {}

        self._loop = loop or asyncio.get_event_loop()
        self._io_executor = AsyncExecutor(5, self._loop)
        self._logger = logging.getLogger('Messenger')

        self._logger.info('Creating messengers...')
        self._tcp_messenger = TCPMessenger(tcp_port, self._io_executor, self._loop)
        self._udp_messenger = UDPMessenger(broadcast_address, broadcast_port, self._io_executor, self._loop)
        self._broadcast_listening = None
        self._tcp_listening = None

        self._daemon = None
        self._message_queue = asyncio.Queue()

    def start(self):
        self._logger.info('Starting TCP messenger...')
        self._tcp_messenger.start()
        self._daemon = asyncio.async(self._listen_messages_loop())
        self._logger.info('Messenger started!')

    def stop(self):
        if self._daemon is not None:
            self._daemon.cancel()
        if self._broadcast_listening is not None:
            self._broadcast_listening.cancel()
        if self._tcp_listening is not None:
            self._tcp_listening.cancel()
        self._io_executor.shutdown(wait=False)
        self._tcp_messenger.stop()
        self._udp_messenger.stop()

    def _serialize(self, message: Message):
        state = message.__getstate__()
        self._logger.debug('Serialized message: {}...'.format(state))

        return json.dumps(state).encode(encoding=ENCODING)

    def _deserialize(self, data_bytes) -> Message:
        message_state = json.loads(data_bytes.decode(encoding=ENCODING))
        self._logger.debug('Parsing message: {}...'.format(message_state))

        message_class = TYPE_TO_CLASS[MessageType((message_state['type'],))]
        return create_object(message_class, message_state)

    @asyncio.coroutine
    def _on_message(self, message: Message, address):
        node_id = message.author_node_id
        if node_id not in self._nodes:
            host = address[0]
            self._nodes[node_id] = host
            self._logger.info('New node: {} at {}! (total number: {})'.format(node_id, host, len(self._nodes)))
            yield from self.send_message(node_id, Message(MessageType.PING, self.node_id))

        if message.type == MessageType.TAKE_TOKEN:
            for node_id, host in message.nodes.items():
                if node_id not in self.nodes:
                    self.nodes[node_id] = host
                    self._logger.info('New node: {} at {}! (total number: {})'.format(node_id, host, len(self._nodes)))
                else:
                    assert host == self.nodes[node_id]

    @asyncio.coroutine
    def _listen_messages_loop(self):

        @asyncio.coroutine
        def listen_message(listen_message, channel):
            address, data_bytes = yield from listen_message()
            message = self._deserialize(data_bytes)
            yield from self._on_message(message, address)
            return message, channel

        self._broadcast_listening = self._broadcast_listening or asyncio.async(
            listen_message(self._udp_messenger.listen_message, 'udp'))
        self._tcp_listening = self._tcp_listening or asyncio.async(
            listen_message(self._tcp_messenger.listen_message, 'tcp'))

        while True:
            done, pending = yield from asyncio.wait([self._broadcast_listening, self._tcp_listening], return_when=concurrent.futures.FIRST_COMPLETED)

            for task in done:
                message, channel = yield from task
                if channel == 'udp':
                    self._broadcast_listening = asyncio.async(listen_message(self._udp_messenger.listen_message, 'udp'))
                else:
                    assert channel == 'tcp'
                    self._tcp_listening = asyncio.async(listen_message(self._tcp_messenger.listen_message, 'tcp'))
                self._message_queue.put_nowait(message)

    @asyncio.coroutine
    def take_message(self):
        message = yield from self._message_queue.get()
        self._logger.debug('Taking message: {}...'.format(message.type))
        return message

    @asyncio.coroutine
    def send_broadcast(self, message):
        message_bytes = self._serialize(message)
        yield from self._udp_messenger.send_message(message_bytes)

    @asyncio.coroutine
    def send_message(self, node_id, message):
        self._logger.debug('Sending "{}" message to node {}...'.format(message.type, node_id))
        host = self.nodes[node_id]
        yield from self._tcp_messenger.send_message(host, self._serialize(message))

    @property
    def node_id(self):
        return str(self._node_id or self._mac)

    @property
    def nodes(self):
        return self._nodes
