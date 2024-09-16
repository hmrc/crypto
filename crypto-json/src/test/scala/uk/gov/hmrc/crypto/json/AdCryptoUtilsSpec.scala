/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Json, JsSuccess, Reads, __}
import uk.gov.hmrc.crypto.{AdDecrypter, AdEncrypter, EncryptedValue}

class AdCryptoUtilsSpec
  extends AnyWordSpecLike
     with Matchers
     with OptionValues {
  import AdCryptoUtilsSpec._

  "sensitiveEncrypterDecrypter" should {
    "encrypt/decrypt primitives" in {

      val testEntityFormat: Format[TestEntity] = {
        implicit val tsef: Format[TestSubEntity] =
          ( (__ \ "aField").format[String]
          ~ (__ \ "bField").format[String]
          )(TestSubEntity.apply, o => (o.aField, o.bField))
        ( (__ \ "name"    ).format[String]
        ~ (__ \ "aString" ).format[String]
        ~ (__ \ "aBoolean").format[Boolean]
        ~ (__ \ "aNumber" ).format[BigDecimal]
        ~ (__ \ "aObject" ).format[TestSubEntity]
        )(TestEntity.apply, o => (o.name, o.aString, o.aBoolean, o.aNumber, o.aObject))
      }

      val cryptoFormat: Format[TestEntity] =
        AdCryptoUtils.encryptWith[TestEntity](
          associatedDataPath  = __ \ "name",
          encryptedFieldPaths = Seq(
                                  __ \ "aString",
                                  __ \ "aBoolean",
                                  __ \ "aNumber",
                                  __ \ "aObject"
                                )
        )(testEntityFormat)

      val testEntity = TestEntity(
                name     = "name",
                aString  = "string",
                aBoolean = true,
                aNumber  = BigDecimal(1.0),
                aObject  = TestSubEntity(
                            aField = "aField",
                            bField = "bField"
                          )
              )

      val cryptoJson = cryptoFormat.writes(testEntity)

      // be able to read json back
      Json.fromJson[TestEntity](cryptoJson)(cryptoFormat).asOpt.value shouldBe testEntity

      // not contain raw values
      (cryptoJson \ "name").as[String] shouldBe "name"
      cryptoJson.toString should not include "string"
      cryptoJson.toString should not include "true"
      cryptoJson.toString should not include "aField"
      cryptoJson.toString should not include "bField"

      // be encrypted
      implicit val evr: Reads[EncryptedValue] = CryptoFormats.encryptedValueFormat
      (cryptoJson \ "aString" ).validate[EncryptedValue] shouldBe a[JsSuccess[_]]
      (cryptoJson \ "aBoolean").validate[EncryptedValue] shouldBe a[JsSuccess[_]]
      (cryptoJson \ "aNumber" ).validate[EncryptedValue] shouldBe a[JsSuccess[_]]
      (cryptoJson \ "aObject" ).validate[EncryptedValue] shouldBe a[JsSuccess[_]]
    }
  }
}

object AdCryptoUtilsSpec {
  implicit val crypto: AdEncrypter with AdDecrypter = {
    val aesKey = {
      val aesKey = new Array[Byte](32)
      new java.security.SecureRandom().nextBytes(aesKey)
      java.util.Base64.getEncoder.encodeToString(aesKey)
    }
    uk.gov.hmrc.crypto.SymmetricCryptoFactory.aesGcmAdCrypto(aesKey)
  }

  case class TestEntity(
    name    : String,
    aString : String,
    aBoolean: Boolean,
    aNumber : BigDecimal,
    aObject : TestSubEntity
  )

  case class TestSubEntity(
    aField: String,
    bField: String
  )
}
