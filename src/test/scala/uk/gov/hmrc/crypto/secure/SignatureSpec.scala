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

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import org.apache.commons.codec.binary.Base64
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SignatureSpec extends AnyWordSpecLike with Matchers with KeyProvider {

  "Signer" should {

    "sign with private key and verify with public key" in {
      val publicKey = getPublicKey("/keys/server.crt")
      val privateKey = getPrivateKey("/keys/key.pk8")
      val data = "fdiuo3uyorijgowrijgwleijlwekmgldisflwejghlsghasasga"
      val dataHash = hash(data)

      val signer = new Signer(privateKey)
      val signatureVerifier = new SignatureVerifier(publicKey)

      val signature = signer.sign(dataHash)
      val verify = signatureVerifier.verify(dataHash, signature)

      verify shouldBe true
    }

  }

  private def hash(data: String): String = {
    val messageDigest = MessageDigest.getInstance("SHA-1")
    messageDigest.update(data.getBytes(StandardCharsets.UTF_8))
    val digest = messageDigest.digest
    Base64.encodeBase64String(digest)
  }

}
