name := "Scala ZMQ utils and RPC"

version := "1.0-SNAPSHOT"

resolvers ++= Seq(
  "repo.novus rels" at "http://repo.novus.com/releases/",
  "repo.novus snaps" at "http://repo.novus.com/snapshots/",
  "Twitter Maven Repo" at "http://maven.twttr.com/"
)

libraryDependencies ++= Seq(
  "com.novus" %% "salat-core" % "0.0.7",
  "com.twitter" % "util" % "1.10.1",
  "com.twitter" % "ostrich" % "4.7.2",
  "org.scalatest" %% "scalatest" % "1.5.1" % "test"
)



