
organization in ThisBuild := "com.github.enalmada"

licenses in ThisBuild += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

version in ThisBuild := "0.8.2"

scalaVersion in ThisBuild := "2.12.18"

crossScalaVersions in ThisBuild := Seq("2.12.18", "2.13.11")

scalacOptions in ThisBuild ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    // "-Xfatal-warnings",
    // "-Xfuture", not on 2.13
    "-Xlint",
    // "-Yno-adapted-args", not on 2.13
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )


scalacOptions in ThisBuild ++= (
  if (scalaVersion.value.startsWith("2.13")) List("-Ymacro-annotations")
  else List("-Xfuture", "-Yno-adapted-args")
  )

scalacOptions in ThisBuild ++= (
    if (scalaVersion.value.startsWith("2.10") || scalaVersion.value.startsWith("2.12") || scalaVersion.value.startsWith("2.13")) Nil
    else List("-Ywarn-unused-import")
  )


resolvers in ThisBuild ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.typesafeRepo("releases"),
    "clojars" at "https://clojars.org/repo",
    "couchbase" at "https://files.couchbase.com/maven2"
  )


shellPrompt in ThisBuild := CustomShellPrompt.customPrompt


// configure publishing to bintray
// bintray.Plugin.bintraySettings


lazy val datomisca = project.
  in(file(".")).
  aggregate(macros, core, tests, integrationTests)

// needed for aggregated build
MacroSettings.settings

libraryDependencies += Dependencies.Compile.datomic

// disable some aggregation tasks for subprojects
aggregate in doc            := false

aggregate in Keys.`package` := false

aggregate in packageBin     := false

aggregate in packageDoc     := false

aggregate in packageSrc     := false

aggregate in publish        := false

aggregate in publishLocal   := false

aggregate in PgpKeys.publishSigned      := false

aggregate in PgpKeys.publishLocalSigned := false


lazy val macros = project in file("macros")

// map macros project classes and sources into root project
mappings in (Compile, packageBin) ++= (mappings in (macros, Compile, packageBin)).value

mappings in (Compile, packageSrc) ++= (mappings in (macros, Compile, packageSrc)).value


lazy val core = project.
  in(file("core")).
  dependsOn(macros)

// map core project classes and sources into root project
mappings in (Compile, packageBin) ++= (mappings in (core, Compile, packageBin)).value

mappings in (Compile, packageSrc) ++= (mappings in (core, Compile, packageSrc)).value


lazy val tests = project.
  in(file("tests")).
  dependsOn(macros, core)


lazy val integrationTests = project.
  in(file("integration")).
  dependsOn(macros, core).
  configs(IntegrationTest)


credentials += Credentials(Path.userHome / ".sbt" / "sonatype.credentials")
releaseCrossBuild:= true
publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
