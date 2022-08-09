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

class AesGcmAdCrypto(aesKey: String) extends AdEncrypter with AdDecrypter {
  private val NONCE_LENGTH = 96

  private val cipher =
    new GCMEncrypterDecrypter(Base64.getDecoder.decode(aesKey), nonceLength = NONCE_LENGTH)

  override def encrypt(valueToEncrypt: String, associatedText: String): EncryptedValue = {
    validateAssociatedText(associatedText)
    val encryptedValue = cipher.encrypt(valueToEncrypt.getBytes, associatedText.getBytes)
    EncryptedValue(
      value = new String(Base64.getEncoder.encode(encryptedValue.value)),
      nonce = new String(Base64.getEncoder.encode(encryptedValue.nonce))
    )
  }

  override def decrypt(valueToDecrypt: EncryptedValue, associatedText: String): String = {
    validateAssociatedText(associatedText)
    val encryptedBytes =
      EncryptedBytes(
        value = Base64.getDecoder.decode(valueToDecrypt.value.getBytes),
        nonce = Base64.getDecoder.decode(valueToDecrypt.nonce.getBytes)
      )
    cipher.decrypt(encryptedBytes, associatedText.getBytes)
  }

  private def validateAssociatedText(associatedText: String): Unit =
    if (associatedText == null || associatedText.isEmpty)
      throw new SecurityException("associated text must not be null")
}
