#!/usr/bin/env bash
sudo apt-get install python3.4 python3-pip
pip3 install -e /vagrant/
python3 /vagrant/run_pi_node.py
