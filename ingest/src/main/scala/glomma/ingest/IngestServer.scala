package glomma.ingest

import scala.concurrent.ExecutionContext.global

import cats.effect._
import cats.implicits._
import fs2.concurrent.Channel
import glomma.event.Event.{Purchase, SessionStart, View}
import glomma.event._
import glomma.ingest.service._
import org.http4s.blaze.server._
import org.http4s.implicits._

object IngestServer extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      channel <- Channel.bounded[IO, Event](5)

      bookService = new BookService()
      sessionService <- SessionService()
      salesService = new SalesService()
      statisticsService <- StatisticsService()
      validationService = ValidationService(bookService, sessionService)
      controller = new IngestController(
        channel,
        bookService,
        salesService,
        statisticsService
      )

      stream = channel.stream
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
	                case Some(session) => statisticsService.addCustomer(session.customerName)
	                case None => IO.unit
                }
              } yield ()
          }
        )
        .compile
        .drain
        .start

      server = BlazeServerBuilder[IO](global)
        .bindHttp(8808)
        .withHttpApp(controller.route.orNotFound)
        .resource
      exitCode <- server.use(_ => IO.never).as(ExitCode.Success)
    } yield exitCode
}
