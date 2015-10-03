import socket
from makers.ConnectsKeeper import ConnectPack
from makers.Settings import Settings


def client_main(ck):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(('', Settings.MY_PORT))

    while True:
        data, addr = s.recvfrom(300)

        mac = int.from_bytes(data[0:6], byteorder=Settings.MAC_BYTEORDER)
        k = int.from_bytes(data[6:7], byteorder='little', signed=True)
        if k <= 0 or len(data) < 7 + k + Settings.TIMESTAMP_LENGTH:
            ck.incorrect += 1
            continue
        try:
            name = data[7:7 + k].decode('utf-8')
        except UnicodeDecodeError:
            ck.incorrect += 1
            continue

        t = int.from_bytes(data[7 + k:7 + k + Settings.TIMESTAMP_LENGTH], byteorder=Settings.DATA_BYTEORDER)

        c = ConnectPack(mac, name)

        ck.add(c)
        ck.to_print.put("Receive msg. MAC: {0} name: {1} time: {2} addr: {3}".
                        format(c.mac_str, c.name, t, addr))
