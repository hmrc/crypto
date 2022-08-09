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

package uk.gov.hmrc.crypto.json

import org.scalatest.OptionValues
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsString, Json}
import uk.gov.hmrc.crypto._
import Sensitive._

class CryptoFormatsSpec
  extends AnyWordSpecLike
     with Matchers
     with OptionValues {
  import CryptoFormatsSpec._

  "sensitiveEncrypterDecrypter" should {
    "encrypt/decrypt primitives" in {
      val e = SensitiveTestEntity(
        "unencrypted",
        SensitiveString("encrypted"),
        SensitiveBoolean(true),
        SensitiveBigDecimal(BigDecimal("234"))
      )

      val json = Json.toJson(e)(SensitiveTestEntity.formats)

      (json \ "normalString"    ).get shouldBe JsString("unencrypted")
      (json \ "encryptedString" ).get shouldBe JsString("3TW3L1raxsKBYuKvtKqPEQ==")
      (json \ "encryptedBoolean").get shouldBe JsString("YhWm43Ad3rW5Votdy855Kg==")
      (json \ "encryptedNumber" ).get shouldBe JsString("Z/ipDOvm7C3ck/TBkiteAg==")
    }

    "decrypt the elements" in {
      val jsonString = """{
        "normalString"    :"unencrypted",
        "encryptedString" : "3TW3L1raxsKBYuKvtKqPEQ==",
        "encryptedBoolean": "YhWm43Ad3rW5Votdy855Kg==",
        "encryptedNumber" : "Z/ipDOvm7C3ck/TBkiteAg=="
      }"""

      val entity = Json.fromJson(Json.parse(jsonString))(SensitiveTestEntity.formats).asOpt.value

      entity shouldBe SensitiveTestEntity(
        "unencrypted",
        SensitiveString("encrypted"),
        SensitiveBoolean(true),
        SensitiveBigDecimal(BigDecimal("234"))
      )
    }

    "encrypt/decrypt custom entity" in {
      val sensitiveTestFormCrypto: Format[SensitiveTestForm] = {
        implicit val s = TestForm.formats
        JsonEncryption.sensitiveEncrypterDecrypter(SensitiveTestForm.apply)
      }

      val form           = TestForm("abdu", "sahin", 100, false)
      val protectd       = SensitiveTestForm(form)
      val encryptedValue = sensitiveTestFormCrypto.writes(protectd)

      encryptedValue shouldBe
        JsString("TeYgL3TgD8e0XnvjhQlZDl0E9imdEjgyHHHSizAcKuUBZwh2ITwo34Ud8XNE88QKzfGOgAOpbMMKwcx+gwaGaA==")

      val decrypted = sensitiveTestFormCrypto.reads(encryptedValue)
      decrypted.asOpt.value shouldBe protectd
    }
  }
}

object CryptoFormatsSpec {
  implicit val crypto = SymmetricCryptoFactory.aesCrypto("P5xsJ9Nt+quxGZzB4DeLfw==")

  case class SensitiveTestEntity(
    normalString    : String,
    encryptedString : SensitiveString,
    encryptedBoolean: SensitiveBoolean,
    encryptedNumber : SensitiveBigDecimal
  )

  object SensitiveTestEntity {
    implicit val formats = {
      implicit val sensitiveStringCrypto    : Format[SensitiveString]     = JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)
      implicit val sensitiveBooleanCrypto   : Format[SensitiveBoolean]    = JsonEncryption.sensitiveEncrypterDecrypter(SensitiveBoolean.apply)
      implicit val sensitiveBigDecimalCrypto: Format[SensitiveBigDecimal] = JsonEncryption.sensitiveEncrypterDecrypter(SensitiveBigDecimal.apply)
      Json.format[SensitiveTestEntity]
    }
  }

  case class TestForm(
    name   : String,
    sname  : String,
    amount : Int,
    isValid: Boolean
  )

  object TestForm {
    implicit val formats = Json.format[TestForm]
  }

  case class SensitiveTestForm(override val decryptedValue: TestForm) extends Sensitive[TestForm]
}
