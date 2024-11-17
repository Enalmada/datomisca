package datomisca

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MovieGraph2SampleSpec
  extends AnyFlatSpec
    with Matchers
    with DatomicFixture
    with AwaitHelper
{

  object MovieGraph2Schema {

    object ns {
      val actor = Namespace("actor")
      val role  = Namespace("role")
      val movie = Namespace("movie")
    }

    val actorName = Attribute(ns.actor / "name", SchemaType.string, Cardinality.one).withDoc("The name of the actor")

    val actorForRole = Attribute(ns.role / "actor", SchemaType.ref, Cardinality.one).withDoc("The actor for this role")
    val movieForRole = Attribute(ns.role / "movie", SchemaType.ref, Cardinality.one).withDoc("The movie in which this role appears")
    val character    = Attribute(ns.role / "character", SchemaType.string, Cardinality.one).withDoc("The character name of this role")

    val movieTitle = Attribute(ns.movie / "title", SchemaType.string, Cardinality.one).withDoc("The title of the movie")
    val movieYear  = Attribute(ns.movie / "year", SchemaType.long, Cardinality.one).withDoc("The year the movie was released")

    val txData = Seq(
      actorName,
      actorForRole, movieForRole, character,
      movieTitle, movieYear
    )
  }

  object MovieGraph2Data {
    import MovieGraph2Schema._

    val `Carrie-Ann Moss` = SchemaFact.add(DId(Partition.USER))(actorName -> "Carrie-Ann Moss")
    val `Hugo Weaving`    = SchemaFact.add(DId(Partition.USER))(actorName -> "Hugo Weaving")
    val `Guy Peace`       = SchemaFact.add(DId(Partition.USER))(actorName -> "Guy Pearce")
    val `Joe Pantoliano`  = SchemaFact.add(DId(Partition.USER))(actorName -> "Joe Pantoliano")

    val actors = Seq(`Carrie-Ann Moss`, `Hugo Weaving`, `Guy Peace`, `Joe Pantoliano`)

    val `The Matrix` = (
      SchemaEntity.newBuilder
        += (movieTitle -> "The Matrix")
        += (movieYear  -> 1999)
      ) withId DId(Partition.USER)

    val `The Matrix Reloaded` = (
      SchemaEntity.newBuilder
        += (movieTitle -> "The Matrix Reloaded")
        += (movieYear  -> 2003)
      ) withId DId(Partition.USER)

    val Memento = (
      SchemaEntity.newBuilder
        += (movieTitle -> "Memento")
        += (movieYear  -> 2000)
      ) withId DId(Partition.USER)

    val movies = Seq(`The Matrix`, `The Matrix Reloaded`, Memento)

    val graphNodesTxData = actors ++ movies

    val graphEdgesTxData = Seq(
      (SchemaEntity.newBuilder
        += (actorForRole -> `Carrie-Ann Moss`.id)
        += (movieForRole -> `The Matrix`.id)
        += (character    -> "Trinity")
        ) withId DId(Partition.USER),
      (SchemaEntity.newBuilder
        += (actorForRole -> `Carrie-Ann Moss`.id)
        += (movieForRole -> `The Matrix Reloaded`.id)
        += (character    -> "Trinity")
        ) withId DId(Partition.USER),
      (SchemaEntity.newBuilder
        += (actorForRole -> `Carrie-Ann Moss`.id)
        += (movieForRole -> Memento.id)
        += (character    -> "Natalie")
        ) withId DId(Partition.USER),
      (SchemaEntity.newBuilder
        += (actorForRole -> `Hugo Weaving`.id)
        += (movieForRole -> `The Matrix`.id)
        += (character    -> "Agent Smith")
        ) withId DId(Partition.USER),
      (SchemaEntity.newBuilder
        += (actorForRole -> `Hugo Weaving`.id)
        += (movieForRole -> `The Matrix Reloaded`.id)
        += (character    -> "Agent Smith")
        ) withId DId(Partition.USER),
      (SchemaEntity.newBuilder
        += (actorForRole -> `Guy Peace`.id)
        += (movieForRole -> Memento.id)
        += (character    -> "Leonard Shelby")
        ) withId DId(Partition.USER),
      (SchemaEntity.newBuilder
        += (actorForRole -> `Joe Pantoliano`.id)
        += (movieForRole -> `The Matrix`.id)
        += (character    -> "Cypher")
        ) withId DId(Partition.USER),
      (SchemaEntity.newBuilder
        += (actorForRole -> `Joe Pantoliano`.id)
        += (movieForRole -> Memento.id)
        += (character    -> "Teddy Gammell")
        ) withId DId(Partition.USER)
    )

    val txData = graphNodesTxData ++ graphEdgesTxData
  }

  object MovieGraph2Queries {
    // Hardcoded attribute names for Clojure/Datomic syntax
    val queryFindMovieByTitle = Query("""
      [:find ?title ?year
       :in $ ?title
       :where
         [?movie :movie/title ?title]
         [?movie :movie/year  ?year]]
    """)

    val queryFindMovieByTitlePrefix = Query("""
      [:find ?title ?year
       :in $ ?prefix
       :where
         [?movie :movie/title ?title]
         [?movie :movie/year  ?year]
         [(.startsWith ^String ?title ?prefix)]]
    """)

    val queryFindActorsInTitle = Query("""
      [:find ?name
       :in $ ?title
       :where
         [?movie :movie/title ?title]
         [?role  :role/movie  ?movie]
         [?role  :role/actor  ?actor]
         [?actor :actor/name  ?name]]
    """)

    val queryFindTitlesAndRolesForActor = Query("""
      [:find ?role ?title
       :in $ ?name
       :where
         [?actor :actor/name  ?name]
         [?role  :role/actor  ?actor]
         [?role  :role/character ?character]
         [?role  :role/movie  ?movie]
         [?movie :movie/title ?title]]
    """)

    val queryFindMoviesThatIncludeActorsInGivenMovie = Query("""
      [:find ?othertitle
       :in $ ?title
       :where
         [?movie  :movie/title    ?title]
         [?role1  :role/movie     ?movie1]
         [?role1  :role/actor     ?actor]
         [?role2  :role/actor     ?actor]
         [?role2  :role/movie     ?movie2]
         [?movie2 :movie/title    ?othertitle]]
    """)

    val queryFindAllMoviesWithRole = Query("""
      [:find ?title
       :in $ ?character
       :where
         [?role  :role/character ?character]
         [?role  :role/movie     ?movie]
         [?movie :movie/title    ?title]]
    """)

  }


  "Movie Graph 2 Sample" should "run to completion" in withDatomicDB { implicit conn =>
    import MovieGraph2Queries._

    await {
      Datomic.transact(MovieGraph2Schema.txData)
    }

    await {
      Datomic.transact(MovieGraph2Data.txData)
    }

    val db = conn.database()

    Datomic.q(queryFindMovieByTitle, db, "The Matrix") should have size (1)
    Datomic.q(queryFindMovieByTitlePrefix, db, "The Matrix") should have size (2)
    Datomic.q(queryFindActorsInTitle, db, "Memento") should have size (3)
    Datomic.q(queryFindTitlesAndRolesForActor, db, "Carrie-Ann Moss") should have size (3)
    Datomic.q(queryFindMoviesThatIncludeActorsInGivenMovie, db, "The Matrix Reloaded") should have size (3)
    Datomic.q(queryFindAllMoviesWithRole, db, "Agent Smith") should have size (2)

  }
}
