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

import java.util.Base64
import javax.crypto.spec.SecretKeySpec

import uk.gov.hmrc.crypto.secure.{SymmetricDecrypter, SymmetricEncrypter}

import scala.util.{Success, Try}

trait AesCrypto extends Encrypter with Decrypter {

  protected val encryptionKey: String

  private lazy val encryptionKeyBytes =
    Base64.getDecoder.decode(encryptionKey.getBytes("UTF-8"))

  private lazy val secretKey = new SecretKeySpec(encryptionKeyBytes, "AES")

  private lazy val encrypter = new SymmetricEncrypter(secretKey)

  private lazy val decrypter = new SymmetricDecrypter(secretKey)

  override def encrypt(plain: PlainContent): Crypted =
    plain match {
      case PlainBytes(bytes) => Crypted(encrypter.encrypt(bytes))
      case PlainText(text)   => Crypted(encrypter.encrypt(text))
      case _                 => throw new Exception(s"Unable to encrypt unkown message type: $plain")
    }

  override def decrypt(encrypted: Crypted): PlainText =
    PlainText(decrypter.decrypt(encrypted.value))

  override def decryptAsBytes(encrypted: Crypted): PlainBytes =
    PlainBytes(decrypter.decryptAsBytes(encrypted.value))
}

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
