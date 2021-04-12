package glomma.data

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.{Random => Rng}

import cats.Comonad
import cats.free.Free
import cats.syntax.all._

object Random {
  @tailrec
  def pick(idx: Int, choice: Double, weights: Array[Double]): Int =
    if (choice <= weights(idx)) idx
    else pick(idx + 1, choice, weights)

  sealed abstract class RandomOp[A]
  object RandomOp {
    final case class Always[A](get: A) extends RandomOp[A]
    final case class Weighted[A](elts: Seq[(A, Double)]) extends RandomOp[A] {
      // Cumulative probability mass assigned to each element
      val (weights: Array[Double], elements: mutable.ArrayBuffer[A]) = {
        val w = Array.ofDim[Double](elts.size)
        val b = mutable.ArrayBuffer.newBuilder[A]

        var i = 0
        var sum = 0.0
        elts.foreach { case (elt, weight) =>
          sum = sum + weight
          w(i) = weight
          b.addOne(elt)
          i = i + 1
        }

        (w, b.result())
      }

      def sample(rng: Rng): A = {
        val idx = Random.pick(0, rng.nextDouble(), weights)
        elements(idx)
      }
    }
    final case object RandomInt extends RandomOp[Int]
    final case class Natural(upperLimit: Int) extends RandomOp[Int]
    final case object RandomDouble extends RandomOp[Double]
    final case object Normal extends RandomOp[Double]
    // Essentially a Markov Chain. State is the state and generate is the transition function
    final case class Fold[A, State](
        var state: State,
        generate: (Rng, State) => (A, State)
    ) extends RandomOp[A] {
      def sample(rng: Rng): A = {
        val (event, newState) = generate(rng, state)
        state = newState
        event
      }
    }
    final case class Stick[A](elements: Seq[A]) extends RandomOp[A] {
      var initialized = false
      // Cumulative probability mass assigned to each element
      val weights = Array.ofDim[Double](elements.size)

      def sample(rng: Rng): A = {
        initialize(rng)

        val idx = Random.pick(0, rng.nextDouble(), weights)
        elements(idx)
      }

      def initialize(rng: Rng): Unit = {
        if (initialized) ()
        else {
          var i = 0
          var stick = 1.0
          var sum = 0.0
          val last = weights.size - 1 // Index of the last element
          elements.foreach { _ =>
            if (i == last) weights(i) = 1.0
            else {
              val break = rng.nextDouble()
              val w = stick * break
              sum = sum + w
              weights(i) = sum
              stick = (stick - w)
              i = i + 1
            }
          }

          initialized = true
        }
      }
    }

    implicit def randomGenerator(implicit
        rng: Rng = scala.util.Random
    ): Comonad[RandomOp] =
      new Comonad[RandomOp] {
        override def coflatMap[A, B](fa: RandomOp[A])(
            f: (RandomOp[A]) => B
        ): RandomOp[B] =
          Always(f(fa))

        override def extract[A](op: RandomOp[A]): A =
          op match {
            case Always(a)      => a
            case w: Weighted[A] => w.sample(rng)
            case RandomInt      => rng.nextInt()
            case Natural(u)     => rng.nextInt(u)
            case RandomDouble   => rng.nextDouble()
            case Normal         => rng.nextGaussian()
            case c: Fold[A, _]  => c.sample(rng)
            case s: Stick[A]    => s.sample(rng)
          }
        override def map[A, B](fa: RandomOp[A])(f: (A) => B): RandomOp[B] =
          Always(f(extract(fa)))
      }
  }

  import RandomOp._

  /** Create a `Random` that always generates the given value. */
  def always[A](in: A): Random[A] =
    Free.pure(in)

  /** Create a `Random` that generates an `Int` uniformly distributed across the entire range. */
  def int: Random[Int] =
    Free.liftF[RandomOp, Int](RandomInt)

  /** Create a `Random` that generates an `Int` uniformly distributed in the range greater than or equal to `lower` and less than `upper`. */
  def int(lower: Int, upper: Int): Random[Int] = {
    val high = (upper max lower)
    val low = (upper min lower)
    val range = Math.abs(high - low)
    natural(range).map { n =>
      n + low
    }
  }

  /** Create a `Random` that generates an `Int` uniformly distributed in the range
    * greater than or equal to zero and less than `upper`.
    */
  def natural(upperLimit: Int): Random[Int] =
    Free.liftF[RandomOp, Int](Natural(upperLimit))

  /** Create a `Random` that generates a `Double` uniformly distributed between
    * 0.0 and 1.0.
    */
  def double: Random[Double] =
    Free.liftF[RandomOp, Double](RandomDouble)

  /** Create a `Random` that generates one of the provided values with uniform
    * distribution.
    */
  def oneOf[A](elts: A*): Random[A] = {
    val length = elts.length
    Random.natural(length).map(idx => elts(idx))
  }

  def weighted[A](elts: (A, Double)*): Random[A] =
    Free.liftF[RandomOp, A](Weighted(elts))

  /** Create a `Random` that generates a normally distributed `Double` with mean 0
    * and standard deviation 1.0.
    */
  def normal: Random[Double] =
    Free.liftF[RandomOp, Double](Normal)

  /** Create a `Random` that generates a normally distributed `Double` with given
    * mean and standard deviation.
    */
  def normal(mean: Double, stdDev: Double): Random[Double] =
    Random.normal.map(x => (stdDev * x) + mean)

  /** Create a `Random` from an initial state and a transition function that
    * transforms the state and generates an output. Effectively a Markov chain
    * (or perhaps a Markov transducer, if you prefer.)
    */
  def fold[A, State](zero: State)(f: (Rng, State) => (A, State)): Random[A] =
    Free.liftF[RandomOp, A](Fold(zero, f))

  /** Construct a `Random` that samples from a finite collection of data using a
    * stick breaking construction which assigns most of the probability mass
    * to a few of the elements. The stick breaks are samples from a uniform
    * distribution.
    */
  def finiteStickBreaking[A](elements: A*): Random[A] =
    Free.liftF[RandomOp, A](Stick(elements))

  /** Given a source of random data, create a `Random` that produces N samples at once from that source.
    */
  def chooseN[A](n: Int)(source: Random[A]): Random[List[A]] =
    source.replicateA(n)
}
