
organization := "vu"

version := "1.0"

name := "scala-edf"

scalaVersion := "2.11.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-Xlint",
  "-language:implicitConversions", "-language:postfixOps",
  "-encoding", "utf8")

fork in run := true

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.1",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)