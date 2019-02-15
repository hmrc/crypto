/*
 * Copyright 2019 HM Revenue & Customs
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

import org.apache.commons.codec.binary.Base64
import org.scalatest.{Matchers, WordSpecLike}

class Sha512CryptoSpec extends WordSpecLike with Matchers {

  private val sha512Crypto = new Sha512Crypto(
    Base64.encodeBase64String(Array[Byte](0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15))
  )

  "A Sha-512 hasher" should {

    "successfully encrypt a string" in {
      val encrypted = sha512Crypto.hash(PlainText("somerandomtext"))

      encrypted should not be "somerandomtext"

      sha512Crypto.verify(PlainText("somerandomtext"), encrypted) should be(true)
      sha512Crypto.verify(PlainText("someRandomText"), encrypted) should be(false)
    }

  }

}
