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

import scala.util.control.NonFatal

object OnewayCryptoFactory {
  /** Composes a current crypto for hashing and verifing, along with any previous verifers.
    * This enables changing the crypto alorithm/secret key while still being able to verify any
    * previously hashed data.
    */
  def composeCrypto(currentCrypto: Hasher with Verifier, previousDecrypters: Seq[Verifier]): Hasher with Verifier =
    new Hasher with Verifier {
      override def hash(value: PlainText): Scrambled =
        currentCrypto.hash(value)

      override def verify(value: PlainText, ncrypted: Scrambled): Boolean = {
        val encrypters = currentCrypto +: previousDecrypters
        encrypters.exists(_.verify(value, ncrypted))
      }
    }

  def sha(key: String) =
    try {
      val crypto = new Sha512Crypto(key)
      crypto.verify(PlainText("assert-valid-key"), crypto.hash(PlainText("assert-valid-key")))
      crypto
    } catch {
      case NonFatal(ex) =>
        throw new SecurityException("Invalid encryption key", ex)
    }

  def shaCryptoFromConfig(baseConfigKey: String, config: Config): Hasher with Verifier = {
    val currentEncryptionKey   = config.getString(baseConfigKey + ".key")
    val previousEncryptionKeys = config.get[List[String]](baseConfigKey + ".previousKeys", ifMissing = List.empty)
    composeCrypto(
      currentCrypto      = sha(currentEncryptionKey),
      previousDecrypters = previousEncryptionKeys.map(sha)
    )
  }
}
