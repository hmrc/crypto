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

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.jdk.CollectionConverters._

class ApplicationCryptoSpec extends AnyWordSpecLike with Matchers {

  "ApplicationCrypto" should {
    "be correctly instantiated without application running" in {
      val config = ConfigFactory.parseMap(
        Map(
          "cookie.encryption.key"         -> "gvBoGdgzqG1AarzF1LY0zQ==",
          "sso.encryption.key"            -> "gvBoGdgzqG1AarzF1LY0zQ==",
          "queryParameter.encryption.key" -> "gvBoGdgzqG1AarzF1LY0zQ==",
          "json.encryption.key"           -> "gvBoGdgzqG1AarzF1LY0zQ=="
        ).asJava
      )

      val applicationCrypto: ApplicationCrypto = new ApplicationCrypto(config)

      applicationCrypto.verifyConfiguration()
      applicationCrypto.verifyJsonConfiguration()
    }
  }
}
