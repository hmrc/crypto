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

import java.util

import org.bouncycastle.crypto.params.{AEADParameters, KeyParameter}
import org.bouncycastle.util.encoders.Base64
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GCMEncrypterDecrypterSpec extends AnyWordSpecLike with Matchers {

  "GCMEncrypterDecrypter" should {

    "encrypt and decrypt without additional text" in {
      val encryptMessage = "data to encrypted!"
      val associatedText = ""
      val cipher         = new GCMEncrypterDecrypter("12345678901234561234567890987654".getBytes)

      val encrypted = cipher.encrypt(encryptMessage.getBytes, associatedText.getBytes)
      val decrypted = cipher.decrypt(encrypted.getBytes     , associatedText.getBytes)

      decrypted.getBytes shouldBe encryptMessage.getBytes
    }

    "encrypt and decrypt with additional text" in {
      val valueToEncrypt = "somedata"
      val associatedText = "additional"
      val cipher   = new GCMEncrypterDecrypter("1234567890123456".getBytes)

      val response = cipher.encrypt(valueToEncrypt.getBytes, associatedText.getBytes)
      val decrypt  = cipher.decrypt(response.getBytes      , associatedText.getBytes)

      valueToEncrypt.getBytes shouldBe decrypt.getBytes
    }

    "successfully encrypt and decrypt payload by manually extracting the nonce" in {
      val encryptMessage = "data to encrypt"
      val key            = "1234567890123456"
      val associatedText = "associate"
      val nonceLength    = 16

      val cipher = new GCMEncrypterDecrypter(key.getBytes, nonceLength = nonceLength)

      // Encrypt
      val encrypted = cipher.encrypt(encryptMessage.getBytes, associatedText.getBytes)

      // Manually extract the nonce from the message and then decrypt using the low level API.
      val decoded          = Base64.decode(encrypted)
      val nonce            = util.Arrays.copyOfRange(decoded, 0, nonceLength)
      val extractEncrypted = util.Arrays.copyOfRange(decoded, nonceLength, decoded.length)
      val keyParam         = new KeyParameter(key.getBytes)
      val params           = new AEADParameters(keyParam, GCMEncrypterDecrypter.MAC_SIZE, nonce, associatedText.getBytes)

      val decrypted = GCM.decrypt(extractEncrypted, params)

      decrypted shouldBe encryptMessage.getBytes
    }

    "fail to decrypt if the nonce is invalid" in {
      val encryptMessage = "data to encrypt"
      val associatedText = "associate"

      val cipher = new GCMEncrypterDecrypter("1234567890123456".getBytes)

      val encrypted = cipher.encrypt(encryptMessage.getBytes, associatedText.getBytes)
      val invalidEncrypted = "aA" + encrypted.substring(2, encrypted.length)

      the [SecurityException] thrownBy {
        cipher.decrypt(invalidEncrypted.getBytes, associatedText.getBytes)
      } should have message "Failed decrypting data"
    }
  }
}
