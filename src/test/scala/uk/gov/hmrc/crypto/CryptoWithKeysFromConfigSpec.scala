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

import org.apache.commons.codec.binary.Base64
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.Configuration
import play.api.test.FakeApplication
import play.api.test.Helpers._

class CryptoWithKeysFromConfigSpec extends WordSpecLike with Matchers with OptionValues with MockitoSugar {

  private val baseConfigKey = "crypto.spec"

  private object CurrentKey {
    val configKey            = baseConfigKey + ".key"
    val encryptionKey        = Base64.encodeBase64String(Array[Byte](0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15))
    val plainMessage         = PlainText("this is my message")
    val encryptedMessage     = Crypted("up/76On5j54pAjzqZR1mqM5E28skTl8Aw0GkKi+zjkk=")
    val plainByteMessage     = PlainBytes("this is a bunch of bytes".getBytes)
    val encryptedByteMessage = Crypted("z9MBLTvjyqRFi0UNZJ7qrIv3fvyuMGGjOU/npaJ7ucU=")
  }

  private object PreviousKey1 {
    val encryptionKey            = Base64.encodeBase64String(Array[Byte](1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
    val plainMessage             = PlainText("this is the first plain message")
    val plainByteMessage         = PlainBytes("this is the first bunch of bytes".getBytes)
    val plainByteMessageResponse = PlainText("this is the first bunch of bytes")
  }

  private object PreviousKey2 {
    val encryptionKey            = Base64.encodeBase64String(Array[Byte](2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2))
    val plainMessage             = PlainText("this is the second plain message")
    val plainByteMessage         = PlainBytes("this is the second bunch of bytes".getBytes)
    val plainByteMessageResponse = PlainText("this is the second bunch of bytes")
  }

  private object PreviousKeys {
    val configKey      = baseConfigKey + ".previousKeys"
    val encryptionKeys = Seq(PreviousKey1.encryptionKey, PreviousKey2.encryptionKey)
  }

  "Constructing a CompositeCryptoWithKeysFromConfig with a current key, but no previous keys configured" should {

    val fakeApplicationWithCurrentKeyOnly = FakeApplication(
      additionalConfiguration = Map(
        CurrentKey.configKey -> CurrentKey.encryptionKey
      ))

    "return a properly initialised, functional CompositeSymmetricCrypto object that works only with the current key" in running(
      fakeApplicationWithCurrentKeyOnly) {
      val crypto = CryptoWithKeysFromConfig(baseConfigKey)
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

    val fakeApplicationWithEmptyPreviousKeys = FakeApplication(
      additionalConfiguration =
        Map(CurrentKey.configKey -> CurrentKey.encryptionKey, PreviousKeys.configKey -> List.empty))

    "return a properly initialised, functional CompositeSymmetricCrypto object that works only with the current key" in running(
      fakeApplicationWithEmptyPreviousKeys) {
      val crypto = CryptoWithKeysFromConfig(baseConfigKey)
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

    val fakeApplicationWithCurrentAndPreviousKeys = FakeApplication(
      additionalConfiguration =
        Map(CurrentKey.configKey -> CurrentKey.encryptionKey, PreviousKeys.configKey -> PreviousKeys.encryptionKeys))

    "allows decrypting payloads that were encrypted using previous keys" in running(
      fakeApplicationWithCurrentAndPreviousKeys) {
      val crypto = CryptoWithKeysFromConfig(baseConfigKey)

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

  "Constructing a CompositeCryptoWithKeysFromConfig with both current and previous keys using new Play 2.5 DI" should {

    "allows decrypting payloads that were encrypted using previous keys" in {
      val configuration = mock[Configuration]
      when(configuration.getString(CurrentKey.configKey)).thenReturn(Some(CurrentKey.encryptionKey))
      when(configuration.getStringSeq(PreviousKeys.configKey)).thenReturn(Some(PreviousKeys.encryptionKeys))
      val crypto = CryptoWithKeysFromConfig(baseConfigKey, configuration)

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

    val fakeApplicationWithoutAnyKeys = FakeApplication()

    "throw a SecurityException on construction" in running(fakeApplicationWithoutAnyKeys) {
      intercept[SecurityException] {
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig without a current key, but with previous keys" should {

    val fakeApplicationWithPreviousKeysOnly = FakeApplication(
      additionalConfiguration = Map(
        PreviousKeys.configKey -> PreviousKeys.encryptionKeys
      ))

    "throw a SecurityException on construction" in running(fakeApplicationWithPreviousKeysOnly) {
      intercept[SecurityException] {
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig with an invalid key" should {

    val keyWithInvalidNumberOfBits   = "ZGVmZ2hpamtsbW4K"
    val keyWithInvalidBase64Encoding = "defgh£jklmn"

    val fakeApplicationWithShortCurrentKey = FakeApplication(
      additionalConfiguration = Map(
        CurrentKey.configKey   -> keyWithInvalidNumberOfBits,
        PreviousKeys.configKey -> PreviousKeys.encryptionKeys
      ))

    val fakeApplicationWithInvalidBase64CurrentKey = FakeApplication(
      additionalConfiguration = Map(
        CurrentKey.configKey   -> keyWithInvalidBase64Encoding,
        PreviousKeys.configKey -> PreviousKeys.encryptionKeys
      ))

    val fakeApplicationWithShortFirstPreviousKey = FakeApplication(
      additionalConfiguration = Map(
        CurrentKey.configKey -> CurrentKey.encryptionKey,
        PreviousKeys.configKey -> Seq(
          keyWithInvalidNumberOfBits,
          PreviousKey1.encryptionKey,
          PreviousKey2.encryptionKey)
      ))

    val fakeApplicationWithInvalidBase64FirstPreviousKey = FakeApplication(
      additionalConfiguration = Map(
        CurrentKey.configKey -> CurrentKey.encryptionKey,
        PreviousKeys.configKey -> Seq(
          keyWithInvalidBase64Encoding,
          PreviousKey1.encryptionKey,
          PreviousKey2.encryptionKey)
      ))

    val fakeApplicationWithShortOtherPreviousKey = FakeApplication(
      additionalConfiguration = Map(
        CurrentKey.configKey -> CurrentKey.encryptionKey,
        PreviousKeys.configKey -> Seq(
          PreviousKey1.encryptionKey,
          keyWithInvalidNumberOfBits,
          PreviousKey2.encryptionKey)
      ))

    val fakeApplicationWithInvalidBase64OtherPreviousKey = FakeApplication(
      additionalConfiguration = Map(
        CurrentKey.configKey -> CurrentKey.encryptionKey,
        PreviousKeys.configKey -> Seq(
          PreviousKey1.encryptionKey,
          keyWithInvalidBase64Encoding,
          PreviousKey2.encryptionKey)
      ))

    "throw a SecurityException if the current key is too short" in running(fakeApplicationWithShortCurrentKey) {
      intercept[SecurityException] {
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }

    "throw a SecurityException if the current key cannot be base 64 decoded" in running(
      fakeApplicationWithInvalidBase64CurrentKey) {
      intercept[SecurityException] {
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }

    "throw a SecurityException if the first previous key is too short" in running(
      fakeApplicationWithShortFirstPreviousKey) {
      intercept[SecurityException] {
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }

    "throw a SecurityException if the first previous key cannot be base 64 decoded" in running(
      fakeApplicationWithInvalidBase64FirstPreviousKey) {
      intercept[SecurityException] {
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }

    "throw a SecurityException if the other previous key is too short" in running(
      fakeApplicationWithShortOtherPreviousKey) {
      intercept[SecurityException] {
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }

    "throw a SecurityException if the other previous key cannot be base 64 decoded" in running(
      fakeApplicationWithInvalidBase64OtherPreviousKey) {
      intercept[SecurityException] {
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }
  }
}
