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

import java.util.Base64
import java.time.Instant

trait Encrypter {
  def encrypt(plain: PlainContent): Crypted
}

trait Decrypter {
  def decrypt(reversiblyEncrypted: Crypted): PlainText

  def decryptAsBytes(reversiblyEncrypted: Crypted): PlainBytes
}

trait AdEncrypter {
  def encrypt(valueToEncrypt: String, associatedText: String): EncryptedValue
}

trait AdDecrypter {
  def decrypt(valueToDecrypt: EncryptedValue, associatedText: String): String
}


trait Hasher {
  def hash(plain: PlainText): Scrambled
}

trait Verifier {
  def verify(sample: PlainText, ncrypted: Scrambled): Boolean
}

sealed trait PlainContent

case class PlainText(value: String) extends PlainContent

case class PlainBytes(value: Array[Byte]) extends PlainContent

case class Crypted(value: String) {
  def toBase64 =
    Base64.getEncoder.encode(value.getBytes("UTF-8"))
}

object Crypted extends (String => Crypted) {
  def fromBase64(s: String) =
    Crypted(new String(Base64.getDecoder.decode(s.getBytes("UTF-8"))))
}

case class Scrambled(value: String) {
  def toBase64 =
    Base64.getEncoder.encode(value.getBytes("UTF-8"))
}

case class EncryptedValue(
  value: String,
  nonce: String
)

/** `Sensitive` identifies types which require encryption (See crypto-json).
  * It is modelled as a trait with concrete implementations rather than a parameterised case class
  * since this avoids type erasure. This helps with for example, the native mongo driver,
  * where codecs are looked up by runtime class.
  * More instances can be created by clients as required.
  */
trait Sensitive[A] {
  def decryptedValue: A
  override def toString = "Sensitive(...)"
}

object Sensitive {
  case class SensitiveString    (override val decryptedValue: String    ) extends Sensitive[String]
  case class SensitiveBoolean   (override val decryptedValue: Boolean   ) extends Sensitive[Boolean]
  case class SensitiveLong      (override val decryptedValue: Long      ) extends Sensitive[Long]
  case class SensitiveDouble    (override val decryptedValue: Double    ) extends Sensitive[Double]
  case class SensitiveBigDecimal(override val decryptedValue: BigDecimal) extends Sensitive[BigDecimal]
  case class SensitiveInstant   (override val decryptedValue: Instant   ) extends Sensitive[Instant]
}
