package glomma.data

import java.util.UUID

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
    id: UUID,
    customer: Customer,
    viewed: List[PageView],
    purchased: List[Book]
)
