/*
 * Copyright 2015 HM Revenue & Customs
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

import org.scalatest.FunSuite

class HashGeneratorTest extends FunSuite {

  test("For a variety of strings, slowEquals should return true when both parameters are the same") {
    import HashGenerator._
    assert(slowEquals(Scrambled(""), Scrambled("")) === true)
    assert(slowEquals(Scrambled("a"), Scrambled("a")) === true)
    assert(slowEquals(Scrambled("A Much Larger String Containing µø¢ etc"), Scrambled("A Much Larger String Containing µø¢ etc")) === true)
  }

  test("For some similar but different strings, slowEquals should return false") {
    import HashGenerator._
    assert(slowEquals(Scrambled(""), Scrambled("1")) === false)
    assert(slowEquals(Scrambled("a"), Scrambled("b")) === false)
    assert(slowEquals(Scrambled("A Much Larger String Containing ¢µø etc"), Scrambled("A Much Larger String Containing µø¢ etc")) === false)
  }

  test("should create random salts with a different output each time it is called") {
    val gen = new HashGenerator(10, 24, 5)
    val s1 = gen.newRandomSalt
    val s2 = gen.newRandomSalt
    val s3 = gen.newRandomSalt
    assert(s1 !== "")
    assert(s1 !== s2)
    assert(s3 !== s2)
    assert(s3 !== s1)
    assert(s1.value.length === 16)
  }

  test("should create random-salted hash with length determined by the constructor parameter") {
    val gen1 = new HashGenerator(12, 1, 1)
    val s1 = gen1.newRandomSalt
    assert(s1.value.length === 16)

    val gen2 = new HashGenerator(24, 1, 1)
    val s2 = gen2.newRandomSalt
    assert(s2.value.length === 32)
  }

  test("should create consistent hashes using a given salt") {
    val gen = new HashGenerator(12, 1, 1)
    val s1 = gen.hashWithSalt(Salt("salt"), PlainText("foo"))
    val s2 = gen.hashWithSalt(Salt("salt"), PlainText("foo"))
    val s3 = gen.withSalt(Salt("salt")).hash(PlainText("foo"))
    assert(s1 !== "foo")
    assert(s1 === s2)
    assert(s1 === s3)
  }

  test("should create specific-salted hash with length determined by the constructor parameter") {
    val gen1 = new HashGenerator(10, 12, 5)
    val s1a = gen1.hashWithSalt(Salt("salt"), PlainText("foo"))
    val s1b = gen1.withSalt(Salt("salt")).hash(PlainText("foo"))
    assert(s1a.value.length === 16)
    assert(s1a !== "foo")

    val gen2 = new HashGenerator(10, 24, 5)
    val s2 = gen2.hashWithSalt(Salt("salt"), PlainText("foo"))
    assert(s2.value.length === 32)
    assert(s2 !== "foo")
  }

  test("should deal with very small parameters ok") {
    val gen1 = new HashGenerator(1, 1, 1)
    val s1 = gen1.newRandomSalt
    assert(s1.value.length === 4)
    assert(s1 !== "foo")

    val gen2 = new HashGenerator(1, 1, 1)
    val s2 = gen2.hashWithSalt(Salt("salt"), PlainText("foo"))
    assert(s2.value.length === 4)
    assert(s2 !== "foo")
  }
}
