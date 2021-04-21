package glomma.event

import java.util.UUID

import io.circe.generic.semiauto.deriveCodec

sealed trait Event
object Event {
  final case class SessionStart(
      id: UUID,
      customerId: UUID,
      customerName: String
  ) extends Event
  final case class View(sessionId: UUID, bookName: String) extends Event
  final case class Purchase(
      sessionId: UUID,
      bookName: String,
      bookPrice: Double
  ) extends Event

  implicit val eventCodec = deriveCodec[Event]
}
