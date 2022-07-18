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

import java.security._
import java.security.cert.{Certificate, CertificateException}

trait CertificateVerifier {

  def verify(certificate: Certificate): Boolean = {
    try {
      if (isSelfSigned(certificate)) {
        throw new SecurityException("Certificate is self signed")
      }
      true
    } catch {
      case ce: CertificateException => throw new SecurityException("Certificate is invalid", ce)
      case nsae: NoSuchAlgorithmException => throw new SecurityException("Unsupported algorithm", nsae)
      case nspe: NoSuchProviderException => throw new SecurityException("Unsupported provider", nspe)
    }
  }

  private def isSelfSigned(certificate: Certificate): Boolean = {
    val publicKey = certificate.getPublicKey
    try {
      certificate.verify(publicKey)
      true
    } catch {
      case e: InvalidKeyException => false
      case e: SignatureException => false
    }
  }

}
