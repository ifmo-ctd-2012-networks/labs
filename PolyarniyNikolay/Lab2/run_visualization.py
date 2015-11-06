__author__ = ["Polyarnyi Nickolay", "Ilya Isaev"]

import sys
import logging
import netifaces
import datetime

import yaml
import asyncio
import concurrent

from task2 import config
from task2.flow.messenger import UDPMessenger
from task2.utils.support import get_interface_mac_broadcast, deep_merge, parse_debug_level, AsyncExecutor, wrap_exc
from task2.utils.serialization import create_object, construct_serial_code
from task2.entity.messages import ChangingStateBroadcast
import asyncio
from aiohttp import web


def on_message(message, logger):
    logger.info(message)


udp_messenger = None
logger = None
state = {}


class Node:
    def __init__(self, message: ChangingStateBroadcast):
        self.current_state = message._new_state
        self.last_time = datetime.datetime.now()

    def update(self, message: ChangingStateBroadcast):
        self.current_state = message._new_state
        self.last_time = datetime.datetime.now()


@asyncio.coroutine
def get_last_state(request):
    logger.info(state)
    response_body = "<table>" \
                    "<tr>" \
                    "<td>Node ID</td>" \
                    "<td>State</td>" \
                    "<td>Last message time</td>" \
                    "<tr>"
    cur_time = datetime.datetime.now()
    for i in state.keys():
        st = state[i]
        if (cur_time - st.last_time).total_seconds() > 30:
            del state[i]
        else:
            response_body += "<tr><td>{}</td><td>{}</td><td>{}</td></tr>".format(i, st.current_state,
                                                                                 st.last_time.strftime("%H:%M:%S.%f"))
    response_body += "</table>"
    return web.Response(body=response_body.encode('ascii'))


@asyncio.coroutine
def update_nodes():
    while True:
        message = yield from udp_messenger.take_message()
        node_id = message._author_node_id
        state[node_id] = Node(message)


def run_visualization_server(mac, broadcast_address, configuration):
    app = web.Application()
    app.router.add_route('GET', '/', get_last_state)
    loop = asyncio.get_event_loop()
    handler = app.make_handler()
    f = loop.create_server(handler, '0.0.0.0', 8888)
    srv = loop.run_until_complete(f)
    print('serving on', srv.sockets[0].getsockname())
    global logger
    logger = logging.getLogger('Node')
    cfg_node = configuration['node']
    io_executor = AsyncExecutor(5, loop)
    global udp_messenger
    logger.info("Debug port: {}".format(cfg_node['debug_port']))
    udp_messenger = UDPMessenger(broadcast_address, cfg_node['debug_port'], io_executor, True, loop)
    udp_messenger.start()

    asyncio.async(update_nodes())
    try:
        loop.run_forever()
    except KeyboardInterrupt:
        pass
    finally:
        loop.run_until_complete(handler.finish_connections(1.0))
        srv.close()
        udp_messenger.stop()
        loop.run_until_complete(srv.wait_closed())
        loop.run_until_complete(app.finish())
    loop.close()


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
    run_visualization_server(mac, broadcast_address, cfg)


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
