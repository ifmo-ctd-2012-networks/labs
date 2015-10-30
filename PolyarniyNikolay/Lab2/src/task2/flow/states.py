__author__ = "Polyarnyi Nickolay"

import asyncio
import concurrent
import concurrent.futures

from task2.flow.context import State, Context
from task2.entity.messages import MessageType
from task2.utils.support import auto_cancellation


class OwnerState(State):

    @asyncio.coroutine
    def execute(self, context: Context):
        self._logger.info('Token is here... Calculating...')
        yield from context.send_broadcast(MessageType.TOKEN_IS_HERE)

        heart_beat_timeout = asyncio.async(asyncio.sleep(context.const.heartbeat_timeout))
        calculating = asyncio.async(context.calculate())

        with auto_cancellation([heart_beat_timeout, calculating]):
            while True:
                reading = asyncio.async(self._take_message(context))
                done, pending = yield from asyncio.wait([heart_beat_timeout, calculating, reading], return_when=concurrent.futures.FIRST_COMPLETED)
                reading.cancel()

                if heart_beat_timeout in done:
                    self._logger.info('Token is here...')
                    yield from context.send_broadcast(MessageType.TOKEN_IS_HERE)

                    heart_beat_timeout = asyncio.async(asyncio.sleep(context.const.heartbeat_timeout))

                if calculating in done:
                    self._logger.info('Calculation finished!')
                    self._logger.info('Transferring token...')

                    yield from context.send_message_to_next(MessageType.TAKE_TOKEN)
                    context.state = WaiterState()
                    return

                if reading in done:
                    message = yield from reading
                    self._logger.info('Message: {}.'.format(message.type))

                    if message.type == MessageType.TOKEN_IS_HERE:
                        if message.token.priority() > context.current_token.priority():
                            yield from context.send_response(message, MessageType.PING)
                            context.state = WaiterState()
                            return
                        elif message.author_node_id != context.node_id:
                            yield from context.send_response(message, MessageType.TOKEN_IS_HERE)
                            yield from context.send_broadcast(MessageType.TOKEN_IS_HERE)

                    elif message.type == MessageType.WHERE_IS_TOKEN:
                        yield from context.send_response(message, MessageType.TOKEN_IS_HERE)

                    elif message.type == MessageType.GENERATING_TOKEN:
                        yield from context.send_response(message, MessageType.TOKEN_IS_HERE)
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
                    self._logger.info('Message: {}.'.format(message.type))

                    if message.type == MessageType.TOKEN_IS_HERE:
                        waiter_timeout.cancel()
                        waiter_timeout = asyncio.async(asyncio.sleep(context.const.waiter_timeout))

                    elif message.type == MessageType.TAKE_TOKEN:
                        context.update_token(message.token, message.data)
                        context.state = OwnerState()
                        return

                    elif message.type == MessageType.WHERE_IS_TOKEN:
                        yield from context.send_response(message, MessageType.TOKEN_WAS_HERE_RECENTLY)


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
                    self._logger.info('Message: {}.'.format(message.type))

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
                    context.increase_generated_tokens_revision()
                    context.state = OwnerState()
                    return

                if reading in done:
                    message = yield from reading
                    self._logger.info('Message: {}.'.format(message.type))

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
