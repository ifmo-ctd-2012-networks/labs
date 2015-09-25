import sys
import socket
import traceback
import uuid
import time

from concurrent.futures import ThreadPoolExecutor


PORT = 1234
address = ('<broadcast>', PORT)
BYTEORDER = 'big'

s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

mac = uuid.getnode()
hostname = socket.gethostname()
hostname_len = len(hostname)
msg_to_send = mac.to_bytes(6, BYTEORDER) + hostname_len.to_bytes(1, BYTEORDER) + hostname.encode('utf8')

pool = ThreadPoolExecutor(max_workers=1)

while True:
    try:
        timestamp = int(time.time())
        s.sendto(msg_to_send + timestamp.to_bytes(4, BYTEORDER), address)
        pool.submit(print, 'Send broadcast...')
        time.sleep(5)
    except KeyboardInterrupt:
        traceback.print_exc()
        s.close()
        sys.exit(0)

