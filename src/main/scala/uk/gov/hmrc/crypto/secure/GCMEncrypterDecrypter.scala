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
import java.security.{NoSuchAlgorithmException, SecureRandom}

import org.apache.commons.codec.binary.Base64
import org.bouncycastle.crypto.params.{AEADParameters, KeyParameter}

// Note: The result of the GSM encryption is {nonce:16bytes}{encrypted GCM result}. To decrypt the encrypted value, the
// nonce is first extracted which is then used to decrypt the remaining GCM encrypted value.

class GCMEncrypterDecrypter(
  private val key        : Array[Byte],
  private val nonceLength: Int         = 16
) {
  import GCMEncrypterDecrypter._

  if (key == null)
    throw new IllegalStateException("There is no Key defined!")

  private lazy val secureRNG = initSecureRNG()
  private lazy val keyParam: KeyParameter = new KeyParameter(key)

  def encrypt(data: Array[Byte], associatedText: Array[Byte]): String = {
    validateAssociatedText(associatedText)
    val nonce = new Array[Byte](nonceLength)
    secureRNG.nextBytes(nonce)
    try {
      val params     = new AEADParameters(keyParam, MAC_SIZE, nonce, associatedText)
      val cipherText = GCM.encrypt(data, params, nonceLength)
      System.arraycopy(nonce, 0, cipherText, 0, nonce.length)
      new String(Base64.encodeBase64(cipherText), StandardCharsets.UTF_8)
    } catch {
      case e: Exception => throw new SecurityException("Failed decrypting data", e)
    }
  }

  def decrypt(data: Array[Byte], associatedText: Array[Byte]): String = {
    validateAssociatedText(associatedText)
    val rawPayload           = Base64.decodeBase64(data)
    val encryptedPayloadSize = rawPayload.length - nonceLength
    val nonce                = new Array[Byte](nonceLength)
    val encrypted            = new Array[Byte](encryptedPayloadSize)
    System.arraycopy(rawPayload, 0          , nonce    , 0, nonceLength         )
    System.arraycopy(rawPayload, nonceLength, encrypted, 0, encryptedPayloadSize)
    try {
      val params = new AEADParameters(keyParam, MAC_SIZE, nonce, associatedText)
      new String(GCM.decrypt(encrypted, params))
    } catch {
      case e: Exception => throw new SecurityException("Failed decrypting data", e)
    }
  }

  private def validateAssociatedText(associatedText: Array[Byte]): Unit =
    if (associatedText == null) throw new IllegalStateException("There is no Associated Text!")

  private def initSecureRNG(): SecureRandom = {
    var random: SecureRandom = null
    try {
      random = SecureRandom.getInstance("SHA1PRNG")
    } catch {
      case e: NoSuchAlgorithmException =>
        throw new SecurityException("Failed to obtain instance of randomizer!", e)
    }
    random.setSeed(random.generateSeed(nonceLength))
    random
  }
}

object GCMEncrypterDecrypter {
  final val MAC_SIZE = 128
}
