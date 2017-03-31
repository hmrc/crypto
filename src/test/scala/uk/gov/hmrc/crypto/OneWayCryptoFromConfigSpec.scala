/*
 * Copyright 2017 HM Revenue & Customs
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

import org.apache.commons.codec.binary.Base64
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.test.FakeApplication
import play.api.test.Helpers._

class OneWayCryptoFromConfigSpec extends WordSpecLike with Matchers with OptionValues {

  private val baseConfigKey = "crypto.spec"

  private object CurrentKey {
    val configKey = baseConfigKey + ".key"
    val encryptionKey = Base64.encodeBase64String(Array[Byte](0, 1, 2, 3, 4, 5 ,6 ,7, 8 ,9, 10, 11, 12, 13, 14, 15))
    val aeadText = Base64.encodeBase64String("some additional text".getBytes)
    val plainMessage = "this is my message"
    val encryptedMessage = "up/76On5j54pAjzqZR1mqM5E28skTl8Aw0GkKi+zjkk="
  }

  def fakeApplicationWithCurrentKey = FakeApplication(additionalConfiguration = Map(
    CurrentKey.configKey -> CurrentKey.encryptionKey
  ))


  "A correctly constructed one way encrypter" should {

    "encrypt and verify a password" in running(fakeApplicationWithCurrentKey) {
      val cryptor = OneWayCryptoFromConfig(baseConfigKey)
      val encrypted = cryptor.hash(PlainText("myPassword"))

      cryptor.verify(PlainText("myPassword"), encrypted) should be (true)
    }
  }

  "Constructing a one way encrypter without current or previous keys" should {

    def fakeApplicationWithoutAnyKeys = FakeApplication()

    "throw a SecurityException on construction" in running(fakeApplicationWithoutAnyKeys) {
      intercept[SecurityException]{
        OneWayCryptoFromConfig(baseConfigKey)
      }
    }
  }

}
