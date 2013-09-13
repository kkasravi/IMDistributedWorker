package com.intel.bigdata.prototype.backend.worker

import akka.actor.Actor

class WorkExecutor extends Actor {

  def receive = {
    case n: String =>
      val result = s"Finished work $n"
      sender ! Worker.WorkComplete(result)
  }

}
