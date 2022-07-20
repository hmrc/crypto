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

import play.api.libs.json._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, Crypted, PlainText, Protected}

import scala.util.{Failure, Success, Try}

@deprecated("Use CryptoFormats.protectedEncrypter", "7.0.0")
class JsonEncryptor[T]()(implicit crypto: CompositeSymmetricCrypto, wrts: Writes[T]) extends Writes[Protected[T]] {
  override def writes(o: Protected[T]): JsValue = {
    val jsonString = wrts.writes(o.decryptedValue).toString
    JsString(crypto.encrypt(PlainText(jsonString)).value)
  }
}

@deprecated("Use CryptoFormats.protectedDecrypter", "7.0.0")
class JsonDecryptor[T](implicit crypto: CompositeSymmetricCrypto, rds: Reads[T]) extends Reads[Protected[T]] {
  override def reads(json: JsValue): JsResult[Protected[T]] = {
    json.validate[String]
      .flatMap { encryptedSting =>
        Try(crypto.decrypt(Crypted(encryptedSting))) match {
          case Success(pt) => JsSuccess(Protected(Json.parse(pt.value).as[T]))
          case Failure(e)  => JsError(s"Failed to decrypt: ${e.getMessage}")
        }
      }
  }
}
