package glomma.ingest

import java.util.UUID

import cats.effect.IO
import fs2.concurrent.Channel
import glomma.event.Event
import glomma.ingest.service._
import munit._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.{Method, Request}

class IngestControllerSuite extends CatsEffectSuite {
  val service: IO[IngestController] =
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
    } yield new IngestController(
      channel,
      bookService,
      salesService,
      statisticsService
    )

  test("Posting an event gives an OK response") {
    service.flatMap(controller =>
      controller.route.orNotFound.run(
        Request(method = Method.POST, uri = uri"/event").withEntity(
          Event.sessionStart(UUID.randomUUID(), UUID.randomUUID(), "Test User")
        )
      )
    )
  }
}
