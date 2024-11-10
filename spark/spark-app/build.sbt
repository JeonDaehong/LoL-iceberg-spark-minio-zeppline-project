name := "KafkaSparkApp"

version := "0.1"

scalaVersion := "2.12.15"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.4.1",
  "org.apache.spark" %% "spark-sql" % "3.4.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.17.1",
  "org.apache.hadoop" % "hadoop-aws" % "3.3.1",
  "com.amazonaws" % "aws-java-sdk-bundle" % "1.12.262"
)

javaOptions ++= Seq(
  "-Dlog4j.configurationFile=log4j2.properties",
  "--add-exports", "java.base/sun.nio.ch=ALL-UNNAMED"
)

fork in run := true

