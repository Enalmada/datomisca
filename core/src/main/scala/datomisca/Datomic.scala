package datomisca

import clojure.{lang => clj}
import datomisca.CollectionCompat.Implicits._

import scala.util.Try

/** Main object containing:
 *    - all Datomic basic functions (Peer, Transactor)
 *    - all Scala basic functions
 *    - all Scala high-level functions (macro, typed ops)
 *
 */
object Datomic
  extends PeerOps
    with TransactOps
    with DatomicFacilities
    with QueryExecutor
    with macros.ExtraMacros

/** Provides all Datomic Scala specific facilities
 */
private[datomisca] trait DatomicFacilities {

  /** Converts any value to a DatomicData given there is the right [[ToDatomicCast]] in the scope
   *
   * {{{
   * val s: String = Datomic.toDatomic("toto")
   * val l: java.lang.Long = Datomic.toDatomic("5L")
   * }}}
   */
  def toDatomic[T](t: T)(implicit tdc: ToDatomicCast[T]): AnyRef = tdc.to(t)

  /** Converts a DatomicData to a type given there is the right implicit in the scope
   *
   * {{{
   * val l: String = Datomic.fromDatomic("toto")
   * val s: Long = Datomic.fromDatomic(5L: java.lang.Long)
   * }}}
   */
  def fromDatomic[DD <: AnyRef, T](dd: DD)(implicit fd: FromDatomicInj[DD, T]): T = fd.from(dd)

  /** Creates a heterogeneous, untyped `java.util.List` from simple types using [[DWrapper]] implicit conversion
   *
   * {{{
   * val addPartOp = Datomic.list("toto", 3L, "tata")
   * }}}
   *
   */
  def list(dw: DWrapper*) = datomic.Util.list(dw.map(_.asInstanceOf[DWrapperImpl].underlying): _*).asInstanceOf[java.util.List[AnyRef]]

  /** Runtime-based helper to create multiple Datomic Operations (Add, Retract, RetractEntity, AddToEntity)
   * compiled from a Clojure String. '''This is not a Macro so no variable in string and it is evaluated
   * at runtime'''
   *
   * You can then directly copy some Clojure code in a String and get it parsed at runtime. This is why
   * it returns a `Try[Seq[TxData]]`
   * It also manages comments.
   *
   * {{{
   * val ops = Datomic.parseOps("""
   * ;; comment blabla
   *   [:db/add #db/id[:db.part/user] :db/ident :character/weak]
   *   ;; comment blabla
   *   [:db/add #db/id[:db.part/user] :db/ident :character/dumb]
   *   [:db/add #db/id[:db.part/user] :db/ident :region/n]
   *   [:db/retract #db/id[:db.part/user] :db/ident :region/n]
   *   [:db/retractEntity 1234]
   *   ;; comment blabla
   *   {
   *     :db/id #db/id[:db.part/user]
   *     :person/name "toto, tata"
   *     :person/age 30
   *     :person/character [ :character/_weak :character/dumb-toto ]
   *   }
   *   { :db/id #db/id[:db.part/user], :person/name "toto",
   *     :person/age 30, :person/character [ :character/_weak, :character/dumb-toto ]
   *   }
   * """)
   * }}}
   *
   * @param q the Clojure string
   * @return a sequence of operations or an error
   */
  def parseOps(ops: String): Try[Seq[TxData]] = Try {
    datomic.Util.readAll(new java.io.StringReader(ops))
      .asInstanceOf[java.util.List[AnyRef]]
      .asScalaList.map { obj =>
        new TxData {
          override val toTxData = obj
        }
      }
  }
}
