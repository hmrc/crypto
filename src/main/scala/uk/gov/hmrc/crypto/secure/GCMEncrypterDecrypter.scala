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

package uk.gov.hmrc.secure

import java.nio.charset.StandardCharsets
import java.security.{NoSuchAlgorithmException, SecureRandom}

import org.apache.commons.codec.binary.Base64
import org.bouncycastle.crypto.params.{AEADParameters, KeyParameter}

// Note: The result of the GSM encryption is {nonce:16bytes}{encrypted GCM result}. To decrypt the encrypted value, the
// nonce is first extracted which is then used to decrypt the remaining GCM encrypted value.

class GCMEncrypterDecrypter(private val key: Array[Byte], private val associatedText: Array[Byte]) {

  import GCMEncrypterDecrypter._

  private lazy val secureRNG = initSecureRNG()
  private lazy val keyParam: KeyParameter = new KeyParameter(key)

  def encrypt(data: Array[Byte]): String = {
    validateKey()
    val nonce = new Array[Byte](NONCE_SIZE)
    secureRNG.nextBytes(nonce)
    try {
      val params = new AEADParameters(keyParam, MAC_SIZE, nonce, associatedText)
      val cipherText = GCM.encrypt(data, params, NONCE_SIZE)
      System.arraycopy(nonce, 0, cipherText, 0, nonce.length)
      new String(Base64.encodeBase64(cipherText), StandardCharsets.UTF_8)
    } catch {
      case e: Exception => throw new SecurityException("Failed decrypting data", e)
    }
  }

  def decrypt(data: Array[Byte]): String = {
    validateKey()
    val rawPayload = Base64.decodeBase64(data)
    val encryptedPayloadSize = rawPayload.length - NONCE_SIZE
    val nonce = new Array[Byte](NONCE_SIZE)
    val encrypted = new Array[Byte](encryptedPayloadSize)
    System.arraycopy(rawPayload, 0, nonce, 0, NONCE_SIZE)
    System.arraycopy(rawPayload, NONCE_SIZE, encrypted, 0, encryptedPayloadSize)
    try {
      val params = new AEADParameters(keyParam, MAC_SIZE, nonce, associatedText)
      val plain = GCM.decrypt(encrypted, params)
      new String(plain)
    } catch {
      case e: Exception => throw new SecurityException("Failed decrypting data", e)
    }
  }

  private def validateKey() {
    if (key == null) throw new IllegalStateException("There is no Key defined!")
    if (associatedText == null) throw new IllegalStateException("There is no Associated Text!")
  }

  private def initSecureRNG(): SecureRandom = {
    var random: SecureRandom = null
    try {
      random = SecureRandom.getInstance("SHA1PRNG")
    }
    catch {
      case scae: NoSuchAlgorithmException => {
        throw new SecurityException("Failed to obtain instance of randomizer!", scae)
      }
    }
    random.setSeed(random.generateSeed(NONCE_SIZE))
    random
  }

}

object GCMEncrypterDecrypter {
  final val MAC_SIZE = 128
  final val NONCE_SIZE = MAC_SIZE / 8
}
