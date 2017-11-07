name := "akka-http-rest"
organization := "me.archdev"
version := "1.0.0"
scalaVersion := "2.12.4"

libraryDependencies ++= {
  val akkaHttpV = "10.0.10"
  val scalaTestV = "3.0.4"
  val slickVersion = "3.2.1"
  val circeV = "0.8.0"
  val sttpV = "1.0.0"
  Seq(
    // HTTP server
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,


    // Mongo driver
    "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0",
    "io.netty" % "netty-all" % "4.1.16.Final",


    // Config file parser
    "com.github.pureconfig" %% "pureconfig" % "0.8.0"
    ,


    // Http client, used currently only for IT test
    "com.softwaremill.sttp" %% "core" % sttpV % Test
    ,
    "com.softwaremill.sttp" %% "akka-http-backend" % sttpV % Test
    ,

    "org.scalatest" %% "scalatest" % scalaTestV % Test
    ,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test
    ,
    "org.mockito" % "mockito-core" % "2.11.0" % Test
  )
}

enablePlugins(UniversalPlugin)