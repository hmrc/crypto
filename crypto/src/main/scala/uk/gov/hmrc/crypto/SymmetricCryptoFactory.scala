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

import com.typesafe.config.Config

import scala.util.{Success, Try}

object SymmetricCryptoFactory {
  /** Composes a current crypto for encryption and decryption, along with any previous decrypters.
    * This enables changing the crypto alorithm/secret key while still being able to decrypt any
    * previously encrypted data.
    */
  def composeCrypto(currentCrypto: Encrypter with Decrypter, previousDecrypters: Seq[Decrypter]): Encrypter with Decrypter =
    new Encrypter with Decrypter {
      override def encrypt(value: PlainContent): Crypted =
        currentCrypto.encrypt(value)

      override def decrypt(scrambled: Crypted): PlainText =
        decrypt(d => Try(d.decrypt(scrambled)))

      override def decryptAsBytes(scrambled: Crypted): PlainBytes =
        decrypt(d => Try(d.decryptAsBytes(scrambled)))

      private def decrypt[T <: PlainContent](tryDecryption: Decrypter => Try[T]): T = {
        val decrypterStream = (currentCrypto +: previousDecrypters).toStream
        decrypterStream
          .map(tryDecryption)
          .collectFirst { case Success(msg) => msg }
          .getOrElse(throw new SecurityException("Unable to decrypt value"))
      }
    }

  /** An implementation of "AES" Cipher.
   *  Prefer `aesGcmCrypto` for any new usage, which will not produce repeatable encryptions.
   */
  def aesCrypto(secretKey: String): Encrypter with Decrypter =
    new AesCrypto {
      override protected val encryptionKey: String = secretKey
    }

  /** An implementation of "AES" Cipher.
   *  Prefer `aesGcmCryptoFromConfig` for any new usage, which will not produce repeatable encryptions.
   */
  def aesCryptoFromConfig(baseConfigKey: String, config: Config): Encrypter with Decrypter = {
    val currentEncryptionKey   = config.getString(baseConfigKey + ".key")
    val previousEncryptionKeys = config.get[List[String]](baseConfigKey + ".previousKeys", ifMissing = List.empty)
    composeCrypto(
      currentCrypto      = aesCrypto(currentEncryptionKey),
      previousDecrypters = previousEncryptionKeys.map(aesCrypto)
    )
  }


  /** An implementation of "AES" Cipher, with "GCM" algorithm.
    * Note, the associated data is always set to an empty array. Use `aesGcmAdCrypto` if you want to provide your own associated data.
    */
  def aesGcmCrypto(secretKey: String): Encrypter with Decrypter =
    new AesGCMCrypto {
      override val encryptionKey: String = secretKey
    }

  def aesGcmCryptoFromConfig(baseConfigKey: String, config: Config): Encrypter with Decrypter = {
    val currentEncryptionKey   = config.getString(baseConfigKey + ".key")
    val previousEncryptionKeys = config.get[List[String]](baseConfigKey + ".previousKeys", ifMissing = List.empty)
    composeCrypto(
      currentCrypto      = aesGcmCrypto(currentEncryptionKey),
      previousDecrypters = previousEncryptionKeys.map(aesGcmCrypto)
    )
  }

  // Associated Data Cryptos

  /** Composes a current crypto for encryption and decryption, along with any previous decrypters.
    * This enables changing the crypto alorithm/secret key while still being able to decrypt any
    * previously encrypted data.
    */
  def composeAdCrypto(currentCrypto: AdEncrypter with AdDecrypter, previousDecrypters: Seq[AdDecrypter]): AdEncrypter with AdDecrypter =
    new AdEncrypter with AdDecrypter {
      override def encrypt(valueToEncrypt: String, associatedText: String): EncryptedValue =
        currentCrypto.encrypt(valueToEncrypt, associatedText)

      override def decrypt(valueToDecrypt: EncryptedValue, associatedText: String): String = {
        val decrypterStream = (currentCrypto +: previousDecrypters).toStream
        decrypterStream
          .map(crypter => Try(crypter.decrypt(valueToDecrypt, associatedText)))
          .collectFirst { case Success(msg) => msg }
          .getOrElse(throw new SecurityException("Unable to decrypt value"))
      }
    }

/** An implementation of "AES" Cipher, with "GCM" algorithm.
  * You can provide your own associated data when encrypting/decrypting.
  */
  def aesGcmAdCrypto(aesKey: String): AdEncrypter with AdDecrypter =
    new AesGcmAdCrypto(aesKey: String)

  def aesGcmAdCryptoFromConfig(baseConfigKey: String, config: Config): AdEncrypter with AdDecrypter = {
    val currentEncryptionKey   = config.get[String](s"$baseConfigKey.key")
    val previousEncryptionKeys = config.get[List[String]](s"$baseConfigKey.previousKeys", ifMissing = List.empty)
    composeAdCrypto(
      currentCrypto      = aesGcmAdCrypto(currentEncryptionKey),
      previousDecrypters = previousEncryptionKeys.map(aesGcmAdCrypto)
    )
  }
}
