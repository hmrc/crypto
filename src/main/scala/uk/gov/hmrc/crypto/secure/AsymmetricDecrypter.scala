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

import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.{PKCS8EncodedKeySpec, RSAPublicKeySpec}
import java.security.{KeyFactory, PrivateKey, PublicKey}

import org.apache.commons.codec.binary.Base64

class AsymmetricDecrypter(override protected val key: PrivateKey) extends Decrypter {

  def getPublicKeySpec: RSAPublicKeySpec = {
    validateKey()
    val rsaCrtKey = key.asInstanceOf[RSAPrivateCrtKey]
    new RSAPublicKeySpec(rsaCrtKey.getModulus, rsaCrtKey.getPublicExponent)
  }

  def getPublicKey(algorithm: String): PublicKey = {
    val keyFactory = KeyFactory.getInstance(algorithm)
    keyFactory.generatePublic(getPublicKeySpec)
  }

}

object AsymmetricDecrypter {

  def buildPrivateKey(base64Content: String, algorithm: String): PrivateKey = {
    val keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(base64Content))
    val keyFactory = KeyFactory.getInstance(algorithm)
    keyFactory.generatePrivate(keySpec)
  }

}
