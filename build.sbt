import Dependencies._

ThisBuild / scalaVersion     := "3.2.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.baovitt"
ThisBuild / organizationName := "baovitt"

lazy val shardmanager = (project in file("shardmanager"))
  .settings(
    name := "shardmanager",
    libraryDependencies ++= Seq(
      com.devsisters.`shardcake-manager`,
      com.devsisters.`shardcake-protocol-grpc`,
      dev.zio.`zio-optics`
    )
  )