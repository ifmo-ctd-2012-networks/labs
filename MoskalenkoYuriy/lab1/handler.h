#ifndef HANDLER_H_
#define HANDLER_H_
#include <map>
#include <string>
#include <mutex>

void handler(std::map<std::string, long long> *last_message, std::mutex *last_message_mutex);

#endif // HANDLER_H_
