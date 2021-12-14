import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import org.http4s.headers.Authorization
import org.http4s.{ AuthScheme, Credentials, Method, Request, Status, Uri }
import prices.data.{ InstanceKind, Price }
import prices.routes.PriceRoutes
import prices.services.PriceService
import prices.services.PriceService.Exception.APICallFailure

import java.time.{ ZoneId, ZonedDateTime }

class PriceRoutesTest extends FunSuite {
  private val price = Price(InstanceKind("foo"), 0.01, ZonedDateTime.of(1, 1, 1, 1, 1, 1, 1, ZoneId.of("UTC")))

  test("happyPath") {
    val priceService = new PriceService[IO]() {
      override def get(instanceKind: InstanceKind): IO[Either[PriceService.Exception, Price]] = IO(Right(price))
    }
    val priceRoutes = PriceRoutes(priceService)
    val (status, payload) = (for {
      resp <- priceRoutes.routes.orNotFound.run(
                Request(method = Method.GET, uri = Uri.unsafeFromString(s"${priceRoutes.prefix}?kind=foo"))
                  .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "doesn't matter")))
              )
      payload <- resp.as[String]
    } yield (resp.status, payload)).unsafeRunSync()

    assertEquals(status, Status.Ok)
    assertEquals(payload, """{"kind":"foo","price":0.01,"timestamp":"0001-01-01T01:01:01.000000001Z[UTC]"}""")
  }
  test("priceService APICallFailure") {
    val priceService = new PriceService[IO]() {
      override def get(instanceKind: InstanceKind): IO[Either[PriceService.Exception, Price]] = IO(Left(APICallFailure("401 Unauthorized")))
    }
    val priceRoutes = PriceRoutes(priceService)
    val (status, payload) = (for {
      resp <- priceRoutes.routes.orNotFound.run(
                Request(method = Method.GET, uri = Uri.unsafeFromString(s"${priceRoutes.prefix}?kind=foo"))
                  .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "doesn't matter")))
              )
      payload <- resp.as[String]
    } yield (resp.status, payload)).unsafeRunSync()

    assertEquals(status, Status.InternalServerError)
    assertEquals(payload, """401 Unauthorized""")
  }
}
