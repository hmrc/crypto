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
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.Base64

class CryptoGcmAdSpec
  extends AnyWordSpecLike
     with Matchers {

  private val secretKey      = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val encrypter      = new CryptoGcmAd(secretKey)
  private val textToEncrypt  = "textNotEncrypted"
  private val associatedText = "associatedText"
  private val encryptedText  = EncryptedValue(
    "jOrmajkEqb7Jbo1GvK4Mhc3E7UiOfKS3RCy3O/F6myQ=",
    "WM1yMH4KBGdXe65vl8Gzd37Ob2Bf1bFUSaMqXk78sNeorPFOSWwwhOj0Lcebm5nWRhjNgL4K2SV3GWEXyyqeIhWQ4fJIVQRHM9VjWCTyf7/1/f/ckAaMHqkF1XC8bnW9"
  )

  "encrypt" should {
    "encrypt some text" in {
      val encryptedValue = encrypter.encrypt(textToEncrypt, associatedText)
      encryptedValue shouldBe an[EncryptedValue]
    }
  }

  "decrypt" should {
    "decrypt text when the same associatedText, nonce and secretKey were used to encrypt it" in {
      val decryptedText = encrypter.decrypt(encryptedText, associatedText)
      decryptedText shouldEqual textToEncrypt
    }

    "return an SecurityException if the encrypted value is different" in {
      val invalidText           = Base64.getEncoder.encodeToString("invalid value".getBytes)
      val invalidEncryptedValue = EncryptedValue(invalidText, encryptedText.nonce)

      val decryptAttempt = intercept[SecurityException](
        encrypter.decrypt(invalidEncryptedValue, associatedText)
      )

      decryptAttempt.getMessage should include("Unable to decrypt value with any key")
    }

    "return an SecurityException if the nonce is different" in {
      val invalidNonce          = Base64.getEncoder.encodeToString("invalid value".getBytes)
      val invalidEncryptedValue = EncryptedValue(encryptedText.value, invalidNonce)

      val decryptAttempt = intercept[SecurityException](
        encrypter.decrypt(invalidEncryptedValue, associatedText)
      )

      decryptAttempt.getMessage should include("Unable to decrypt value with any key")
    }

    "return an SecurityException if the associated text is different" in {
      val decryptAttempt = intercept[SecurityException](
        encrypter.decrypt(encryptedText, "invalid associated text")
      )

      decryptAttempt.getMessage should include("Unable to decrypt value with any key")
    }

    "return an SecurityException if the associated text is empty" in {
      val decryptAttempt = intercept[SecurityException](
        encrypter.decrypt(encryptedText, "")
      )

      decryptAttempt.getMessage should include("associated text must not be null")
    }

    "return an SecurityException if the secret key is different" in {
      val encrypter = new CryptoGcmAd("cXo7u0HuJK8B/52xLwW7eQ==")

      val decryptAttempt = intercept[SecurityException](
        encrypter.decrypt(encryptedText, associatedText)
      )

      decryptAttempt.getMessage should include("Unable to decrypt value with any key")
    }

    "return an SecurityException if the key is empty" in {
      val decryptAttempt = intercept[SecurityException](
        new CryptoGcmAd("")
      )

      decryptAttempt.getMessage should include("The key provided is invalid")
    }
  }
}
