# IMDistributedWorkers

Based on [DistributedWorkers](http://typesafe.com/activator/template/akka-distributed-workers) with the following changes:
* Incorporates a web frontend using Spray
* Introduces a service concept in addition to a work item
* Uses Publish, Subscribe to ask a set of workers to execute a service
