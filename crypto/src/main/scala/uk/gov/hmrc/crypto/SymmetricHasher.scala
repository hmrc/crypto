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

import java.nio.charset.StandardCharsets
import java.security.{InvalidKeyException, Key, NoSuchAlgorithmException, SignatureException}
import java.util.Base64
import javax.crypto.Mac

class SymmetricHasher(secretKey: Key) {
  def hash(data: PlainText): Scrambled =
    try {
      val sha512_HMAC = Mac.getInstance(secretKey.getAlgorithm)
      sha512_HMAC.init(secretKey)
      Scrambled(Base64.getEncoder.encodeToString(sha512_HMAC.doFinal(data.value.getBytes(StandardCharsets.UTF_8))))
    } catch {
      case e: NoSuchAlgorithmException => throw new SecurityException(s"Algorithm '${secretKey.getAlgorithm}' is not supported", e)
      case e: InvalidKeyException      => throw new SecurityException("The private key is invalid", e)
      case e: SignatureException       => throw new SecurityException("Signature error", e)
    }
}
