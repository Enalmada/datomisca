package datomisca
package macros

import scala.jdk.CollectionConverters._
import java.{lang => jl}
import java.{math => jm}
import clojure.{lang => clj}

import scala.reflect.macros.whitebox

private[datomisca] class Helper[C <: whitebox.Context](val c: C) {
  import c.universe._

  private def abortWithMessage(message: String) =
    c.abort(c.enclosingPosition, message)

  def literalEDN(edn: Any, stk: List[c.Tree] = Nil): c.Tree = edn match {
    case b: java.lang.Boolean =>
      literalBoolean(b)
    case s: java.lang.String =>
      q"$s"
    case ch: java.lang.Character =>
      literalCharacter(ch)
    case s: clj.Symbol =>
      literalCljSymbol(s, stk)
    case k: clj.Keyword =>
      literalCljKeyword(k)
    case l: java.lang.Long =>
      literalLong(l)
    case d: java.lang.Double =>
      literalDouble(d)
    case d: java.math.BigDecimal =>
      literalBigDecimal(d)
    case i: clj.BigInt =>
      literalCljBigInt(i)
    case r: clj.Ratio =>
      literalCljRatio(r)
    case coll: clj.PersistentVector =>
      literalVector(coll, stk)
    case coll: clj.PersistentList =>
      literalList(coll, stk)
    case coll: clj.IPersistentMap =>
      literalMap(coll, stk)
    case coll: clj.PersistentHashSet =>
      literalSet(coll, stk)
    case x =>
      if (x == null)
        abortWithMessage("nil is not supported")
      else
        abortWithMessage(s"unexpected value $x with ${x.getClass}")
  }

  def literalBoolean(b: jl.Boolean): c.Tree =
    q"new _root_.java.lang.Boolean(${b.booleanValue})"

  def literalCljSymbol(s: clj.Symbol, stk: List[c.Tree]): c.Tree = {
    val m = s.meta
    if (m == null) {
      if (s.getName == "!") {
        stk match {
          case t :: nextStk =>
            if (t.tpe =:= typeOf[String]) {
              q"""_root_.datomic.Util.read("\"%s\"".format($t))"""
            } else {
              q"_root_.datomic.Util.read($t.toString)"
            }
          case _ => abortWithMessage("The symbol '!' is reserved by Datomisca")
        }
      } else {
        q"_root_.clojure.lang.Symbol.intern(${s.getNamespace}, ${s.getName})"
      }
    } else {
      val metaT = literalMap(m, stk)
      q"_root_.clojure.lang.Symbol.intern(${s.getNamespace}, ${s.getName}).withMeta($metaT).asInstanceOf[clojure.lang.Symbol]"
    }
  }

  def literalCljKeyword(k: clj.Keyword): c.Tree =
    q"_root_.clojure.lang.Keyword.intern(${k.getNamespace}, ${k.getName})"

  def literalLong(l: jl.Long): c.Tree =
    q"new _root_.java.lang.Long(${l.longValue})"

  def literalDouble(d: jl.Double): c.Tree =
    q"new _root_.java.lang.Double(${d.doubleValue})"

  def literalCljBigInt(k: clj.BigInt): c.Tree =
    q"_root_.clojure.lang.BigInt.fromBigInteger(new _root_.java.math.BigInteger(${k.toString}))"

  def literalCljRatio(r: clj.Ratio): c.Tree =
    q"new _root_.clojure.lang.Ratio(new _root_.java.math.BigInteger(${r.numerator.toString}), new _root_.java.math.BigInteger(${r.denominator.toString}))"

  def literalBigDecimal(d: jm.BigDecimal): c.Tree =
    q"new _root_.java.math.BigDecimal(${d.toString})"

  def literalCharacter(char: jl.Character): c.Tree =
    q"_root_.java.lang.Character.valueOf(${char.charValue()})"

  def literalVector(coll: clj.PersistentVector, stk: List[c.Tree]): c.Tree = {
    val args = coll.iterator.asScala.map(literalEDN(_, stk)).toList
    q"_root_.clojure.lang.PersistentVector.create(_root_.java.util.Arrays.asList(..$args))"
  }

  def literalList(coll: clj.PersistentList, stk: List[c.Tree]): c.Tree = {
    val args = coll.iterator.asScala.map(literalEDN(_, stk)).toList
    q"_root_.clojure.lang.PersistentList.create(_root_.java.util.Arrays.asList(..$args))"
  }

  def literalMap(coll: clj.IPersistentMap, stk: List[c.Tree]): c.Tree = {
    val freshName = TermName(c.freshName("map$"))
    val builder = List.newBuilder[c.Tree]
    builder += q"val $freshName = new _root_.java.util.HashMap[AnyRef, AnyRef](${coll.count()})"
    for (o <- coll.iterator.asScala) {
      val e = o.asInstanceOf[clj.MapEntry]
      val keyT = literalEDN(e.key(), stk)
      val valT = literalEDN(e.`val`(), stk)
      builder += q"${freshName}.put($keyT, $valT)"
    }
    builder += q"_root_.clojure.lang.PersistentArrayMap.create($freshName)"
    q"{ ..${builder.result()} }"
  }

  def literalSet(coll: clj.PersistentHashSet, stk: List[c.Tree]): c.Tree = {
    val args = coll.iterator.asScala.map(literalEDN(_, stk)).toList
    q"_root_.clojure.lang.PersistentHashSet.create(java.util.Arrays.asList(..$args))"
  }

  def literalQueryRules(rules: c.Tree): c.Expr[QueryRules] =
    c.Expr[QueryRules](q"new _root_.datomisca.QueryRules($rules)")

  def literalQuery(query: c.Tree, inputSize: Int, outputSize: Int): c.Expr[AbstractQuery] = {
    val typeArgs =
      List.fill(inputSize)(tq"AnyRef") :+
        (outputSize match {
          case 0 => tq"Unit"
          case 1 => tq"Any"
          case n =>
            val typeName = TypeName("Tuple" + n)
            val args = List.fill(n)(tq"Any")
            tq"$typeName[..$args]"
        })
    val queryClassName =
      Select(
        Select(
          Select(
            Ident(TermName("_root_")),
            TermName("datomisca")),
          TermName("gen")),
        TypeName("TypedQuery" + inputSize))

    c.Expr[AbstractQuery](q"new $queryClassName[..$typeArgs]($query)")
  }
}
