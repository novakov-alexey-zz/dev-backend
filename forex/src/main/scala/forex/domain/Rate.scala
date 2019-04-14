package forex.domain

import cats.Show
import cats.syntax.show._
import io.circe._
import io.circe.generic.semiauto._

case class Rate(pair: Rate.Pair, price: Price, timestamp: Timestamp)

object Rate {
  final case class Pair(from: Currency, to: Currency)

  object Pair {
    implicit val encoder: Encoder[Pair] = deriveEncoder[Pair]
  }

  implicit val show: Show[Pair] = Show.show { p ⇒
    p.from.show + p.to.show
  }

  implicit val encoder: Encoder[Rate] =
    deriveEncoder[Rate]
}
