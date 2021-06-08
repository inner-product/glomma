package glomma.ingest.service

import java.util.UUID

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import glomma.event.Event.{Purchase, SessionStart, View}
import glomma.event._
import glomma.event.rule.Rule

// Validate events
final case class ValidationService(
    books: BookService,
    sessions: SessionService
) {
  import ValidationService._

  val viewSessionExists = sessionExists("view", sessions)
  val viewBookExists = bookExists("view", books)
  val purchaseSessionExists = sessionExists("purchase", sessions)
  val purchaseBookIsCorrect = bookIsCorrect("purchase", books)

  def validate(event: Event): IO[Either[NonEmptyList[String], Event]] =
    event match {
      case SessionStart(_, _, _) =>
        IO(Right(event))

      case View(sessionId, bookName) =>
        viewSessionExists
          .product(bookNameNotEmpty.and(viewBookExists))
          .apply((sessionId, bookName))
          .map(_.as(event))

      case p: Purchase =>
        // First step of validation is to check session and easy properties of
        // the book
        purchaseSessionExists
          .contramap[Purchase](_.sessionId)
          .and(bookNameNotEmpty.contramap[Purchase](_.bookName))
          .apply(p)
          .map(_.as(event))
    }
}
object ValidationService {

  // Type alias to make for a bit less typing
  type ValidationRule[A] = Rule[IO, A, NonEmptyList[String]]

  def stringNotEmpty(message: String): ValidationRule[String] =
    Rule.pure[IO](message)((x: String) => x.nonEmpty)

  def sessionExists(
      source: String,
      sessionService: SessionService
  ): ValidationRule[UUID] =
    Rule(s"$source contained a session that does not exist")((id: UUID) =>
      sessionService.getSession(id).map(_.nonEmpty)
    )

  def bookExists(
      source: String,
      bookService: BookService
  ): ValidationRule[String] =
    Rule.pure[IO](
      s"$source contained a book that does not exist in the catalogue"
    )((name: String) => bookService.getBook(name).nonEmpty)

  def bookIsCorrect(
      source: String,
      bookService: BookService
  ): ValidationRule[Book] =
    Rule { book =>
      IO.pure(
        bookService
          .getBook(book.name)
          .map(correctBook =>
            Either.cond(
              correctBook.price == book.price,
              book,
              NonEmptyList.one(
                s"$source book ${book.name} had price ${book.price} which should have been ${correctBook.price}"
              )
            )
          )
          .getOrElse(
            Left(
              NonEmptyList
                .one(
                  s"$source book ${book.name} does not exist in the catalogue"
                )
            )
          )
      )
    }

  val bookNameNotEmpty = stringNotEmpty("book name was empty")
  val bookPricePositive =
    Rule.pure[IO]("book price was zero or lower")((x: Double) => x > 0.0)
  val customerNameNotEmpty = stringNotEmpty("customer name was empty")

}
