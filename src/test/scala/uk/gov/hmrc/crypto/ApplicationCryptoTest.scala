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

import org.mockito.Mockito._
import org.scalatest.WordSpec
import org.scalatest.mock.MockitoSugar
import play.api.Configuration

class ApplicationCryptoSpec extends WordSpec {
  "ApplicationCryptoDI" should {
    "be correctly instantiated without application running" in new TestCase {
      val applicationCrypto: ApplicationCrypto = new ApplicationCryptoDI(configurationMock)

      applicationCrypto.verifyConfiguration()
      applicationCrypto.verifyJsonConfiguration()
    }
  }
}

trait TestCase extends MockitoSugar {
  val configurationMock: Configuration = {
    val configuration = mock[Configuration]
    when(configuration.getString("cookie.encryption.key")).thenReturn(Some("gvBoGdgzqG1AarzF1LY0zQ=="))
    when(configuration.getString("sso.encryption.key")).thenReturn(Some("gvBoGdgzqG1AarzF1LY0zQ=="))
    when(configuration.getString("queryParameter.encryption.key")).thenReturn(Some("gvBoGdgzqG1AarzF1LY0zQ=="))
    when(configuration.getString("json.encryption.key")).thenReturn(Some("gvBoGdgzqG1AarzF1LY0zQ=="))
    when(configuration.getStringSeq("cookie.encryption.previousKeys")).thenReturn(None)
    when(configuration.getStringSeq("sso.encryption.previousKeys")).thenReturn(None)
    when(configuration.getStringSeq("queryParameter.encryption.previousKeys")).thenReturn(None)
    when(configuration.getStringSeq("json.encryption.previousKeys")).thenReturn(None)
    configuration
  }
}
