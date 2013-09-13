package com.intel.bigdata.prototype.backend.master

object MasterWorkerProtocol {
  // Messages from Workers
  case class RegisterWorker(workerId: String)
  case class WorkerRequestsWork(workerId: String)
  case class WorkIsDone(workerId: String, workId: String, result: Any)
  case class WorkFailed(workerId: String, workId: String)

  // Messages related to category of workers and services
  case class ServiceIsComplete(workerId: String, workId: String, result: Any)

  // Messages to Workers
  case object WorkIsReady
  case class Ack(id: String)
}
