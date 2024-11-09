import sbt._

object Dependencies {

  object V {
    val macroParadise = "2.1.1"

    val datomic       = "1.0.7260"

    val specs2        = "2.5"
    val scalaTest     = "3.0.3"
  }

  object Compile {
    val datomic = "com.datomic"    %    "peer"    %    V.datomic    %    "provided" exclude("org.slf4j", "slf4j-nop")
  }

  object Test {
    val specs2 = "org.specs2"    %%    "specs2-core"    %    V.specs2    %    "test"
  }

  object IntegrationTest {
    val scalaTest = "org.scalatest" %% "scalatest" % V.scalaTest % "it"
  }

}
