__author__ = "Polyarnyi Nickolay"

import sys
import yaml
import logging
import asyncio
import netifaces

from task2 import config
from task2.entity.consts import Const
from task2.flow.context import Context
from task2.flow.states import LooserState
from task2.flow.messenger import Messenger, UDPMessenger
from task2.utils.support import get_interface_mac_broadcast, deep_merge, parse_debug_level, AsyncExecutor


def run_node(mac, broadcast_address, cfg):
    logger = logging.getLogger('Node')
    consts = Const()

    cfg_node = cfg['node']
    hostname = cfg_node['hostname'] or mac
    messenger = Messenger(mac, broadcast_address, cfg_node['broadcasting_port'], cfg_node['tcp_port'], node_id=hostname)
    messenger.start()
    debug_messenger = None  # UDPMessenger(broadcast_address, cfg_node['debug_port'], AsyncExecutor(2), False)

    context = Context(None, consts, messenger, debug_messenger)

    logger.debug('Running...')

    context.state = LooserState()
    try:
        asyncio.get_event_loop().set_debug(True)
        while True:
            asyncio.get_event_loop().run_until_complete(context.state.execute(context))
    except KeyboardInterrupt:
        logger.info('Stopped by user.')
        messenger.stop()
        debug_messenger.stop()


def main(cfg):
    logger = logging.getLogger('Main')

    logger.info('Available interfaces: {}.'.format(', '.join(netifaces.interfaces())))
    interface_name = cfg['node']['interface']
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

    run_node(mac, broadcast_address, cfg)


if __name__ == '__main__':
    args = sys.argv
    if len(args) != 2:
        print('Usage: [config]\n')
    else:
        config_path = sys.argv[1]
        with open(config_path) as f:
            cfg = yaml.load(f)
        cfg = deep_merge(config.create_default(), cfg)

        params = {'level': parse_debug_level(cfg['logging']['level']),
                  'format': cfg['logging']['format']}
        if cfg['logging']['filename'] is not None:
            params['filename'] = cfg['logging']['filename']
        logging.basicConfig(**params)

        main(cfg)
