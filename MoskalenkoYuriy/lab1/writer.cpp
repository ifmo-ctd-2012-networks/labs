#include <future>

#include "writer.h"

void write(Writer *writer)
{
    while(true)
    {
        std::lock_guard<std::mutex> guard(writer->_mutex);

        if (writer->_queue.size() != 0)
        {
            std::cout << writer->_queue.front();
            writer->_queue.pop();
        }
        else
        {
            std::cout.flush();
        }
    }
}

Writer::Writer()
{

}

Writer &Writer::operator <<(const std::string &mes)
{
    std::lock_guard<std::mutex> guard(_mutex);
    _queue.push(mes);

    return *this;
}

Writer &Writer::operator <<(long long mes)
{
    std::lock_guard<std::mutex> guard(_mutex);
    _queue.push(std::to_string(mes));

    return *this;
}

void Writer::start()
{
    _thread = std::thread(write, this);
}
