package glomma.ingest.service

import scala.collection.mutable

import glomma.event.Book

// Stores the books that are for sale.
class BookService() {

  /** Map from book name to book */
  private val booksByName: mutable.HashMap[String, Book] = mutable.HashMap.empty

  def addBooks(books: List[Book]): Unit =
    books.foreach { book =>
      booksByName += (book.name -> book)
    }

  def getBook(name: String): Option[Book] =
    booksByName.get(name)

}
