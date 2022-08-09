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

import uk.gov.hmrc.crypto.secure.{EncryptedBytes, GCMEncrypterDecrypter}

import java.util.Base64

// Note: The result of the GSM encryption is {nonce:16bytes}{encrypted GCM result}. To decrypt the encrypted value, the
// nonce is first extracted which is then used to decrypt the remaining GCM encrypted value.

trait AesGCMCrypto extends Encrypter with Decrypter {
  private val emptyAssociatedData = Array[Byte]()

  protected val encryptionKey: String

  private val nonceLength = 16

  private lazy val encryptionKeyBytes =
    Base64.getDecoder.decode(encryptionKey.getBytes("UTF-8"))

  private lazy val crypto =
    new GCMEncrypterDecrypter(encryptionKeyBytes, nonceLength = nonceLength)

  override def encrypt(plain: PlainContent): Crypted =
    plain match {
      case PlainBytes(bytes) => Crypted(fromEncryptedValue(crypto.encrypt(bytes        , emptyAssociatedData)))
      case PlainText(text)   => Crypted(fromEncryptedValue(crypto.encrypt(text.getBytes, emptyAssociatedData)))
      case _                 => throw new RuntimeException(s"Unable to encrypt unknown message type: $plain")
    }

  private def fromEncryptedValue(ev: EncryptedBytes): String = {
    val combined = new Array[Byte](ev.nonce.length + ev.value.length)
    System.arraycopy(ev.nonce, 0, combined, 0              , ev.nonce.length)
    System.arraycopy(ev.value, 0, combined, ev.nonce.length, ev.value.length)
    new String(Base64.getEncoder.encode(combined))
  }

  override def decrypt(encrypted: Crypted): PlainText =
    PlainText(crypto.decrypt(toEncryptedValue(encrypted.value), emptyAssociatedData))

  override def decryptAsBytes(encrypted: Crypted): PlainBytes =
    PlainBytes(crypto.decrypt(toEncryptedValue(encrypted.value), emptyAssociatedData).getBytes)

  private def toEncryptedValue(encryptedString: String): EncryptedBytes = {
    val encryptedBytes: Array[Byte] = Base64.getDecoder.decode(encryptedString)
    EncryptedBytes(
      nonce = encryptedBytes.take(nonceLength),
      value = encryptedBytes.drop(nonceLength)
    )
  }
}
