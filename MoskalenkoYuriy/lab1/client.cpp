#include <iostream>
#include <string>
#include <thread>

#include "handler.h"
#include "client.h"

#include <boost/asio.hpp>

void split(const std::string &str, std::string &mac_addres, std::string &host_name, long long &time)
{
    int length_mac = 6;

    mac_addres = std::string(str.begin(), str.begin() + length_mac);

    std::stringstream ss;
    for(size_t i = 0; i < mac_addres.size(); ++i)
    {
        ss << std::hex << (int)(unsigned char)mac_addres[i];
    }

    mac_addres = ss.str();

    int length = str[6];

    host_name = std::string(str.begin() + length_mac + 1, str.begin() + length_mac + length + 1);

    time = std::stoll(std::string(str.begin() + length_mac + length + 1, str.end()));
}

void start_client(int port, Writer *writer)
{
    boost::asio::io_service io_service;
    boost::asio::ip::udp::socket socket(io_service, boost::asio::ip::udp::endpoint(boost::asio::ip::udp::v4(), port));

    std::map<std::string, long long> last_message;
    std::mutex last_message_mutex;

    std::thread handler_message(&handler, &last_message, &last_message_mutex);

    while(true)
    {
        std::array<char, 1024> buffer;

        boost::asio::ip::udp::endpoint sender_endpoint;

        std::size_t bytes_transferred = socket.receive_from(boost::asio::buffer(buffer), sender_endpoint);

        std::string mac_addres;
        std::string host_name;
        long long time = 0;

        split(std::string(buffer.begin(), buffer.begin() + bytes_transferred), mac_addres, host_name, time);

        *writer << "CLIENT received: " << mac_addres << " " << host_name << " " << time << "\n";

        {
            std::lock_guard<std::mutex> guard(last_message_mutex);
            last_message[mac_addres] = time;
        }
    }
}
