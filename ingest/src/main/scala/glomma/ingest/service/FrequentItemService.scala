package glomma.ingest.service

import cats.effect.IO
import cats.effect.kernel.GenConcurrent
import cats.effect.std.Semaphore
import glomma.ingest.algorithm.MisraGries

class FrequentItemService[A](size: Int, semaphore: Semaphore[IO]) {

  private val frequentItems: MisraGries[A] = new MisraGries(size)

  def add(element: A): IO[Unit] =
    semaphore.permit.use(_ => IO(frequentItems.add(element)))

  def get: IO[Array[(A, Long)]] = {
    semaphore.permit.use(_ => IO(frequentItems.get))
  }
}
object FrequentItemService {
  def apply[A](size: Int)(implicit
      f: GenConcurrent[IO, _]
  ): IO[FrequentItemService[A]] =
    Semaphore(1).map(new FrequentItemService(size, _))
}
