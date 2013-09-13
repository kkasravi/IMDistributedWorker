package com.intel.bigdata.prototype.backend.worker

import akka.actor.Actor
import akka.actor.ActorLogging

class WorkExecutor extends Actor with ActorLogging {

  def receive = {
    case n: String =>
      val result = s"Finished work $n"
      sender ! Worker.WorkComplete(result)

    case service: Service => 
      log.info("WorkExecutor service!!!")
      val id = service.id
      val result = s"Finished service $id"
      sender ! Worker.WorkComplete(result)
  }

}
