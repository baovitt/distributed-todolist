package com.baovitt

import com.devsisters.shardcake.*
import com.devsisters.shardcake.interfaces.*
import zio.{Config => *, *}

object TodoListApp extends ZIOAppDefault:

  val program =
    for _ <- Sharding.registerEntity(TodoListEntity, TodoListEntity.behavior)
    yield ()

  def run: Task[Unit] =
    ZIO
      .scoped(program)
      .provide(
        ZLayer.succeed(Config.default),
        ZLayer.succeed(GrpcConfig.default),
        Serialization.javaSerialization,
        Storage.memory,
        ShardManagerClient.liveWithSttp,
        GrpcPods.live,
        Sharding.live,
        GrpcShardingService.live
      )
end TodoListApp
