/*
 * Copyright 2020 HM Revenue & Customs
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

import scala.util.control.NonFatal

class CryptoGCMWithKeysFromConfig(baseConfigKey: String, config: Config) extends CompositeSymmetricCrypto {

  override protected val currentCrypto = {
    val configKey = baseConfigKey + ".key"
    val currentEncryptionKey = config.get[String](
      key       = configKey,
      ifMissing = throw new SecurityException(s"Missing required configuration entry: $configKey")
    )
    aesGCMCrypto(currentEncryptionKey, "")
  }

  override protected val previousCryptos = {
    val configKey              = baseConfigKey + ".previousKeys"
    val previousEncryptionKeys = config.get[List[String]](configKey, ifMissing = List.empty)
    previousEncryptionKeys.map(k => aesGCMCrypto(k, ""))
  }

  private def aesGCMCrypto(key: String, additional: String) =
    // Constructor initialisation - verify crypto before returning handle.
    try {
      val crypto = new AesGCMCrypto {
        override val encryptionKey = key
      }
      val encrypted = crypto.encrypt(PlainText("assert-valid-key"))
      crypto.decrypt(encrypted)
      crypto
    } catch {
      case NonFatal(ex) =>
        throw new SecurityException("Invalid encryption key", ex)
    }
}
