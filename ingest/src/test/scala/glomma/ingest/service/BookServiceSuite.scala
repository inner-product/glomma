package glomma.ingest.service

import glomma.data.data.Scenarios
import glomma.data.random._
import glomma.event.Book
import munit._

class BookServiceSuite extends FunSuite {
  // Sample a selection of books from the small scenario
  def books(): List[Book] =
    Scenarios.small.sample().books.map(b => Book(b.name, b.price))
}
