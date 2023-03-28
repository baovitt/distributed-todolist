import sbt._

object Dependencies {
  lazy val munit = "org.scalameta" %% "munit" % "0.7.29"

  lazy val shardcakeVersion = "2.0.6"
  lazy val zioOpticsVersion = "0.2.1"

  object com {
    object devsisters {
      lazy val `shardcake-manager` = "com.devsisters" %% "shardcake-manager" % shardcakeVersion
      lazy val `shardcake-entities` = "com.devsisters" %% "shardcake-entities" % shardcakeVersion
      lazy val `shardcake-protocol-grpc` = "com.devsisters" %% "shardcake-protocol-grpc" % shardcakeVersion
    }
  }

  object dev {
    object zio {
      lazy val `zio-optics` = "dev.zio" %% "zio-optics" % zioOpticsVersion
    }
  }
}
