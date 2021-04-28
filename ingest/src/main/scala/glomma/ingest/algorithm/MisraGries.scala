package glomma.ingest.algorithm

import scala.collection.mutable

/**
 * Implementation of the Misra-Gries algorithm for computing "heavy-hitters"
 * (most frequent elements) in a stream of elements of type A in O(1) time. */
final class MisraGries[A](val size: Int) {
  private val counts: mutable.HashMap[A,Long] = mutable.HashMap.empty

  /**
   * Observe the given element and update internal state appropriately
   */
  def add(element: A): Unit = {
    if(counts.size < size) counts += (element -> 1)
    else ???
  }

  def get: Array[(A, Long)] = counts.toArray
}
