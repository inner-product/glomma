package glomma.event

import io.circe.generic.semiauto.deriveCodec

final case class Book(name: String, price: Double)
object Book {
  implicit val bookCodec = deriveCodec[Book]
}
