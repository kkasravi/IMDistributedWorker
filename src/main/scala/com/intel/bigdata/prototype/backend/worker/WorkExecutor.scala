package com.intel.bigdata.prototype.backend.worker

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.reflect.io.File
import scala.util.Random

class WorkExecutor extends Actor with ActorLogging {
  val random = new Random

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
          Thread.sleep(1000 + random.nextInt(100))
          sender ! Worker.ServiceComplete(result)
        case "status" =>
          val result = s"Finished service $command $id"
          sender ! Worker.ServiceComplete(result)
        case "config" =>
          val result = File("src/main/resources/core-site.xml").slurp
          sender ! Worker.ServiceComplete(result)
      }
  }

}
