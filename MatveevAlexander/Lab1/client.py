import queue
import sys
import socket
import threading
import traceback
import time

from concurrent.futures import ThreadPoolExecutor


class Listener:

    BUF_SIZE = 4096
    PORT = 3439

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
                self.queue.join()
                self.socket.close()
                sys.exit(0)

    def handle(self):
        instances = {}
        counts = {}

        while True:
            while not self.queue.empty():
                try:
                    buf, _ = self.queue.get()
                    items = buf.split(','.encode('utf8'))
                    instances[items[0]] = (items[1], items[2], items[3])
                    counts[items[0]] = 5
                finally:
                    self.queue.task_done()

            for key in instances.keys():
                counts[key] -= 1
                if counts[key] == 0:
                    del instances[key]
                    del counts[key]

            # print('Instances: {0}'.format(instances))
            # print('Counts: {0}'.format(counts))
            time.sleep(5)

if __name__ == '__main__':
    Listener()
