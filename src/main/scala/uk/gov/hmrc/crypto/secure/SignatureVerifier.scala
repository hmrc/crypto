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

package uk.gov.hmrc.crypto.secure

import uk.gov.hmrc.crypto.secure.Algorithm._

import java.nio.charset.StandardCharsets
import java.security._
import java.util.Base64

class SignatureVerifier(val publicKey: PublicKey) {

  def verify(
    data     : String,
    signature: String,
    algorithm: Algorithm = SHA1withRSA
  ): Boolean =
    try {
      val sig = Signature.getInstance(algorithm.value())
      sig.initVerify(publicKey)
      sig.update(data.getBytes(StandardCharsets.UTF_8))
      sig.verify(Base64.getDecoder.decode(signature))
    } catch {
      case e: NoSuchAlgorithmException => throw new SecurityException(s"Algorithm '${algorithm.value()}' is not supported", e)
      case e: InvalidKeyException      => throw new SecurityException("The private key is invalid", e)
      case e: SignatureException       => throw new SecurityException("Signature error", e)
    }
}
