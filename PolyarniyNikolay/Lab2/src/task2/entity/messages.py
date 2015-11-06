__author__ = "Polyarnyi Nickolay"

from enum import Enum
from abc import ABCMeta

from task2.entity.token import Token
from task2.utils.serialization import construct_serial_code, create_object


class MessageType(Enum):
    PING = "ping",
    TOKEN_IS_HERE = "token is here",
    WHERE_IS_TOKEN = "where is token",
    GENERATING_TOKEN = "generating token",
    TAKE_TOKEN = "take token",
    TOKEN_WAS_HERE_RECENTLY = "token was here recently",
    CHANGING_STATE = "my state",


MESSAGES_TYPES_WITH_TOKEN = {MessageType.TAKE_TOKEN, MessageType.TOKEN_IS_HERE, MessageType.GENERATING_TOKEN}


class Message:
    serial_code = 581831

    def __init__(self, message_type: MessageType, author_node_id):
        self._type = message_type
        self._author_node_id = author_node_id

    @property
    def type(self):
        return self._type

    @property
    def author_node_id(self):
        return self._author_node_id

    def __getstate__(self):
        return {
            'serial_code': construct_serial_code(self),
            'type': self._type.value[0],
            'author_node_id': self._author_node_id
        }

    def __setstate__(self, state):
        self._type = MessageType((state['type'],))
        self._author_node_id = state['author_node_id']


class MessageWithToken(Message):
    serial_code = 328067

    __metaclass__ = ABCMeta

    def __init__(self, message_type: MessageType, author_node_id, token: Token):
        assert message_type in MESSAGES_TYPES_WITH_TOKEN
        super(MessageWithToken, self).__init__(message_type, author_node_id)
        self._token = token

    @property
    def token(self):
        return self._token

    def __getstate__(self):
        state = super(MessageWithToken, self).__getstate__()
        state.update({
            'token': self._token.__getstate__()
        })
        return state

    def __setstate__(self, state):
        super(MessageWithToken, self).__setstate__(state)
        self._token = create_object(Token, state['token'])


class TakeTokenMessage(MessageWithToken):
    serial_code = 137344

    def __init__(self, author_node_id, token: Token, nodes: dict, data):
        super(TakeTokenMessage, self).__init__(MessageType.TAKE_TOKEN, author_node_id, token)
        self._nodes = nodes
        self._data = data

    @property
    def data(self):
        return self._data

    @property
    def nodes(self):
        return self._nodes

    def __getstate__(self):
        state = super(TakeTokenMessage, self).__getstate__()
        state.update({
            'nodes': self._nodes,
            'data': self._data
        })
        return state

    def __setstate__(self, state):
        super(TakeTokenMessage, self).__setstate__(state)
        self._nodes = state['nodes']
        self._data = state['data']


class ChangingStateBroadcast(Message):
    serial_code = 318467

    __metaclass__ = ABCMeta

    def __init__(self, node_id, old_state, new_state):
        super(ChangingStateBroadcast, self).__init__(MessageType.CHANGING_STATE, node_id)
        self._old_state = old_state
        self._new_state = new_state

    def __getstate__(self):
        state = super(ChangingStateBroadcast, self).__getstate__()
        state.update({
            'old_state': self._old_state,
            'new_state': self._new_state
        })
        return state

    def __setstate__(self, state):
        super(ChangingStateBroadcast, self).__setstate__(state)
        self._old_state = state['old_state']
        self._new_state = state['new_state']


TYPE_TO_CLASS = {
    message_type: Message for message_type in MessageType
}

TYPE_TO_CLASS.update({
    message_type: MessageWithToken for message_type in MESSAGES_TYPES_WITH_TOKEN
})

TYPE_TO_CLASS.update({
    MessageType.TAKE_TOKEN: TakeTokenMessage,
    MessageType.CHANGING_STATE: ChangingStateBroadcast,
})
