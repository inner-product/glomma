package glomma.ingest

import scala.concurrent.ExecutionContext.global

import cats.effect._
import fs2.concurrent.Channel
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
      _ <- IngestService(
        channel,
        salesService,
        sessionService,
        statisticsService,
        validationService
      )
      controller = new IngestController(
        channel,
        bookService,
        salesService,
        statisticsService
      )

      server = BlazeServerBuilder[IO](global)
        .bindHttp(8808)
        .withHttpApp(controller.route.orNotFound)
        .resource
      exitCode <- server.use(_ => IO.never).as(ExitCode.Success)
    } yield exitCode
}
