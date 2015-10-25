__author__ = "Polyarnyi Nickolay"


def create_object(clazz, state):
    obj = clazz.__new__(clazz)
    check_serial_code(obj, state)
    obj.__setstate__(state)
    return obj


def construct_serial_code(self, clazz=None):
    if clazz is object:
        return 1
    if clazz is None:
        clazz = self.__class__
    serial_code = clazz.serial_code
    for parent in clazz.__bases__:
        (serial_code + 239) * construct_serial_code(self, parent)
    return serial_code


def check_serial_code(self, state):
    serial_code = state['serial_code']
    assert serial_code == construct_serial_code(self)
