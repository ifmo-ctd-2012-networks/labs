__author__ = "Polyarnyi Nickolay"

import asyncio
import logging
from abc import abstractmethod, ABCMeta

from task2.entity.token import Token
from task2.entity.consts import Const
from task2.entity.messages import MessageType, Message, MESSAGES_TYPES_WITH_TOKEN, TakeTokenMessage, MessageWithToken, \
    ChangingStateBroadcast, serialize_message
from task2.flow.messenger import Messenger, UDPMessenger
from task2.utils import support
import task2.flow.messenger

logger = logging.getLogger(__name__)


class Context:
    def __init__(self, state, const: Const, messenger: Messenger, debug_messenger: UDPMessenger):
        self._state = state
        self._const = const
        self._messenger = messenger
        self._debug_messenger = debug_messenger

        self._data = ''
        self._generated_tokens_revision = 0
        self._given_token = None

    def _construct_message(self, message_type) -> Message:
        if message_type == MessageType.TAKE_TOKEN:
            return TakeTokenMessage(self.node_id, self.current_token, self._messenger.nodes, self._data)
        elif message_type in MESSAGES_TYPES_WITH_TOKEN:
            return MessageWithToken(message_type, self.node_id, self.current_token)
        else:
            return Message(message_type, self.node_id)

    def generate_token(self):
        self._given_token = None
        self._generated_tokens_revision += 1

    def update_token(self, token: Token, data):
        if token.priority() > self.current_token.priority():
            logger.info('Token updated from {} to {}.'.format(self.current_token, token))
            self._given_token = token
            logger.info(
                'Changing data from {} to {}. (len: {}->{})'.format(self._data, data, len(self._data), len(data)))
            self._data = data
            return True
        else:
            return False

    @asyncio.coroutine
    def take_message(self):
        return (yield from self._messenger.take_message())

    @asyncio.coroutine
    def send_broadcast(self, message_type: MessageType):
        message = self._construct_message(message_type)
        yield from self._messenger.send_broadcast(message)

    @asyncio.coroutine
    def send_response(self, income_message: Message, response_type: MessageType):
        response_message = self._construct_message(response_type)
        success = yield from self._messenger.send_message(income_message.author_node_id, response_message)
        if not success:
            logger.warn('Response message sending failure! (request: {}, response: {}, to node_id={})'
                        .format(income_message.type, response_type, income_message.author_node_id))

    @asyncio.coroutine
    def send_message_to_next(self, message_type: MessageType):
        next_node_id = self._messenger.get_next_available_node_id()
        message = self._construct_message(message_type)
        logger.info('Sending message to next node (node_id={}, host={})...'
                    .format(next_node_id, self._messenger.nodes[next_node_id]))
        success = yield from self._messenger.send_message(next_node_id, message)
        return success

    @asyncio.coroutine
    def calculate(self):
        yield from asyncio.sleep(2.0)
        pi_data = '3,1415926535897932384626433832795028841971693993751058209749'
        if len(self._data) >= len(pi_data):
            self._data += '239'[(len(self._data) - len(pi_data)) % 3]
        else:
            self._data = pi_data[:len(self._data) + 1]

    def notify_state_changed(self, old_state, new_state):
        if self._debug_messenger is None:
            return
        message = ChangingStateBroadcast(self.node_id, old_state, new_state)
        logger.debug("Sending debug message: {}".format(message.__getstate__()))
        support.wrap_exc(asyncio.async(self._debug_messenger.send_message(
            serialize_message(message, task2.flow.messenger.ENCODING))), logger)

    @property
    def node_id(self):
        return self._messenger.node_id

    @property
    def state(self):
        return self._state

    @state.setter
    def state(self, state):
        old_state = "NONE" if self._state is None else self._state.get_state_name()
        self.notify_state_changed(old_state, state.get_state_name())
        logger.info('State: {} -> {}.'.format(None if self.state is None else self.state.get_state_name(),
                                              state.get_state_name()))
        self._state = state

    @property
    def const(self):
        return self._const

    @property
    def current_token(self) -> Token:
        if self._given_token is None:
            return Token(len(self._data), self.node_id, self._generated_tokens_revision)
        else:
            return Token(len(self._data), self._given_token._author_node_id, self._given_token._author_token_revision)


class State:
    __metaclass__ = ABCMeta

    def __init__(self):
        self._messages = asyncio.Queue()
        self._logger = logging.getLogger(self.__class__.__name__)

    @abstractmethod
    @asyncio.coroutine
    def execute(self, context: Context):
        pass

    @asyncio.coroutine
    def _take_message(self, context: Context):
        return (yield from context.take_message())

    def on_message(self, message: Message):
        self._messages.put_nowait(message)

    def get_state_name(self):
        return self.__class__.__name__
