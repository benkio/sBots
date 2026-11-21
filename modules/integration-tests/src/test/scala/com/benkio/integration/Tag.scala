package com.benkio.integration

object SlowTest extends org.scalatest.Tag("SlowTest")

// Provider-dependent tests (live Dropbox/Mega downloads) are excluded from the default
// verification path and run only through `sbt integrationLiveNetworkTests`.
object LiveNetwork extends munit.Tag("LiveNetwork")
