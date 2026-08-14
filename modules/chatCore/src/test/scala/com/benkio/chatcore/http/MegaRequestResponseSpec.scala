package com.benkio.chatcore.http

import io.circe.parser.decode
import munit.FunSuite

class MegaRequestResponseSpec extends FunSuite {

  val megaEncryptedFileResponseJsons: List[String] = List(
    """|[
       |  {
       |    "s" : 4149975,
       |    "at" : "eswFpFOvyTtf_N_7TMwyTpPyTO2zIuK1LGR7VkU3zoDLqLsCQsqaSkJME1Qi-QobFJJcKKufBtyQanm9YlOrZw",
       |    "msd" : 1,
       |    "fa" : "705:0*eCBjlVH2O0Y/705:1*3rF_yMoHKIY/524:8*VEakmJfplGQ/616:9*CnbhvU_E_Co",
       |    "g" : [
       |      "https://gfs214n216.userstorage.mega.co.nz/dl/5b-5BAAypgUuVgchsuZwSN25Ri-R0jxTuQlLf5UCCqIasbuFTf4r7UDGZwrXzy4sQkShBf_38khwFWtcqUgh5uI33KLAbR_txRTcnTJeWgDu1ARpmBzsALbBtDdigg",
       |      "https://gfs270n477.userstorage.mega.co.nz/dl/lD0jO3JPdMSbDQdD47T3xUkI1FvJ3VLsYpdkgPliZk5TOJCFVyYs3R5oFPiVCLtQLqBOaA5a0Qnl1zjaAYEnHlbD0mTlwGkMTImIPkoIoq8-kuGG7i-Rr5cHPLp0Vg",
       |      "https://gfs204n326.userstorage.mega.co.nz/dl/jxkaTmJjSysIlgc1T3T-Vwx_rAw311RmDl_C8ILvA7IIrtjnMdjDmpzeYIWrO2MnAFl06fsqPkhd5Vfrsy5SH4WfeBjdvrrVjrb_Bmg-x8lCPq9ukfBhU7AMthlGlA",
       |      "https://gfs208n223.userstorage.mega.co.nz/dl/WKajdv2N2de3tgcPOcS4u6uCF2a3Ia68mVpoCZAf0KCCqAqZrVeGONDlLwRsKAjlW4hPi12PdDbvDH5OQm9YSiGaUSbtWCXaKiEwplkot7a6YR5iP3P-w5I3LBtBmg",
       |      "https://gfs240n137.userstorage.mega.co.nz/dl/MC9RTeEJDk5cqAcMqFZL8dqnwdScS9N_ORNUQj5cHs06EkaLmvx356pCaQYhE1fQrHkL-dRUF_XodQXp4tNbizpXk-NarOCvBDCffKRz2uklP4PNDTTZHUVwRsmfEw",
       |      "https://gfs206n477.userstorage.mega.co.nz/dl/bsKjJnskqsZbUgezLGJSsWfzYDQ5lJPKFrZsX8Rxj5_U5NEL4c0bivBw8B60MgzB_JrvizdxBPL3cOG1f-ekPSQtuo_jEoNRxnog3haZKvXeOJCizRFhplfVt8qQBA"
       |    ],
       |    "ip" : [
       |      "185.206.27.144",
       |      "2a0b:e43:1::144",
       |      "31.216.148.58",
       |      "2001:67c:1998:2212::58",
       |      "185.206.24.172",
       |      "2a0b:e40:1::172",
       |      "185.206.26.153",
       |      "2a0b:e42:1::153",
       |      "69.30.89.59",
       |      "2a09:a380:1::59",
       |      "94.24.37.42",
       |      "2a0b:e45:1::42"
       |    ],
       |    "fh" : "31jhrH7nC6E"
       |  }
       |]""".stripMargin,
    """|[
       |  {
       |    "s" : 1714419,
       |    "at" : "qkJNwwotwZchRUFvp33W8d9BXWYRMvNFExc4OnRAg5S17NPbicDhjkWWlatEsDdbCmr40qmncxix_w0ce8YAc4R2ly6JcBGC-l6BKPtdrf5AyAgLKNwC7u0dSRQ3iaazxO0-mXDMXul5wnVpuJH3JA",
       |    "msd" : 1,
       |    "fa" : "705:0*p3F0sSpU44U/705:1*C0s0eiskKdI/30:8*_6KFMhcVtEc",
       |    "g" : [
       |      "https://gfs214n222.userstorage.mega.co.nz/dl/xOumvmLxXd-55AfsjkQh8ATBt-CarS9Y7XNn-nKL13Bczgw-ZhwWD1ROwB0idLjV1eyEM6eA0Zy85CxqXyW2TxivtiXmKncmIR2rW_0zrp6TRydMxrwcutBwPFB0ig",
       |      "https://gfs270n482.userstorage.mega.co.nz/dl/csG7dc2a-KYufwf_7mVEV-zvJSjfsrz7jKn8jnPfMR3nPksTMXqvvqs4NXbb1H3pDE4_0ZYcYtzGJNmVImFJPXdzm1lrfn6-3nHZnXKnk3IY6ViSW8Kb4ZAwEyW7fg",
       |      "https://gfs204n331.userstorage.mega.co.nz/dl/UA0qLKRBm_0bMwcKxIgXRFdvRMSX_MkYnFeB2dfcZvpZfO1TDTHMoxGQ-Cv45zkBmDMRglcPAHL9cVseWwao_FAkXKMDo9BD_ATcDSbHLxE7VaD8nHhs8yg3Oefm8A",
       |      "https://gfs208n228.userstorage.mega.co.nz/dl/xPtoT5SfahZa3gcxb0J2kZiQw6KDVdwZ2f6b3UhiYLLseOQNXekxK4VpWHlAZhNb2hwsPT-3rBQzuiqZVctGLXVJnNVR2zW4S4loFsothcLDF-E59dN3G5ZmnUDjbw",
       |      "https://gfs240n144.userstorage.mega.co.nz/dl/0dMJaJo3gsdJzQeyTYw9vkFR09_1XhiKUAII5o5IvdzUJqVhviWi8-j-zcZavYnP6q36G_jt4Zvm6H0tt48dUGB7da1V3pjKRAvaLpb_6Dhd2ij4fBDM8DhHqWT-lg",
       |      "https://gfs206n482.userstorage.mega.co.nz/dl/xy69dr0ZmOb9jwcTS62fNVdfTU7q1ljg4fVSi2vvCjd2-9ZPCSX4M-sd0O4EeWntqVG2sHdY3HhBdXPX1XjvurXKkTJajOrOaNPTaVPZTXzkoOj-h4Iq8SNoTF2DyQ"
       |    ],
       |    "ip" : [
       |      "185.206.27.149",
       |      "2a0b:e43:1::149",
       |      "89.44.168.179",
       |      "2001:678:25c:2216::179",
       |      "185.206.24.202",
       |      "2a0b:e40:1::202",
       |      "185.206.26.72",
       |      "2a0b:e42:1::72",
       |      "69.30.89.78",
       |      "2a09:a380:1::78",
       |      "94.24.37.225",
       |      "2a0b:e45:1::225"
       |    ],
       |    "fh" : "73LR47AT8Yo"
       |  }
       |]""".stripMargin,
    """|[
       |  {
       |    "s" : 1184980,
       |    "at" : "Ws3zyyoH82ZyaRNg7pgUo_rBPXuMtamSyB6aY6C3JS55nZR7j8LHQKvQaYg6BzSkLbr0WeU_coJPvQWepmwecBoVl8m-ZGhMNJcGE1K4aYc",
       |    "msd" : 1,
       |    "fa" : "1017:8*HDzC6gryBZM",
       |    "g" : [
       |      "https://gfs262n366.userstorage.mega.co.nz/dl/oETxE7fh67seZwJN0bwYhr2yoNRE9rjpzNdQgh727Ea8Ih1dKnRuocmEsApDmtzwsPfhFDQiG0sA3eRE0e1kOLTsEAs99wJvNcGQy3npSGq8TXDV_p1WuRKIb5hb0Q",
       |      "https://gfs214n177.userstorage.mega.co.nz/dl/Xb4HdjWUNXZWkQcKf7RbKp6sa9foPThfwBBhDod-RzqjlWVWzpXPDRqtxqVpRsRZ3xvjOCYAQCy11aHg57MG2O2DAtDttG6GGQ2Fe9DI5Zk_2CIFLlgzMM1GmIpwng",
       |      "https://gfs270n117.userstorage.mega.co.nz/dl/gwRXTBf0E5Rn1ALjV5ojZekqQ1D7rG1-xCKy43HoJmiwwsKyCzHLmG2r6fcjKvO2LVjRig4tSbgpX6JQyAGc-O63QdkR_5Iy4_lQ088rLfNX6Byd6XI8ZOBdCr0N3A",
       |      "https://gfs208n186.userstorage.mega.co.nz/dl/55fzFNgqDdvpoAfHQ_Pq4uhgpwOwnxvi_J7DOxjzEHtm84lN0laJvJe5yTM43CMJJm-OW0MsL0R54FPtXUJHHdGMONNWdEJVaLlyMk1n9K2XWRbZ8P9eibfqNyTuzA",
       |      "https://gfs204n184.userstorage.mega.co.nz/dl/aIclVTR1UsoO8gLlYlD18JntWLsTvMzbfYbzqch77LLsAps480AWimEii1_ERytAuuoQuraX1wGzLcc1YE7sxM4xia_JZXsyl8MhPsaOFB77Vj3hA2SCU0Sgese-Vg",
       |      "https://gfs206n179.userstorage.mega.co.nz/dl/bnwjLasUuX8zVQfunF0EZEg8jbS2oTCtT1QJ-xSSZNpQoD3V_mZs6Vh8k4lWUP8xovkeo0HjaWZxghOEh3w7gHvu8T4oqdg6n6Ig3ZpGxXMn7DQkquZH1cEzSey5Ow"
       |    ],
       |    "ip" : [
       |      "94.24.36.76",
       |      "2a0b:e44:1::76",
       |      "185.206.27.87",
       |      "2a0b:e43:1::87",
       |      "89.44.168.177",
       |      "2001:678:25c:2216::177",
       |      "185.206.26.96",
       |      "2a0b:e42:1::96",
       |      "185.206.24.137",
       |      "2a0b:e40:1::137",
       |      "94.24.37.89",
       |      "2a0b:e45:1::89"
       |    ],
       |    "fh" : "fzA9Hy51LJ0"
       |  }
       |]""".stripMargin
  )

  test("MegaEncryptedFileResponse should decode from supported JSON payloads") {
    for input <- megaEncryptedFileResponseJsons do decode[Array[MegaEncryptedFileResponse]](input).fold(
      e => fail("failed to parse MegaEncryptedFileResponse", e),
      responses => {
        responses.foreach(response => {
          assert(response.s >= 0)
          assert(response.g.fold(_.renderString.nonEmpty, _.forall(_.renderString.nonEmpty)))
        })
      }
    )
  }
}
