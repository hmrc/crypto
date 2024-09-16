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

import play.api.libs.json._
import uk.gov.hmrc.crypto.{AdDecrypter, AdEncrypter}

object AdCryptoUtils {

  /** Will adapt a Format (written plainly without encryption) to one applying encryption with associated data.
    * It requires a JsPath to point to the associated data field, and JsPaths to point at the fields to be encrypted with it.
    *
    * @param associatedDataPath field to be used as the associated data. It must resolve to a single `String` field, otherwise use of the Format will result in an error.
    *
    * @param encryptedFieldPaths fields to be encrypted. The fields can be optional - if the field does not exist, it simply won't be encrypted. This does mean that client's tests must ensure that encryption occurs as expected to detect misspellings etc.
    */
  def encryptWith[A](
    associatedDataPath : JsPath,
    encryptedFieldPaths: Seq[JsPath]
  )(
    f: Format[A]
  )(implicit
    crypto: AdEncrypter with AdDecrypter
  ): Format[A] =
    Format(
      f.preprocess(js => decryptTransform(associatedDataPath, encryptedFieldPaths)(js)),
      f.transform(encryptTransform(associatedDataPath, encryptedFieldPaths))
    )

  private def decryptTransform(associatedDataPath: JsPath, encryptedFieldPaths: Seq[JsPath])(jsValue: JsValue)(implicit crypto: AdEncrypter with AdDecrypter): JsValue = {
    lazy val ad = associatedData(associatedDataPath, jsValue)
    def transform(js: JsValue): JsValue =
      CryptoFormats.encryptedValueFormat.reads(js) match {
        case JsSuccess(ev, _) => Json.parse(crypto.decrypt(ev, ad))
        case JsError(errors)  => sys.error(s"Failed to decrypt value: $errors")
      }
    encryptedFieldPaths.foldLeft(jsValue){ (js, encryptedFieldPath) =>
      if (encryptedFieldPath(jsValue).nonEmpty)
        transformWithoutMerge(js, encryptedFieldPath, transform) match {
          case JsSuccess(r, _) => r
          case JsError(errors) => sys.error(s"Could not decrypt at $encryptedFieldPath: $errors")
        }
      else
        js
    }
  }

  private def encryptTransform(associatedDataPath: JsPath, encryptedFieldPaths: Seq[JsPath])(jsValue: JsValue)(implicit crypto: AdEncrypter with AdDecrypter): JsValue = {
    lazy val ad = associatedData(associatedDataPath, jsValue)
    def transform(js: JsValue): JsValue =
      CryptoFormats.encryptedValueFormat.writes(crypto.encrypt(js.toString, ad))
    encryptedFieldPaths.foldLeft(jsValue){ (js, encryptedFieldPath) =>
      if (encryptedFieldPath(jsValue).nonEmpty)
        transformWithoutMerge(js, encryptedFieldPath, transform) match {
          case JsSuccess(r, _) => r
          case JsError(errors) => sys.error(s"Could not encrypt at $encryptedFieldPath: $errors")
        }
      else
        js
    }
  }

  private def associatedData(associatedDataPath: JsPath, jsValue: JsValue) =
    associatedDataPath(jsValue) match {
      case List(associatedData) => associatedData.as[String] // Note only supports associatedDataPath which points to a String
      case Nil                  => sys.error(s"No associatedData was found with $associatedDataPath")
      case _                    => sys.error(s"Multiple associatedData was found with $associatedDataPath")
    }

  private def transformWithoutMerge(js: JsValue, path: JsPath, transformFn: JsValue => JsValue): JsResult[JsValue] =
    js.transform(
      path.json.update(implicitly[Reads[JsValue]].map(_ => transformFn(path(js).head)))
        // Calling js.transform(path.json.update ...) actually merges the result of transform
        // So we have to ensure the original (unencrypted) JsObject is removed
        .composeWith(path.json.update(implicitly[Reads[JsValue]].map(_ => JsNull)))
    )
}
