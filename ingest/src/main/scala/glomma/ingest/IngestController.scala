package glomma.ingest

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import glomma.event.{Book, Event}
import io.circe.parser.decode
import glomma.ingest.service.BookService

class IngestController(bookService: BookService) extends Controller {
  post("/event") { request: Request =>
    val event = decode[Event](request.contentString)
    println(event)
    response.ok
  }

  post("/books") { request: Request =>
    decode[List[Book]](request.contentString) match {
	    case Left(_) =>
        response.badRequest("Your JSON wasn't any good.")
	    case Right(books) =>
        println(s"Received books ${books.take(10)} etc.")
        bookService.addBooks(books)
    }
  }
}
