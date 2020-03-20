/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.crypto

import com.typesafe.config.{ConfigException, ConfigFactory}
import org.scalatest.{Matchers, WordSpec}

class TypesafeConfigOpsSpec extends WordSpec with Matchers {

  "Getting a string" should {

    "return a string from config if exists" in {
      val expectedValue = "bar"
      val config        = ConfigFactory.parseString(s"foo = $expectedValue")

      config.get[String]("foo") shouldBe expectedValue
    }

    "fallback to default value if key does not exist" in {
      val fallbackValue = "bar"
      val config        = ConfigFactory.empty()

      config.get("foo", ifMissing = fallbackValue) shouldBe fallbackValue
    }

    "throw upstream exception if now fallback provided" in {
      val config = ConfigFactory.empty()

      a[ConfigException] should be thrownBy config.get[String]("foo")
    }
  }

  "Getting a list of strings" should {

    "return a list of strings if exists" in {
      val expectedList = List("a", "b")
      val config       = ConfigFactory.parseString(""" foo = [ "a", "b" ] """)

      config.get[List[String]]("foo") shouldBe expectedList
    }

    "fallback to default value if key does not exist" in {
      val fallbackValue = List("a")
      val config        = ConfigFactory.empty()

      config.get("foo", ifMissing = fallbackValue) shouldBe fallbackValue
    }

    "throw upstream exception if now fallback provided" in {
      val config = ConfigFactory.empty()

      a[ConfigException] should be thrownBy config.get[List[String]]("foo")
    }
  }
}
