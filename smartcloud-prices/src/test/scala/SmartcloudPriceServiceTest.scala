import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.munit.TestContainerForAll
import munit.FunSuite
import prices.config.Config.SmartcloudConfig
import prices.data.{ InstanceKind, Price }
import prices.services.{ PriceService, SmartcloudPriceService }

import scala.concurrent.duration._
import scala.language.postfixOps

class SmartcloudPriceServiceTest extends FunSuite with TestContainerForAll {
  override val containerDef: GenericContainer.Def[GenericContainer] =
    GenericContainer.Def("smartpayco/smartcloud:latest", exposedPorts = Seq(9999))

  private val instanceKind = InstanceKind("sc2-micro")

  test("get") {
    withContainers { container =>
      val config = SmartcloudConfig(
        s"http://${container.host}:${container.mappedPort(9999)}",
        "lxwmuKofnxMxz6O2QE1Ogh",
        3 seconds
      )
      val httpclient = new prices.HttpClient[IO](config)
      val service    = SmartcloudPriceService.make[IO](config, httpclient)
      (1 until 15).foreach { n =>
        println(s"Running loop $n")
        val price: Either[PriceService.Exception, Price] = service.get(instanceKind).unsafeRunSync()
        assert(price.isRight)
      }
    }
  }
}
