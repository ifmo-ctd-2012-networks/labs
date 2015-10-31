#!/usr/bin/env bash
python3 /vagrant/run_pi_node.py eth1 &> /home/vagrant/node.log & disown
