#!/bin/bash

# This script exists on every worker instance.
# It automatically loads another script.
# Update that script to change worker initialisation behaviour.

LOG="init.log"
DYNAMIC="https://raw.githubusercontent.com/rvanheest/IN4392_Cloud_Computing_Labs/master/worker-dynamic.sh"


#Step 1: Go to familiar places
rm -r /home/ubuntu/worker-workspace/
mkdir /home/ubuntu/worker-workspace/
cd /home/ubuntu/worker-workspace/

#Step 2: Leave traces that you were there
echo worker-init.sh: The workspace has been initialized >> $LOG

#Step 3: Get someone more important than you
wget $DYNAMIC -nv -O dynamic.sh
awk '{ sub("\r$", ""); print }' dynamic.sh > dynamic.sh.fix
mv dynamic.sh.fix dynamic.sh
chmod +x dynamic.sh
echo worker-init.sh: Dynamic part of script download >> $LOG

#Step 4: Execute said someone
nohup /bin/bash dynamic.sh 0<&- &> /home/ubuntu/deamon.log &
#/bin/bash dynamic.sh
echo worker-init.sh: Dynamic part of script executed >> $LOG

