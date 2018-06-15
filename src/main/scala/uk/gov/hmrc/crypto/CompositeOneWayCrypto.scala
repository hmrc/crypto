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

import com.typesafe.config.Config

import scala.util.control.NonFatal

class CompositeOneWayCrypto(baseConfigKey: String, config: Config) extends Hasher with Verifier {

  override def hash(value: PlainText): Scrambled = currentCrypto.hash(value)

  override def verify(value: PlainText, ncrypted: Scrambled): Boolean = {
    val encrypters = currentCrypto +: previousCryptos
    encrypters.exists(d => d.verify(value, ncrypted))
  }

  private val currentCrypto: Sha512Crypto = {
    val configKey = baseConfigKey + ".key"
    val currentEncryptionKey = config.get[String](
      key       = configKey,
      ifMissing = throw new SecurityException(s"Missing required configuration entry: $configKey")
    )
    sha(currentEncryptionKey)
  }

  private val previousCryptos: Seq[Verifier] = {
    val configKey = baseConfigKey + ".previousKeys"
    val previousEncryptionKeys = config.get[List[String]](
      key       = configKey,
      ifMissing = List.empty
    )
    previousEncryptionKeys.map(sha)
  }

  private def sha(key: String) =
    try {
      val crypto = new Sha512Crypto(key)
      crypto.verify(PlainText("assert-valid-key"), crypto.hash(PlainText("assert-valid-key")))
      crypto
    } catch {
      case NonFatal(ex) =>
        throw new SecurityException("Invalid encryption key", ex)
    }

}
