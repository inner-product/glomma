package glomma.ingest

import cats.effect.IO
import cats.effect.std.Queue
import glomma.event.{Book, Event}
import glomma.ingest.service.BookService
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.io._

class IngestController(events: Queue[IO, Event], bookService: BookService) {
  val route = HttpRoutes.of[IO] {
    case r @ POST -> Root / "event" =>
      for {
        event <- r.as[Event]
        _ <- events.offer(event)
        ok <- Ok()
      } yield ok

    case r @ POST -> Root / "books" =>
      r.as[List[Book]].flatMap { books =>
        bookService.addBooks(books)
        Ok()
      }
  }
}
