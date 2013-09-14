package com.intel.bigdata.prototype.backend.worker

import akka.actor.Actor
import akka.actor.ActorLogging

class WorkExecutor extends Actor with ActorLogging {

  def receive = {
    case n: String =>
      val result = s"Finished work $n"
      sender ! Worker.WorkComplete(result)

    case service: Service => 
      val id = service.id
      val command = service.command
      command match {
        case "start" =>
          val result = s"Finished service $command $id"
          sender ! Worker.ServiceComplete(result)
        case "status" =>
          val result = s"Finished service $command $id"
          sender ! Worker.ServiceComplete(result)
        case "config" =>
          val result = s"Finished service $command $id"
          sender ! Worker.ServiceComplete(result)
      }
  }

}
