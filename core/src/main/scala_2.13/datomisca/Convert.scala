package datomisca

import scala.jdk.CollectionConverters._

private[datomisca] object Convert {

  private[datomisca] def toScala(v: AnyRef): Any = {

    v match {
      // :db.type/string
      case s: java.lang.String =>
        s
      // :db.type/boolean
      case b: java.lang.Boolean =>
        b: Boolean
      // :db.type/long
      case l: java.lang.Long =>
        l: Long
      // attribute id
      case i: java.lang.Integer =>
        i.toLong: Long
      // :db.type/float
      case f: java.lang.Float =>
        f: Float
      // :db.type/double
      case d: java.lang.Double =>
        d: Double
      // :db.type/bigint
      case bi: java.math.BigInteger =>
        BigInt(bi)
      // :db.type/bigdec
      case bd: java.math.BigDecimal =>
        BigDecimal(bd)
      // :db.type/instant
      case d: java.util.Date =>
        d
      // :db.type/uuid
      case u: java.util.UUID =>
        u
      // :db.type/uri
      case u: java.net.URI =>
        u
      // :db.type/keyword
      case kw: clojure.lang.Keyword =>
        kw
      // :db.type/bytes
      case bytes: Array[Byte] =>
        bytes
      // an entity map
      case e: datomic.Entity =>
        new Entity(e)
      // Handling of Clojure PersistentHashSet specifically
      case set: clojure.lang.PersistentHashSet =>
        set.asScala.map(toScala).toSet
      // a collection
      case coll: java.util.Collection[_] =>
        new Iterable[Any] {
          override def iterator = new Iterator[Any] {
            private val jIter = coll.iterator.asInstanceOf[java.util.Iterator[AnyRef]]

            override def hasNext = {
              val hasNext = jIter.hasNext
              hasNext
            }

            override def next() = {
              val nextVal = jIter.next()
              toScala(nextVal)
            }
          }

          override def isEmpty = coll.isEmpty

          override def size = coll.size

          override def toString = coll.toString
        }
      // otherwise
      case v =>
        throw new UnsupportedDatomicTypeException(v.getClass)
    }
  }
}
