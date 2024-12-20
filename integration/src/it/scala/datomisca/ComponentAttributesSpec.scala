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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global


class ComponentAttributesSpec
  extends AnyFlatSpec
     with Matchers
     with ScalaFutures
     with DatomicFixture
{

  "Component attributes example" should "run to completion" in withSampleDatomicDB(SocialNewsSampleData) { implicit conn =>

    whenReady(Datomic.transact(SocialNewsSampleData.storyWithComments)) { _ =>
      whenReady(Datomic.transact(Entity.retract(conn.database().entid(Datomic.KW(":storyWithComments"))))) { txReport =>
        val builder = Set.newBuilder[Long]
        for (datom <- txReport.txData)
          if (!datom.added)
            builder += datom.id

        builder.result() should have size (3)
      }
    }
  }
}
