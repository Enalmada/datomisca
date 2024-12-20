/*
 * Copyright 2012 Pellucid and Zenexity
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package datomisca

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class DatabaseFilteringSpec
  extends AnyFlatSpec
     with Matchers
     with DatomicFixture
     with AwaitHelper
{

  val countStories = Query("""
    [:find (count ?e)
     :where [?e :story/url]]
  """)


  "Database filtering example" should "run to completion" in withSampleDatomicDB(SocialNewsSampleData) { conn =>

    await {
      Future.traverse(SocialNewsSampleData.txDatas)(conn.transact)
    }

    val db = conn.database()

    Datomic.q(countStories, db).head should equal (4)

    val filteredDb = db.filter { (db, datom) =>
      db.entity(datom.tx).get(Datomic.KW(":publish/at")).isDefined
    }

    Datomic.q(countStories, filteredDb).head should equal (1)
  }
}
