package glomma.ingest.service

import java.util.UUID

import scala.collection.mutable

import cats.effect.IO
import cats.effect.kernel.GenConcurrent
import cats.effect.std.Semaphore
import glomma.event.Event

class SessionService(semaphore: Semaphore[IO]) {
  import SessionService._

  val sessionsById: mutable.HashMap[UUID, Session] = mutable.HashMap.empty

  def addSession(session: Event.SessionStart): IO[Unit] =
    semaphore.permit.use(_ =>
      IO(
        sessionsById += (session.id -> Session(
          session.id,
          session.customerId,
          session.customerName
        ))
      )
    )

  def getSession(sessionId: UUID): IO[Option[Session]] =
    semaphore.permit.use(_ => IO(sessionsById.get(sessionId)))
}
object SessionService {
  final case class Session(id: UUID, customerId: UUID, customerName: String)

  def service(implicit f: GenConcurrent[IO, _]): IO[SessionService] =
    Semaphore(1).map(new SessionService(_))
}
