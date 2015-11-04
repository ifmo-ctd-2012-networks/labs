#!/usr/bin/env bash
python3 /vagrant/run_pi_node.py /vagrant/pi_node.default.yml &> /home/vagrant/node.log & disown
