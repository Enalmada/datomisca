package datomisca

import scala.jdk.CollectionConverters._
import scala.util.Try

/** Main object containing: */
object Datomic
  extends PeerOps
    with TransactOps
    with DatomicFacilities
    with QueryExecutor
    with macros.ExtraMacros

/** Provides all Datomic Scala specific facilities */
private[datomisca] trait DatomicFacilities {

  /** Converts any value to a DatomicData given there is the right [[ToDatomicCast]] in the scope */
  def toDatomic[T](t: T)(implicit tdc: ToDatomicCast[T]): AnyRef = tdc.to(t)

  /** converts a DatomicData to a type given there is the right implicit in the scope */
  def fromDatomic[DD <: AnyRef, T](dd: DD)(implicit fd: FromDatomicInj[DD, T]): T = fd.from(dd)

  /** Creates a heterogenous, untyped `java.util.List` from simple types using [[DWrapper]] implicit conversion */
  def list(dw: DWrapper*) = datomic.Util.list(dw.map(_.asInstanceOf[DWrapperImpl].underlying):_*).asInstanceOf[java.util.List[AnyRef]]

  /** Runtime-based helper to create multiple Datomic Operations */
  def parseOps(ops: String): Try[Seq[TxData]] = Try {
    datomic.Util.readAll(new java.io.StringReader(ops))
      .asInstanceOf[java.util.List[AnyRef]]
      .asScala
      .iterator  // Convert to Iterator
      .map { obj =>
        new TxData {
          override val toTxData = obj
        }
      }
      .toSeq  // Convert to Seq explicitly
  }
}
