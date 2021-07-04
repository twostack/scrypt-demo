import org.twostack.bitcoin4j.Address
import org.twostack.bitcoin4j.PrivateKey
import org.twostack.bitcoin4j.Utils
import org.twostack.bitcoin4j.params.NetworkAddressType
import org.twostack.bitcoin4j.script.Script
import org.twostack.bitcoin4j.transaction.*
import java.math.BigInteger

/*
Alice spends to Bob using the contract she created using sCrypt
 */
fun createAliceSpendingTransaction(outputScript: Script) : String {

    val aliceWif = "cRHYFwjjw2Xn2gjxdGw6RRgKJZqipZx7j8i64NdwzxcD6SezEZV5"
    val alicePrivateKey: PrivateKey = PrivateKey.fromWIF(aliceWif)

    //Get the public Keys
    val alicePub = alicePrivateKey.publicKey

    //Get the addresses
    val aliceAddress = Address.fromKey(NetworkAddressType.TEST_PKH, alicePub)

    val aliceAddrStr = "n3vkuf1YPY3QRXx3kaLF6p8QhgWDZ2zg8F"

    /*
        alice receiving funds txId:
     */

    val aliceFundingRawTx = ""
    val aliceFundingTx: Transaction = Transaction.fromHex(aliceFundingRawTx)

    val unlockBuilder: UnlockingScriptBuilder = P2PKHUnlockBuilder(alicePub)

    /* send funds to Bob, locking those funds using sCrypt template */
    val bobLockingBuilder: LockingScriptBuilder = DefaultLockBuilder(outputScript)

    val aliceLockingBuilder: LockingScriptBuilder = P2PKHLockBuilder(aliceAddress)

    val txBuilder: TransactionBuilder = TransactionBuilder()

    val spendingTx: Transaction = txBuilder.spendFromTransaction(aliceFundingTx, 1, Transaction.NLOCKTIME_MAX_VALUE, unlockBuilder)
        .spendTo(bobLockingBuilder, BigInteger.valueOf(10000))
        .sendChangeTo(aliceAddress, aliceLockingBuilder)
        .withFeePerKb(512)
        .build(true)

    val fundingOutput: TransactionOutput = aliceFundingTx.outputs[1]
    TransactionSigner().sign(
        spendingTx,
        fundingOutput,
        0,
        alicePrivateKey,
        SigHashType.ALL.value or SigHashType.FORKID.value
    )

    return Utils.HEX.encode(spendingTx.serialize())

}

fun composeOutputScriptTemplate(y : Int) : Script{

    val templateStr = "OP_NOP OP_0 $y OP_0 OP_PICK OP_2 OP_ROLL OP_DROP OP_1 OP_ROLL OP_DROP OP_NOP OP_1 OP_PICK OP_1 OP_PICK OP_NUMEQUAL OP_NIP OP_NIP"

    return Script.fromBitcoindAsmString(templateStr)
}





fun createBobSpendingTransaction(fundingTx: String, outputIndex: Int, inputScript: Script): String {

    val bobWif = "cStLVGeWx7fVYKKDXYWVeEbEcPZEC4TD73DjQpHCks2Y8EAjVDSS"
    val bobPrivateKey: PrivateKey = PrivateKey.fromWIF(bobWif)

    //Get the public Keys
    val bobPub = bobPrivateKey.publicKey

    //Get the addresses
    val bobAddress = Address.fromKey(NetworkAddressType.TEST_PKH, bobPub)

    val bobAddrStr = "mpjFGX8CRr57qaGZKibryf1VqSwGQL5Khp"

    /*
        bob's receiving funds txId:
     */

    val bobFundingTx: Transaction = Transaction.fromHex(fundingTx)

    val unlockBuilder: UnlockingScriptBuilder = DefaultUnlockBuilder(inputScript)

    val bobLockingBuilder: LockingScriptBuilder = P2PKHLockBuilder(bobAddress)

    val txBuilder: TransactionBuilder = TransactionBuilder()
    val spendingTx: Transaction =
        txBuilder.spendFromTransaction(bobFundingTx, outputIndex, Transaction.NLOCKTIME_MAX_VALUE, unlockBuilder)
            .spendTo(bobLockingBuilder, BigInteger.valueOf(1200))
            .withFeePerKb(512)
            .build(true)

    val fundingOutput: TransactionOutput = bobFundingTx.outputs[outputIndex]
    TransactionSigner().sign(
        spendingTx,
        fundingOutput,
        0,
        bobPrivateKey,
        SigHashType.ALL.value or SigHashType.FORKID.value
    )

    return Utils.HEX.encode(spendingTx.serialize())

}


/*
bob: cStLVGeWx7fVYKKDXYWVeEbEcPZEC4TD73DjQpHCks2Y8EAjVDSS
 */
fun main(args: Array<String>) {

    val fundingBoxTx = "0100000002ac5513c1b67afa0b495305ecd6cdb3cea08c2a7870bbd86e7a1ecad3d9d81b6f010000006b483045022100f0519ac3dfa11ac9410d5fa8926f09336bc5c9099ecbae8214cf04d88d2e6ca60220678cc946e81ab3a45efecee3d93c08d05a71eae68693fb42e16d3b726a5cdbe64121033febada60fbba51caacd2c89188f393205c2b0a47be1c3efa5e122b439de7c6affffffff423dd4f65772bbe267b4417ba43ba45a6a71653d9c130de1aef5b9cbdeb808aa000000006b483045022100e5b0c44f64ca909fb058abb50f9d47b5def07807aa0e165fae9e7883bbf49b3402207f01a23ec6549f4a96961d0acf19d28b2cf83e0326794c023dcdddbc62f5e5414121033febada60fbba51caacd2c89188f393205c2b0a47be1c3efa5e122b439de7c6affffffff024006000000000000040052779c18440000000000001976a91407f3aa63dd60127b6cf1e1fbcdaccf10b418652488ac00000000"
    val bobsInputScript = Script.fromBitcoindAsmString("OP_2")
    val bobsRawTx = createBobSpendingTransaction(fundingBoxTx, 0, bobsInputScript)

    println(bobsRawTx)

}






