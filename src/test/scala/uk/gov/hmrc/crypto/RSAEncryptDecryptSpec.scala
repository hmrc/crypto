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

import java.math.BigInteger

import org.scalatest.{Matchers, WordSpecLike}

class RSAEncryptDecryptSpec extends WordSpecLike with Matchers {

  "The RSA encrypter/decrypter " should {

    "successfully encrypt and decrypt using the RSA Encrypt Decrypt wrapper" in  {

      val original: String = "osidg9n  rg jt wtjwitu4549gv  p9 u243t  93t2ut34ijt g"

      // Please note: base64 private key
      val privateKeyContent = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMb5BTonZjvTbnz0iYodyhgRDG+jLEdlSlCl1sjOs542i7GS17uHt7jtx5vsn3ukvJZ5HpdYcXiK+ZAdFoyGp17Ht7dQid0pxPTOvSBa3VZzTpVftjmk5Bb6AOhcHvocmeC5ddrLfxxT4BOLR1LKWjHVa+rmv63yhOZrwigCb1k5AgMBAAECgYAJI3LMAvc9FC7k6k5O26Nmi5HoprPn640IOzWsL2IsiBDObRIfeNJFWhZq4OlKQnyu5I01IkjD4o4nwk5A0gZpzS721VmeY1s1JAYhGrBRrM0y6f8nbvvq2daGk9FvYPCFd3MH8s4LDdmQp38qbWIPeV/pMb7nEOuujr2ewTgTSQJBAPZASZg4WB/Cdj/gk06LQewTjIZrxhRwMXKj/j1Cxsp4dH5OGiXnuh9PMjy8bgq2cmpCl4NF4kpDOUj4pTe5WBMCQQDO2ZMdfUHv2Um48C/DkSf+kPWGndlFqgCYbQnmYtFPwfZawGIV25sHGL8yUkG/Fn36cAnvShk83CvS/suF64sDAkANcbES6HCXO0ytbBtevGea9e5EIHot/3oTojRtL8oen7jsdGMlEqOstewuOLNhTlisyHnxJs2V9FmaTEjo4+uVAkEAnbUXTGGG10a0xbMix5mxg46kE/nfgRTHlW91H9lxuryohrKtVfr0bOm6RLSgef+9Fyc6+91j9pnrM9id9cILXwJBAKOp5pg3NPuBFJoxpEhz+eBLObdsmAme3CIQGsF+eh4X4EVhXvRnJkvY0lNlTykm8Tx3xTlT+KB4aQ4PP6SwO4k="
      val modulus  = new BigInteger("139723406855580871468776363003208364548340881244120928091404534332846447142633429051984569949685736783867780462911374761792533690929543753007625644532138840405632670634557340863509819839472712457322581565998929162905701800090642472512482251261222219030700352400431679781511204075449367615685738863644074006841")
      val exponent = new BigInteger("65537")
      val rsa = new RSAEncryptDecrypt(privateKeyContent)

      val encrypted = rsa.getAsymmetricEncrypter().encrypt(original)
      val decrypted = rsa.getAsymmetricDecrypter().decrypt(encrypted)

      decrypted shouldBe original
      rsa.getPublicKeySpec().getModulus shouldBe modulus
      rsa.getPublicKeySpec().getPublicExponent shouldBe exponent
    }
  }

}
