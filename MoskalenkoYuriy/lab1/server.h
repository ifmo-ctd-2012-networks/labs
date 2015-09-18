#ifndef SERVER_H_
#define SERVER_H_

#include <mutex>

#include "writer.h"

void start_server(int port, Writer *writer);

#endif // SERVER_H_

