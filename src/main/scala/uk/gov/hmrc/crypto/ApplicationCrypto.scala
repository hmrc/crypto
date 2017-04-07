/*
 * Copyright 2017 HM Revenue & Customs
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

import javax.inject.Inject

import play.api.{Configuration, Play}

trait ApplicationCrypto {

  implicit val configuration: () => Configuration

  private def sessionCookieCrypto = CryptoGCMWithKeysFromConfig(baseConfigKey = "cookie.encryption")

  private def ssoPayloadCrypto = CryptoWithKeysFromConfig(baseConfigKey = "sso.encryption")

  private def queryParameterCrypto = CryptoWithKeysFromConfig(baseConfigKey = "queryParameter.encryption")

  private def jsonCrypto = CryptoWithKeysFromConfig(baseConfigKey = "json.encryption")

  lazy val SessionCookieCrypto = sessionCookieCrypto
  lazy val SsoPayloadCrypto = ssoPayloadCrypto
  lazy val QueryParameterCrypto = queryParameterCrypto
  lazy val JsonCrypto = jsonCrypto

  def verifyConfiguration() {
    sessionCookieCrypto
    queryParameterCrypto
    ssoPayloadCrypto
  }

  def verifyJsonConfiguration() {
    jsonCrypto
  }
}

object ApplicationCrypto extends ApplicationCrypto {
  implicit val configuration: () => Configuration = () => Play.current.configuration
}

class ApplicationCryptoDI @Inject()(config: Configuration) extends ApplicationCrypto {
  implicit val configuration: () => Configuration = () => config
}
