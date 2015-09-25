#include <iostream>
#include <map>
#include <string>
#include <mutex>
#include <thread>

#include "handler.h"

long long now_time();

void handler(std::map<std::string, long long> *last_message, std::mutex *last_message_mutex)
{
    std::map<std::string, int> missed;

    while(true)
    {
        {
            std::lock_guard<std::mutex> guard(*last_message_mutex);

            for(auto it = last_message->cbegin(); it != last_message->cend();)
            {
                if (now_time() - it->second  > std::chrono::milliseconds(5000).count())
                {
                    if (missed.find(it->first) == missed.end())
                    {
                        missed[it->first] = 0;
                    }

                    missed[it->first]++;

                    if (missed[it->first] == 5)
                    {
                        missed.erase(missed.find(it->first));
                        last_message->erase(it++);
                        continue;
                    }
                }

                ++it;
            }
        }

        std::this_thread::sleep_for(std::chrono::seconds(5));
    }
}

