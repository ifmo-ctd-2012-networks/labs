__author__ = "Polyarnyi Nickolay"

import asyncio
import netifaces
from contextlib import contextmanager
from concurrent.futures import ThreadPoolExecutor


class AsyncExecutor:

    def __init__(self, max_workers, loop: asyncio.events.AbstractEventLoop=None):
        self._executor = ThreadPoolExecutor(max_workers)
        self._loop = loop or asyncio.get_event_loop()

    def __del__(self):
        self.shutdown()

    @asyncio.coroutine
    def map(self, fn, *args):
        result = yield from self._loop.run_in_executor(self._executor, fn, *args)
        return result

    def shutdown(self, wait=True):
        if self._executor is None:
            return False
        else:
            self._executor.shutdown(wait=wait)
            self._executor = None
            return True


def get_interface_mac_broadcast(addrs):
    mac, broadcast = None, None
    if netifaces.AF_LINK in addrs:
        for addr in addrs[netifaces.AF_LINK]:
            mac = addr['addr']
    if netifaces.AF_INET in addrs:
        for addr in addrs[netifaces.AF_INET]:
            broadcast = addr['broadcast']
    return mac, broadcast


@contextmanager
def auto_cancellation(fs):
    yield
    for f in fs:
        f.cancel()
