name := "forex"
version := "1.0.0"

scalaVersion := "2.12.8"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification",
  "-language:experimental.macros",
  "-language:implicitConversions"
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val circeVersion = "0.11.1"

libraryDependencies ++= Seq(
  "com.github.pureconfig"      %% "pureconfig"                      % "0.7.2",
  "com.softwaremill.quicklens" %% "quicklens"                       % "1.4.11",
  "com.typesafe.akka"          %% "akka-actor"                      % "2.5.20",
  "com.typesafe.akka"          %% "akka-http"                       % "10.1.7",
  "org.scalatest"              %% "scalatest"                       % "3.0.5" % Test,
  "de.heikoseeberger"          %% "akka-http-circe"                 % "1.25.2",
  "io.circe"                   %% "circe-core"                      % circeVersion,
  "io.circe"                   %% "circe-generic"                   % circeVersion,
  "io.circe"                   %% "circe-generic-extras"            % circeVersion,
  "io.circe"                   %% "circe-java8"                     % circeVersion,
  "io.circe"                   %% "circe-jawn"                      % circeVersion,
  "org.atnos"                  %% "eff"                             % "5.5.0",
  "org.atnos"                  %% "eff-monix"                       % "5.5.0",
  "com.softwaremill.sttp"      %% "core"                            % "1.5.12",
  "com.softwaremill.sttp"      %% "async-http-client-backend-monix" % "1.5.12",
  "com.softwaremill.sttp"      %% "circe"                           % "1.5.12",
  "org.typelevel"              %% "cats-core"                       % "1.5.0",
  "org.zalando"                %% "grafter"                         % "2.6.1",
  "ch.qos.logback"             % "logback-classic"                  % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"                   % "3.7.2",
  compilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.4"),
  compilerPlugin("org.scalamacros" %% "paradise"       % "2.1.1" cross CrossVersion.full)
)
