/*
 * Copyright 2015 HM Revenue & Customs
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

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

trait Sha512Crypto extends Hasher with Verifier {

  protected val encryptionKey: String

  private lazy val encrypter = {
    val encryptionKeyBytes = Base64.getDecoder.decode(encryptionKey.getBytes(StandardCharsets.UTF_8))
    val secretKey = new SecretKeySpec(encryptionKeyBytes, "HmacSHA512")
    new SymmetricHasher(secretKey)
  }

  override def hash(plainText: PlainText): Scrambled = encrypter.hash(plainText)

  override def verify(plainText: PlainText, ncrypted: Scrambled): Boolean =
    encrypter.hash(plainText) == ncrypted

}

trait CompositeOneWayCrypto extends Hasher with Verifier {

  protected val currentCrypto: Hasher with Verifier

  protected val previousCryptos: Seq[Verifier]

  override def hash(value: PlainText): Scrambled = currentCrypto.hash(value)

  override def verify(value: PlainText, ncrypted: Scrambled): Boolean = {

    val encrypterStream = (currentCrypto +: previousCryptos).toStream
    encrypterStream.map(d => d.verify(value, ncrypted)) contains true
  }
}
