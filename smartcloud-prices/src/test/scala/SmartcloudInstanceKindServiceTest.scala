import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.munit.TestContainerForAll
import munit.FunSuite
import prices.config.Config.SmartcloudConfig
import prices.data.InstanceKind
import prices.services.SmartcloudInstanceKindService

import scala.concurrent.duration._
import scala.language.postfixOps

class SmartcloudInstanceKindServiceTest extends FunSuite with TestContainerForAll {
  override val containerDef: GenericContainer.Def[GenericContainer] =
    GenericContainer.Def("smartpayco/smartcloud:latest", exposedPorts = Seq(9999))
  val instanceKinds = List(
    "sc2-micro",
    "sc2-small",
    "sc2-medium",
    "sc2-std-2",
    "sc2-std-4",
    "sc2-std-8",
    "sc2-std-16",
    "sc2-std-32",
    "sc2-himem-2",
    "sc2-himem-4",
    "sc2-himem-8",
    "sc2-himem-16",
    "sc2-himem-32",
    "sc2-hicpu-2",
    "sc2-hicpu-4",
    "sc2-hicpu-8",
    "sc2-hicpu-16",
    "sc2-hicpu-32"
  )

  test("getAll") {
    withContainers { container =>
      val config = SmartcloudConfig(
        s"http://${container.host}:${container.mappedPort(9999)}",
        "lxwmuKofnxMxz6O2QE1Ogh",
        3 seconds
      )
      val httpclient = new prices.HttpClient[IO](config)
      val service    = SmartcloudInstanceKindService.make[IO](config, httpclient)
      (1 until 15).foreach { n =>
        println(s"Running loop $n")
        assertEquals(
          service.getAll().unsafeRunSync(),
          Right(instanceKinds.map(InstanceKind))
        )
      }

    }
  }

}
