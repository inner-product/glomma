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
    assert(customers.size <= 10)

    println("-----------------------------------------")
    println("Frequent Customers")
    println("-----------------------------------------")
    customers.foreach { case (name, _) =>
      println(name)
    }
  }

  test("Most frequently viewed books") {
    val service = new FrequentItemService[String](10)

    scenario.sessions.foreach(session =>
      session.viewed.foreach(view => service.add(view.book.name))
    )
    val views = Await.result(service.get, Duration(30, SECONDS))
    assert(views.size <= 10)

    println("-----------------------------------------")
    println("Frequently Viewed Books")
    println("-----------------------------------------")
    views.foreach { case (name, _) =>
      println(name)
    }
  }

  test("Most frequently purchased books") {
    val service = new FrequentItemService[String](10)

    scenario.sessions.foreach(session =>
      session.purchased.foreach(book => service.add(book.name))
    )
    val purchases = Await.result(service.get, Duration(30, SECONDS))
    assert(purchases.size <= 10)

    println("-----------------------------------------")
    println("Frequently Purchased Books")
    println("-----------------------------------------")
    purchases.foreach { case (name, _) =>
      println(name)
    }
  }
}
