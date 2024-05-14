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

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar

import java.util.Base64
import scala.jdk.CollectionConverters._

@annotation.nowarn("msg=deprecated")
class CryptoWithKeysFromConfigSpec extends AnyWordSpecLike with Matchers with MockitoSugar {

  private val baseConfigKey = "crypto.spec"

  private object CurrentKey {
    val configKey            = baseConfigKey + ".key"
    val encryptionKey        = Base64.getEncoder.encodeToString(Array[Byte](0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15))
    val plainMessage         = PlainText("this is my message")
    val encryptedMessage     = Crypted("up/76On5j54pAjzqZR1mqM5E28skTl8Aw0GkKi+zjkk=")
    val plainByteMessage     = PlainBytes("this is a bunch of bytes".getBytes)
    val encryptedByteMessage = Crypted("z9MBLTvjyqRFi0UNZJ7qrIv3fvyuMGGjOU/npaJ7ucU=")
  }

  private object PreviousKey1 {
    val encryptionKey            = Base64.getEncoder.encodeToString(Array[Byte](1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
    val plainMessage             = PlainText("this is the first plain message")
    val plainByteMessage         = PlainBytes("this is the first bunch of bytes".getBytes)
    val plainByteMessageResponse = PlainText("this is the first bunch of bytes")
  }

  private object PreviousKey2 {
    val encryptionKey            = Base64.getEncoder.encodeToString(Array[Byte](2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2))
    val plainMessage             = PlainText("this is the second plain message")
    val plainByteMessage         = PlainBytes("this is the second bunch of bytes".getBytes)
    val plainByteMessageResponse = PlainText("this is the second bunch of bytes")
  }

  private object PreviousKeys {
    val configKey      = baseConfigKey + ".previousKeys"
    val encryptionKeys = Seq(PreviousKey1.encryptionKey, PreviousKey2.encryptionKey)
  }

  "Constructing a CompositeCryptoWithKeysFromConfig with a current key, but no previous keys configured" should {
    "return a properly initialised, functional CompositeSymmetricCrypto object that works only with the current key" in {
      val config = ConfigFactory.parseMap(
        Map(
          CurrentKey.configKey -> CurrentKey.encryptionKey
        ).asJava
      )

      val crypto = new CryptoWithKeysFromConfig(baseConfigKey, config)
      crypto.encrypt(CurrentKey.plainMessage)     shouldBe CurrentKey.encryptedMessage
      crypto.decrypt(CurrentKey.encryptedMessage) shouldBe CurrentKey.plainMessage

      crypto.encrypt(CurrentKey.plainByteMessage)                  shouldBe CurrentKey.encryptedByteMessage
      crypto.decryptAsBytes(CurrentKey.encryptedByteMessage).value shouldBe CurrentKey.plainByteMessage.value

      val previousKey1Crypto        = CompositeSymmetricCrypto.aes(PreviousKey1.encryptionKey, Seq.empty)
      val encryptedWithPreviousKey1 = crypto.encrypt(PreviousKey1.plainMessage, previousKey1Crypto)
      intercept[SecurityException] {
        crypto.decrypt(encryptedWithPreviousKey1)
      }
    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig with a current key and empty previous keys" should {
    "return a properly initialised, functional CompositeSymmetricCrypto object that works only with the current key" in {
      val config = ConfigFactory.parseMap(
        Map(
          CurrentKey.configKey   -> CurrentKey.encryptionKey,
          PreviousKeys.configKey -> List.empty.asJava
        ).asJava
      )

      val crypto = new CryptoWithKeysFromConfig(baseConfigKey, config)
      crypto.encrypt(CurrentKey.plainMessage)     shouldBe CurrentKey.encryptedMessage
      crypto.decrypt(CurrentKey.encryptedMessage) shouldBe CurrentKey.plainMessage

      crypto.encrypt(CurrentKey.plainByteMessage)                  shouldBe CurrentKey.encryptedByteMessage
      crypto.decryptAsBytes(CurrentKey.encryptedByteMessage).value shouldBe CurrentKey.plainByteMessage.value

      val previousKey1Crypto        = CompositeSymmetricCrypto.aes(PreviousKey1.encryptionKey, Seq.empty)
      val encryptedWithPreviousKey1 = crypto.encrypt(PreviousKey1.plainMessage, previousKey1Crypto)
      intercept[SecurityException] {
        crypto.decrypt(encryptedWithPreviousKey1)
      }
    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig with both current and previous keys" should {
    "allows decrypting payloads that were encrypted using previous keys" in {
      val config = ConfigFactory.parseMap(
        Map(
          CurrentKey.configKey   -> CurrentKey.encryptionKey,
          PreviousKeys.configKey -> PreviousKeys.encryptionKeys.asJava
        ).asJava
      )

      val crypto = new CryptoWithKeysFromConfig(baseConfigKey, config)

      val previousKey1Crypto             = CompositeSymmetricCrypto.aes(PreviousKey1.encryptionKey, Seq.empty)
      val encryptedWithPreviousKey1      = crypto.encrypt(PreviousKey1.plainMessage, previousKey1Crypto)
      val encryptedBytesWithPreviousKey1 = crypto.encrypt(PreviousKey1.plainByteMessage, previousKey1Crypto)
      crypto.decrypt(encryptedWithPreviousKey1)      shouldBe PreviousKey1.plainMessage
      crypto.decrypt(encryptedBytesWithPreviousKey1) shouldBe PreviousKey1.plainByteMessageResponse

      val previousKey2Crypto             = CompositeSymmetricCrypto.aes(PreviousKey2.encryptionKey, Seq.empty)
      val encryptedWithPreviousKey2      = crypto.encrypt(PreviousKey2.plainMessage, previousKey2Crypto)
      val encryptedBytesWithPreviousKey2 = crypto.encrypt(PreviousKey2.plainByteMessage, previousKey2Crypto)
      crypto.decrypt(encryptedWithPreviousKey2)      shouldBe PreviousKey2.plainMessage
      crypto.decrypt(encryptedBytesWithPreviousKey2) shouldBe PreviousKey2.plainByteMessageResponse
    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig without current or previous keys" should {
    "throw a SecurityException on construction" in {
      intercept[SecurityException] {
        new CryptoWithKeysFromConfig(baseConfigKey, ConfigFactory.empty)
      }
    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig without a current key, but with previous keys" should {
    "throw a SecurityException on construction" in {
      val config = ConfigFactory.parseMap(
        Map(
          PreviousKeys.configKey -> PreviousKeys.encryptionKeys.asJava
        ).asJava
      )

      intercept[SecurityException] {
        new CryptoWithKeysFromConfig(baseConfigKey, config)
      }
    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig with an invalid key" should {

    val keyWithInvalidNumberOfBits   = "ZGVmZ2hpamtsbW4K"
    val keyWithInvalidBase64Encoding = "defgh£jklmn"

    "throw a SecurityException if the current key is too short" in {
      val config = ConfigFactory.parseMap(
        Map(
          CurrentKey.configKey   -> keyWithInvalidNumberOfBits,
          PreviousKeys.configKey -> PreviousKeys.encryptionKeys.asJava
        ).asJava
      )

      intercept[SecurityException] {
        new CryptoWithKeysFromConfig(baseConfigKey, config)
      }
    }

    "throw a SecurityException if the current key cannot be base 64 decoded" in {
      val config = ConfigFactory.parseMap(
        Map(
          CurrentKey.configKey   -> keyWithInvalidBase64Encoding,
          PreviousKeys.configKey -> PreviousKeys.encryptionKeys.asJava
        ).asJava
      )

      intercept[SecurityException] {
        new CryptoWithKeysFromConfig(baseConfigKey, config)
      }
    }

    "throw a SecurityException if the first previous key is too short" in {
      val config = ConfigFactory.parseMap(
        Map(
          CurrentKey.configKey -> CurrentKey.encryptionKey,
          PreviousKeys.configKey -> Seq(
            keyWithInvalidNumberOfBits,
            PreviousKey1.encryptionKey,
            PreviousKey2.encryptionKey
          ).asJava
        ).asJava
      )

      intercept[SecurityException] {
        new CryptoWithKeysFromConfig(baseConfigKey, config)
      }
    }

    "throw a SecurityException if the first previous key cannot be base 64 decoded" in {
      val config = ConfigFactory.parseMap(
        Map(
          CurrentKey.configKey -> CurrentKey.encryptionKey,
          PreviousKeys.configKey -> Seq(
            keyWithInvalidBase64Encoding,
            PreviousKey1.encryptionKey,
            PreviousKey2.encryptionKey
          ).asJava
        ).asJava
      )

      intercept[SecurityException] {
        new CryptoWithKeysFromConfig(baseConfigKey, config)
      }
    }

    "throw a SecurityException if the other previous key is too short" in {
      val config = ConfigFactory.parseMap(
        Map(
          CurrentKey.configKey -> CurrentKey.encryptionKey,
          PreviousKeys.configKey -> Seq(
            PreviousKey1.encryptionKey,
            keyWithInvalidNumberOfBits,
            PreviousKey2.encryptionKey
          ).asJava
        ).asJava
      )

      intercept[SecurityException] {
        new CryptoWithKeysFromConfig(baseConfigKey, config)
      }
    }

    "throw a SecurityException if the other previous key cannot be base 64 decoded" in {
      val config = ConfigFactory.parseMap(
        Map(
          CurrentKey.configKey -> CurrentKey.encryptionKey,
          PreviousKeys.configKey -> Seq(
            PreviousKey1.encryptionKey,
            keyWithInvalidBase64Encoding,
            PreviousKey2.encryptionKey
          ).asJava
        ).asJava
      )

      intercept[SecurityException] {
        new CryptoWithKeysFromConfig(baseConfigKey, config)
      }
    }
  }
}
