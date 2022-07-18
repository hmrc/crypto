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

package uk.gov.hmrc.crypto.secure

import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.GCMBlockCipher
import org.bouncycastle.crypto.params.AEADParameters

// Notes...
// http://unafbapune.blogspot.co.uk/2012/06/aesgcm-with-associated-data.html
// https://tools.ietf.org/html/rfc5288#page-2
// https://tools.ietf.org/html/rfc5116
// http://crypto.stackexchange.com/questions/6711/how-to-use-gcm-mode-and-associated-data-properly

object GCM {

  def encrypt(plaintext: Array[Byte], params: AEADParameters, outputOffset: Int): Array[Byte] = {
    val gcm= new GCMBlockCipher(new AESEngine)
    gcm.init(true, params)
    val outsize = gcm.getOutputSize(plaintext.length)
    val out = new Array[Byte](outsize + outputOffset)
    val offOut = gcm.processBytes(plaintext, 0, plaintext.length, out, outputOffset)
    gcm.doFinal(out, offOut + outputOffset)
    out
  }

  def decrypt(ciphertext: Array[Byte], params: AEADParameters): Array[Byte] = {
    val gcm = new GCMBlockCipher(new AESEngine)
    gcm.init(false, params)
    val outsize = gcm.getOutputSize(ciphertext.length)
    val out = new Array[Byte](outsize)
    val offOut = gcm.processBytes(ciphertext, 0, ciphertext.length, out, 0)
    gcm.doFinal(out, offOut)
    out
  }

}
