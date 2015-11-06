#!/bin/bash
iptables -I INPUT -s $1 -j $2; 
iptables -I OUTPUT -s $1 -j $2 ;


