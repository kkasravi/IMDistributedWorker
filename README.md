# IMDistributedWorkers

Based on [DistributedWorkers](http://typesafe.com/activator/template/akka-distributed-workers) with the following changes:
* Incorporates a web frontend using Spray
* Introduces a service concept in addition to a work item
* Uses Publish, Subscribe to ask a set of workers to execute a service

How to build the project:
1. sbt clean assembly

How to run the project:
1. Bring up several terminal windows
2. In terminal (1) java -cp target/scala-2.10/im-distributed-workers-assembly-0.1.jar com.intel.bigdata.prototype.frontend.server.IMServer
3. In terminal (2...n) java -cp  target/scala-2.10/im-distributed-workers-assembly-0.1.jar com.intel.bigdata.prototype.backend.worker.Main
