# IMDistributedWorkers

Based on [DistributedWorkers](http://typesafe.com/activator/template/akka-distributed-workers) with the following changes:
* Incorporates a web frontend using Spray
* Introduces a service concept in addition to a work item
* Uses Publish, Subscribe to ask a set of workers to execute a service

### How to build the project:
* sbt clean assembly

### How to run a local test on the command line (agents are run locally):

1. Prerequisites: curl and node
1. Edit src/main/resources/application.conf and change masterHost to be the FQDN or IP of the master host. For example:
<pre>
masterHost = "192.168.1.12"
</pre>
1. Now run the command './run_test.sh' to get usage. 
<pre>
$ ./run_test.sh
Usage: ./run_test.sh command num
command: start|status|config
num: number of agents
</pre>
1. Running run_test.sh with a number of agents will return the time each agent took to run a service (start|config|status) and the time when all agents were done reporting their status to the master.
<pre>
$ ./run_test.sh status 4
{ "service": { "id": "3949eb18-1c7e-459a-aea2-91c82e807af3", "timesPerWorker": [24, 24, 25, 26], "completionTime": 26}}
</pre>

### How to run a local test using the web browser (agents are run locally):

1. Edit src/main/resources/application.conf and change masterHost to be the FQDN or IP of the master host. For example:
<pre>
masterHost = "192.168.1.12"
</pre>
1. Open new terminal window 
<pre>
$ ./run_master.sh
</pre>
1. Open as many new terminal windows as you would like agents
<pre>
$ ./run_agent.sh
</pre>
1. Point your browser to localhost:8090. Click on start or status or config. An alert will display the completionTime.
