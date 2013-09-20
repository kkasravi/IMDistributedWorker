package com.intel.bigdata.prototype.backend.worker

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.reflect.io.File
import scala.util.Random
import com.intel.bigdata.prototype.api.CommandRequest
import com.intel.bigdata.prototype.api.CommandResult
import com.intel.bigdata.prototype.api.ConfigResult
import com.intel.bigdata.prototype.api.Result
import com.intel.bigdata.prototype.api.State
import com.intel.bigdata.prototype.api.ServiceStatus
import akka.actor.ActorSystem
import java.net.InetAddress

class WorkExecutor extends Actor with ActorLogging {
  val random = new Random
  var serviceStatuses: Map[String, State.StateType] = Map()

  def receive = {
    case n: String =>
      val result = s"Finished work $n"
      sender ! Worker.WorkComplete(result)

    case service: Service => 
      val id = service.id
      val command = service.command
      command match {
        case req : CommandRequest if req.action equals "start" =>
          val result = CommandResult(Option(InetAddress.getLocalHost().getCanonicalHostName()), Result.SUCCESS, "Completed successfully") //s"Finished service $command $id"
          sleep(context.system)
          serviceStatuses += (req.service -> State.RUNNING)
          sender ! Worker.ServiceComplete(result)
        case req : CommandRequest if req.action equals "stop" =>
          val result = CommandResult(Option(InetAddress.getLocalHost().getCanonicalHostName()), Result.SUCCESS, "Completed successfully") //s"Finished service $command $id"
          sleep(context.system)
          serviceStatuses += (req.service -> State.STOPPED)
          sender ! Worker.ServiceComplete(result)
        case req : CommandRequest if req.action equals "status" =>
          val result = status(req.node, req.service)
          sender ! Worker.ServiceComplete(result)
        case req : CommandRequest if req.action equals "config" =>
          val is = getClass.getResourceAsStream(Settings(context.system).ConfigPayloadFile)
          val configStr = scala.io.Source.fromInputStream(is).getLines().mkString("\n")
          val result = ConfigResult(Option(InetAddress.getLocalHost().getCanonicalHostName()), configStr)
          sender ! Worker.ServiceComplete(result)
      }
  }
  
  def status(node: String, service: String) : ServiceStatus = {
    if (serviceStatuses.contains(service)) {
      ServiceStatus(Option(InetAddress.getLocalHost().getCanonicalHostName()), serviceStatuses(service))      
    } else {
      ServiceStatus(Option(InetAddress.getLocalHost().getCanonicalHostName()), State.UNKNOWN)
    }
  }

  def sleep(system: ActorSystem) = {
	Thread.sleep(Settings(system).LRDelay + random.nextInt(Settings(system).LRDelta))
  }

}
