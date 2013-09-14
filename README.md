# IMDistributedWorkers

Based on [DistributedWorkers](http://typesafe.com/activator/template/akka-distributed-workers) with the following changes:
* Incorporates a web frontend using Spray
* Introduces a service concept in addition to a work item
* Uses Publish, Subscribe to ask a set of workers to execute a service

How to build the project:
* sbt clean assembly

How to run a local test (agents are run locally):

1. Edit src/main/resources/application.conf and change masterHost to be the FQDN or IP of the master host. For example:
<pre>
masterHost = "192.168.1.12"
</pre>
1. Now run the command './run_test.sh' to get usage. 
<pre>
$ ./run_test.sh
Usage: ./run_test.sh num
num: number of agents
</pre>
1. Running run_test.sh with a number of agents will return the time each agent took to run a service and the time when all agents were done reporting their status to the master.
<pre>
$ ./run_test.sh 4
{ "service": { "id": "06f5ab67-0d20-4958-8745-9f277d2fdcf6", "timesPerWorker": [38, 44, 53, 54], "completionTime": 54}}
</pre>
