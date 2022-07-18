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

import java.util
import java.util.concurrent.CountDownLatch

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.collection.mutable.ListBuffer

class GCMConcurrencySpec extends AnyWordSpecLike with Matchers {

  "GCM" should {
    "be thread safe" in {
      val testResult = new TestResult

      val latch = new CountDownLatch(1)

      val wrapper = new GCMEncrypterDecrypter("1234567890123456".getBytes)

      val threads = new ListBuffer[Thread]()

      for (i <- 0 to 500) {
        val thread = new Thread(new GCMEncrypterDecrypterThread(latch, i, wrapper, testResult))
        threads += thread
        thread.start()
      }

      latch.countDown() // inform all the threads to start.

      threads.foreach(_.join)

      println("finished")

      testResult.failed shouldBe false
    }
  }

  private class TestResult {
    var failed: Boolean = false
  }

  private class GCMEncrypterDecrypterThread(
    val latch  : CountDownLatch,
    val Id     : Int,
    val wrapper: GCMEncrypterDecrypter,
    val result : TestResult
  ) extends Runnable {
    override def run(): Unit = {
      try {
        latch.await()
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }

      val valueToEncrypt = "somedata"
      try {
        val response = wrapper.encrypt(valueToEncrypt.getBytes, "additional".getBytes)
        val decrypt  = wrapper.decrypt(response.getBytes, "additional".getBytes)
        val equal    = util.Arrays.equals(valueToEncrypt.getBytes, decrypt.getBytes)
        println(s"Encrypted/Decrypted successfully: $equal")
        if (!equal) {
          result.failed = true
        }
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }
}
