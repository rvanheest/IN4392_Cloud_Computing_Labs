#!/bin/bash

cd /home/ec2-user/

echo Its-a me >> Mario.txt

wget https://www.dropbox.com/s/0m7tbj0xj4ht1gh/dummy.sh?dl=0 -O dummy.sh
chmod +x dummy.sh

/bin/bash dummy.sh

