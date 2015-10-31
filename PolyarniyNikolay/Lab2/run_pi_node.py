__author__ = "Polyarnyi Nickolay"

import sys
import logging
import asyncio
import netifaces

from task2.entity.consts import Const
from task2.flow.context import Context
from task2.flow.states import LooserState
from task2.flow.messenger import Messenger
from task2.utils.support import get_interface_mac_broadcast


BROADCAST_DEFAULT_PORT = 23914
TCP_DEFAULT_PORT = 8239


def run_node(mac, broadcast_address, hostname=None):
    logger = logging.getLogger('Node')
    consts = Const()

    messenger = Messenger(mac, broadcast_address, BROADCAST_DEFAULT_PORT, TCP_DEFAULT_PORT, node_id=hostname)
    messenger.start()

    context = Context(None, consts, messenger)

    logger.debug('Running...')

    context.state = LooserState()
    try:
        while True:
            asyncio.get_event_loop().run_until_complete(context.state.execute(context))
    except KeyboardInterrupt:
        logger.info('Stopped by user.')
        messenger.stop()


def main(hostname=None, interface_name=None):
    logger = logging.getLogger('Main')

    logger.info('Available interfaces: {}.'.format(', '.join(netifaces.interfaces())))
    if interface_name is not None:
        if interface_name not in netifaces.interfaces():
            logger.warn('There is no interface {}!'.format(interface_name))
            return
        else:
            mac, broadcast_address = get_interface_mac_broadcast(netifaces.ifaddresses(interface_name))
            if mac is None:
                logger.warn('MAC not found on interface {}!'.format(interface_name))
            if broadcast_address is None:
                logger.warn('Broadcast address not found on interface {}!'.format(interface_name))
    else:
        for interface_name in netifaces.interfaces():
            if interface_name.startswith('lo'):
                continue
            mac, broadcast_address = get_interface_mac_broadcast(netifaces.ifaddresses(interface_name))
            if mac is not None and broadcast_address is not None:
                break
        if interface_name is None:
            logger.warn('There is no available appropriate interfaces!')
            return
    logger.info('Used interface: {}. MAC: {}. Broadcast address: {}.'.format(interface_name, mac, broadcast_address))

    mac = int(mac.replace(':', ''), 16)

    logger.info('Integer MAC: {}.'.format(mac))

    run_node(mac, broadcast_address, hostname)


if __name__ == '__main__':
    logging.basicConfig(filename='node.log',
                        level=logging.DEBUG,
                        format='%(relativeCreated)d [%(threadName)s]\t%(name)s [%(levelname)s]:\t %(message)s')

    args = sys.argv
    if len(args) > 3:
        print('Usage: [interface] [hostname]\n')
    else:
        hostname, interface_name = None, None
        if len(args) >= 2:
            interface_name = args[1]
        if len(args) >= 3:
            hostname = args[2]

        main(hostname, interface_name)
