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

package uk.gov.hmrc.crypto

import com.typesafe.config.Config

import javax.inject.Inject

@deprecated("Use uk.gov.hmrc.play.bootstrap.frontend.crypto.ApplicationCrypto provided by bootstrap-frontend-play instead.", "8.3.0")
class ApplicationCrypto @Inject()(config: Config) {

  /** Should only be used to encrypt/decrypt the cookie.
    *
    * It is shared by all services.
    *
    * This is a platform key, and should not be used for any other use-case since it may be rotated at any time.
    */
  lazy val SessionCookieCrypto =
    SymmetricCryptoFactory.aesGcmCryptoFromConfig(baseConfigKey = "cookie.encryption", config)

  /** Should only be used for SSO with the Portal.
    *
    * It is shared by all services.
    *
    * This is a platform key, and should not be used for any other use-case since it may be rotated at any time.
    */
  lazy val SsoPayloadCrypto =
    SymmetricCryptoFactory.aesCryptoFromConfig(baseConfigKey = "sso.encryption", config)

  /** Can be used to encrypt query parameters - e.g. for callbacks and redirects.
    *
    * By default it is shared by all services, but it can be overridden if required to be private to the service.
    *
    * Given by default it is provided by the platform, it should be assumed it may be rotated at any time, and not
    * used for storing data.
    */
  lazy val QueryParameterCrypto =
    SymmetricCryptoFactory.aesCryptoFromConfig(baseConfigKey = "queryParameter.encryption", config)

  @deprecated(
    "This will be removed since it's intention was ambiguous. Create and manage your own crypto instead.\n" +
    "For encrypting mongo data, create your own crypto with the key `mongodb.encryption`."
  , "8.3.0"
  )
  lazy val JsonCrypto =
    SymmetricCryptoFactory.aesCryptoFromConfig(baseConfigKey = "json.encryption", config)

  def verifyConfiguration(): Unit = {
    SessionCookieCrypto
    QueryParameterCrypto
    SsoPayloadCrypto
  }

  @deprecated("For encrypting mongo data, use `MongoCrypto`. For encrypting http payloads, use `QueryParameterCrypto`", "8.3.0")
  def verifyJsonConfiguration(): Unit =
    JsonCrypto
}
