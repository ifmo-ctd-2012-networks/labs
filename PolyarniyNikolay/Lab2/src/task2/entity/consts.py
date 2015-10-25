__author__ = "Polyarnyi Nickolay"


class Const:

    def __init__(self, heartbeat_timeout=0.5, waiter_timeout=2.0, looser_ask_timeout=0.5, looser_answer_timeout=2.0, generating_timeout=2.0):
        self.heartbeat_timeout = heartbeat_timeout
        self.waiter_timeout = waiter_timeout
        self.looser_ask_timeout = looser_ask_timeout
        self.looser_answer_timeout = looser_answer_timeout
        self.generating_timeout = generating_timeout
