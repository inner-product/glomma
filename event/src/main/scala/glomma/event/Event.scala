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

  def sessionStart(
      id: UUID,
      customerId: UUID,
      customerName: String
  ): Event = SessionStart(id, customerId, customerName)
  def view(sessionId: UUID, bookName: String): Event = View(sessionId, bookName)
  def purchase(
      sessionId: UUID,
      bookName: String,
      bookPrice: Double
  ): Event = Purchase(sessionId, bookName, bookPrice)
  implicit val eventCodec = deriveCodec[Event]
}
