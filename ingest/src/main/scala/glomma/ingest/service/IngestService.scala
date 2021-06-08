package glomma.ingest.service

import cats.effect.IO
import cats.implicits._
import fs2.concurrent.Channel
import glomma.event.Event._
import glomma.event._

/** Main service that wires everything together
  */
object IngestService {
  def apply(
      channel: Channel[IO, Event],
      salesService: SalesService,
      sessionService: SessionService,
      statisticsService: StatisticsService,
      validationService: ValidationService
  ): IO[Unit] =
    for {
      stream <- IO(channel.stream)
      _ <- stream
        .evalMapFilter(evt =>
          validationService
            .validate(evt)
            .map(either =>
              either match {
                case Left(error) =>
                  println(s"Validation failed with reason $error")
                  none[Event]
                case Right(event) =>
                  event.some
              }
            )
        )
        .evalMap(evt =>
          evt match {
            case s: SessionStart =>
              sessionService.addSession(s)
            case View(_, bookName) =>
              statisticsService.addView(bookName)
            case Purchase(sessionId, bookName, bookPrice) =>
              for {
                _ <- statisticsService.addPurchase(bookName)
                _ <- salesService.addPurchase(Book(bookName, bookPrice))
                maybeSession <- sessionService.getSession(sessionId)
                _ <- maybeSession match {
                  case Some(session) =>
                    statisticsService.addCustomer(session.customerName)
                  case None => IO.unit
                }
              } yield ()
          }
        )
        .compile
        .drain
        .start
    } yield ()
}
