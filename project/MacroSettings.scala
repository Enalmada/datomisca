import sbt._
import Keys._

object MacroSettings {

  val settings = Seq(

    libraryDependencies ++= {
      val scalaVer = scalaVersion.value
      val scalaReflect = "org.scala-lang" % "scala-reflect" % scalaVer
      val additionalPlugins = CrossVersion.partialVersion(scalaVer) match {
        case Some((2, 12)) =>
          Seq(
            compilerPlugin("org.scalamacros" % "paradise" % Dependencies.V.macroParadise cross CrossVersion.full)
          )
        case _ => Seq.empty
      }
      Seq(scalaReflect) ++ additionalPlugins
    }
  )
}

