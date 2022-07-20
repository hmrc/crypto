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

import java.security.{NoSuchAlgorithmException, SecureRandom}
import javax.crypto.{Cipher, SecretKey}
import javax.crypto.spec.{GCMParameterSpec, SecretKeySpec}
import scala.util.{Failure, Success, Try}


// Notes...
// http://unafbapune.blogspot.co.uk/2012/06/aesgcm-with-associated-data.html
// https://tools.ietf.org/html/rfc5288#page-2
// https://tools.ietf.org/html/rfc5116
// http://crypto.stackexchange.com/questions/6711/how-to-use-gcm-mode-and-associated-data-properly


case class EncryptedBytes(
  value: Array[Byte],
  nonce: Array[Byte]
)

class GCMEncrypterDecrypter(
  private val key        : Array[Byte],
  private val nonceLength: Int         = 16
) {
  private val TAG_BIT_LENGTH = 128
  private val ALGORITHM_TO_TRANSFORM_STRING = "AES/GCM/PKCS5Padding"
  private val ALGORITHM_KEY = "AES"

  private lazy val secureRandom =
    try {
      val random = SecureRandom.getInstance("SHA1PRNG")
      random.setSeed(random.generateSeed(nonceLength))
      random
     } catch {
      case e: NoSuchAlgorithmException =>
        throw new SecurityException("Failed to obtain instance of randomizer!", e)
     }


  private val secretKey = toSecretKey(key)
  encrypt("assert-valid-key".getBytes, "assert-valid-key".getBytes) // encrypt something to check if key is valid

  def encrypt(valueToEncrypt: Array[Byte], associatedData: Array[Byte]): EncryptedBytes = {
    validateAssociatedData(associatedData)
    val initialisationVector = generateInitialisationVector()
    val encrypted            = encryptBytes(
                                 valueToEncrypt,
                                 associatedData   = associatedData,
                                 gcmParameterSpec = new GCMParameterSpec(TAG_BIT_LENGTH, initialisationVector)
                               )

    EncryptedBytes(
      nonce = initialisationVector,
      value = encrypted
    )
  }

  def decrypt(encryptedBytes: EncryptedBytes, associatedData: Array[Byte]): String = {
    validateAssociatedData(associatedData)
    decryptBytes(
      valueToDecrypt   = encryptedBytes.value,
      associatedData   = associatedData,
      gcmParameterSpec = new GCMParameterSpec(TAG_BIT_LENGTH, encryptedBytes.nonce)
    )
  }

  private def generateInitialisationVector(): Array[Byte] = {
    val iv = new Array[Byte](nonceLength)
    secureRandom.nextBytes(iv)
    iv
  }

  private def toSecretKey(aesKey: Array[Byte]): SecretKey =
    Try {
      new SecretKeySpec(aesKey, ALGORITHM_KEY)
    } match {
      case Success(secretKey) => secretKey
      case Failure(ex)        => throw new SecurityException("The key provided is invalid", ex)
    }

  private[secure] def encryptBytes(
    valueToEncrypt  : Array[Byte],
    associatedData  : Array[Byte],
    gcmParameterSpec: GCMParameterSpec
  ): Array[Byte] =
   Try {
      val cipher = getCipherInstance()
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec, new SecureRandom())
      cipher.updateAAD(associatedData)
      cipher.doFinal(valueToEncrypt)
    } match {
      case Success(result) => result
      case Failure(ex)     => throw new SecurityException("Failed encrypting data", ex)
    }

  private[secure] def decryptBytes(
    valueToDecrypt  : Array[Byte],
    associatedData  : Array[Byte],
    gcmParameterSpec: GCMParameterSpec
  ): String =
    Try {
      val cipher = getCipherInstance()
      cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec, new SecureRandom())
      cipher.updateAAD(associatedData)
      cipher.doFinal(valueToDecrypt)
    } match {
      case Success(value) => new String(value)
      case Failure(ex)    => throw new SecurityException("Failed decrypting data", ex)
    }

  private def getCipherInstance(): Cipher =
    Cipher.getInstance(ALGORITHM_TO_TRANSFORM_STRING)

  private def validateAssociatedData(associatedData: Array[Byte]) =
    if (associatedData == null)
      throw new SecurityException("associated data must not be null", null)
}
