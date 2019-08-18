

name := "banking"

version := "0.1"

scalaVersion := "2.12.7"

lazy val resolvers = Seq(
  "mvnrepository" at "http://mvnrepository.com/artifact/",
  "mvncentral" at "http://repo1.maven.org/maven2/"
)

scalacOptions += "-Ypartial-unification"

lazy val doobieVersion = "0.7.0"

libraryDependencies ++= Seq(
  "org.tpolecat"        %% "doobie-core"     % doobieVersion,
  "org.tpolecat"        %% "doobie-postgres" % doobieVersion,
  "org.tpolecat"        %% "doobie-scalatest"   % doobieVersion,
  "org.tpolecat"        %% "doobie-hikari"   % doobieVersion,
  "com.lightbend.akka"  %% "akka-stream-alpakka-csv" % "0.18"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "org.scalamock" %% "scalamock" % "4.4.0" % "test"
libraryDependencies += "com.google.jimfs" % "jimfs" % "1.1" % "test"