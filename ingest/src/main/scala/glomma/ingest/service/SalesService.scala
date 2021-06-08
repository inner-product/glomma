package glomma.ingest.service

import java.util.concurrent.atomic.DoubleAdder

import scala.collection.concurrent.TrieMap

import cats.effect.IO
import cats.implicits._
import glomma.event.Book

class SalesService() {
  private val totalSalesAdder: DoubleAdder = new DoubleAdder()
  private val salesByBook: TrieMap[String, Double] = new TrieMap()

  def addPurchase(book: Book): IO[Unit] = {
    IO(
      salesByBook.updateWith(book.name)(opt =>
        opt.map(_ + book.price).orElse(book.price.some)
      )
    ) >> IO.unit
  }

  def totalSales: IO[Double] =
    IO(totalSalesAdder.sum())

  def totalSalesForBook(bookName: String): IO[Double] =
    IO(salesByBook.getOrElseUpdate(bookName, 0.0))

  def totalSalesByBook: IO[Map[String, Double]] =
    IO(salesByBook.snapshot().toMap)
}
