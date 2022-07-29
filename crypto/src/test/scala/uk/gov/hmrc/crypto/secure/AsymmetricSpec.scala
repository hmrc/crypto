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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class AsymmetricSpec extends AnyWordSpecLike with Matchers with KeyProvider {

  private val original = "osidg9n  rg jt wtjwitu4549gv  p9 u243t  93t2ut34ijt g"

  "Asymetric encrypter/decrypter" should {

    "encrypt using public key and decrypt using private key" in {
      val publicKey = getPublicKey("/keys/server.crt")
      val privateKey = getPrivateKey("/keys/key.pk8")

      val encrypter = new AsymmetricEncrypter(publicKey) {
                        override lazy val algorithm = "RSA/ECB/OAEPWithSHA1AndMGF1Padding"
                      }
      val decrypter = new AsymmetricDecrypter(privateKey) {
                        override lazy val algorithm = "RSA/ECB/OAEPWithSHA1AndMGF1Padding"
                      }

      val encrypted = encrypter.encrypt(original)
      val decrypted = decrypter.decrypt(encrypted)

      decrypted shouldBe original
    }

    "encrypt using public key spec and decrypt using private key" in {
      // Contents of file key.pk8 in base 64 - RSA private key.
      val privateKeyBase64Content = "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"

      val privateKey = AsymmetricDecrypter.buildPrivateKey(privateKeyBase64Content, "RSA")


      val decrypter = new AsymmetricDecrypter(privateKey)
      val encrypter = new AsymmetricEncrypter(decrypter.getPublicKey("RSA"))


      val encrypted = encrypter.encrypt(original)
      val decrypted = decrypter.decrypt(encrypted)

      decrypted shouldBe original
    }
  }
}
