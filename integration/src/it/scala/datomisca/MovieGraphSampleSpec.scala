package datomisca

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class MovieGraphSampleSpec
  extends AnyFlatSpec
    with Matchers
    with DatomicFixture
    with AwaitHelper {

  object MovieGraphSchema {
    object ns {
      val actor = Namespace("actor")
      val movie = Namespace("movie")
    }

    val actorName = Attribute(ns.actor / "name", SchemaType.string, Cardinality.one).withDoc("The name of the actor")
    val actorActs = Attribute(ns.actor / "acts-in", SchemaType.ref, Cardinality.many).withDoc("References to the movies the actor has acted in")
    val actorRole = Attribute(ns.actor / "role", SchemaType.string, Cardinality.one).withDoc("The character name of a role in a movie")

    val movieTitle = Attribute(ns.movie / "title", SchemaType.string, Cardinality.one).withDoc("The title of the movie")
    val movieYear = Attribute(ns.movie / "year", SchemaType.long, Cardinality.one).withDoc("The year the movie was released")

    val txData = Seq(actorName, actorActs, actorRole, movieTitle, movieYear)
  }

  object MovieGraphData {

    import MovieGraphSchema._

    // Actors
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

    // In MovieGraphData, modify the graphEdgesTxData method:
    def graphEdgesTxData(tempIds: Map[DId, Long]): Seq[Seq[TxData]] = Seq(
      Seq(
        SchemaFact.add(tempIds(`Carrie-Ann Moss`.id))(actorActs -> tempIds(`The Matrix`.id)),
        SchemaFact.add(DId(Partition.TX))(actorRole -> "Trinity")
      ),
      Seq(
        SchemaFact.add(tempIds(`Carrie-Ann Moss`.id))(actorActs -> tempIds(`The Matrix Reloaded`.id)),
        SchemaFact.add(DId(Partition.TX))(actorRole -> "Trinity")
      ),
      Seq(
        SchemaFact.add(tempIds(`Carrie-Ann Moss`.id))(actorActs -> tempIds(Memento.id)),
        SchemaFact.add(DId(Partition.TX))(actorRole -> "Natalie")
      ),
      Seq(
        SchemaFact.add(tempIds(`Hugo Weaving`.id))(actorActs -> tempIds(`The Matrix`.id)),
        SchemaFact.add(DId(Partition.TX))(actorRole -> "Agent Smith")
      ),
      Seq(
        SchemaFact.add(tempIds(`Hugo Weaving`.id))(actorActs -> tempIds(`The Matrix Reloaded`.id)),
        SchemaFact.add(DId(Partition.TX))(actorRole -> "Agent Smith")
      ),
      Seq(
        SchemaFact.add(tempIds(`Guy Peace`.id))(actorActs -> tempIds(Memento.id)),
        SchemaFact.add(DId(Partition.TX))(actorRole -> "Leonard Shelby")
      ),
      Seq(
        SchemaFact.add(tempIds(`Joe Pantoliano`.id))(actorActs -> tempIds(`The Matrix`.id)),
        SchemaFact.add(DId(Partition.TX))(actorRole -> "Cypher")
      ),
      Seq(
        SchemaFact.add(tempIds(`Joe Pantoliano`.id))(actorActs -> tempIds(Memento.id)),
        SchemaFact.add(DId(Partition.TX))(actorRole -> "Teddy Gammell")
      )
    )
  }

    object MovieGraphQueries {
      val queryFindMovieByTitle = Query(
        """
      [:find ?title ?year
       :in $ ?title
       :where
         [?movie :movie/title ?title]
         [?movie :movie/year  ?year]]
    """)

      val queryFindMovieByTitlePrefix = Query(
        """
      [:find ?title ?year
       :in $ ?prefix
       :where
         [?movie :movie/title ?title]
         [?movie :movie/year  ?year]
         [(.startsWith ^String ?title ?prefix)]]
    """)

      val queryFindActorsInTitle = Query(
        """
      [:find ?name
       :in $ ?title
       :where
         [?movie :movie/title ?title]
         [?actor :actor/acts-in ?movie]
         [?actor :actor/name   ?name]]
    """)

      val queryFindTitlesAndRolesForActor = Query(
        """
      [:find ?role ?title
       :in $ ?name
       :where
         [?actor :actor/name   ?name]
         [?actor :actor/acts-in ?movie ?tx]
         [?movie :movie/title  ?title]
         [?tx    :actor/role   ?role]]
    """)

      val queryFindMoviesThatIncludeActorsInGivenMovie = Query(
        """
      [:find ?othertitle
       :in $ ?title
       :where
         [?movie :movie/title ?title]
         [?actor :actor/acts-in ?movie]
         [?actor :actor/acts-in ?othermovie]
         [?othermovie :movie/title ?othertitle]]
    """)

      val queryFindAllMoviesWithRole = Query(
        """
      [:find ?title
       :in $ ?role
       :where
         [?tx    :actor/role   ?role]
         [?actor :actor/acts-in ?movie ?tx]
         [?movie :movie/title  ?title]]
    """)
    }

  "Movie Graph Sample" should "run to completion" in withDatomicDB { implicit conn =>
    import MovieGraphQueries._
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    await {
      Datomic.transact(MovieGraphSchema.txData)
    }

    val txReport = await {
      Datomic.transact(MovieGraphData.graphNodesTxData)
    }

    await {
      Future.sequence {
        MovieGraphData.graphEdgesTxData(txReport.tempidMap) map Datomic.transact
      }
    }

    val db = conn.database()

    Datomic.q(queryFindMovieByTitle, db, "The Matrix") should have size (1)

    Datomic.q(queryFindMovieByTitlePrefix, db, "The Matrix") should have size (2)

    Datomic.q(queryFindActorsInTitle, db, "Memento") should have size (3)

    Datomic.q(queryFindTitlesAndRolesForActor, db, "Carrie-Ann Moss") should have size (3)

    Datomic.q(queryFindMoviesThatIncludeActorsInGivenMovie, db, "The Matrix Reloaded") should have size (3)

    Datomic.q(queryFindAllMoviesWithRole, db, "Agent Smith") should have size (2)

  }

  /*
    // And in the test section:
    "Movie Graph Sample" should "run to completion" in withDatomicDB { implicit conn =>
      import MovieGraphQueries._

      // Transactional operations
      println("Applying schema")
      await(Datomic.transact(MovieGraphSchema.txData))
      println("Schema transaction completed successfully.")

      // Transact the entities
      println("Transacting entities")
      val txReport = await(Datomic.transact(MovieGraphData.graphNodesTxData))
      println("Entities transaction completed successfully.")

      println("Transaction Reports Temporary IDs:")
      if (txReport.tempidMap.nonEmpty) {
        // Proceed with edge transactions
        val edgeData = MovieGraphData.graphEdgesTxData(txReport.tempidMap.toMap)
        println("Transacting edges")
        await(Datomic.transact(edgeData))
        println("Edges transaction completed successfully.")

        // Queries to validate the graph
        val db = conn.database()

        Datomic.q(queryFindMovieByTitle, db, "The Matrix") should have size 1
        Datomic.q(queryFindMovieByTitlePrefix, db, "The Matrix") should have size 2
        Datomic.q(queryFindActorsInTitle, db, "Memento") should have size 3
        Datomic.q(queryFindTitlesAndRolesForActor, db, "Carrie-Ann Moss") should have size 3
        Datomic.q(queryFindMoviesThatIncludeActorsInGivenMovie, db, "The Matrix Reloaded") should have size 3
        Datomic.q(queryFindAllMoviesWithRole, db, "Agent Smith") should have size 2
      } else {
        fail("No temporary IDs resolved in transaction")
      }
    }

   */
  }

