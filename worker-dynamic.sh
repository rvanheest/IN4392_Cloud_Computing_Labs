#!/bin/bash

# Let everyone know you ran
echo Its a me >> /home/ubuntu/Luigi.txt

# Be a deamon and live forever
while true
do 
    date > /home/ubuntu/lasttick.txt
    sleep 2s
done



