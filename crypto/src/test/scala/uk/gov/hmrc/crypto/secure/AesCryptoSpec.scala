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

package uk.gov.hmrc.crypto.secure

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.crypto.AesCrypto
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.crypto.Crypted
import java.util.Base64

class AesCryptoSpec extends AnyWordSpec with Matchers {
 "AesCrypto" should {
  "decrypt both legacy and GCM encrypted values" in {
      val testEncryptionKey = Base64.getEncoder.encodeToString(
        Array[Byte](0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
      )

      val crypto = new AesCrypto {
        override protected val encryptionKey: String = testEncryptionKey
      }

      val legacyEncryptedValue = "rzIqecVRld6ytjwJ6R5lRQ==" // .encrypt("test")
      crypto.decrypt(Crypted(legacyEncryptedValue)) shouldBe PlainText("test")

      val encrypted = crypto.encrypt(PlainText("test"))
      crypto.decrypt(encrypted) shouldBe PlainText("test")
    }
  }
}