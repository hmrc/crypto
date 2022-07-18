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

import javax.crypto.spec.SecretKeySpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class SymmetricSpec extends AnyWordSpecLike with Matchers {

  "Symetric encrypter/decrypter" should {

    "be symmetric using fixed secret key using AES" in {
      val secretKey = new SecretKeySpec("kr*r4k?3%p-bEgR1".getBytes(StandardCharsets.UTF_8), "AES")

      val encrypter = new SymmetricEncrypter(secretKey)
      val decrypter = new SymmetricDecrypter(secretKey)

      val original = "pweoitpwjgpwoiejxgpwoijgpwoiejgpwojg9t0934876034976034ugjgp3i4u6pti3j4tk3gvm3;f"

      val encrypted = encrypter.encrypt(original)
      val decrypted = decrypter.decrypt(encrypted)

      encrypted should not be original
      decrypted shouldBe original
    }

    "be symmetric using fixed secret key using DES" in {
      // DES has 64 bit key size
      val secretKey = new SecretKeySpec("kr*r4/?3".getBytes(StandardCharsets.UTF_8), "DES")

      val encrypter = new SymmetricEncrypter(secretKey)
      val decrypter = new SymmetricDecrypter(secretKey)

      val original = "pweoitpwjgpwoiejxgpwoijgpwoiejgpwojg9t0934876034976034ugjgp3i4u6pti3j4tk3gvm3;f"

      val encrypted = encrypter.encrypt(original)
      val decrypted = decrypter.decrypt(encrypted)

      encrypted should not be original
      decrypted shouldBe original
    }

    "throw exception if encryption attempted without a secret key" in {
      an [IllegalStateException] should be thrownBy {
        new SymmetricEncrypter(null)
      }
    }

    "throw exception if decryption attempted without a secret key" in {
      an [IllegalStateException] should be thrownBy {
        new SymmetricDecrypter(null)
      }
    }

  }


}
