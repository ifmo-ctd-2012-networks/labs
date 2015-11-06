__author__ = "Polyarnyi Nickolay"

import asyncio
import concurrent
import concurrent.futures

from task2.flow.context import State, Context
from task2.entity.messages import MessageType
from task2.utils.support import auto_cancellation, wrap_exc


class OwnerState(State):

    @asyncio.coroutine
    def execute(self, context: Context):
        self._logger.info('Token is here... Calculating...')
        yield from context.send_broadcast(MessageType.TOKEN_IS_HERE)

        heart_beat_timeout = asyncio.async(asyncio.sleep(context.const.heartbeat_timeout))
        calculating = asyncio.async(context.calculate())
        transferring_token = None

        with auto_cancellation([heart_beat_timeout, calculating, calculating, transferring_token]):
            while True:
                reading = asyncio.async(self._take_message(context))
                done, pending = yield from asyncio.wait([heart_beat_timeout, calculating, reading], return_when=concurrent.futures.FIRST_COMPLETED)
                reading.cancel()

                if heart_beat_timeout in done:
                    self._logger.debug('Token is here...')
                    yield from context.send_broadcast(MessageType.TOKEN_IS_HERE)

                    heart_beat_timeout = asyncio.async(asyncio.sleep(context.const.heartbeat_timeout))

                if calculating in done:
                    self._logger.info('Calculation finished!')
                    self._logger.info('Transferring token...')

                    calculating = None
                    transferring_token = wrap_exc(asyncio.async(context.send_message_to_next(MessageType.TAKE_TOKEN)), self._logger)

                if transferring_token in done:
                    success = yield from transferring_token
                    if success:
                        context.state = WaiterState()
                        return
                    else:
                        self._logger.warn('Transferring token failed, transferring token again...')
                        transferring_token = wrap_exc(asyncio.async(context.send_message_to_next(MessageType.TAKE_TOKEN)), self._logger)

                if reading in done:
                    message = yield from reading
                    self._logger.debug('Message: {}.'.format(message.type))

                    if message.type == MessageType.TOKEN_IS_HERE:
                        if message.token.priority() > context.current_token.priority():
                            wrap_exc(asyncio.async(context.send_response(message, MessageType.PING)), self._logger)
                            context.state = WaiterState()
                            return
                        elif message.author_node_id != context.node_id:
                            wrap_exc(asyncio.async(context.send_response(message, MessageType.TOKEN_IS_HERE)), self._logger)
                            context.send_broadcast(MessageType.TOKEN_IS_HERE)

                    elif message.type == MessageType.WHERE_IS_TOKEN:
                        wrap_exc(asyncio.async(context.send_response(message, MessageType.TOKEN_IS_HERE)), self._logger)

                    elif message.type == MessageType.GENERATING_TOKEN:
                        wrap_exc(asyncio.async(context.send_response(message, MessageType.TOKEN_IS_HERE)), self._logger)
                        yield from context.send_broadcast(MessageType.TOKEN_IS_HERE)

    
class WaiterState(State):

    @asyncio.coroutine
    def execute(self, context: Context):
        waiter_timeout = asyncio.async(asyncio.sleep(context.const.waiter_timeout))

        with auto_cancellation([waiter_timeout]):
            while True:
                reading = asyncio.async(self._take_message(context))
                done, pending = yield from asyncio.wait([waiter_timeout, reading], return_when=concurrent.futures.FIRST_COMPLETED)
                reading.cancel()

                if waiter_timeout in done:
                    self._logger.info('Token was lost!')
                    context.state = LooserState()
                    return

                if reading in done:
                    message = yield from reading
                    self._logger.debug('Message: {}.'.format(message.type))

                    if message.type == MessageType.TOKEN_IS_HERE:
                        waiter_timeout.cancel()
                        waiter_timeout = asyncio.async(asyncio.sleep(context.const.waiter_timeout))

                    elif message.type == MessageType.TAKE_TOKEN:
                        context.update_token(message.token, message.data)
                        context.state = OwnerState()
                        return

                    elif message.type == MessageType.WHERE_IS_TOKEN:
                        wrap_exc(asyncio.async(context.send_response(message, MessageType.TOKEN_WAS_HERE_RECENTLY)), self._logger)


class LooserState(State):

    @asyncio.coroutine
    def execute(self, context: Context):
        looser_ask_timeout = asyncio.async(asyncio.sleep(context.const.looser_ask_timeout))
        looser_answer_timeout = asyncio.async(asyncio.sleep(context.const.looser_answer_timeout))

        self._logger.info('Searching for token...')
        yield from context.send_broadcast(MessageType.WHERE_IS_TOKEN)

        with auto_cancellation([looser_ask_timeout, looser_answer_timeout]):
            while True:
                reading = asyncio.async(self._take_message(context))
                done, pending = yield from asyncio.wait([looser_ask_timeout, looser_answer_timeout, reading], return_when=concurrent.futures.FIRST_COMPLETED)
                reading.cancel()

                if looser_answer_timeout in done:
                    self._logger.info('Token lost!')
                    context.state = GeneratingState()
                    return

                if looser_ask_timeout in done:
                    self._logger.info('Searching for token...')
                    yield from context.send_broadcast(MessageType.WHERE_IS_TOKEN)
                    looser_ask_timeout = asyncio.async(asyncio.sleep(context.const.looser_ask_timeout))

                if reading in done:
                    message = yield from reading
                    self._logger.debug('Message: {}.'.format(message.type))

                    if message.type == MessageType.GENERATING_TOKEN:
                        if message.token.priority() < context.current_token.priority():
                            context.state = GeneratingState()
                            return

                    elif message.type == MessageType.TOKEN_IS_HERE:
                        context.state = WaiterState()
                        return

                    elif message.type == MessageType.TAKE_TOKEN:
                        context.update_token(message.token, message.data)
                        context.state = OwnerState()
                        return

                    elif message.type == MessageType.TOKEN_WAS_HERE_RECENTLY:
                        looser_ask_timeout = asyncio.async(asyncio.sleep(context.const.looser_ask_timeout))
                        looser_answer_timeout = asyncio.async(asyncio.sleep(context.const.looser_answer_timeout))


class GeneratingState(State):

    @asyncio.coroutine
    def execute(self, context: Context):
        generating_timeout = asyncio.async(asyncio.sleep(context.const.generating_timeout))

        self._logger.info('I am generating token...')
        yield from context.send_broadcast(MessageType.GENERATING_TOKEN)

        with auto_cancellation([generating_timeout]):
            while True:
                reading = asyncio.async(self._take_message(context))
                done, pending = yield from asyncio.wait([generating_timeout, reading], return_when=concurrent.futures.FIRST_COMPLETED)
                reading.cancel()

                if generating_timeout in done:
                    self._logger.info('I generated token!')
                    context.generate_token()
                    context.state = OwnerState()
                    return

                if reading in done:
                    message = yield from reading
                    self._logger.debug('Message: {}.'.format(message.type))

                    if message.type == MessageType.TOKEN_IS_HERE:
                        if message.token.priority() > context.current_token.priority():
                            context.state = WaiterState()
                            return

                    elif message.type == MessageType.GENERATING_TOKEN:
                        if message.token.priority() > context.current_token.priority():
                            context.state = LooserState()
                            return

                    elif message.type == MessageType.TAKE_TOKEN:
                        context.update_token(message.token, message.data)
                        context.state = OwnerState()
                        return
