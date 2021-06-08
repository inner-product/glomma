package glomma.ingest

import scala.concurrent.ExecutionContext.global

import cats.effect._
import cats.effect.std.Queue
import cats.implicits._
import fs2.Stream
import glomma.event.Event.{Purchase, SessionStart, View}
import glomma.event._
import glomma.ingest.service._
import org.http4s.blaze.server._
import org.http4s.implicits._

object IngestServer extends IOApp {
  val bookService = new BookService()

  def run(args: List[String]): IO[ExitCode] =
    for {
      queue <- Queue.bounded[IO, Event](5)
      stream = Stream.fromQueueUnterminated(queue)

      sessionService <- SessionService()
      salesService = new SalesService()
      statisticsService <- StatisticsService()
      validationService = ValidationService(bookService, sessionService)
      controller = new IngestController(
        queue,
        bookService,
        salesService,
        statisticsService
      )

      _ = stream
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
            case s @ SessionStart(_, _, customerName) =>
              for {
                _ <- sessionService.addSession(s)
                _ <- statisticsService.addCustomer(customerName)
              } yield ()
            case View(_, bookName) =>
              statisticsService.addView(bookName)
            case Purchase(_, bookName, bookPrice) =>
              statisticsService
                .addPurchase(bookName)
                .flatMap(_ =>
                  salesService.addPurchase(Book(bookName, bookPrice))
                )
          }
        )

      server = BlazeServerBuilder[IO](global)
        .bindHttp(8808, "localhost")
        .withHttpApp(controller.route.orNotFound)
        .resource
      exitCode <- server.use(_ => IO.never).as(ExitCode.Success)
    } yield exitCode
}
