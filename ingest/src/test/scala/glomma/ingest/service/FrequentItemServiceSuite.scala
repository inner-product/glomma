package glomma.ingest.service

import scala.concurrent.Await
import scala.concurrent.duration._

import glomma.data.data._
import glomma.data.random._
import munit._

class FrequentItemServiceSuite extends FunSuite {
  // Sample a scenario used to generate our other data
  val scenario = Scenarios.small.sample()

  test("Most frequent customers") {
    val service = new FrequentItemService[String](10)

    scenario.sessions.foreach(session => service.add(session.customer.name))
    val customers = Await.result(service.get, Duration(30, SECONDS))
    assertEquals(customers.size, 10)

    println("-----------------------------------------")
    println("Frequent Customers")
    println("-----------------------------------------")
    customers.foreach { case (name, _) =>
      println(name)
    }
  }
}
