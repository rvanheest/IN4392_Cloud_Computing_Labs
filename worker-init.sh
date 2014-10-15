#!/bin/bash

#Step 1: Go to familiar places
rm -r /home/ubuntu/worker-workspace/
mkdir /home/ubuntu/worker-workspace/
cd /home/ec2-user/worker-workspace/

#Step 2: Leave traces that you were there
echo Its-a me >> Mario.txt

#Step 3: Get someone more important than you
wget https://www.dropbox.com/s/0m7tbj0xj4ht1gh/dummy.sh?dl=0 -O dummy.sh
awk '{ sub("\r$", ""); print }' dummy.sh > dummy.sh.fix
mv dummy.sh.fix dummy.sh
chmod +x dummy.sh

#Step 4: Execute said someone
nohup /bin/bash dummy.sh 0<&- &> /home/ubuntu/deamon.log &

