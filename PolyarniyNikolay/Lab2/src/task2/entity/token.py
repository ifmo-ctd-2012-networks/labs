__author__ = "Polyarnyi Nickolay"

from task2.utils.serialization import construct_serial_code


class Token:

    serial_code = 407246

    def __init__(self, progress, author_node_id, author_token_revision):
        self._progress = progress
        self._author_node_id = author_node_id
        self._author_token_revision = author_token_revision

    def priority(self):
        return (self._progress, self._author_node_id, self._author_token_revision)

    def __getstate__(self):
        return {
            'serial_code': construct_serial_code(self),
            'progress': self._progress,
            'author_node_id': self._author_node_id,
            'author_token_revision': self._author_token_revision
        }

    def __setstate__(self, state):
        self._progress = state['progress']
        self._author_node_id = state['author_node_id']
        self._author_token_revision = state['author_token_revision']

    def __str__(self, *args, **kwargs):
        return "Token(progress={}, author_node_id={}, author_token_revision={})"\
            .format(self._progress, self._author_node_id, self._author_token_revision)
