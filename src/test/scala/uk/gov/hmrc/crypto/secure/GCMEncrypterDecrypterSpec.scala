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
      val cipher = new GCMEncrypterDecrypter("12345678901234561234567890987654".getBytes, "".getBytes)

      val encrypted = cipher.encrypt(encryptMessage.getBytes)
      val decrypted = cipher.decrypt(encrypted.getBytes)

      decrypted.getBytes shouldBe encryptMessage.getBytes
    }

    "encrypt and decrypt with additional text" in {
      val valueToEncrypt = "somedata"
      val wrapper = new GCMEncrypterDecrypter("1234567890123456".getBytes, "additional".getBytes)

      val response = wrapper.encrypt(valueToEncrypt.getBytes)
      val decrypt = wrapper.decrypt(response.getBytes)

      valueToEncrypt.getBytes shouldBe decrypt.getBytes
    }

    "successfully encrypt and decrypt payload by manually extracting the nonce" in {
      val encryptMessage = "data to encrypt"
      val key = "1234567890123456"
      val associatedText = "associate"
      val cipher = new GCMEncrypterDecrypter(key.getBytes, associatedText.getBytes)

      // Encrypt
      val encrypted = cipher.encrypt(encryptMessage.getBytes)

      // Manually extract the nonce from the message and then decrypt using the low level API.
      val decoded = Base64.decode(encrypted)
      val nonce = util.Arrays.copyOfRange(decoded, 0, GCMEncrypterDecrypter.NONCE_SIZE)
      val extractEncrypted = util.Arrays.copyOfRange(decoded, GCMEncrypterDecrypter.NONCE_SIZE, decoded.length)
      val keyParam = new KeyParameter(key.getBytes)
      val params = new AEADParameters(keyParam, GCMEncrypterDecrypter.MAC_SIZE, nonce, associatedText.getBytes)

      val decrypted = GCM.decrypt(extractEncrypted, params)

      decrypted shouldBe encryptMessage.getBytes
    }

    "fail to decrypt if the nonce is invalid" in {
      val encryptMessage = "data to encrypt"
      val cipher = new GCMEncrypterDecrypter("1234567890123456".getBytes, "associate".getBytes)

      val encrypted = cipher.encrypt(encryptMessage.getBytes)
      val invalidEncrypted = "aA" + encrypted

      the [SecurityException] thrownBy {
        cipher.decrypt(invalidEncrypted.getBytes)
      } should have message "Failed decrypting data"

    }

  }
}
