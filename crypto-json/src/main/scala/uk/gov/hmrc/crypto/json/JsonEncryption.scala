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

package uk.gov.hmrc.crypto.json

import play.api.libs.json._
import uk.gov.hmrc.crypto.{Crypted, Crypto, Decrypter, Encrypter, PlainText, Protected, Sensitive}

object JsonEncryption {
  def stringEncrypter(implicit crypto: Encrypter): Writes[String] =
    implicitly[Writes[String]]
      .contramap[String](s => crypto.encrypt(PlainText(s)).value)

  def stringDecrypter(implicit crypto: Decrypter): Reads[String] =
    implicitly[Reads[String]]
      .map[String](s => crypto.decrypt(Crypted(s)).value)

  def stringEncrypterDecrypter(implicit crypto: Crypto): Format[String] =
    Format(stringDecrypter, stringEncrypter)

  def protectedEncrypter[T](implicit crypto: Encrypter, wrts: Writes[T]): Writes[Protected[T]] =
    stringEncrypter.contramap(o => wrts.writes(o.decryptedValue).toString)

  def protectedDecrypter[T](implicit crypto: Decrypter, rds: Reads[T]): Reads[Protected[T]] =
    stringDecrypter.map(s => Protected(Json.parse(s).as[T]))

  def protectedEncrypterDecrypter[T](implicit crypto: Crypto, fmt: Format[T]): Format[Protected[T]] =
    Format(protectedDecrypter, protectedEncrypter)

  def sensitiveEncrypter[A : Writes, B <: Sensitive[A]](implicit crypto: Encrypter): Writes[B] =
    stringEncrypter.contramap(o => implicitly[Writes[A]].writes(o.decryptedValue).toString)

  def sensitiveDecrypter[A : Reads, B <: Sensitive[A]](toSensitive: A => B)(implicit crypto: Decrypter): Reads[B] =
    stringDecrypter.map(s => toSensitive(Json.parse(s).as[A]))

  def sensitiveEncrypterDecrypter[A : Format, B <: Sensitive[A]](toSensitive: A => B)(implicit crypto: Encrypter with Decrypter): Format[B] =
    Format(sensitiveDecrypter(toSensitive), sensitiveEncrypter)
}
