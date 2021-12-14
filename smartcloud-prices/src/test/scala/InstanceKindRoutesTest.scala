import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import org.http4s.headers.Authorization
import org.http4s.{ AuthScheme, Credentials, Method, Request, Status, Uri }
import prices.data.InstanceKind
import prices.routes.InstanceKindRoutes
import prices.services.InstanceKindService
import prices.services.InstanceKindService.Exception.APICallFailure

class InstanceKindRoutesTest extends FunSuite {
  test("happyPath") {
    val instanceKindService = new InstanceKindService[IO]() {
      override def getAll(): IO[Either[InstanceKindService.Exception, List[InstanceKind]]] = IO(Right(List(InstanceKind("foo"))))
    }
    val instanceKindRoutes = InstanceKindRoutes(instanceKindService)
    val (status, payload) = (for {
      resp <- instanceKindRoutes.routes.orNotFound.run(
                Request(method = Method.GET, uri = Uri.unsafeFromString(instanceKindRoutes.prefix))
                  .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "doesn't matter")))
              )
      payload <- resp.as[String]
    } yield (resp.status, payload)).unsafeRunSync()

    assertEquals(status, Status.Ok)
    assertEquals(payload, """[{"kind":"foo"}]""")
  }
  test("instanceKindService APICallFailure") {
    val instanceKindService = new InstanceKindService[IO]() {
      override def getAll(): IO[Either[InstanceKindService.Exception, List[InstanceKind]]] = IO(Left(APICallFailure("401 Unauthorized")))
    }
    val instanceKindRoutes = InstanceKindRoutes(instanceKindService)
    val (status, payload) = (for {
      resp <- instanceKindRoutes.routes.orNotFound.run(
                Request(method = Method.GET, uri = Uri.unsafeFromString(instanceKindRoutes.prefix))
                  .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "doesn't matter")))
              )
      payload <- resp.as[String]
    } yield (resp.status, payload)).unsafeRunSync()

    assertEquals(status, Status.InternalServerError)
    assertEquals(payload, """401 Unauthorized""")
  }
}
