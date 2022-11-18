ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "BroadcastApplication"
  )

val akkaVersion = "2.7.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.2.14",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion
)
