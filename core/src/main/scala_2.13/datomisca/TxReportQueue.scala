package datomisca

import java.util.{concurrent => juc}
import java.{util => ju}
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

/** The data queue associated with a connection. */
class TxReportQueue(
                     val queue:    juc.BlockingQueue[ju.Map[_, _]]
                   ) extends AnyVal {

  /** Removes all available transaction reports from
   * this queue and returns them as a list.
   */
  def drain(): List[TxReport] = {
    val c = new ju.LinkedList[ju.Map[_, _]]
    queue.drainTo(c)
    c.asScala.iterator.map(new TxReport(_)).toList // Use `toList` directly
  }

  /** Removes at most the given number of available
   * transaction reports from this queue and returns
   * them as a list.
   */
  def drain(maxReports: Int): List[TxReport] = {
    val c = new ju.LinkedList[ju.Map[_, _]]
    queue.drainTo(c, maxReports)
    c.asScala.iterator.map(new TxReport(_)).toList // Use `toList` directly
  }

  /** Retrieves and removes the head of this queue, waiting up to the specified wait time if necessary for an element to become available. */
  def poll(timeout: Duration): Option[TxReport] =
    Option {
      queue.poll(timeout.toNanos, NANOSECONDS)
    } map (new TxReport(_))

  /** Retrieves and removes the head of this queue, or returns `None` if this queue is empty. */
  def poll(): Option[TxReport] = {
    Option {
      queue.poll()
    } map (new TxReport(_))
  }

  /** Retrieves and removes the head of this queue, waiting if necessary until an element becomes available. */
  def take(): TxReport =
    new TxReport(queue.take())

  /** Retrieves, but does not remove, the head of this queue, or `None` if this queue is empty. */
  def peek(): Option[TxReport] = {
    Option {
      queue.peek()
    } map (new TxReport(_))
  }

  /** Returns `true` if this queue contains no transaction reports. */
  def isEmpty(): Boolean =
    queue.isEmpty()

  /** Returns an iterator over the transaction reports in this queue. */
  def iterator(): Iterator[TxReport] =
    queue.iterator.asScala.map(new TxReport(_))

  /** Returns the number of transaction reports in the queue. */
  def size(): Int =
    queue.size()

}
