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

import com.github.ghik.silencer.silent
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.crypto._

class JsonEncryptionSpec extends AnyWordSpecLike with Matchers {
  "formatting an entity" should {
    "encrypt the elements" in {
      val e = TestEntity(
        "unencrypted",
        Protected[String]("encrypted"),
        Protected[Boolean](true),
        Protected[BigDecimal](BigDecimal("234"))
      )

      val json = Json.toJson(e)(TestEntity.formats)

      (json \ "normalString"    ).get shouldBe JsString("unencrypted")
      (json \ "encryptedString" ).get shouldBe JsString("3TW3L1raxsKBYuKvtKqPEQ==")
      (json \ "encryptedBoolean").get shouldBe JsString("YhWm43Ad3rW5Votdy855Kg==")
      (json \ "encryptedNumber" ).get shouldBe JsString("Z/ipDOvm7C3ck/TBkiteAg==")
    }

    "decrypt the elements" in {

      val jsonString = """{
        "normalString":"unencrypted",
        "encryptedString" : "3TW3L1raxsKBYuKvtKqPEQ==",
        "encryptedBoolean" : "YhWm43Ad3rW5Votdy855Kg==",
        "encryptedNumber" : "Z/ipDOvm7C3ck/TBkiteAg=="
      }""".stripMargin

      val entity = Json.fromJson(Json.parse(jsonString))(TestEntity.formats).get

      entity shouldBe TestEntity(
        "unencrypted",
        Protected[String]("encrypted"),
        Protected[Boolean](true),
        Protected[BigDecimal](BigDecimal("234"))
      )
    }
  }

  "json encryption" should {
    "encrypt/decrypt a given Protected entitiy" in {
      val form                               = TestForm("abdu", "sahin", 100, false)
      val protectd                           = Protected[TestForm](form)
      val encryptor: JsonEncryptor[TestForm] = new JsonEncryptor()(TestEntity.crypto, TestForm.formats)
      val encryptedValue                     = encryptor.writes(protectd)
      val decryptor                          = new JsonDecryptor()(TestEntity.crypto, TestForm.formats)
      val decrypted                          = decryptor.reads(encryptedValue)

      decrypted.asOpt should be(Some(protectd))
      encryptedValue should be(
        JsString("TeYgL3TgD8e0XnvjhQlZDl0E9imdEjgyHHHSizAcKuUBZwh2ITwo34Ud8XNE88QKzfGOgAOpbMMKwcx+gwaGaA=="))
    }
  }
}

case class TestEntity(
  normalString    : String,
  encryptedString : Protected[String],
  encryptedBoolean: Protected[Boolean],
  encryptedNumber : Protected[BigDecimal]
)

@silent("deprecated")
object TestEntity {
    implicit val crypto = new CompositeSymmetricCrypto {
    override protected val currentCrypto: Encrypter with Decrypter = new AesCrypto {
      override protected val encryptionKey: String = "P5xsJ9Nt+quxGZzB4DeLfw=="
    }
    override protected val previousCryptos: Seq[Decrypter] = Seq.empty
  }

  object JsonStringEncryption extends JsonEncryptor[String]
  object JsonBooleanEncryption extends JsonEncryptor[Boolean]
  object JsonBigDecimalEncryption extends JsonEncryptor[BigDecimal]

  object JsonStringDecryption extends JsonDecryptor[String]
  object JsonBooleanDecryption extends JsonDecryptor[Boolean]
  object JsonBigDecimalDecryption extends JsonDecryptor[BigDecimal]

  implicit val formats = {
    implicit val encryptedStringFormats     = JsonStringEncryption
    implicit val encryptedBooleanFormats    = JsonBooleanEncryption
    implicit val encryptedBigDecimalFormats = JsonBigDecimalEncryption

    implicit val decryptedStringFormats     = JsonStringDecryption
    implicit val decryptedBooleanFormats    = JsonBooleanDecryption
    implicit val decryptedBigDecimalFormats = JsonBigDecimalDecryption

    Json.format[TestEntity]
  }
}

case class TestForm(name: String, sname: String, amount: Int, isValid: Boolean)

object TestForm {
  implicit val formats = Json.format[TestForm]
}
