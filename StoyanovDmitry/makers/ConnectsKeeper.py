import queue
import socket
from uuid import getnode as get_mac
from makers.Settings import Settings


class ConnectPack:
    def __init__(self, mac_int, name):
        self.mac_bytes_be = mac_int.to_bytes(6, byteorder=Settings.MAC_BYTEORDER)
        self.mac_str = ':'.join("%02x" % x for x in self.mac_bytes_be)
        self.mac_int = mac_int
        self.name = name
        self.lost = 0

    def __str__(self):
        return " | ".join([self.mac_str, self.name, " Lost:" + str(self.lost)])

    @staticmethod
    def make_localhost_pack():
        return ConnectPack(get_mac(), socket.gethostname())


class ConnectsKeeper:
    def __init__(self):
        self.to_print = queue.Queue()
        self.to_add = queue.Queue()
        self.__saved = dict()
        self.incorrect = 0

    def add(self, connect_pack):
        connect_pack.lost = 0
        self.__saved[connect_pack.mac_int] = connect_pack

    def increment_lost(self):
        for k, v in self.__saved.items():
            v.lost += 1

    def delete_lost(self):
        self.__saved = dict((k, v) for k, v in self.__saved.items() if v.lost < Settings.LOST_LIMIT)

    def status(self):
        return "---  Status  ---\n" + "\n".join(map(str, self.__saved.values())) + \
               "\n--- Incorrect get: " + str(self.incorrect) + "\n----------------\n"
