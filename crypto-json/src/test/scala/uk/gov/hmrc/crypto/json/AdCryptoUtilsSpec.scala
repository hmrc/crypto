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
        ( (__ \ "asd" \ "name"    ).format[String]
        ~ (__ \ "asd" \ "aString" ).format[String]
        ~ (__ \ "asd" \ "aBoolean").format[Boolean]
        ~ (__ \ "asd" \ "aNumber" ).format[BigDecimal]
        ~ (__ \ "asd" \ "aObject" ).format[TestSubEntity]
        ~ (__ \ "asd" \ "anArray" ).format[List[String]]
        )(TestEntity.apply, o => (o.name, o.aString, o.aBoolean, o.aNumber, o.aObject, o.anArray))
      }

      val cryptoFormat: Format[TestEntity] =
        AdCryptoUtils.encryptWith[TestEntity](
          associatedDataPath  = __ \ "asd" \ "name",
          encryptedFieldPaths = Seq(
                                  __ \ "asd" \ "aString",
                                  __ \ "asd" \ "aBoolean",
                                  __ \ "asd" \ "aNumber",
                                  __ \ "asd" \ "aObject",
                                  __ \ "asd" \ "anArray",
                                  __ \ "nonExisting"
                                )
        )(testEntityFormat)

      val testEntity =
        TestEntity(
          name     = "name",
          aString  = "string",
          aBoolean = true,
          aNumber  = BigDecimal(1.0),
          aObject  = TestSubEntity(
                       aField = "aField",
                       bField = "bField"
                     ),
          anArray  = List("array0", "array1")
        )

      val cryptoJson = cryptoFormat.writes(testEntity)

      // be able to read json back
      Json.fromJson[TestEntity](cryptoJson)(cryptoFormat).asOpt.value shouldBe testEntity

      // not contain raw values
      (cryptoJson \ "asd" \ "name").as[String] shouldBe "name"
      cryptoJson.toString should not include "string"
      cryptoJson.toString should not include "true"
      cryptoJson.toString should not include "aField"
      cryptoJson.toString should not include "bField0"
      cryptoJson.toString should not include "array0"
      cryptoJson.toString should not include "array1"

      // be encrypted
      implicit val evr: Reads[EncryptedValue] = CryptoFormats.encryptedValueFormat
      (cryptoJson \ "asd" \ "aString" ).validate[EncryptedValue] shouldBe a[JsSuccess[_]]
      (cryptoJson \ "asd" \ "aBoolean").validate[EncryptedValue] shouldBe a[JsSuccess[_]]
      (cryptoJson \ "asd" \ "aNumber" ).validate[EncryptedValue] shouldBe a[JsSuccess[_]]
      (cryptoJson \ "asd" \ "aObject" ).validate[EncryptedValue] shouldBe a[JsSuccess[_]]
      (cryptoJson \ "asd" \ "anArray" ).validate[EncryptedValue] shouldBe a[JsSuccess[_]]
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
    name     : String,
    aString  : String,
    aBoolean : Boolean,
    aNumber  : BigDecimal,
    aObject  : TestSubEntity,
    anArray  : List[String]
  )

  case class TestSubEntity(
    aField: String,
    bField: String
  )
}
