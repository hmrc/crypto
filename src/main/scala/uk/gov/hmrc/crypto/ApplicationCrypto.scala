/*
 * Copyright 2018 HM Revenue & Customs
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

import com.typesafe.config.Config
import javax.inject.Inject

class ApplicationCrypto @Inject()(config: Config) {

  lazy val SessionCookieCrypto  = new CryptoGCMWithKeysFromConfig(baseConfigKey = "cookie.encryption", config)
  lazy val SsoPayloadCrypto     = new CryptoWithKeysFromConfig(baseConfigKey    = "sso.encryption", config)
  lazy val QueryParameterCrypto = new CryptoWithKeysFromConfig(baseConfigKey    = "queryParameter.encryption", config)
  lazy val JsonCrypto           = new CryptoWithKeysFromConfig(baseConfigKey    = "json.encryption", config)

  def verifyConfiguration(): Unit = {
    SessionCookieCrypto
    QueryParameterCrypto
    SsoPayloadCrypto
  }

  def verifyJsonConfiguration(): Unit =
    JsonCrypto

}
