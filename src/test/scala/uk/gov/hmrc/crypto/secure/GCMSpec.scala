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

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util

import org.bouncycastle.crypto.InvalidCipherTextException
import org.bouncycastle.crypto.params.{AEADParameters, KeyParameter}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GCMSpec extends AnyWordSpecLike with Matchers {

  private val rand = new SecureRandom
  private val keybytes = new Array[Byte](16 * 2)
  private val nonceLength = 16
  private val nouncebytes = new Array[Byte](nonceLength)
  private val testKey = new KeyParameter(keybytes)
  private val associatedText = "Some Associated Text".getBytes(StandardCharsets.UTF_8)
  private val plaintext = "Some plaintext".getBytes(StandardCharsets.UTF_8)

  rand.nextBytes(keybytes); // a random key
  rand.nextBytes(nouncebytes); // a random nounce

  "GCM" should {
    "throw exception if key used to decrypt is different" in {
      val params = new AEADParameters(testKey, GCMEncrypterDecrypter.MAC_SIZE, nouncebytes, associatedText)
      val ciphertext = GCM.encrypt(plaintext, params, 0)
      val badKey = new Array[Byte](16 * 2)
      rand.nextBytes(badKey)
      val paramsWithBadKey = new AEADParameters(new KeyParameter(badKey), GCMEncrypterDecrypter.MAC_SIZE, nouncebytes, associatedText)

      the [InvalidCipherTextException] thrownBy {
        GCM.decrypt(ciphertext, paramsWithBadKey)
      } should have message "mac check in GCM failed"
    }

    "successfully encrypt and decrypt payload" in {
      rand.nextBytes(nouncebytes)
      val params = new AEADParameters(testKey, GCMEncrypterDecrypter.MAC_SIZE, nouncebytes, associatedText)
      val ciphertext = GCM.encrypt(plaintext, params, 0)

      val decrypted = GCM.decrypt(ciphertext, params)

      plaintext shouldBe decrypted

    }

    "write the encrypted on the given offset" in {
      rand.nextBytes(nouncebytes)
      val params = new AEADParameters(testKey, GCMEncrypterDecrypter.MAC_SIZE, nouncebytes, associatedText)
      val ciphertextWithBlankPreamble = GCM.encrypt(plaintext, params, 16)
      val cipherText = util.Arrays.copyOfRange(ciphertextWithBlankPreamble, 16, ciphertextWithBlankPreamble.length)

      val decrypted = GCM.decrypt(cipherText, params)

      plaintext shouldBe decrypted
    }

    "fail when nonce used to decrypt is wrong" in {
      val params = new AEADParameters(testKey, GCMEncrypterDecrypter.MAC_SIZE, nouncebytes, associatedText)
      val ciphertext = GCM.encrypt(plaintext, params, 0)
      val paramsWithBadNonce = new AEADParameters(testKey, GCMEncrypterDecrypter.MAC_SIZE, new Array[Byte](nonceLength), associatedText)

      the [InvalidCipherTextException] thrownBy {
        GCM.decrypt(ciphertext, paramsWithBadNonce)
      } should have message "mac check in GCM failed"
    }

    "fail when associated text used to decrypt is wrong" in {
      val params = new AEADParameters(testKey, GCMEncrypterDecrypter.MAC_SIZE, nouncebytes, associatedText)
      val ciphertext = GCM.encrypt(plaintext, params, 0)
      val paramsWithBadAD = new AEADParameters(testKey, GCMEncrypterDecrypter.MAC_SIZE, nouncebytes, new Array[Byte](16))

      the [InvalidCipherTextException] thrownBy {
        GCM.decrypt(ciphertext, paramsWithBadAD)
      } should have message "mac check in GCM failed"
    }

  }

}
