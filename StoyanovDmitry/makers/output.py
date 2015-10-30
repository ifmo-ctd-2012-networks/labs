import itertools
import time
from makers.ConnectsKeeper import ConnectsKeeper
from makers.Settings import Settings


def output_main(ck):
    assert isinstance(ck, ConnectsKeeper)

    for i in itertools.count():
        if not ck.to_print.empty():
            print(ck.to_print.get())


def lost_keeper(ck):
    assert isinstance(ck, ConnectsKeeper)

    for i in itertools.count():
        time.sleep(1)

        ck.increment_lost()

        if i % Settings.TIME_PAUSE_CHECKING == 0:
            ck.delete_lost()
            print(ck.status())
