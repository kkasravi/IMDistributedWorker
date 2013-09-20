package com.intel.bigdata.prototype.api

case class ServiceStatus (node: Option[String] = None, state: State.StateType)

object State extends Enumeration {
  type StateType = Value
  val RUNNING, STOPPED, UNKNOWN = Value
}
