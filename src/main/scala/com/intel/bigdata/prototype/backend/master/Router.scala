package com.intel.bigdata.prototype.backend.master

import scala.concurrent.duration._
import akka.actor.Actor
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Send
import akka.pattern._
import akka.util.Timeout
import akka.actor.ActorLogging
import com.intel.bigdata.prototype.backend.worker.{Work,Service,ServiceInfo,ServiceTimes}

object Router {
  case object Ok
  case object NotOk
}

class Router extends Actor with ActorLogging {
  import Router._
  import context.dispatcher
  val mediator = DistributedPubSubExtension(context.system).mediator
  def receive = {
    case work: Work =>
      implicit val timeout = Timeout(30.seconds)
      (mediator ? Send("/user/master/active", work, localAffinity = false)) map {
        case Master.Ack(_) => 
          Ok
      } recover { 
        case _ => NotOk 
      } pipeTo sender

    case service: Service =>
      implicit val timeout = Timeout(30.seconds)
      (mediator ? Send("/user/master/active", service, localAffinity = false)) map {
        case Master.Ack(_) => 
          Ok
      } recover { 
        case _ => NotOk 
      } pipeTo sender

    case serviceInfo: ServiceInfo =>
      implicit val timeout = Timeout(30.seconds)
      (mediator ? Send("/user/master/active", serviceInfo, localAffinity = false)) map {
        case serviceTimes: ServiceTimes => 
          serviceTimes
      } pipeTo sender
  }

}
