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

package uk.gov.hmrc.secure

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import java.nio.charset.StandardCharsets
import java.security.cert.{Certificate, CertificateFactory}
import java.text.MessageFormat

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class CertificateVerifierSpec extends AnyWordSpecLike with Matchers {

  "the verifier" should {

    "throw exception if certificate is self signed" in {
      val certificate = getCertificate("/keys/server.crt")
      val verifier = new CertificateVerifier {}

      the[SecurityException] thrownBy {
        verifier.verify(certificate)
      } should have message "Certificate is self signed"

    }

  }

  private def getCertificate(path: String): Certificate = {
    val is = this.getClass.getResourceAsStream(path)
    try {
      val bytes = readStream(is)
      val partialCert = new String(bytes, StandardCharsets.UTF_8)
      val certificateFactory = CertificateFactory.getInstance("X.509")
      val fullCert = if (partialCert.contains("-----BEGIN CERTIFICATE-----")) partialCert else MessageFormat.format("-----BEGIN CERTIFICATE-----\n{0}\n-----END CERTIFICATE-----", partialCert.trim)
      val byteArrayInputStream = new ByteArrayInputStream(fullCert.getBytes(StandardCharsets.UTF_8))
      certificateFactory.generateCertificate(byteArrayInputStream)
    } finally {
      is.close()
    }
  }

  private def readStream(is: InputStream): Array[Byte] = {
    val buffer = new Array[Byte](1024)
    val ous = new ByteArrayOutputStream
    var read = 0
    while ( {
      read = is.read(buffer)
      read
    } != -1) {
      ous.write(buffer, 0, read)
    }
    is.close()
    ous.toByteArray
  }
}
