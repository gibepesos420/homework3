import sbt.Keys.libraryDependencies

version := "0.1.0-SNAPSHOT"

scalaVersion := "3.3.4"

name := "WeatherApp"


lazy val akkaHttpVersion = sys.props.getOrElse("akka-http.version", "10.7.0")
lazy val akkaVersion    = "2.10.0"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
  "com.typesafe.akka" %% "akka-pki"                 % akkaVersion,
  "ch.qos.logback"    % "logback-classic"           % "1.5.12",

  "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
  "org.scalatest"     %% "scalatest"                % "3.2.19"        % Test
)