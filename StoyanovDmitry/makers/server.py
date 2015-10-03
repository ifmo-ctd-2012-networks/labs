import time
import socket
from makers.ConnectsKeeper import ConnectsKeeper, ConnectPack
from makers.Settings import Settings


def server_main(ck):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(('', 0))
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    c = ConnectPack.make_localhost_pack()
    header = c.mac_bytes_be
    header += len(c.name).to_bytes(1, byteorder='little')
    header += c.name.encode()

    while True:
        t = int(time.time())
        data_time = t.to_bytes(Settings.TIMESTAMP_LENGTH, byteorder=Settings.DATA_BYTEORDER)

        s.sendto(header + data_time, ('<broadcast>', Settings.MY_PORT))

        ck.to_print.put("Send msg. My mac: {0} name: {1} time: {2}".format(c.mac_str, c.name, t))

        time.sleep(Settings.TIME_PAUSE_SENDING)
