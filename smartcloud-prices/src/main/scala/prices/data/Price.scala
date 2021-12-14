package prices.data

import io.circe.syntax.EncoderOps
import io.circe.{ Decoder, Encoder, HCursor, Json }

import java.time.ZonedDateTime

final case class Price(kind: InstanceKind, price: Double, timestamp: ZonedDateTime)

object Price {
  private val priceKey     = "price"
  private val kindKey      = "kind"
  private val timestampKey = "timestamp"
  implicit val decodePrice: Decoder[Price] = (c: HCursor) =>
    for {
      kindString <- c.downField(kindKey).as[String]
      price <- c.downField(priceKey).as[Double]
      timestamp <- c.downField(timestampKey).as[ZonedDateTime]
    } yield Price(InstanceKind(kindString), price, timestamp)

  implicit val encodePrice: Encoder[Price] = (price: Price) =>
    Json.obj(
      (kindKey, price.kind.getString.asJson),
      (priceKey, price.price.asJson),
      (timestampKey, price.timestamp.asJson)
    )
}
