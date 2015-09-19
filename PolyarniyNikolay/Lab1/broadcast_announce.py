import sys
import uuid
import socket
import asyncio
import logging
from time import time
from tabulate import tabulate

DEFAULT_PORT = 2391
DEFAULT_BYTE_ORDER = 'big'
HOSTNAME_LIMIT_BYTE_SIZE = 255
HOSTNAME_ENCODING = 'utf-8'
DELAY_SECONDS = 5
MISSED_PACKETS_LIMIT = 5


@asyncio.coroutine
def broadcaster(hostname, port=DEFAULT_PORT, byteorder=DEFAULT_BYTE_ORDER):
    logger = logging.getLogger('Broadcaster:{} {}'.format(port, hostname))

    hostname_bytes = hostname.encode(encoding=HOSTNAME_ENCODING)
    assert len(hostname_bytes) < HOSTNAME_LIMIT_BYTE_SIZE

    broadcast_address = ('<broadcast>', port)
    broadcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    broadcast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    mac = uuid.getnode()
    message_prefix = 'MAC={} hostname={}'.format(mac, hostname)
    data_prefix = mac.to_bytes(6, byteorder) + len(hostname_bytes).to_bytes(1, byteorder) + hostname_bytes
    while True:
        unix_timestamp = int(time())
        timestamp_data = unix_timestamp.to_bytes(4, byteorder)

        message_data = data_prefix + timestamp_data
        broadcast_socket.sendto(message_data, broadcast_address)

        logger.info('Message sent: "{} timestamp={}" (Message bytes: {})'
                    .format(message_prefix, unix_timestamp, message_data))
        yield from asyncio.sleep(DELAY_SECONDS)


@asyncio.coroutine
def listener(on_message_callback=None, port=DEFAULT_PORT, loop=None):
    logger = logging.getLogger('Listener:{}'.format(port))
    loop = loop or asyncio.get_event_loop()

    broadcast_address = ('', port)
    broadcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    broadcast_socket.setblocking(0)
    broadcast_socket.bind(broadcast_address)
    while True:
        data = yield from loop.sock_recv(broadcast_socket, 6 + 1 + HOSTNAME_LIMIT_BYTE_SIZE + 4)
        logger.debug('Message: {}'.format(data))
        if on_message_callback:
            on_message_callback(data)


@asyncio.coroutine
def message_handler(queue, byteorder=DEFAULT_BYTE_ORDER, loop=None):
    logger = logging.getLogger('Handler')
    loop = loop or asyncio.get_event_loop()

    messages_by_mac = {}
    tracked_macs, missed_packets, last_packet = set(), {}, {}

    def update_broadcasters():
        macs_to_delete = set()

        for mac in tracked_macs:
            if mac not in messages_by_mac:
                missed_packets[mac] += 1
                if MISSED_PACKETS_LIMIT is not None and missed_packets[mac] >= MISSED_PACKETS_LIMIT:
                    macs_to_delete.add(mac)
            else:
                missed_packets[mac], last_packet[mac] = 0, messages_by_mac[mac]

        for mac, packet in messages_by_mac.items():
            if mac not in tracked_macs:
                tracked_macs.add(mac)
                missed_packets[mac], last_packet[mac] = 0, packet
        messages_by_mac.clear()

        def to_table(macs):
            return tabulate([[m, last_packet[m][2], last_packet[m][3], missed_packets[m]]
                             for m in sorted(macs)],
                            headers=['Mac', 'Hostname', 'Last packet timestamp', 'Missed packets'], tablefmt='grid')
        if len(macs_to_delete) > 0:
            logger.info('Deleted broadcasters:\n' + to_table(macs_to_delete))

        for mac in macs_to_delete:
            tracked_macs.remove(mac)
            del missed_packets[mac]
            del last_packet[mac]

        logger.info('Broadcasters:\n' + to_table(tracked_macs))

        loop.call_later(DELAY_SECONDS, update_broadcasters)

    update_broadcasters()
    while True:
        data = yield from queue.get()

        sender_mac, data = int.from_bytes(data[:6], byteorder), data[6:]
        hostname_bytes_len, data = data[0], data[1:]
        hostname, data = str(data[:hostname_bytes_len], encoding=HOSTNAME_ENCODING), data[hostname_bytes_len:]
        timestamp = int.from_bytes(data[:8], byteorder)

        logger.info('Message received: "MAC={} hostname={} timestamp={}"'
                    .format(sender_mac, hostname, timestamp))
        messages_by_mac[sender_mac] = (sender_mac, data, hostname, timestamp)


def main(hostname, port=DEFAULT_PORT):
    logger = logging.getLogger('Main')
    messages_queue = asyncio.Queue()

    tasks = asyncio.gather(
        asyncio.async(listener(lambda data: messages_queue.put_nowait(data), port)),
        asyncio.async(broadcaster(hostname, port)),
        asyncio.async(message_handler(messages_queue))
    )

    try:
        asyncio.get_event_loop().run_until_complete(tasks)
    except KeyboardInterrupt as e:
        logger.info('Stopped by user.')
        tasks.cancel()
        asyncio.get_event_loop().run_forever()
        tasks.exception()


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO,
                        format='%(relativeCreated)d [%(threadName)s]\t%(name)s [%(levelname)s]:\t %(message)s')

    args = sys.argv
    if len(args) not in [2, 3, 4]:
        print('Usage: hostname [port] [delay]\n\n   delay - in seconds')
    else:
        hostname, port = args[1], DEFAULT_PORT
        if len(args) >= 3:
            port = int(args[2])
        if len(args) >= 4:
            DELAY_SECONDS = float(args[3])
        main(hostname, port)
