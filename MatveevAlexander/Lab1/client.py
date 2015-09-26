import queue
import sys
import socket
import threading
import traceback
import time

from concurrent.futures import ThreadPoolExecutor


class Listener:

    BUF_SIZE = 4096
    PORT = 1234
    BYTEORDER = 'big'

    def __init__(self):
        self.pool = ThreadPoolExecutor(max_workers=1)
        self.queue = queue.Queue()

        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        self.socket.bind(('', Listener.PORT))

        handler = threading.Thread(target=self.handle)
        handler.daemon = True

        handler.start()

        self.listen()

    def listen(self):
        while True:
            try:
                (buf, address) = self.socket.recvfrom(Listener.BUF_SIZE)
                self.queue.put((buf, address))
                self.pool.submit(print, (buf, address))
            except KeyboardInterrupt:
                traceback.print_exc()
                self.socket.close()
                sys.exit(0)

    def handle(self):
        def get_data(buf):
            mac = int.from_bytes(buf[:6], Listener.BYTEORDER)
            buf = buf[6:]
            hostname_len = int.from_bytes(buf[:1], Listener.BYTEORDER)
            buf = buf[1:]
            hostname = str(buf[:hostname_len], encoding='utf8')
            buf = buf[hostname_len:]
            timestamp = int.from_bytes(buf, Listener.BYTEORDER)

            return mac, hostname_len, hostname, timestamp

        instances = {}
        counts = {}

        while True:
            try:
                while not self.queue.empty():
                    try:
                        buf, _ = self.queue.get()
                        mac, hostname_len, hostname, timestamp = get_data(buf)
                        instances[mac] = (hostname_len, hostname, timestamp)
                        counts[mac] = -1
                    finally:
                        self.queue.task_done()

                instances_tmp = {}
                counts_tmp = {}
                for key in instances.keys():
                    counts[key] += 1
                    if counts[key] < 5:
                        instances_tmp[key] = instances[key]
                        counts_tmp[key] = counts[key]

                instances = instances_tmp
                counts = counts_tmp
                for instance in instances:
                    self.pool.submit(print, 'Host: {0} Lost: {1}'.format(instances[instance], counts[instance]))

                time.sleep(5)
            except KeyboardInterrupt:
                break

if __name__ == '__main__':
    Listener()
