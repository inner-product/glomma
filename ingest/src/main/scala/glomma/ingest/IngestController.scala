package glomma.ingest

import cats.effect.IO
import cats.effect.std.Queue
import cats.implicits._
import glomma.event._
import glomma.ingest.service._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._

class IngestController(
    events: Queue[IO, Event],
    bookService: BookService,
    salesService: SalesService,
    statsService: StatisticsService
) {
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

    case GET -> Root / "sales" =>
      salesService.totalSales.flatMap(v => Ok(TotalSales(v)))

    case GET -> Root / "sale" / bookName =>
      salesService
        .totalSalesForBook(bookName)
        .flatMap(v => Ok(Sales(bookName, v)))

    case GET -> Root / "stats" =>
      (
        statsService.getViews,
        statsService.getPurchases,
        statsService.getCustomers
      ).mapN((v, p, c) => Statistics(v, p, c)).flatMap(Ok(_))

  }
}
