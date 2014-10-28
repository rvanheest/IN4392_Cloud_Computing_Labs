IN4392_Cloud_Computing_Labs
===========================
This repository contains our experiments on cloud computing, that we performed during the IN4392 - Cloud Computing course at the Delft University of Technology as part of our master Computer Science.

We build a small prototype for a cloud based system (using Amazon EC2) that processes images. The system receives images on the *head node* and sends those to one of the available *worker nodes*. If none is available, a new machine is leased and deployed, acting as a *worker node*. The worker itself does a number of operations on the image and sends the final result back to the *head node*. This sends it back to the source it received the image from earlier.

We focus on a number of features the prototype must have:
1) Automation: by working as much as possible without human intervention.
2) Elasticity (auto-scaling): by provisioning VMs, that is, adding to and removing leased VMs from the resource pool managed by the system.
3) Performance (load balancing): by allocating service units (running application instances) to VMs from the resource pool (by starting and stopping service units).
4) Reliability: through redundancy, re-starts, etc.
5) Monitoring: by observing and recording the status of the system. How many users? What is the usage of resources by the system? What is the usage of resources in the system? What is the performance of the system?
