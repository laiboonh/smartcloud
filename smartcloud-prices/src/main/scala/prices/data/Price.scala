package prices.data

import io.circe.syntax.EncoderOps
import io.circe.{ Decoder, Encoder, HCursor, Json }

final case class Price(kind: InstanceKind, amount: Double)

object Price {
  private val kindKey = "kind"
  implicit val decodePrice: Decoder[Price] = (c: HCursor) =>
    for {
      kindString <- c.downField(kindKey).as[String]
      amount <- c.downField("price").as[Double]
    } yield Price(InstanceKind(kindString), amount)

  implicit val encodePrice: Encoder[Price] = (price: Price) =>
    Json.obj(
      (kindKey, price.kind.getString.asJson),
      ("amount", price.amount.asJson)
    )
}
