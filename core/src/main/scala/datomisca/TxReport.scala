package datomisca

import scala.util.Properties

class TxReport(rawReport: java.util.Map[_, _]) {
  import datomic.Connection.{DB_AFTER, DB_BEFORE, TEMPIDS, TX_DATA}
  import datomic.db.Db

  def dbBefore: Database =
    new Database(rawReport.get(DB_BEFORE).asInstanceOf[Db])

  def dbAfter: Database =
    new Database(rawReport.get(DB_AFTER).asInstanceOf[Db])

  lazy val txData: Seq[Datom] = {
    val builder = Seq.newBuilder[Datom]
    val iter = rawReport.get(TX_DATA).asInstanceOf[java.util.List[datomic.Datom]].iterator
    while (iter.hasNext) {
      builder += new Datom(iter.next())
    }
    builder.result()
  }

  private val tempids = rawReport.get(TEMPIDS).asInstanceOf[AnyRef]

  def resolve(id: DId): Long =
    resolveOpt(id).getOrElse { throw new TempidNotResolved(id) }

  def resolve(identified: TempIdentified): Long =
    resolve(identified.id)

  def resolve(ids: DId*): Seq[Long] =
    ids map { resolve(_) }

  def resolveOpt(id: DId): Option[Long] =
    Option {
      datomic.Peer.resolveTempid(dbAfter.underlying, tempids, id.toDatomicId)
    } map {
      id => id.asInstanceOf[Long]
    }

  def resolveOpt(ids: DId*): Seq[Option[Long]] =
    ids map { resolveOpt(_) }

  def resolveEntity(id: DId): Entity =
    dbAfter.entity(resolve(id))

  // Abstract Map implementation
  abstract class BaseTempidMap extends collection.Map[DId, Long] {
    override def get(tempId: DId): Option[Long] = resolveOpt(tempId)

    override def iterator: Iterator[(DId, Long)] =
      throw new UnsupportedOperationException("Iterator is not supported for tempidMap.")

    override def +[V1 >: Long](kv: (DId, V1)): collection.Map[DId, V1] =
      throw new UnsupportedOperationException("Addition is not supported for tempidMap.")

    override def -(key: DId): collection.Map[DId, Long] =
      throw new UnsupportedOperationException("Removal is not supported for tempidMap.")

    // Core Scala 2.13 methods
    override def -(key1: DId, key2: DId, keys: DId*): collection.Map[DId, Long] =
      throw new UnsupportedOperationException("Multi-key removal is not supported for tempidMap.")
  }

  lazy val tempidMap: collection.Map[DId, Long] = {
    class Scala213TempidMap extends BaseTempidMap {
      def removed(key: DId): collection.Map[DId, Long] =
        throw new UnsupportedOperationException("Removal is not supported for tempidMap.")

      def updated[V1 >: Long](key: DId, value: V1): collection.Map[DId, V1] =
        throw new UnsupportedOperationException("Update is not supported for tempidMap.")
    }

    if (Properties.versionNumberString.startsWith("2.13"))
      new Scala213TempidMap
    else
      new BaseTempidMap {}
  }

  override def toString: String =
    s"""TxReport {
       |  dbBefore: $dbBefore,
       |  dbBefore.basisT: ${dbBefore.basisT}
       |  dbAfter: $dbAfter,
       |  dbAfter.basisT: ${dbAfter.basisT},
       |  txData: $txData,
       |  tempids: $tempids
       |}""".stripMargin
}
