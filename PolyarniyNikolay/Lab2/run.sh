#!/usr/bin/env bash
python3 /vagrant/run_pi_node.py /vagrant/pi_node.vagrant.yml &> "/vagrant/logs/$(hostname).log" & disown
