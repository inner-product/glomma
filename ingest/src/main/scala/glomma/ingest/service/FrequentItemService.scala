package glomma.ingest.service

import glomma.ingest.algorithm.MisraGries
import java.util.concurrent.ArrayBlockingQueue
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

class FrequentItemService[A](size: Int) {
  import FrequentItemService._

  private val frequentItems: MisraGries[A] = new MisraGries(size)
  private val queue: ArrayBlockingQueue[Event[A]] = new ArrayBlockingQueue(5)

  private val concurrentProcess = Future{
    // Get elements from the queue and take appropriate action
  }

  def add(element: A): Unit =
    queue.put(Observe(element))

  def get: Future[Array[(A, Long)]] = {
    val promise = Promise[Array[(A, Long)]]()
    queue.put(Get(promise))

    promise
  }
}
object FrequentItemService {
  // Events that can be send to the asynchronous parts of the frequent item service
  sealed trait Event[A]
  final case class Get[A](promise: Promise[Array[(A, Long)]]) extends Event[A]
  final case class Observe[A](value: A) extends Event[A]
}
