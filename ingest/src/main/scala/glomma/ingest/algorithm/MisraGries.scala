package glomma.ingest.algorithm

import scala.collection.mutable

/** Implementation of the Misra-Gries algorithm for computing "heavy-hitters"
  * (most frequent elements) in a stream of elements of type A in O(1) time.
  */
final class MisraGries[A](val size: Int) {
  // Not the most efficient data structure but easy to work with
  private val counts: mutable.HashMap[A, Long] = mutable.HashMap.empty

  /** Observe the given element and update internal state appropriately
    */
  def add(element: A): Unit = {
    counts.get(element) match {
      case Some(count) => counts += (element -> (count + 1))
      case None =>
        if (counts.size < size) counts += (element -> 1)
        else
          counts.foreachEntry((elt, count) =>
            if (count == 1) counts -= elt
            else counts += (elt -> (count - 1))
          )
    }
  }

  /** Get the most frequent elements and their approximate counts */
  def get: Array[(A, Long)] = counts.toArray
}
