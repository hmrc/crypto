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

import uk.gov.hmrc.secure.AsymmetricDecrypter
import uk.gov.hmrc.secure.AsymmetricEncrypter

import java.security.KeyFactory
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.RSAPublicKeySpec

// NOTE: This is an implementation for RSA encryption where the PublicKey is derived from the PrivateKey. Both the exponent and the modulus can be
// derived from the PrivateKey.

class RSAEncryptDecrypt(contents: String) {
  private val RSA = "RSA"

  private val privateKey = AsymmetricDecrypter.buildPrivateKey(contents, RSA)
  private val decrypter = new AsymmetricDecrypter(privateKey)
  private val encrypter = new AsymmetricEncrypter(decrypter.getPublicKey(RSA))

  def getAsymmetricDecrypter() = decrypter

  def getAsymmetricEncrypter() = encrypter

  def getPublicKey(alrorithm: String) = {
    val keyFactory = KeyFactory.getInstance(alrorithm)
    keyFactory.generatePublic(getPublicKeySpec())
  }

  def getPublicKeySpec() = {
    val rsaCrtKey = privateKey.asInstanceOf[RSAPrivateCrtKey]
    new RSAPublicKeySpec(rsaCrtKey.getModulus(), rsaCrtKey.getPublicExponent())
  }

}
