# IMDistributedWorkers

Based on [DistributedWorkers](http://typesafe.com/activator/template/akka-distributed-workers) with the following changes:
* Incorporates a web frontend using Spray
* Introduces a service concept in addition to a work item
* Uses Publish, Subscribe to ask a set of workers to execute a service

How to build the project:
* sbt clean assembly

How to run a local test (agents are run locally):

1. run_test.sh
<pre>
Usage: ./run_test.sh num
num: number of agents
</pre>
1. run_test.sh 4
<pre>
{ "service": { "id": "06f5ab67-0d20-4958-8745-9f277d2fdcf6", "timesPerWorker": [38, 44, 53, 54], "completionTime": 54}}
</pre>
