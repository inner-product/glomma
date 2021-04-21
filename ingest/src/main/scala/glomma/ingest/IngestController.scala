package glomma.ingest

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import glomma.event.Event
import io.circe.parser.decode

class IngestController() extends Controller {
  post("/event") { request: Request =>
    val event = decode[Event](request.contentString)
    println(event)
    response.ok
  }
}
