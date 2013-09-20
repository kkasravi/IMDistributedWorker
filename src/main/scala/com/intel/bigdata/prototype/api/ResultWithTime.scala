package com.intel.bigdata.prototype.api

case class ResultWithTime[T](time:Long, result:T)