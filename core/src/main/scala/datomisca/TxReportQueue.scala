package datomisca

import scala.concurrent.duration._
import java.{util => ju}
import java.util.{concurrent => juc}

// Import the compatibility implicits
import CollectionCompat.Implicits._

class TxReportQueue(val queue: juc.BlockingQueue[ju.Map[_, _]]) extends AnyVal {

  def drain(): List[TxReport] = {
    val c = new ju.LinkedList[ju.Map[_, _]]
    queue.drainTo(c)
    c.asScalaList.map(new TxReport(_))
  }

  def drain(maxReports: Int): List[TxReport] = {
    val c = new ju.LinkedList[ju.Map[_, _]]
    queue.drainTo(c, maxReports)
    c.asScalaList.map(new TxReport(_))
  }

  def poll(timeout: Duration): Option[TxReport] =
    Option(queue.poll(timeout.toNanos, NANOSECONDS)).map(new TxReport(_))

  def poll(): Option[TxReport] =
    Option(queue.poll()).map(new TxReport(_))

  def take(): TxReport =
    new TxReport(queue.take())

  def peek(): Option[TxReport] =
    Option(queue.peek()).map(new TxReport(_))

  def isEmpty(): Boolean =
    queue.isEmpty()

  def iterator(): Iterator[TxReport] =
    queue.iterator.asScalaIterator.map(new TxReport(_))

  def size(): Int =
    queue.size()
}
