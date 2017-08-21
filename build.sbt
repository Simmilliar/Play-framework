name := """play-java-seed"""
organization := "com.example"

version := "1.0-SNAPSHOT"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1206-jdbc42"

libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5" % Test
libraryDependencies += "org.powermock" % "powermock-api-mockito" % "1.7.1" % Test
libraryDependencies += "org.powermock" % "powermock-module-junit4" % "1.7.1" % Test

libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3-RC2"
libraryDependencies += "com.typesafe.play" %% "play-mailer" % "6.0.0"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "6.0.0"

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.82"
libraryDependencies += "com.stripe" % "stripe-java" % "5.4.0"

libraryDependencies += "org.im4java" % "im4java" % "1.4.0"

libraryDependencies += javaWs

testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")
