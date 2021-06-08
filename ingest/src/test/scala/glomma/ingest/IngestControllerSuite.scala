package glomma.ingest

import java.util.UUID

import cats.effect.IO
import cats.effect.std.Queue
import glomma.event.Event
import glomma.ingest.service._
import munit._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.{Method, Request}

class IngestControllerSuite extends CatsEffectSuite {
  val service: IO[IngestController] =
    for {
      queue <- Queue.bounded[IO, Event](5)

      bookService = new BookService()
      salesService = new SalesService()
      statisticsService <- StatisticsService()
    } yield new IngestController(
      queue,
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
