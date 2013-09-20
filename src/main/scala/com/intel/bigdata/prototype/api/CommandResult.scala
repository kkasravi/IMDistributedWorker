package com.intel.bigdata.prototype.api

case class CommandResult(
    node: Option[String] = None, 
    result: Result.ResultType, 
    details: String
    )

object Result extends Enumeration ("SUCCESS", "FAILED", "TIMEOUT", "CANCELED") {
  type ResultType = Value
  val SUCCESS, FAILED, TIMEOUT, CANCELED = Value 
}