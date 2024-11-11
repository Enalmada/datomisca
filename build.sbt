import sbt._
import sbt.Keys._
import sbt.librarymanagement.Resolver

ThisBuild / organization := "com.github.enalmada"

ThisBuild / version := "0.8.4"

ThisBuild / scalaVersion := "2.13.15"

// If supporting multiple Scala versions, make sure crossScalaVersions is set.
// ThisBuild / crossScalaVersions := Seq("2.12.18", "2.13.15")

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)

ThisBuild / scalacOptions ++= (
  if (scalaVersion.value.startsWith("2.13")) List("-Ymacro-annotations")
  else List("-Xfuture", "-Yno-adapted-args")
  )

ThisBuild / scalacOptions ++= (
  if (scalaVersion.value.startsWith("2.10") || scalaVersion.value.startsWith("2.12") || scalaVersion.value.startsWith("2.13")) Nil
  else List("-Ywarn-unused-import")
  )

ThisBuild / resolvers ++= Seq[Resolver](
    Resolver.sonatypeRepo("releases"),
    Resolver.typesafeRepo("releases"),
    "clojars" at "https://clojars.org/repo",
    "couchbase" at "https://files.couchbase.com/maven2"
  )

ThisBuild / shellPrompt := CustomShellPrompt.customPrompt

// Configure publishing to Sonatype (Bintray is deprecated)
publishMavenStyle := true

organization := "com.github.enalmada"

description := "Datomisca"

startYear := Some(2016)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

Test / publishArtifact := false

pomIncludeRepository := { _ => false }

Global / pomExtra := {
  <url>https://github.com/Enalmada/datomisca</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:git@github.com:enalmada/datomisca.git</connection>
      <developerConnection>scm:git:git@github.com:enalmada/datomisca.git</developerConnection>
      <url>https://github.com/enalmada</url>
    </scm>
    <developers>
      <developer>
        <id>enalmada</id>
        <name>Adam Lane</name>
        <url>https://github.com/enalmada</url>
      </developer>
    </developers>
}

// Defined projects

lazy val datomisca = (project in file("."))
  .aggregate(macros, core, tests, integrationTests)

// Needed for aggregated build
MacroSettings.settings

libraryDependencies += Dependencies.Compile.datomic

// Disable some aggregation tasks for subprojects
doc / aggregate := false
Keys.`package` / aggregate := false
packageBin / aggregate := false
packageDoc / aggregate := false
packageSrc / aggregate := false
publish / aggregate := false
publishLocal / aggregate := false
PgpKeys.publishSigned / aggregate := false
PgpKeys.publishLocalSigned / aggregate := false

lazy val macros = project in file("macros")

// Map macros project classes and sources into root project
Compile / packageBin / mappings ++= (macros / Compile / packageBin / mappings).value
Compile / packageSrc / mappings ++= (macros / Compile / packageSrc / mappings).value

lazy val core = (project in file("core"))
  .dependsOn(macros)

// Map core project classes and sources into root project
Compile / packageBin / mappings ++= (core / Compile / packageBin / mappings).value
Compile / packageSrc / mappings ++= (core / Compile / packageSrc / mappings).value

lazy val tests = (project in file("tests"))
  .dependsOn(macros, core)

lazy val integrationTests = (project in file("integration"))
  .dependsOn(macros, core)
  .configs(IntegrationTest)

// Credential and publishing configuration
credentials += Credentials(Path.userHome / ".sbt" / "sonatype.credentials")
releaseCrossBuild := true
publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
