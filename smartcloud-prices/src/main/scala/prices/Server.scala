package prices

import cats.effect._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import prices.config.Config
import prices.routes.InstanceKindRoutes
import prices.services.SmartcloudInstanceKindService

object Server {

  def serve(config: Config): Stream[IO, ExitCode] = {

    val httpClient: HttpClient[IO] = new prices.HttpClient[IO](config.smartcloud)

    val instanceKindService = SmartcloudInstanceKindService.make[IO](
      config.smartcloud,
      httpClient
    )

    val httpApp = (
      InstanceKindRoutes[IO](instanceKindService).routes
    ).orNotFound

    Stream
      .eval(
        EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString(config.app.host).get)
          .withPort(Port.fromInt(config.app.port).get)
          .withHttpApp(Logger.httpApp(logHeaders = true, logBody = true)(httpApp))
          .build
          .useForever
      )
  }

}
