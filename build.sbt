
organization in ThisBuild := "com.github.enalmada"

version in ThisBuild := "0.7.3"

scalaVersion in ThisBuild := "2.12.18"

// crossScalaVersions in ThisBuild := Seq("2.12.18", "2.13.11")

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



//*******************************
// Maven settings
//*******************************

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

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra in Global := {
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

