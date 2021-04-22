package glomma.ingest.service

import glomma.event.Book

// Stores the books that are for sale.
class BookService() {
  def addBooks(books: List[Book]): Unit =
    println(s"Got some books: ${books.size}")
}
