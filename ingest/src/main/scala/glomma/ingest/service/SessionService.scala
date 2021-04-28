package glomma.ingest.service

import java.util.UUID

import scala.collection.mutable

import glomma.event.Event

class SessionService() {
  import SessionService._

  val sessionsById: mutable.HashMap[UUID, Session] = mutable.HashMap.empty

  def addSession(session: Event.SessionStart): Unit =
    sessionsById += (session.id -> Session(
      session.id,
      session.customerId,
      session.customerName
    ))

  def getSession(sessionId: UUID): Option[Session] =
    sessionsById.get(sessionId)
}
object SessionService {
  final case class Session(id: UUID, customerId: UUID, customerName: String)
}
