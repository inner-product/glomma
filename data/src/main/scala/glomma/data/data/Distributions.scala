package glomma.data.data

import java.util.UUID

import cats.syntax.all._
import glomma.data.random.Random

object Distributions {
  val firstName: Random[String] =
    Random.oneOf(Data.firstNames: _*)

  val lastName: Random[String] =
    Random.oneOf(Data.lastNames: _*)

  /** Creates a Random from which the first samples will be famous names until no
    * famous names remain. Further samples will be randomly generated from
    * common first and last names. Since the result is stateful this method
    * allows multiple instances to be constructed.
    */
  def name(): Random[String] =
    Random.fold(Data.famous)((rng, famous) =>
      famous match {
        case Nil =>
          val result = (firstName, lastName)
            .mapN((f, l) => s"$f $l")
            .sample(rng)
          (result, Nil)
        case h :: t => (h, t)
      }
    )

  def makeCluster(nClusters: Int): Random[Int] =
    Random.natural(nClusters)

  def makeCustomer(cluster: Random[Int]): Random[Customer] =
    name().flatMap(name =>
      cluster.map(cluster => Customer(name, UUID.randomUUID(), cluster))
    )

  def makeCustomers(
      nCustomers: Int,
      customer: Random[Customer]
  ): Random[List[Customer]] =
    customer.chooseN(nCustomers)

  val prices = Random.weighted(
    (9.99, 20),
    (14.99, 30),
    (29.99, 20),
    (39.99, 20),
    (49.99, 10)
  )
  val makeBooks: Random[List[Book]] =
    Random
      .chooseN(Data.books.size)(prices)
      .map(prices =>
        Data.books.zip(prices).map { case (book, price) => Book(book, price) }
      )

  def makeClusters(nClusters: Int, books: List[Book]): Array[Random[Book]] =
    Array.fill(nClusters)(Random.finiteStickBreaking(books: _*))

  /** Number of purchases made in a session */
  val nPurchases: Random[Int] = Random.weighted(
    (0, 0.2),
    (1, 0.3),
    (2, 0.1),
    (3, 0.1),
    (4, 0.1),
    (5, 0.05),
    (6, 0.05),
    (7, 0.05),
    (8, 0.05)
  )

  /** Number of pages viewed in a session (is always greater than or equal to the number of purchases) */
  def nViews(nPurchases: Int): Random[Int] =
    Random.int(nPurchases, nPurchases * 4 + 4)

  def makeSession(
      customer: Customer,
      clusters: Array[Random[Book]]
  ): Random[Session] =
    for {
      nP <- nPurchases
      nV <- nViews(nP)
      sessionId = UUID.randomUUID()
      cluster = clusters(customer.cluster)
      books <- cluster.chooseN(nV)
      viewed = books.map(book => PageView(sessionId, book))
    } yield Session(sessionId, customer, viewed, books.take(nP))

  def makeScenario(
      nCustomers: Int,
      nClusters: Int,
      nSessions: Int
  ): Random[Scenario] = {
    val cluster = makeCluster(nClusters)
    val customer = makeCustomer(cluster)

    val scenario =
      for {
        books <- makeBooks
        customers <- makeCustomers(nCustomers, customer)
        clusters = makeClusters(nClusters, books)
        sessions <- Random
          .oneOf(customers: _*)
          .flatMap(customer => makeSession(customer, clusters))
          .chooseN(nSessions)
      } yield Scenario(customers, books, sessions)

    scenario
  }

  /** Small scenario for testings */
  val small = makeScenario(100, 5, 1000)

  /** Large scenario for the real thing */
  val large = makeScenario(10000, 10, 1000000)
}
