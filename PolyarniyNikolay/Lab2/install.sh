#!/usr/bin/env bash

sudo apt-get install -y libyaml-cpp-dev
sudo apt-get install -y htop
sudo apt-get install -y python3.4 python3-pip
pip3 install PyYAML==3.11
sudo pip3 install -e /vagrant/
