/*
 * Copyright 2018 HM Revenue & Customs
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

import org.apache.commons.codec.binary.Base64
import uk.gov.hmrc.secure.GCMEncrypterDecrypter

trait AesGCMCrypto extends Encrypter with Decrypter {
  private val EMPTY_ASSOCIATE_TEXT = ""

  protected val encryptionKey: String

  private lazy val encryptionKeyBytes = Base64.decodeBase64(encryptionKey.getBytes("UTF-8"))

  private lazy val crypto = new GCMEncrypterDecrypter(encryptionKeyBytes, EMPTY_ASSOCIATE_TEXT.getBytes)

  override def encrypt(plain: PlainContent): Crypted = plain match {
    case PlainBytes(bytes) => Crypted(crypto.encrypt(bytes))

    case PlainText(text) => Crypted(crypto.encrypt(text.getBytes))

    case _ => throw new RuntimeException(s"Unable to encrypt unknown message type: $plain")
  }

  override def decrypt(encrypted: Crypted): PlainText = {
    PlainText(crypto.decrypt(encrypted.value.getBytes))
  }

  override def decryptAsBytes(encrypted: Crypted): PlainBytes = {
    PlainBytes(crypto.decrypt(encrypted.value.getBytes).getBytes)
  }

}
