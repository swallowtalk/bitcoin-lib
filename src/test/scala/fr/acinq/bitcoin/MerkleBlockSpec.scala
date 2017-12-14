package fr.acinq.bitcoin

import java.util

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.annotation.tailrec

@RunWith(classOf[JUnitRunner])
class MerkleBlockSpec extends FunSuite {
  import MerkleBlock._

  test("compute tree width") {
    assert(calcTreeWidth(5, 0) == 5)
    assert(calcTreeWidth(5, 1) == 3)
    assert(calcTreeWidth(5, 2) == 2)
    assert(calcTreeWidth(5, 3) == 1)
  }

  test("serialize merkle block") {
    // we asked for the txout proof for the following txs
    // 67cb5d5f5c696faa4d1d95a1f2e5a02725195dc3fe27eacb8893e213c0f6c6da pos = 36
    // 4cadfec91508f7a4e7b09632077adb9dc60520918931fc397a37cbe89eb1ed8a pos = 834
    // b1319c3fa2ee1d2dd95494daa8c783a498f50d183efc2b93e6aac130ab7f39ad pos = 858
    val raw = BinaryData("000000201f62c4c90805ee0b47915a9cfe8d95f558a6fa277eadf70a54f4070000000000e74be55116d76b39a48a0a32e0c7c31f8d771f6b7dc12b66dcb650eb00abcd9b2b42b859ffff001d00fb7179a5030000187c6a325cedc29832d523b992178ec0f17353c6c8da717bc52e1af593bc8830be8ed74002e07e8a20c96573f03fb72f88af22ddc460e7967ec29422a6a0d87e7bd86fb2fb16126d7eb7358ae1628154936ad0b4b84762f562fe5e4aa1b2c6b753dac6f6c013e29388cbea27fec35d192527a0e5f2a1951d4daa6f695c5f5dcb67b596d74d60134114d3067f629ada73d116163486fb44c54ead4b5ca9f5228d0af53b3339b13b565c37841fd01594a52655490d362ae2921a1bacbcc38c232b5ed0a8336391d2ba904c73885a32894ecabe4ea17ca571a5bbc86e295e38a4ae2344bbcf6208bf5578c04529ea0ccb612d62d884b69c2948649cb6cd4e801d8f9de8710d724cdc92cda3cd2cf0f48c532d6e66192eea7a91f559d423fb1aa721675226be3fe4580fc43f0ff0982e1011ac5e178bd284c6f39d49aac00d215bcfeba8c3604abd62c4c13d2c7cef15d1fa36afc9cfebdda07e93c9cb01178bd2c04ac686501ec71f43ab4b722b9b2fea8b11c3d751097cba0df0eb3e56320951cfad332fb2133ee30a84717be1a54e0bd869e13df139a615630f3b2bb57e8764c5a02b3a14a74e304372abeb137ef095e864bf9b4315697b6db47121036f7c7d6aab8aedb19ee8cb377a39fc3189912005c69ddb7a073296b0e7a4f70815c9fead4c8eec18260c0e65d1b71e29bed9a7f1299b30ee716fff6c03905c717fa8bc925e18afb493d3538dfc7a576ba1ca632a8dcf665aa3b4df5b85e37082205e4bd8632473bb783b5844194b6cf0f0e0997c1837fff2876ef3810559797cd77548b69f9acdc8256249e2dc5d5c881546ac6c28f62fbc9c0849e9d5b8d12be7ce8ffe84ad397fab30c1aae6932bfc3e180df598a483c7a8da9454d92d1deea23f9c31b14192673f384ba44edac51c2544e8c6661e3dc7da2eb34535c95f530041c311922a335049c3d5dbec046ef0e283f474cfd87f3c5890981a1012b872db05123dceda71a8d447dfb4324998d721a75f3e552b86d5540e5b67556681417eb01a145208653fa1cd720bec20cd5527a2267e96e4524bf64c1cabdb7f5802ae275c8e2606df2bd05ed306")
    val merkleBlock = MerkleBlock.read(raw)

    assert(MerkleBlock.write(merkleBlock) == raw)
    MerkleBlock.verify(merkleBlock)

    val (root, matched, _, _) = merkleBlock.computeRoot
    assert(matched.toSet === Set(
      BinaryData("dac6f6c013e29388cbea27fec35d192527a0e5f2a1951d4daa6f695c5f5dcb67") -> 35,
      BinaryData("8aedb19ee8cb377a39fc3189912005c69ddb7a073296b0e7a4f70815c9fead4c") -> 838,
      BinaryData("ad397fab30c1aae6932bfc3e180df598a483c7a8da9454d92d1deea23f9c31b1") -> 858
    ))
  }

  test("#2") {
    val merkleBlock = MerkleBlock.read("00000020a23be1ec44bbf1998ccb9b312c20ecbfbfae5eb89b085e339d3af5880a956214578bfa60861e1c56335fdea4086a5ba1ee7e0624e12ed7d4c3eafdff84aa35f5af34b859ffff7f20000000000500000004668a988978e90ceb37ade8f9ebb0c9f23eb64d36a3f56e86d3c5539fe99fa446187f6458c1b85c50608d2fecf560f32dca042c288bd99f96a455b07cdf523b70674ab388c2a6869767ec7886befc699e7d61b99b1519fe1dfcd1e9491f1e8f134d352c6b0197c7918dc4017aa0105382d210a79a0f07ab1b1bc21897dd60f746010f")
    MerkleBlock.verify(merkleBlock)
  }

  test("#3") {
    val merkleBlock = MerkleBlock.read("00000020a23be1ec44bbf1998ccb9b312c20ecbfbfae5eb89b085e339d3af5880a956214578bfa60861e1c56335fdea4086a5ba1ee7e0624e12ed7d4c3eafdff84aa35f5af34b859ffff7f20000000000500000002b820aa298d593d62ca3d6a73f85613d76d31fb05bacb1087524601cb2e25cad74a3dfd63165cdcdbc2ae6539eede2d3639fbbf3b7a7f62b4f4b3ba0f3b24075f011d")
    MerkleBlock.verify(merkleBlock)
  }
}
