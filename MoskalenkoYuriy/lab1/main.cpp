#include <iostream>
#include <thread>
#include <mutex>

#include "client.h"
#include "server.h"
#include "writer.h"

int main(int argc, char *argv[])
{
    if (argc != 2)
    {
        std::cerr << "Usage: <number port>" << std::endl;
        return -1;
    }

    int port = std::stoi(argv[1]);

    Writer writer;

    writer.start();

    std::thread server_thread(&start_server, port, &writer);
    std::thread client_thread(&start_client, port, &writer);

    client_thread.join();
    server_thread.join();

    return 0;
}
