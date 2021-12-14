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

object SmartcloudPriceService {

  def make[F[_]: Concurrent: Async](config: SmartcloudConfig, httpClient: HttpClient[F]): PriceService[F] =
    new SmartcloudPriceService(config, httpClient)

  private final class SmartcloudPriceService[F[_]: Concurrent: Async](config: SmartcloudConfig, httpClient: HttpClient[F]) extends PriceService[F] {

    implicit val priceEntityDecoder: EntityDecoder[F, Price] = jsonOf[F, Price]

    private def request(instanceKind: InstanceKind): Request[F] =
      Request[F](
        uri = Uri.unsafeFromString(s"${config.baseUri}/instances/${instanceKind.getString}"),
        headers = Headers(Accept(MediaType.text.strings), Authorization(Credentials.Token(AuthScheme.Bearer, config.token)))
      )

    override def get(instanceKind: InstanceKind): F[Either[InstanceKindService.Exception, Price]] =
      httpClient.resource.use { client =>
        client.run(request(instanceKind)).use {
          case Successful(response) =>
            response.as[Price].map(Right(_))
          case response =>
            val result: Either[InstanceKindService.Exception, Price] =
              Left(InstanceKindService.Exception.APICallFailure(response.status.toString()))
            result.pure
        }
      }
  }
}
