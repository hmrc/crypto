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

import java.nio.charset.StandardCharsets
import java.security.{GeneralSecurityException, SecureRandom}
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Provides a simple API for PBKDF2 password hashing. These algorithms are deliberately slow,
 * providing protection against brute force attacks.
 *
 * Larger values for pbkdf2Iterations will cost more CPU to evaluate; this is deliberate.
 */
class HashGenerator(saltByteSize: Int, hashByteSize: Int, pbkdf2Iterations: Int) {

  // The following constructs an instance with default parameters.
  def this() = this(24, 24, 100)

  require(saltByteSize > 0)
  require(hashByteSize > 0)
  require(pbkdf2Iterations > 0)

  val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1"

  // "PDBKDF2WithHmacSHA512" has a longer SHA hash, but is not necessarily better than just having more iterations
  // val PBKDF2_ALGORITHM: String = "PDBKDF2WithHmacSHA512"

  /**
   * Creates a random salt. Repeated calls to this method will yield different results. The result is a
   * base64-encoded string.
   */
  @throws(classOf[GeneralSecurityException])
  def newRandomSalt: Salt = {
    val random = new SecureRandom
    val bytes = new Array[Byte](saltByteSize)
    random.nextBytes(bytes)
    Salt(base64String(bytes))
  }

  /**
   * Creates a hash using a specified salt. For a given source string, subsequent calls to this method will
   * yield the same result provided the same salt is used. The result is a base64-encoded string.
   */
  @throws(classOf[GeneralSecurityException])
  def hashWithSalt(salt: Salt, password: PlainText): Scrambled = {
    val bytes = pbkdf2(password.value.toCharArray, Base64.getDecoder.decode(salt.utf8Bytes))
    Scrambled(base64String(bytes))
  }

  /** Gets a Hasher for the current HashGenerator. */
  def withSalt(salt: Salt): Hasher = {
    new Hasher {
      /** Creates a hash. The result is a base64-encoded string. */
      @throws(classOf[GeneralSecurityException])
      override def hash(plain: PlainText): Scrambled = hashWithSalt(salt, plain)
    }
  }

  @throws(classOf[GeneralSecurityException])
  private def pbkdf2(password: Array[Char], salt: Array[Byte]): Array[Byte] = {
    val spec = new PBEKeySpec(password, salt, pbkdf2Iterations, hashByteSize * 8)
    val skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
    skf.generateSecret(spec).getEncoded
  }

  private def base64String(bytes: Array[Byte]) = utf8String(Base64.getEncoder.encode(bytes))

  private def utf8String(bytes: Array[Byte]) = new String(bytes, StandardCharsets.UTF_8)
}


object HashGenerator {
  /**
   * Compares two hashed strings in length-constant time. This comparison method
   * is used so that password hashes cannot be extracted from an on-line
   * system using a timing attack and then attacked off-line.
   */
  def slowEquals(a: Scrambled, b: Scrambled): Boolean = {
    slowEquals(a.utf8Bytes, b.utf8Bytes)
  }

  /**
   * Compares two byte arrays in length-constant time. This comparison method
   * is used so that password hashes cannot be extracted from an on-line
   * system using a timing attack and then attacked off-line.
   */
  def slowEquals(a: Array[Byte], b: Array[Byte]): Boolean = {
    var diff = a.length ^ b.length
    var i = 0
    while (i < a.length && i < b.length) {
      diff |= a(i) ^ b(i)
      i += 1
    }

    diff == 0
  }

  /**
   * Allows easy generation of hashes.
   */
  def main(args: Array[String]) {
    if (args.length < 2) {
      println("Usage: HashGenerator salt password [salt-bytes] [hash-bytes] [iterations]")
      System.exit(0)
    }

    val saltBytes = if (args.length > 2) args(2).toInt else 24
    val hashBytes = if (args.length > 3) args(3).toInt else 24
    val iterations = if (args.length > 4) args(4).toInt else 100

    val gen = new HashGenerator(saltBytes, hashBytes, iterations)
    val salt = Salt(args(0))
    val password = PlainText(args(1))
    println(gen.withSalt(salt).hash(password))
  }
}
