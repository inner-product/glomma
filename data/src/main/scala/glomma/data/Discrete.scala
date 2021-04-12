package glomma.data

import scala.util.{Random => Rng}

/** A Discrete random variable, from which we can sample and sample without replacement */
sealed abstract class Discrete[A] {
  import Discrete._

  def map[B](f: A => B): Discrete[B] =
    Map(this, f)

  def flatMap[B](f: A => Discrete[B]): Discrete[B] =
    FlatMap(this, f)

  def sample(random: Rng = scala.util.Random): A = ???
}
object Discrete {
  final case class Always[A](get: A) extends Discrete[A]
  final case object Integer extends Discrete[Int]
  final case class Natural(upperLimit: Int) extends Discrete[Int]
  final case class Weighted[A](elements: Seq[(A, Double)]) extends Discrete[A] {
    final val weights: Array[Double] = {
      // Make a nod to efficiency here
      val w = Array.ofDim[Double](elements.size)

      var i = 0
      var sum = 0.0
      elements.foreach { case (_, weight) =>
        w(i) = weight
        sum = sum + weight
        i = i + 1
      }

      i = 0
      w.foreach { weight =>
        w(i) = weight / sum
        i = i + 1
      }

      w
    }

  }

  final case class FlatMap[A, B](source: Discrete[A], f: A => Discrete[B])
      extends Discrete[B]
  final case class Map[A, B](source: Discrete[A], f: A => B) extends Discrete[B]
  final case class Fold[A, State](
      state: State,
      transition: (Rng, State) => (A, State)
  ) extends Discrete[A]

  /** Create a `Discrete` that always generates the given value. */
  def always[A](in: A): Discrete[A] = Always(in)

  /** Create a `Discrete` that generates an `Int` uniformly distributed across the entire range. */
  def int: Discrete[Int] = Integer

  /** Create a `Discrete` that generates an `Int` uniformly distributed in the range
    * greater than or equal to zero and less than `upper`.
    */
  def natural(upperLimit: Int): Discrete[Int] =
    Natural(upperLimit)

  /** Create a `Discrete` that generates an `Int` uniformly distributed in the
    * range greater than or equal to `lower` and less than `upper`.
    */
  def int(lower: Int, upper: Int): Discrete[Int] = {
    val high = (upper max lower)
    val low = (upper min lower)
    val range = Math.abs(high - low)
    natural(range).map { n =>
      n + low
    }
  }

  /** Create a `Discrete` that generates one of the provided values with uniform
    * distribution.
    */
  def oneOf[A](elts: A*): Discrete[A] = {
    val size = elts.size
    natural(size).map(idx => elts(idx))
  }

  def weighted[A](elts: (A, Double)*): Discrete[A] =
    Weighted(elts)

  /** Create a `Discrete` from an initial state and a transition function that
    * transforms the state and generates an output. Effectively a Markov chain
    * (or perhaps a Markov transducer, if you prefer.)
    */
  def fold[A, State](zero: State)(f: (Rng, State) => (A, State)): Discrete[A] =
    Fold(zero, f)
}
