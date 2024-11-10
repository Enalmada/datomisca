name := "datomisca-core"

libraryDependencies += Dependencies.Compile.datomic

mappings in (Compile, packageSrc) ++= Def.task {
  val base = (sourceManaged in Compile).value
  val srcs = (managedSources in Compile).value
  srcs pair (Path.relativeTo(base) | Path.flat)
}.value

(sourceGenerators in Compile) += Def.task {
  val base = (sourceManaged in Compile).value
  Boilerplate.genCore(base)
}.taskValue

publish := ()

publishLocal := ()

publishArtifact := false
