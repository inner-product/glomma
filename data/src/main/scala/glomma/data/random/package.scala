package glomma.data

import cats.free.Free

package object random {
  type Random[A] = Free[Random.RandomOp, A]

  implicit class RandomExtension[A](random: Random[A]) {
    import cats.syntax.all._

    def chooseN(n: Int): Random[List[A]] =
      Random.chooseN(n)(random)

    def sample(rng: scala.util.Random = scala.util.Random): A =
      random.run(Random.RandomOp.randomGenerator(rng))

    def sampleN(n: Int, rng: scala.util.Random = scala.util.Random): List[A] = {
      random.replicateA(n).sample(rng)
    }
  }
}
