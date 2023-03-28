package com.baovitt

// ZIO Imports:
import zio.*

object ShardManagerApp extends ZIOAppDefault:
  
  // SHARDCAKE Imports:
  import com.devsisters.shardcake.*
  import com.devsisters.shardcake.interfaces.*

  def run: Task[Nothing] =
    Server.run.provide(
      ZLayer.succeed(ManagerConfig.default),
      ZLayer.succeed(GrpcConfig.default),
      PodsHealth.local, // just ping a pod to see if it's alive
      GrpcPods.live, // use gRPC protocol
      Storage.memory, // store data in memory
      ShardManager.live // shard manager logic
    )
end ShardManagerApp
