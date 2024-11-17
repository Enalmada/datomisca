package datomisca

import scala.jdk.CollectionConverters._

private[datomisca] object Convert {

  private[datomisca] def toScala(v: AnyRef): Any = v match {
    // :db.type/string
    case s: java.lang.String => s
    // :db.type/boolean
    case b: java.lang.Boolean => b: Boolean
    // :db.type/long
    case l: java.lang.Long => l: Long
    // attribute id
    case i: java.lang.Integer => i.toLong: Long
    // :db.type/float
    case f: java.lang.Float => f: Float
    // :db.type/double
    case d: java.lang.Double => d: Double
    // :db.type/bigint
    case bi: java.math.BigInteger => BigInt(bi)
    // :db.type/bigdec
    case bd: java.math.BigDecimal => BigDecimal(bd)
    // :db.type/instant
    case d: java.util.Date => d
    // :db.type/uuid
    case u: java.util.UUID => u
    // :db.type/uri
    case u: java.net.URI => u
    // :db.type/keyword
    case kw: clojure.lang.Keyword => kw
    // :db.type/bytes
    case bytes: Array[Byte] => bytes
    // an entity map
    case e: datomic.Entity => new Entity(e)
    // Handling of Clojure PersistentHashSet specifically
    case set: clojure.lang.PersistentHashSet =>
      set.asScala.map(toScala).toSet
    // a collection
    case coll: java.util.Collection[_] =>
      coll.asScala.iterator.map(item => toScala(item.asInstanceOf[AnyRef])).toList
    // otherwise
    case v => throw new UnsupportedDatomicTypeException(v.getClass)
  }
}
