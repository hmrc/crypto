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
import org.scalatest.wordspec.AnyWordSpecLike

class GCMEncrypterDecrypterSpec extends AnyWordSpecLike with Matchers {

  "GCMEncrypterDecrypter" should {

    "encrypt and decrypt without additional text" in {
      val valueToEncrypt = "data to encrypt"
      val associatedText = ""
      val cipher         = new GCMEncrypterDecrypter("12345678901234561234567890987654".getBytes)

      val encrypted = cipher.encrypt(valueToEncrypt.getBytes, associatedText.getBytes)
      val decrypted = cipher.decrypt(encrypted              , associatedText.getBytes)

      valueToEncrypt.getBytes shouldBe decrypted.getBytes
    }

    "encrypt and decrypt with additional text" in {
      val valueToEncrypt = "data to encrypt"
      val associatedText = "associatedText"
      val cipher   = new GCMEncrypterDecrypter("1234567890123456".getBytes)

      val encrypted = cipher.encrypt(valueToEncrypt.getBytes, associatedText.getBytes)
      val decrypted = cipher.decrypt(encrypted, associatedText.getBytes)

      valueToEncrypt.getBytes shouldBe decrypted.getBytes
    }

    "fail to decrypt if the nonce is invalid" in {
      val valueToEncrypt = "data to encrypt"
      val associatedText = "associatedText"

      val cipher = new GCMEncrypterDecrypter("1234567890123456".getBytes)

      val encrypted = cipher.encrypt(valueToEncrypt.getBytes, associatedText.getBytes)
      val invalidEncrypted = encrypted.copy(nonce = "aA".getBytes ++ encrypted.nonce.drop(2))

      the [SecurityException] thrownBy {
        cipher.decrypt(invalidEncrypted, associatedText.getBytes)
      } should have message "Failed decrypting data"
    }

    "fail to decrypt if the key is different" in {
      val valueToEncrypt = "data to encrypt"
      val associatedText = "associatedText"

      val cipher1 = new GCMEncrypterDecrypter("1234567890123456".getBytes)
      val cipher2 = new GCMEncrypterDecrypter("6543210987654321".getBytes)

      val encrypted = cipher1.encrypt(valueToEncrypt.getBytes, associatedText.getBytes)

      the [SecurityException] thrownBy {
        cipher2.decrypt(encrypted, associatedText.getBytes)
      } should have message "Failed decrypting data"
    }

    "fail to decrypt if the associated text is different" in {
      val valueToEncrypt = "data to encrypt"
      val associatedText = "associatedText"

      val cipher = new GCMEncrypterDecrypter("1234567890123456".getBytes)

      val encrypted = cipher.encrypt(valueToEncrypt.getBytes, associatedText.getBytes)

      the [SecurityException] thrownBy {
        val associatedText2 = associatedText + "asd"
        cipher.decrypt(encrypted, associatedText2.getBytes)
      } should have message "Failed decrypting data"
    }
  }
}
