/*
import sbt.*
import Keys.*

object MacroSettings {

  val settings = Seq(
    libraryDependencies ++= {
      val scalaVer = scalaVersion.value
      (CrossVersion.partialVersion(scalaVer) match {
        case Some((2, 12)) if scalaVer == "2.12.18" =>
          Seq(
            compilerPlugin("org.scalamacros" % "paradise" % Dependencies.V.macroParadise cross CrossVersion.full),
            "org.scala-lang" % "scala-reflect" % scalaVer,
            // "org.scalamacros" %% "quasiquotes" % Dependencies.V.macroParadise
          )
        case Some((2, 13)) | Some((2, 14)) => // Replace 14 with future Scala versions that also shouldn't include it
          Seq("org.scala-lang" % "scala-reflect" % scalaVer)
        case _ =>
          Seq("org.scala-lang" % "scala-reflect" % scalaVer)
      })
    }
  )
}
*/

import sbt._
import Keys._

object MacroSettings {

  val settings = Seq(
    addCompilerPlugin("org.scalamacros" % "paradise" % Dependencies.V.macroParadise cross CrossVersion.full),

    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,

    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("2.10")) List("org.scalamacros" %% "quasiquotes" % Dependencies.V.macroParadise)
      else Nil
      )
  )
}
