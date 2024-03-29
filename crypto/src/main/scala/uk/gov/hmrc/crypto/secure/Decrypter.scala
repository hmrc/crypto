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

import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.Base64
import javax.crypto.Cipher

trait Decrypter {

  protected val key: Key

  validateKey()

  protected lazy val algorithm = key.getAlgorithm

  protected def validateKey(): Unit =
    if (key == null) throw new IllegalStateException("There is no Key defined for this Decrypter")

  def decrypt(data: String): String =
    new String(decryptAsBytes(data))

  def decryptAsBytes(data: String): Array[Byte] =
    try {
      val cipher: Cipher = Cipher.getInstance(algorithm)
      cipher.init(Cipher.DECRYPT_MODE, key, cipher.getParameters)
      cipher.doFinal(Base64.getDecoder.decode(data.getBytes(StandardCharsets.UTF_8)))
    } catch {
      case e: Exception => throw new SecurityException("Failed decrypting data", e)
    }
}
