#include <chrono>
#include <string>
#include <sys/socket.h>
#include <thread>

#include "server.h"

#include <boost/asio.hpp>

long long now_time()
{
    return std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()).count();
}

std::string merge(const std::string &mac_addres, const std::string &host_name, long long time)
{
    return mac_addres + (char)host_name.size() + host_name  + std::to_string(time);
}

std::string get_mac_addres()
{
    struct ifreq ifr;
    int s = socket(AF_INET, SOCK_DGRAM, 0);
    strcpy(ifr.ifr_name, "eth0");
    ioctl(s, SIOCGIFHWADDR, &ifr);

    return std::string(ifr.ifr_hwaddr.sa_data, ifr.ifr_hwaddr.sa_data + 6);
}

void start_server(int port, Writer *writer)
{
    boost::asio::io_service io_service;

    boost::asio::ip::udp::socket socket(io_service, boost::asio::ip::udp::endpoint(boost::asio::ip::udp::v4(), 0));
    socket.set_option(boost::asio::socket_base::broadcast(true));

    boost::asio::ip::udp::endpoint broadcast_endpoint(boost::asio::ip::address_v4::broadcast(), port);

    const std::string MAC_ADDRES = get_mac_addres();
    const std::string HOST_NAME = boost::asio::ip::host_name();

    while(true)
    {
        std::string buffer = merge(MAC_ADDRES, HOST_NAME, now_time());

        socket.send_to(boost::asio::buffer(buffer), broadcast_endpoint);

        *writer << "SERVER sent :" << buffer << "\n";

        std::this_thread::sleep_for(std::chrono::seconds(5));
    }
}
