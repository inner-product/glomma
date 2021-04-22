package glomma.ingest

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import glomma.ingest.service.BookService

object IngestServerMain extends IngestServer

class IngestServer extends HttpServer {

  override def configureHttp(router: HttpRouter): Unit = {
    router.add(new IngestController(new BookService()))
    ()
  }
}
