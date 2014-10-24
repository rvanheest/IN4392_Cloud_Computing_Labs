#!/bin/bash

# This script is dynamically loaded onto every worker instance on boot.
# Use it to get the lastest version of the worker binaries and execute them.

LOG="init.log"
EXECUTABLE="uninode.jar"

cd cd /home/ubuntu/worker-workspace/

# Let everyone know you ran
echo $0 : starting >> $LOG

# Get worker executable
wget https://www.dropbox.com/s/nnhdjevstwx552k/uninode.jar?dl=0 -nv -O $EXECUTABLE
echo $0 : downloaded $EXECUTABLE >> $LOG
echo $0 : not executing $EXECUTABLE yet >> $LOG

# Be a deamon and live forever
while true
do 
    date > /home/ubuntu/lasttick.txt
    sleep 2s
done



