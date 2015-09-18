project(network_HW1)
cmake_minimum_required(VERSION 2.8)
aux_source_directory(. SRC_LIST)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread -std=c++11")

find_package(Boost REQUIRED COMPONENTS system regex date_time thread)

include_directories(${Boost_INCLUDE_DIRS})

add_executable(${PROJECT_NAME} ${SRC_LIST} ${HEADERS_FILES})

target_link_libraries(${PROJECT_NAME} ${Boost_LIBRARIES})

