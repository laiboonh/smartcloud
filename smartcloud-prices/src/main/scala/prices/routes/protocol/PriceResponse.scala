package prices.routes.protocol

import io.circe.syntax._
import io.circe._
import prices.data._

final case class PriceResponse(value: Price)

object PriceResponse {
  implicit val encoder: Encoder[PriceResponse] =
    Encoder.instance[PriceResponse] {
      case PriceResponse(p) => p.asJson
    }
}
