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
import uk.gov.hmrc.crypto.secure.{EncryptedBytes, GCMEncrypterDecrypter}

import java.util.Base64
import javax.inject.Inject
import scala.util.{Success, Try}

case class EncryptedValue(
  value: String,
  nonce: String
)

class AesGcmAdCryptoFromConfig(baseConfigKey: String, config: Config)
  extends AesGcmAdCrypto(
    aesKey          = config.get[String](s"$baseConfigKey.key"),
    previousAesKeys = config.get[List[String]](s"$baseConfigKey.previousKeys", ifMissing = List.empty)
  )

class AesGcmAdCrypto @Inject()(aesKey: String, previousAesKeys: List[String] = List.empty) {
  private val NONCE_LENGTH = 96

  private val cipher =
    new GCMEncrypterDecrypter(Base64.getDecoder.decode(aesKey), nonceLength = NONCE_LENGTH)

  private val previousCiphers =
    previousAesKeys.map(key =>
      new GCMEncrypterDecrypter(Base64.getDecoder.decode(key), nonceLength = NONCE_LENGTH)
    )

  def encrypt(valueToEncrypt: String, associatedText: String): EncryptedValue = {
    validateAssociatedText(associatedText)
    val encryptedValue = cipher.encrypt(valueToEncrypt.getBytes, associatedText.getBytes)
    EncryptedValue(
      value = new String(Base64.getEncoder.encode(encryptedValue.value)),
      nonce = new String(Base64.getEncoder.encode(encryptedValue.nonce))
    )
  }

  def decrypt(valueToDecrypt: EncryptedValue, associatedText: String): String = {
    validateAssociatedText(associatedText)
    val encryptedBytes =
      EncryptedBytes(
        value = Base64.getDecoder.decode(valueToDecrypt.value.getBytes),
        nonce = Base64.getDecoder.decode(valueToDecrypt.nonce.getBytes)
      )
    (cipher +: previousCiphers).toStream
      .map(cipher => Try(cipher.decrypt(encryptedBytes, associatedText.getBytes)))
      .collectFirst { case Success(res) => res }
      .getOrElse(throw new SecurityException("Unable to decrypt value with any key"))
  }

  private def validateAssociatedText(associatedText: String): Unit =
    if (associatedText == null || associatedText.isEmpty)
      throw new SecurityException("associated text must not be null")
}
