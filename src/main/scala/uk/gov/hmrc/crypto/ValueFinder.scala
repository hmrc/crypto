/*
 * Copyright 2021 HM Revenue & Customs
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

import com.typesafe.config.Config
import collection.JavaConverters._

import scala.util.Try

private[crypto] trait ValueFinder[T] {
  def apply(config: Config, key: String): Try[T]
}

private[crypto] object ValueFinder {
  implicit object StringValueFinder extends ValueFinder[String] {
    def apply(config: Config, key: String): Try[String] =
      Try(config.getString(key))
  }

  implicit object StringListValueFinder extends ValueFinder[List[String]] {
    def apply(config: Config, key: String): Try[List[String]] =
      Try(config.getStringList(key).asScala.toList)
  }
}
