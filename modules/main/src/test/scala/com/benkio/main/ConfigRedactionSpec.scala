package com.benkio.main

import cron4s.Cron
import munit.FunSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri

class ConfigRedactionSpec extends FunSuite {

  test("the toString of the HttpServerConfig does not show the keystore password") {
    val shown = HttpServerConfig("0.0.0.0", 443, Some("keystore.p12"), Some("s3cr3t-password")).toString
    assert(!shown.contains("s3cr3t-password"), shown)
    assert(shown.contains("keystore.p12"), shown)
  }

  test("the toString of the HttpServerConfig tells there is no password") {
    assert(HttpServerConfig("0.0.0.0", 443, None, None).toString.contains("None"))
  }

  test("the toString of the HealthcheckPingConfig does not show the ping url") {
    val shown =
      HealthcheckPingConfig(uri"https://hc-ping.com/0a1b2c3d-secret", Cron.unsafeParse("0 */10 * ? * *")).toString
    assert(!shown.contains("0a1b2c3d-secret"), shown)
    assert(!shown.contains("hc-ping.com"), shown)
  }
}
