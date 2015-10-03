import time
import socket
from makers.ConnectsKeeper import ConnectsKeeper, ConnectPack
from makers.Settings import Settings
from random import randint


def server_main_spam(ck):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(('', 0))
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    c = ConnectPack.make_localhost_pack()
    header = c.mac_bytes_be
    header += len(c.name).to_bytes(1, byteorder='little')
    header += c.name.encode()

    while True:
        header = randint(0, 255 ** 1000).to_bytes(1000, byteorder='big')
        s.sendto(header, ('<broadcast>', Settings.MY_PORT))
        # ck.to_print.put("Send spam msg.")
        time.sleep(Settings.TIME_PAUSE_SPAM)
