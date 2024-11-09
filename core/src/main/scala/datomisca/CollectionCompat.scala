package datomisca

import scala.collection.JavaConverters._

object CollectionCompat {
  object Implicits {
    implicit class RichJavaList[A](val list: java.util.List[A]) extends AnyVal {
      def asScalaList: List[A] = list.asScala.toList
    }

    implicit class RichIterator[A](val it: java.util.Iterator[A]) extends AnyVal {
      def asScalaIterator: Iterator[A] = it.asScala
    }
  }
}
