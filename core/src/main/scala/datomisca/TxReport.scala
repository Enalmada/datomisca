package datomisca

class TxReport(rawReport: java.util.Map[_, _]) {
  import datomic.Connection.{DB_BEFORE, DB_AFTER, TX_DATA, TEMPIDS}
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
    resolveOpt(id) getOrElse { throw new TempidNotResolved(id) }

  def resolve(identified: TempIdentified): Long =
    resolve(identified.id)

  def resolve(ids: DId*): Seq[Long] =
    ids.map(resolve)

  def resolveOpt(id: DId): Option[Long] =
    Option {
      datomic.Peer.resolveTempid(dbAfter.underlying, tempids, id.toDatomicId)
    } map (_.asInstanceOf[Long])

  def resolveOpt(ids: DId*): Seq[Option[Long]] =
    ids.map(resolveOpt)

  def resolveEntity(id: DId): Entity =
    dbAfter.entity(resolve(id))

  // Custom map implementation compatible with Scala 2.12 and 2.13
  lazy val tempidMap: scala.collection.Map[DId, Long] = new scala.collection.AbstractMap[DId, Long] {
    override def get(tempId: DId): Option[Long] = resolveOpt(tempId)

    override def iterator: Iterator[(DId, Long)] =
      throw new UnsupportedOperationException("Iterator is not supported for tempidMap")

    // Implement + and - operators as required in Scala 2.12
    override def +[V1 >: Long](kv: (DId, V1)): scala.collection.Map[DId, V1] =
      throw new UnsupportedOperationException("Update is not supported for tempidMap")

    override def -(key: DId): scala.collection.Map[DId, Long] =
      throw new UnsupportedOperationException("Remove is not supported for tempidMap")

    // Implement Scala 2.13 specific method
    override def -(key1: DId, key2: DId, keys: DId*): scala.collection.Map[DId, Long] =
      throw new UnsupportedOperationException("Remove is not supported for tempidMap")
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
