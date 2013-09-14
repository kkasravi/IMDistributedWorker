package com.intel.bigdata.prototype.backend.worker

case class Service(id: String, command: Any, startTime:Long = System.currentTimeMillis())

case class ServiceInfo(service: Service)

case class ServiceTimes(service: Service, timesPerWorker: Seq[Long] = Seq[Long](), completionTime: Long = 0)

