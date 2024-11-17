package datomisca

import scala.language.reflectiveCalls

object PersonSampleData extends SampleData {

  object Schema {

    object ns {
      val person = new Namespace("person") {
        val mood = Namespace("person.mood")
      }
    }

    val idAttr = Attribute(ns.person / "id", SchemaType.long, Cardinality.one).withUnique(Unique.identity)
    val nameAttr = Attribute(ns.person / "name", SchemaType.string, Cardinality.one)
      .withDoc("A person's name")
      .withFullText(true)
    val ageAttr  = Attribute(ns.person / "age",  SchemaType.long, Cardinality.one)
      .withDoc("A Person's age")
    val moodAttr = Attribute(ns.person / "mood", SchemaType.ref, Cardinality.many)
      .withDoc("A person's mood")

    val happyMood    = AddIdent(ns.person.mood / "happy")
    val sadMood      = AddIdent(ns.person.mood / "sad")
    val excitedMood  = AddIdent(ns.person.mood / "excited")
    val stressedMood = AddIdent(ns.person.mood / "stressed")
    val angryMood    = AddIdent(ns.person.mood / "angry")

  }

  import Schema._

  override val schema = Seq(
    idAttr, nameAttr, ageAttr, moodAttr,
    happyMood, sadMood, excitedMood,
    stressedMood, angryMood
  )

  val toto = new {
    val id  = 123
    val name  = "toto"
    val age   = 30L
    val moods = Set(happyMood, excitedMood)
  }

  val totoTxData = (
    SchemaEntity.newBuilder
      += (idAttr -> toto.id)
      += (nameAttr -> toto.name)
      += (ageAttr  -> toto.age)
      ++= (moodAttr -> toto.moods)
    ) withId DId(Partition.USER)

  val tutu = new {
    val name  = "tutu"
    val age   = 54L
    val moods = Set(sadMood, stressedMood)
  }

  val tutuTxData = (
    SchemaEntity.newBuilder
      += (nameAttr -> tutu.name)
      += (ageAttr  -> tutu.age)
      ++= (moodAttr -> tutu.moods)
    ) withId DId(Partition.USER)

  val tata = new {
    val name  = "tata"
    val age   = 23L
    val moods = Set(excitedMood, angryMood)
  }

  val tataTxData = (
    SchemaEntity.newBuilder
      += (nameAttr -> tata.name)
      += (ageAttr  -> tata.age)
      ++= (moodAttr -> tata.moods)
    ) withId DId(Partition.USER)

  override val txData = Seq(
    totoTxData, tutuTxData, tataTxData
  )

  // Hardcoded attribute path for :person/name
  val queryPersonIdByName = Query("""
    [:find ?e
     :in $ ?name
     :where [?e :person/name ?name]]
  """)
}
