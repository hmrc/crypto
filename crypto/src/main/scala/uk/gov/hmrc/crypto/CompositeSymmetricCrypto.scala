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

import scala.util.{Success, Try}

@deprecated("Use SymmetricCryptoFactory.composeCrypto to compose cryptos. Use `Encrypter with Decrypter` as the interface fo cryptos", "7.0.0")
trait CompositeSymmetricCrypto extends Encrypter with Decrypter {

  protected val currentCrypto: Encrypter with Decrypter

  protected val previousCryptos: Seq[Decrypter]

  override def encrypt(value: PlainContent): Crypted =
    encrypt(value, currentCrypto)

  override def decrypt(scrambled: Crypted): PlainText =
    decrypt(d => Try(d.decrypt(scrambled)))

  override def decryptAsBytes(scrambled: Crypted): PlainBytes =
    decrypt(d => Try(d.decryptAsBytes(scrambled)))

  private def decrypt[T <: PlainContent](tryDecryption: Decrypter => Try[T]): T = {
    val decrypterStream = (currentCrypto +: previousCryptos).toStream

    val message =
      decrypterStream
        .map(tryDecryption)
        .collectFirst { case Success(msg) => msg }

    message.getOrElse(throw new SecurityException("Unable to decrypt value"))
  }

  private[crypto] def encrypt(value: PlainContent, encrypter: Encrypter): Crypted =
    encrypter.encrypt(value)
}

object CompositeSymmetricCrypto {

  @deprecated("Use SymmetricCryptoFactory.aesCrypto", "7.0.0")
  def aes(currentKey: String, previousKeys: Seq[String]): CompositeSymmetricCrypto =
    new CompositeSymmetricCrypto {
      override protected val currentCrypto: Encrypter with Decrypter =
        new AesCrypto {
          val encryptionKey: String = currentKey
        }

      override protected val previousCryptos: Seq[Decrypter] =
        previousKeys.map(previousKey =>
          new AesCrypto {
            val encryptionKey: String = previousKey
        })
    }

  @deprecated("Use SymmetricCryptoFactory.aesCrypto", "7.0.0")
  def aesGCM(currentKey: String, previousKeys: Seq[String]): CompositeSymmetricCrypto =
    new CompositeSymmetricCrypto {
      override protected val currentCrypto: Encrypter with Decrypter = new AesGCMCrypto {
        val encryptionKey: String = currentKey
      }

      override protected val previousCryptos: Seq[Decrypter] =
        previousKeys.map(previousKey =>
          new AesGCMCrypto {
            val encryptionKey: String = previousKey
        })
    }
}
