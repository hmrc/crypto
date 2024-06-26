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
import uk.gov.hmrc.crypto.EncryptedValue

/** Standard formats - assumes that encryption has already taken place */
object CryptoFormats {
  val encryptedValueFormat: Format[EncryptedValue] =
    ( (__ \ "value").format[String]
    ~ (__ \ "nonce").format[String]
    )(EncryptedValue.apply, ev => (ev.value, ev.nonce))
}
