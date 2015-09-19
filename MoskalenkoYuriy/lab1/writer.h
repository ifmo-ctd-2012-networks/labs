#ifndef WRITER_H_
#define WRITER_H_

#include <mutex>
#include <string>
#include <iostream>
#include <queue>

class Writer
{
public:
    friend void write(Writer *writer);

    Writer();

    Writer &operator <<(const std::string &mes);
    Writer &operator <<(long long mes);

    void start();

private:
    std::queue<std::string> _queue;
    std::mutex _mutex;

    std::thread _thread;
};

#endif // WRITER_H_

