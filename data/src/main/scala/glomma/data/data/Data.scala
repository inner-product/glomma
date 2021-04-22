package glomma.data.data

import java.util.UUID
import cats.data.NonEmptyList
import glomma.event.Event

object Data {
  val books: List[String] = Reader.read("books.txt")
  val famous: List[String] = Reader.read("famous.txt")
  val firstNames: List[String] = Reader.read("first-names.txt")
  val lastNames: List[String] = Reader.read("last-names.txt")
}

final case class Book(name: String, price: Double)
final case class Customer(name: String, customerId: UUID, cluster: Int)
final case class PageView(sessionId: UUID, book: Book)
final case class Session(
    sessionId: UUID,
    customer: Customer,
    viewed: List[PageView],
    purchased: List[Book]
) {
  def toEvents: NonEmptyList[Event] = {
    val start = Event.SessionStart(
      id = sessionId,
      customerId = customer.customerId,
      customerName = customer.name
    )
    val views = viewed.map(v => Event.View(sessionId, v.book.name))
    val purchases =
      purchased.map(b => Event.Purchase(sessionId, b.name, b.price))

    NonEmptyList(start, (purchases ++ views))
  }
}
final case class Scenario(
    customers: List[Customer],
    books: List[Book],
    sessions: List[Session]
)
