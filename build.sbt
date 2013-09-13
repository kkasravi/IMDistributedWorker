name := "im-distributed-workers"

version := "0.1"

scalaVersion := "2.10.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-contrib" % "2.2.0-RC1",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.0-RC1",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
  "io.spray" % "spray-can" % "1.2-M8",
  "io.spray" % "spray-routing" % "1.2-M8",
  "io.spray" % "spray-testkit" % "1.2-M8",
  "io.spray" %%  "spray-json" % "1.2.5"
)

assemblySettings
