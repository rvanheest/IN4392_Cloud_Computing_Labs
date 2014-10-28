#!/bin/bash

# This script is dynamically loaded onto every worker instance on boot.
# Use it to get the lastest version of the worker binaries and execute them.

LOG="init.log"
EXECUTABLE="uninode.jar"
HEAD="ip-172-31-10-31.eu-west-1.compute.internal"

cd cd /home/ubuntu/worker-workspace/

# Let everyone know you ran
echo $0 : starting >> $LOG

# Get worker executable
wget https://www.dropbox.com/s/nnhdjevstwx552k/uninode.jar?dl=0 -O $EXECUTABLE
echo $0 : downloaded $EXECUTABLE >> $LOG

# Start worker
nohup java -jar $EXECUTABLE worker $HEAD 0<&- &> /home/ubuntu/worker-workspace/worker.log &
echo $0 : executing $EXECUTABLE >> $LOG

# Be a deamon and live forever
while true
do 
    date > /home/ubuntu/lasttick.txt
    sleep 2s
done



