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

class Sha512Crypto(encryptionKey: String) extends Hasher with Verifier {

  private lazy val encrypter = {
    val encryptionKeyBytes = Base64.getDecoder.decode(encryptionKey.getBytes("UTF-8"))
    val secretKey          = new SecretKeySpec(encryptionKeyBytes, "HmacSHA512")
    new SymmetricHasher(secretKey)
  }

  override def hash(plainText: PlainText): Scrambled =
    encrypter.hash(plainText)

  override def verify(plainText: PlainText, ncrypted: Scrambled): Boolean =
    encrypter.hash(plainText) == ncrypted
}
