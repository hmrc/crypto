/*
 * Copyright 2022 HM Revenue & Customs
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

import com.typesafe.config.ConfigFactory
import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.Base64
import scala.collection.JavaConverters._

class CompositeOneWayCryptoSpec extends AnyWordSpecLike with Matchers with MockitoSugar {

  private val baseConfigKey = "crypto.spec"

  private object CurrentKey {
    val configKey        = baseConfigKey + ".key"
    val encryptionKey    = Base64.getEncoder.encodeToString(Array[Byte](0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15))
    val aeadText         = Base64.getEncoder.encodeToString("some additional text".getBytes)
    val plainMessage     = "this is my message"
    val encryptedMessage = "up/76On5j54pAjzqZR1mqM5E28skTl8Aw0GkKi+zjkk="
  }

  "A correctly constructed one way encrypter" should {
    "encrypt and verify a password" in {
      val config = ConfigFactory.parseMap(
        Map(
          CurrentKey.configKey -> CurrentKey.encryptionKey
        ).asJava
      )
      val cryptor = new CompositeOneWayCrypto(baseConfigKey, config)

      val encrypted = cryptor.hash(PlainText("myPassword"))

      cryptor.verify(PlainText("myPassword"), encrypted) should be(true)
    }
  }

  "Constructing a one way encrypter without current or previous keys" should {
    "throw a SecurityException on construction" in {
      intercept[SecurityException] {
        new CompositeOneWayCrypto(baseConfigKey, ConfigFactory.empty)
      }
    }
  }
}
