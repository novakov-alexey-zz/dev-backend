package forex.services.oneforge.models

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

object Quote {
  implicit val encoder: Decoder[Quote] = deriveDecoder[Quote]
}

final case class Quote(symbol: String, price: BigDecimal, timestamp: Long)
