package com.intel.bigdata.prototype.backend.worker

case class Service(id: String, command: Any, startTime:Long = System.currentTimeMillis())

case class ServiceInfo(service: Service)

case class ServiceTimes(service: Service, times: Seq[Long])

