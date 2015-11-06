__author__ = "Polyarnyi Nickolay"

import json
import socket
import asyncio
import logging
import concurrent
import concurrent.futures
from abc import abstractmethod

from task2.utils.serialization import create_object
from task2.entity.messages import TYPE_TO_CLASS, MessageType, TakeTokenMessage
from task2.entity.messages import Message
from task2.utils.support import AsyncExecutor, auto_cancellation, wrap_exc

ENCODING = 'utf-8'
BROADCAST_MESSAGE_SIZE_LIMIT = 4 * 1024


class ProtocolListener:
    def __init__(self):
        self._daemon = None
        self._message_queue = asyncio.Queue()
        self._on_message = None

    def start(self):
        self._daemon = wrap_exc(asyncio.async(self._listen_messages()), self._get_logger())

    def stop(self):
        if self._daemon is not None:
            self._daemon.cancel()
            self._daemon = None

    def set_on_message(self, cb_coroutine):
        self._on_message = cb_coroutine

    @asyncio.coroutine
    def take_message(self):
        message = yield from self._message_queue.get()
        self._get_logger().debug('Taking message: {}...'.format(message.type))
        return message

    def _deserialize(self, data_bytes) -> Message:
        message_state = json.loads(data_bytes.decode(encoding=ENCODING))
        self._get_logger().debug('Parsing message: {}...'.format(message_state))

        message_class = TYPE_TO_CLASS[MessageType((message_state['type'],))]
        return create_object(message_class, message_state)

    @asyncio.coroutine
    def _listen_messages(self):
        while True:
            address, data_bytes = yield from self.listen_message()

            message = self._deserialize(data_bytes)
            if self._on_message is not None:
                # noinspection PyCallingNonCallable
                wrap_exc(asyncio.async(self._on_message(message, address)), self._get_logger())
            self._message_queue.put_nowait(message)

    @abstractmethod
    def _get_logger(self):
        pass

    @abstractmethod
    def listen_message(self):
        pass


class TCPMessenger(ProtocolListener):
    def __init__(self, port, io_executor: AsyncExecutor, connection_timeout=2.0,
                 loop: asyncio.events.AbstractEventLoop = None):
        super().__init__()
        self._port = port
        self._connection_timeout = connection_timeout

        self._io_executor = io_executor
        self._loop = loop or asyncio.get_event_loop()

        self._server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._server_socket.bind(('', self._port))  # TODO: '' should be replaced with self._inet_address
        self._server_socket.setblocking(1)

        self._logger = logging.getLogger('TCPMessenger:{}'.format(self._port))

    def _get_logger(self):
        return self._logger

    def start(self):
        super(TCPMessenger, self).start()
        self._server_socket.listen(1)
        self._logger.debug('Listening...')

    def stop(self):
        super(TCPMessenger, self).stop()
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
            self._logger.debug('Connecting... {}'.format(address))

            assert len(address) == 1
            address = address[0]

            client_socket.settimeout(self._connection_timeout)
            try:
                yield from self._io_executor.map(client_socket.connect, address[4])
                # yield from self._loop.sock_connect(client_socket, address[4])

                self._logger.debug('Sending data... ({} bytes)'.format(len(data_bytes)))
                yield from self._io_executor.map(client_socket.sendall, data_bytes)
                # yield from self._loop.sock_sendall(client_socket, data_bytes)
                self._logger.debug('Data sent!')
            except OSError as e:
                self._logger.warn('Exception occurred while sending data to {}! ({})'.format(host, e))
                return False
            except Exception:
                self._logger.warn('Exception occurred while sending data to {}!'.format(host), exc_info=True)
                return False
        self._logger.debug('Sent {} bytes to {}!'.format(len(data_bytes), address[4]))
        return True


class UDPMessenger(ProtocolListener):
    def __init__(self, broadcast_address, port, io_executor: AsyncExecutor, listenable: bool,
                 loop: asyncio.events.AbstractEventLoop = None):
        super().__init__()
        self._broadcast_address = broadcast_address
        self._port = port

        self._io_executor = io_executor
        self._loop = loop or asyncio.get_event_loop()

        self._broadcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self._broadcast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        self._broadcast_socket.setblocking(1)
        if listenable:
            self._listening_broadcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            self._listening_broadcast_socket.bind(
                ('', self._port))  # TODO: '' should be replaced with self._inet_address
            self._listening_broadcast_socket.setblocking(1)
        else:
            self._listening_broadcast_socket = None
        self._logger = logging.getLogger('UDPMessenger:{}'.format(self._port))

    def _get_logger(self):
        return self._logger

    def stop(self):
        super(UDPMessenger, self).stop()
        self._broadcast_socket.close()
        if self._listening_broadcast_socket is not None:
            self._listening_broadcast_socket.close()

    @asyncio.coroutine
    def listen_message(self):
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
        self._lost_nodes = set()

        self._loop = loop or asyncio.get_event_loop()
        self._io_executor = AsyncExecutor(5, self._loop)
        self._logger = logging.getLogger('Messenger')

        self._logger.info('Creating messengers...')
        if tcp_port != 0:
            self._tcp_messenger = TCPMessenger(tcp_port, self._io_executor, loop=self._loop)
        if broadcast_port != 0:
            self._udp_messenger = UDPMessenger(broadcast_address, broadcast_port, self._io_executor, True, self._loop)

        self._listeners = {
            'tcp': self._tcp_messenger,
            'udp': self._udp_messenger,
        }

        for messenger in self._listeners.values():
            messenger.set_on_message(self._on_message)

        self._daemon = None
        self._message_queue = asyncio.Queue()

    def start(self):
        self._logger.info('Starting messengers...')
        for messenger in self._listeners.values():
            messenger.start()
        self._logger.info('Messengers started!')
        self._daemon = wrap_exc(asyncio.async(self._taking_messages_loop()), self._logger)

    def stop(self):
        if self._daemon is not None:
            self._daemon.cancel()
            self._daemon = None
        self._tcp_messenger.stop()
        self._udp_messenger.stop()
        self._io_executor.shutdown(wait=False)

    def _serialize(self, message: Message):
        state = message.__getstate__()
        self._logger.debug('Serialized message: {}...'.format(state))

        return json.dumps(state).encode(encoding=ENCODING)

    @asyncio.coroutine
    def _on_message(self, message: Message, address):
        node_id = message.author_node_id
        if node_id in self._lost_nodes:
            self._lost_nodes.remove(node_id)
            self._logger.info('Heard about lost node again! (node_id={}) (so lost nodes: {})'
                              .format(node_id, self._lost_nodes))
        if node_id not in self.nodes:
            host = address[0]
            self._add_node(node_id, host)
            wrap_exc(asyncio.async(self.send_message(node_id, Message(MessageType.PING, self.node_id))), self._logger)
        elif address[0] != self.nodes[node_id]:
            self._logger.error('Unexpected state! My nodes: {}. {} != {} (node_id={})'
                               .format(self.nodes, address[0], self.nodes[node_id], node_id))

        if message.type == MessageType.TAKE_TOKEN:
            assert isinstance(message, TakeTokenMessage)
            for node_id, host in message.nodes.items():
                if node_id not in self.nodes:
                    self._add_node(node_id, host)
                else:
                    if host != self.nodes[node_id]:
                        self._logger.error('Unexpected state! My nodes: {}. Nodes in message: {}. {} != {} (node_id={})'
                                           .format(self.nodes, message.nodes, host, self.nodes[node_id], node_id))

    def _add_node(self, node_id, host):
        self.nodes[node_id] = host
        self._logger.info('New node: {} at {}! (total number: {})'.format(node_id, host, len(self.nodes)))
        def format_node_id(node_id):

            return '{}{}'.format(node_id, '' if self.node_id != node_id else ' (me)')
        self._logger.info('Nodes order: {}'.format([(format_node_id(node_id), self.nodes[node_id]) for node_id in sorted(self.nodes.keys())]))

    @asyncio.coroutine
    def _taking_messages_loop(self):
        while True:
            takings = []
            for listener in self._listeners.values():
                takings.append(wrap_exc(asyncio.async(listener.take_message()), self._logger))
            with auto_cancellation(takings):
                done, pending = yield from asyncio.wait(takings, return_when=concurrent.futures.FIRST_COMPLETED)
                for f in done:
                    message = yield from f
                    self._message_queue.put_nowait(message)
                    self._logger.debug(
                        'Message {} putted. Queue size: {}.'.format(message.type, self._message_queue.qsize()))

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
        if node_id in self._lost_nodes:
            self._logger.error('Sending message to node, that seems to be lost! (node id={}, message type={})'
                               .format(node_id, message.type))
        self._logger.debug('Sending "{}" message to node {}...'.format(message.type, node_id))
        host = self.nodes[node_id]
        success = yield from self._tcp_messenger.send_message(host, self._serialize(message))
        if not success:
            self._lost_nodes.add(node_id)
            self._logger.warn('Node seems to be lost! (node_id={})'.format(node_id))
            if self.node_id == node_id:
                self._logger.error('We can not connect with our-self?! ')
        return success

    def get_next_available_node_id(self):
        nodes = sorted(self.nodes.keys())
        next_index = (nodes.index(self.node_id) + 1)
        for candidate_id in nodes[next_index:] + nodes[:next_index]:
            if candidate_id not in self._lost_nodes:
                return candidate_id

    @property
    def node_id(self):
        return str(self._node_id or self._mac)

    @property
    def nodes(self):
        return self._nodes
