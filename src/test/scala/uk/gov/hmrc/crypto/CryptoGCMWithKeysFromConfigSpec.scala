/*
 * Copyright 2017 HM Revenue & Customs
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

import java.security.SecureRandom

import org.apache.commons.codec.binary.Base64
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import play.api.test.FakeApplication
import play.api.test.Helpers._

class CryptoGCMWithKeysFromConfigSpec extends WordSpecLike with Matchers with OptionValues {

  private val keybytes = new Array[Byte](16 * 2)
  private val previousKeybytes1 = new Array[Byte](16 * 2)
  private val previousKeybytes2 = new Array[Byte](16 * 2)

  val rand = new SecureRandom()
  rand.nextBytes(keybytes)
  rand.nextBytes(previousKeybytes1)
  rand.nextBytes(previousKeybytes2)

  private val baseConfigKey = "crypto.spec"

  private object CurrentKey {
    val configKey = baseConfigKey + ".key"
    val encryptionKey = Base64.encodeBase64String(keybytes)
    val plainMessage = PlainText("this is my message")
    val plainByteMessage = PlainBytes("this is a bunch of bytes".getBytes)
    val plainByteMessageResponse = PlainText("this is a bunch of bytes")
  }
  
  private object PreviousKey1 {
    val encryptionKey = Base64.encodeBase64String(previousKeybytes1)
    val plainMessage = PlainText("this is the first plain message")
    val plainByteMessage = PlainBytes("this is the first bunch of bytes".getBytes)
    val plainByteMessageResponse = PlainText("this is the first bunch of bytes")
  }

  private object PreviousKey2 {
    val encryptionKey = Base64.encodeBase64String(previousKeybytes2)
    val plainMessage = PlainText("this is the second plain message")
    val plainByteMessage = PlainBytes("this is the second bunch of bytes".getBytes)
    val plainByteMessageResponse = PlainText("this is the second bunch of bytes")
  }

  private object PreviousKeys {
    val configKey = baseConfigKey + ".previousKeys"
    val encryptionKeys = Seq(PreviousKey1.encryptionKey, PreviousKey2.encryptionKey)
  }

  "Constructing a CompositeCryptoWithKeysFromConfig with a current key, but no previous keys configured" should {

    val fakeApplicationWithCurrentKeyOnly = FakeApplication(additionalConfiguration = Map(
      CurrentKey.configKey -> CurrentKey.encryptionKey
    ))

    "return a properly initialised, functional AuthenticatedEncryption object that works with the current key only" in running(fakeApplicationWithCurrentKeyOnly)  {
      val crypto = CryptoGCMWithKeysFromConfig(baseConfigKey)

      crypto.decrypt(crypto.encrypt(CurrentKey.plainMessage)) shouldBe CurrentKey.plainMessage
      crypto.decrypt(crypto.encrypt(CurrentKey.plainByteMessage)) shouldBe CurrentKey.plainByteMessageResponse

      val previousKey1Crypto = CompositeSymmetricCrypto.aesGCM(PreviousKey1.encryptionKey, Seq.empty)
      val encryptedWithPreviousKey1 = crypto.encrypt(PreviousKey1.plainMessage, previousKey1Crypto)
      intercept[SecurityException] {
        crypto.decrypt(encryptedWithPreviousKey1)
      }
    }
  }

  "Constructing a CryptoGCMWithKeysFromConfig with a current key and empty previous keys" should {

    val fakeApplicationWithEmptyPreviousKeys = FakeApplication(additionalConfiguration = Map(
      CurrentKey.configKey -> CurrentKey.encryptionKey,
      PreviousKeys.configKey -> List.empty)
    )

    "return a properly initialised, functional AuthenticatedEncryption object that works with the current key only" in running(fakeApplicationWithEmptyPreviousKeys)  {
      val crypto = CryptoGCMWithKeysFromConfig(baseConfigKey)

      crypto.decrypt(crypto.encrypt(CurrentKey.plainMessage)) shouldBe CurrentKey.plainMessage
      crypto.decrypt(crypto.encrypt(CurrentKey.plainByteMessage)) shouldBe CurrentKey.plainByteMessageResponse

      val previousKey1Crypto = CompositeSymmetricCrypto.aesGCM(PreviousKey1.encryptionKey, Seq.empty)
      val encryptedWithPreviousKey1 = crypto.encrypt(PreviousKey1.plainMessage, previousKey1Crypto)
      intercept[SecurityException] {
        crypto.decrypt(encryptedWithPreviousKey1)
      }
    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig with both current and previous keys" should {

    val fakeApplicationWithCurrentAndPreviousKeys = FakeApplication(additionalConfiguration = Map(
      CurrentKey.configKey -> CurrentKey.encryptionKey,
      PreviousKeys.configKey -> PreviousKeys.encryptionKeys)
    )

    "allows decrypting payloads that were encrypted using previous keys" in running(fakeApplicationWithCurrentAndPreviousKeys)  {
      val crypto = CryptoGCMWithKeysFromConfig(baseConfigKey)

      val previousKey1Crypto = CompositeSymmetricCrypto.aesGCM(PreviousKey1.encryptionKey, Seq.empty)
      val encryptedWithPreviousKey1 = crypto.encrypt(PreviousKey1.plainMessage, previousKey1Crypto)
      val encryptedBytesWithPreviousKey1 = crypto.encrypt(PreviousKey1.plainByteMessage, previousKey1Crypto)
      crypto.decrypt(encryptedWithPreviousKey1) shouldBe PreviousKey1.plainMessage
      crypto.decrypt(encryptedBytesWithPreviousKey1) shouldBe PreviousKey1.plainByteMessageResponse

      val previousKey2Crypto = CompositeSymmetricCrypto.aesGCM(PreviousKey2.encryptionKey, Seq.empty)
      val encryptedWithPreviousKey2 = crypto.encrypt(PreviousKey2.plainMessage, previousKey2Crypto)
      val encryptedBytesWithPreviousKey2 = crypto.encrypt(PreviousKey2.plainByteMessage, previousKey2Crypto)
      crypto.decrypt(encryptedWithPreviousKey2) shouldBe PreviousKey2.plainMessage
      crypto.decrypt(encryptedBytesWithPreviousKey2) shouldBe PreviousKey2.plainByteMessageResponse

    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig with both current and previous keys using new Play 2.5 DI" should {

    val fakeApplicationWithCurrentAndPreviousKeys = FakeApplication(additionalConfiguration = Map(
      CurrentKey.configKey -> CurrentKey.encryptionKey,
      PreviousKeys.configKey -> PreviousKeys.encryptionKeys)
    )

    "allows decrypting payloads that were encrypted using previous keys" in running(fakeApplicationWithCurrentAndPreviousKeys)  {
      implicit val configurationThunk = () => fakeApplicationWithCurrentAndPreviousKeys.configuration
      val crypto = CryptoGCMWithKeysFromConfig(baseConfigKey)

      val previousKey1Crypto = CompositeSymmetricCrypto.aesGCM(PreviousKey1.encryptionKey, Seq.empty)
      val encryptedWithPreviousKey1 = crypto.encrypt(PreviousKey1.plainMessage, previousKey1Crypto)
      val encryptedBytesWithPreviousKey1 = crypto.encrypt(PreviousKey1.plainByteMessage, previousKey1Crypto)
      crypto.decrypt(encryptedWithPreviousKey1) shouldBe PreviousKey1.plainMessage
      crypto.decrypt(encryptedBytesWithPreviousKey1) shouldBe PreviousKey1.plainByteMessageResponse

      val previousKey2Crypto = CompositeSymmetricCrypto.aesGCM(PreviousKey2.encryptionKey, Seq.empty)
      val encryptedWithPreviousKey2 = crypto.encrypt(PreviousKey2.plainMessage, previousKey2Crypto)
      val encryptedBytesWithPreviousKey2 = crypto.encrypt(PreviousKey2.plainByteMessage, previousKey2Crypto)
      crypto.decrypt(encryptedWithPreviousKey2) shouldBe PreviousKey2.plainMessage
      crypto.decrypt(encryptedBytesWithPreviousKey2) shouldBe PreviousKey2.plainByteMessageResponse

    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig without current or previous keys" should {

    val fakeApplicationWithoutAnyKeys = FakeApplication()

    "throw a SecurityException on construction" in running(fakeApplicationWithoutAnyKeys) {
      intercept[SecurityException]{
        CryptoGCMWithKeysFromConfig(baseConfigKey)
      }
    }
  }

  "Constructing a CompositeCryptoWithKeysFromConfig without a current key, but with previous keys" should {

    val fakeApplicationWithPreviousKeysOnly = FakeApplication(additionalConfiguration = Map(
      PreviousKeys.configKey -> PreviousKeys.encryptionKeys
    ))

    "throw a SecurityException on construction" in running(fakeApplicationWithPreviousKeysOnly) {
      intercept[SecurityException]{
        CryptoGCMWithKeysFromConfig(baseConfigKey)
      }
    }
  }

  "Constructing a CryptoGCMWithKeysFromConfig with an invalid key" should {

    val keyWithInvalidNumberOfBits = "ZGVmZ2hpamtsbW4K"
    val keyWithInvalidKeySize = "defgh£jklmn"

    val fakeApplicationWithShortCurrentKey = FakeApplication(additionalConfiguration = Map(
      CurrentKey.configKey -> keyWithInvalidNumberOfBits,
      PreviousKeys.configKey -> PreviousKeys.encryptionKeys
    ))

    val fakeApplicationWithInvalidKeySize = FakeApplication(additionalConfiguration = Map(
      CurrentKey.configKey -> keyWithInvalidKeySize,
      PreviousKeys.configKey -> PreviousKeys.encryptionKeys
    ))

    val fakeApplicationWithShortFirstPreviousKey = FakeApplication(additionalConfiguration = Map(
      CurrentKey.configKey -> CurrentKey.encryptionKey,
      PreviousKeys.configKey -> Seq(keyWithInvalidNumberOfBits, PreviousKey1.encryptionKey, PreviousKey2.encryptionKey)
    ))

    val fakeApplicationWithInvalidBase64FirstPreviousKey = FakeApplication(additionalConfiguration = Map(
      CurrentKey.configKey -> CurrentKey.encryptionKey,
      PreviousKeys.configKey -> Seq(keyWithInvalidKeySize, PreviousKey1.encryptionKey, PreviousKey2.encryptionKey)
    ))

    val fakeApplicationWithShortOtherPreviousKey = FakeApplication(additionalConfiguration = Map(
      CurrentKey.configKey -> CurrentKey.encryptionKey,
      PreviousKeys.configKey -> Seq(PreviousKey1.encryptionKey, keyWithInvalidNumberOfBits, PreviousKey2.encryptionKey)
    ))

    val fakeApplicationWithInvalidBase64OtherPreviousKey = FakeApplication(additionalConfiguration = Map(
      CurrentKey.configKey -> CurrentKey.encryptionKey,
      PreviousKeys.configKey -> Seq(PreviousKey1.encryptionKey, keyWithInvalidKeySize, PreviousKey2.encryptionKey)
    ))

    "throw a SecurityException if the current key is too short" in running(fakeApplicationWithShortCurrentKey) {
      intercept[SecurityException]{
        CryptoGCMWithKeysFromConfig(baseConfigKey)
      }
    }

    "throw a SecurityException if the current key length is not 128 bits" in running(fakeApplicationWithInvalidKeySize) {
      intercept[SecurityException]{
        CryptoGCMWithKeysFromConfig(baseConfigKey)
      }
    }


    "throw a SecurityException if the first previous key is too short" in running(fakeApplicationWithShortFirstPreviousKey) {
      intercept[SecurityException]{
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }

    "throw a SecurityException if the first previous key cannot be base 64 decoded" in running(fakeApplicationWithInvalidBase64FirstPreviousKey) {
      intercept[SecurityException]{
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }

    "throw a SecurityException if the other previous key is too short" in running(fakeApplicationWithShortOtherPreviousKey) {
      intercept[SecurityException]{
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }

    "throw a SecurityException if the other previous key cannot be base 64 decoded" in running(fakeApplicationWithInvalidBase64OtherPreviousKey) {
      intercept[SecurityException]{
        CryptoWithKeysFromConfig(baseConfigKey)
      }
    }

  }
}
