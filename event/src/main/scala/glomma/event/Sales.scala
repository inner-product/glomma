package glomma.event

import io.circe.generic.semiauto.deriveCodec

final case class Sales(bookName: String, sales: Double)
object Sales {
  implicit val salesCodec = deriveCodec[Sales]
}
