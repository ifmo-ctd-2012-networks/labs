#!/usr/bin/env bash
sudo apt-get update
sudo apt-get install python3.4
sudo apt-get install pip3
pip3 install -e .
run_pi_node
