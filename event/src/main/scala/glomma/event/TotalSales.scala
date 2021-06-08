package glomma.event

import io.circe.generic.semiauto.deriveCodec

final case class TotalSales(value: Double)
object TotalSales {
  implicit val salesCodec = deriveCodec[TotalSales]
}
