package glomma.event

import io.circe.generic.semiauto.deriveCodec

final case class Statistics(
    views: Seq[(String, Long)],
    purchases: Seq[(String, Long)],
    customers: Seq[(String, Long)]
)
object Statistics {
  implicit val salesCodec = deriveCodec[Statistics]
}
