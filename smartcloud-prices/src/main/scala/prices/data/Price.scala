package prices.data

import io.circe.{ Decoder, HCursor }

import java.time.ZonedDateTime

final case class Price(kind: InstanceKind, price: Double, timestamp: ZonedDateTime)

object Price {
  implicit val decodePrice: Decoder[Price] = (c: HCursor) =>
    for {
      kindString <- c.downField("kind").as[String]
      price <- c.downField("price").as[Double]
      timestamp <- c.downField("timestamp").as[ZonedDateTime]
    } yield Price(InstanceKind(kindString), price, timestamp)
}
