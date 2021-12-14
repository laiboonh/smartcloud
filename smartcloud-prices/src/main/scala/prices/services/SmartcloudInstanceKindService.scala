package prices.services

import cats.effect._
import cats.implicits.{ catsSyntaxApplicativeId, toFunctorOps }
import org.http4s.Status.Successful
import org.http4s._
import org.http4s.circe._
import org.http4s.headers.{ Accept, Authorization }
import prices.HttpClient
import prices.config.Config.SmartcloudConfig
import prices.data._

import scala.language.postfixOps

object SmartcloudInstanceKindService {

  def make[F[_]: Concurrent: Async](config: SmartcloudConfig, httpClient: HttpClient[F]): InstanceKindService[F] =
    new SmartcloudInstanceKindService(config, httpClient)

  private final class SmartcloudInstanceKindService[F[_]: Concurrent: Async](config: SmartcloudConfig, httpClient: HttpClient[F])
      extends InstanceKindService[F] {

    implicit val instanceKindsEntityDecoder: EntityDecoder[F, List[String]] = jsonOf[F, List[String]]

    private val request: Request[F] =
      Request[F](
        uri = Uri.unsafeFromString(s"${config.baseUri}/instances"),
        headers = Headers(Accept(MediaType.text.strings), Authorization(Credentials.Token(AuthScheme.Bearer, config.token)))
      )

    override def getAll(): F[Either[InstanceKindService.Exception, List[InstanceKind]]] =
      httpClient.resource.use { client =>
        client.run(request).use {
          case Successful(response) =>
            response.as[List[String]].map((payload: List[String]) => Right(payload.map(InstanceKind)))
          case response =>
            val result: Either[InstanceKindService.Exception, List[InstanceKind]] =
              Left(InstanceKindService.Exception.APICallFailure(response.status.toString()))
            result.pure
        }
      }
  }
}
