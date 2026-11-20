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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.crypto.json.utils.AesCrypto
import AesGCMCryptoSpec._

class AesGCMCryptoSpec extends AnyWordSpec with Matchers {
  "AesGCMCrypto" should {
    "encrypt and decrypt text using GCM" in {
      val plain = PlainText("test")

      val encrypted = crypto.encrypt(plain)

      encrypted.value should not be plain.value
      crypto.decrypt(encrypted) shouldBe plain
    }

    "encrypt and decrypt bytes using GCM" in {
      val plain = PlainBytes(Array[Byte](1, 2, 3, 4, 5))

      val encrypted = crypto.encrypt(plain)

      crypto.decryptAsBytes(encrypted).value.toSeq shouldBe plain.value.toSeq
    }

    "decrypt text encrypted using legacy ECB" in {
      val plain = PlainText("legacy")

      val encrypted = legacyEncrypter.encrypt(plain)

      crypto.decrypt(encrypted) shouldBe plain
    }

    "decrypt bytes encrypted using legacy ECB" in {
      val plain = PlainBytes(Array[Byte](1, 2, 3, 4, 5))

      val encrypted = legacyEncrypter.encrypt(plain)

      crypto.decryptAsBytes(encrypted).value.toSeq shouldBe plain.value.toSeq
    }
  }
}

object AesGCMCryptoSpec {
  private val key = "P5xsJ9Nt+quxGZzB4DeLfw=="
  private val crypto = new AesGCMCrypto {
    override protected val encryptionKey: String = key
  }
  private val legacyEncrypter =
    new AesCrypto {
      override protected val encryptionKey: String = key
    }
}