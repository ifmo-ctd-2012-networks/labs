#ifndef CLIENT_H_
#define CLIENT_H_

#include <mutex>

#include "writer.h"

void start_client(int port, Writer *writer);

#endif // CLIENT_H_

