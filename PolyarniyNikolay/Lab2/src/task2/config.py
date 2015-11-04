__author__ = "Polyarnyi Nickolay"


def create_default():
    return {
        "node": {
            "hostname": None,  # if None - MAC address used

            "interface": None,  # if None - interface will be auto-chosen

            "broadcasting_port": 23912,
            "tcp_port": 8239,

            "debug": {
                "enabled": False,
                "broadcasting_port": 23930,
            }
        },

        "logging": {
            "filename": None,  # if None - stderr used
            "level": "info",  # can be debug/info/warn/error
            "format": "%(relativeCreated)d [%(threadName)s]\t%(name)s [%(levelname)s]:\t %(message)s",
        }
    }
