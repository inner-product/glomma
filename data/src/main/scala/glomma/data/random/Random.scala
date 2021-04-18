package glomma.data.random

import scala.annotation.tailrec
import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.{Random => Rng}

import breeze.stats.distributions.{Beta => BreezeBeta}
import cats.Comonad
import cats.free.Free
import cats.syntax.all._

object Random {

  /** Chose an index at random from a monotonically increasing set of weights that end at 1.0 */
  @tailrec
  def pick(idx: Int, choice: Double, weights: Array[Double]): Int =
    if (choice <= weights(idx)) idx
    else pick(idx + 1, choice, weights)

  /** Shuffle an array in-place, using the Fisher-Yates algorithm */
  def shuffle[A](elements: Array[A], rng: Rng): Unit = {
    var i = 0
    while (i < elements.size - 1) {
      val gap = elements.size - i
      val j = rng.nextInt(gap) + i
      val tmp = elements(i)
      elements(i) = elements(j)
      elements(j) = tmp
      i = i + 1
    }
    ()
  }

  sealed abstract class RandomOp[A]
  object RandomOp {
    final case class Always[A](get: A) extends RandomOp[A]
    final case class Weighted[A](elts: Seq[(A, Double)]) extends RandomOp[A] {
      // Weights holds the cumulative probability mass assigned to an element
      // and preceding elements. This makes sampling a staight forward process.
      val (weights: Array[Double], elements: mutable.ArrayBuffer[A]) = {
        val w = Array.ofDim[Double](elts.size)
        val b = mutable.ArrayBuffer.newBuilder[A]

        var i = 0
        var sum = 0.0
        elts.foreach { case (elt, weight) =>
          sum = sum + weight
          w(i) = sum
          b.addOne(elt)
          i = i + 1
        }

        i = 0
        while (i < w.size) {
          w(i) = w(i) / sum
          i = i + 1
        }
        w(w.size - 1) = 1.0

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
    final case class Beta(alpha: Double, beta: Double)
        extends RandomOp[Double] {
      val b = BreezeBeta(alpha, beta)

      def sample(): Double =
        b.draw()
    }
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
    final case class Stick[A: ClassTag](elts: Seq[A], break: Random[Double])
        extends RandomOp[A] {
      var initialized = false
      val elements = elts.toArray
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
          // Shuffle the elements, otherwise all sticks on these elements will
          // be biased towards the same elements
          Random.shuffle(elements, rng)

          var i = 0
          var stick = 1.0
          var sum = 0.0
          val last = weights.size - 1 // Index of the last element
          elements.foreach { _ =>
            if (i == last) weights(i) = 1.0
            else {
              val proportion = break.sample(rng)
              val w = stick * proportion
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
            case b: Beta        => b.sample()
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
  def natural(upperLimit: Int): Random[Int] = {
    assert(
      upperLimit > 0,
      "The upperLimit for a Random natural must be greater than or equal to zero"
    )
    Free.liftF[RandomOp, Int](Natural(upperLimit))
  }

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

  def beta(alpha: Double, beta: Double): Random[Double] =
    Free.liftF[RandomOp, Double](Beta(alpha, beta))

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
    * stick breaking construction which assigns most of the probability mass to
    * a few of the elements. The stick breaks are sampled from Beta(1, 5), which
    * biases the stick breaking towards more breaks than a uniform distribution.
    */
  def finiteStickBreaking[A: ClassTag](elements: A*): Random[A] =
    Free.liftF[RandomOp, A](Stick(elements, Random.beta(1, 5)))

  /** Construct a `Random` that samples from a finite collection of data using a
    * stick breaking construction which assigns most of the probability mass to
    * a few of the elements. The stick breaks are sampled from the given
    * distribution, which must returns Doubles in 0 <= x < 1.
    */
  def finiteStickBreaking[A: ClassTag](
      break: Random[Double],
      elements: A*
  ): Random[A] =
    Free.liftF[RandomOp, A](Stick(elements, break))

  /** Given a source of random data, create a `Random` that produces N samples at once from that source.
    */
  def chooseN[A](n: Int)(source: Random[A]): Random[List[A]] =
    source.replicateA(n)
}
