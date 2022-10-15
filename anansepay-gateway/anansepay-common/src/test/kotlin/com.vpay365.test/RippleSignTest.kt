package im.ananse.test

import com.ripple.crypto.ecdsa.ECDSASignature
import com.ripple.crypto.ecdsa.SECP256K1
import com.ripple.crypto.ecdsa.Seed
import com.ripple.encodings.base58.B58
import com.ripple.utils.HashUtils
import im.ananse.gateway.wallet.*
import io.vertx.core.json.Json
import org.junit.Test
import org.ripple.bouncycastle.crypto.params.ECPublicKeyParameters
import org.ripple.bouncycastle.crypto.signers.ECDSASigner
import java.security.SecureRandom
import java.util.*


class RippleSignTest {
    var account = "rhcfR9Cg98qCxHpCcPBmMonbDBXo84wyTn"
    var secret = "shHM53KPZ87Gwdqarm1bAmPeXg8Tn"
    var key_type = "secp256k1"

/*
    Test vectors:
    secret: shHM53KPZ87Gwdqarm1bAmPeXg8Tn == 0x71ED064155FFADFA38782C5E0158CB26
    private generator: 0x7CFBA64F771E93E817E15039215430B53F7401C34931D111EAB3510B22DBB0D8
    public generator: fht5yrLWh3P8DrJgQuVNDPQVXGTMyPpgRHFKGQzFQ66o3ssesk3o
    first private key: pwMPbuE25rnajigDPBEh9Pwv8bMV2ebN9gVPTWTh4c3DtB14iGL
    first public key: aBRoQibi2jpDofohooFuzZi9nEzKw9Zdfc4ExVNmuXHaJpSPh8uJ
    first Ripple address: rhcfR9Cg98qCxHpCcPBmMonbDBXo84wyTn
 */

    @Test
    fun TestVerifySecretKeyChecksum () {
        val b58codec = B58("rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz")

        // The secret key is the master key seed
        println("Secret: "+secret)
        val secretbytes = b58codec.decode(secret)
        // Ensure B58 works ok
        assert(b58codec.encodeToString(secretbytes) == secret)
        // Confirm that the first byte is 33
        assert (secretbytes.get(0).toInt() == 33)
        // And that the length is 21
        assert (secretbytes.size == 21)
        // The payload is the first 17 bytes here
        val payload = secretbytes.slice(IntRange(0,16)).toByteArray()
        // The checksum is the last 4 bytes
        val checksum = secretbytes.slice(IntRange(17,20)).toByteArray()
        // find SHA256(SHA256(payload)) with ripple hashutils
        val sha = HashUtils.doubleDigest(payload)

        // Compare sha2(0..4) with checksum(0..4)
        var i: Int =0
        while (i<4) {
            assert(sha.get(i).toInt() == checksum.get(i).toInt())
            i++
        }
    }

    @Test
    fun TestGenerateAddressfromSecret(){
        // Generate a seed from the secret
        // Decode from base58
        val seed = Seed.fromBase58(secret)
        println("Seed: "+secret)
        // Create a keypair from the secret
        val keypair = seed.keyPair()

        // Print public and private keys
        val publickeyhex = keypair.canonicalPubHex()
        val privatekeyhex = keypair.privHex()
        println("Publickey: "+publickeyhex)
        println("Privatekey: "+privatekeyhex)

        // Generate address from public key
        val b58codec = B58("rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz")
        //
        val payload = keypair.pub160Hash()
        // Now to create the address we need to add 0 at the beginning
        val payloadwithprefix: ByteArray = kotlin.ByteArray(21)
        // Copy the SHA256-RIPEMD160 to bytes 1-20 inclusive
        var i =0
        while (i<20) {
            payloadwithprefix[i+1] = payload[i]
            i++
        }
        val sha = HashUtils.doubleDigest(payloadwithprefix)


        // add the checksum as the first four bytes of the double SHA256 of the SHA256-RIPEMD160
        val address : ByteArray = kotlin.ByteArray(25)
        i=0
        while (i<21) {
            address[i]=payloadwithprefix[i]
            i++
        }

        address[21]=sha[0]
        address[22]=sha[1]
        address[23]=sha[2]
        address[24]=sha[3]

        // And encode to base58 again
        val encaddress = b58codec.encodeToString(address)
        assert(encaddress == account)

    }

    @Test
    fun TestSignVerifywithKeyPair(){
        val keypair = Seed.fromBase58(secret).keyPair()

        val messagetosign = "This is a test message"
        val signature = keypair.signMessage(messagetosign.toByteArray())

        assert(keypair.verifySignature(messagetosign.toByteArray(),signature)== true)
    }

    @Test
    fun TestSignVerifywithPubKey(){
        // This signature is as generated on the client
        val keypair = Seed.fromBase58(secret).keyPair()
        val messagetosign = "This is a test message"
        val signature = keypair.signMessage(messagetosign.toByteArray())

        // Now the server only has a publickey and the message
        val pubkey= keypair.canonicalPubBytes()
        val messagetoverify = "This is a test message"
        // the signature is done on a half SHA512 hash
        val hash = HashUtils.halfSha512(messagetoverify.toByteArray())

        //Decode the signature from a DER format
        //And verify the ECDSA signagure
        val decodedsig = ECDSASignature.decodeFromDER(signature)
        val signer = ECDSASigner()
        val point = SECP256K1.curve().decodePoint(pubkey)
        val params = ECPublicKeyParameters(point, SECP256K1.params())
        signer.init(false, params)
        assert(signer.verifySignature(hash,decodedsig.r,decodedsig.s)==true)
    }

    @Test
    fun TestVPay365walletclass (){
        val wallet = VPay365Wallet(this.secret)
        assert (wallet.address == this.account)

        val pubkey = wallet.publickey()

        val message = "This is a test message I would like to sign with this wallet"
        val signature = wallet.signMessage(message)
        // Verification using keypair
        assert (wallet.verifyMessage(message,signature)==true)

        // Verification using public key only
        assert (wallet.verifyMessage(message,signature,pubkey)==true)
    }

    /*
    * This is to generate a provisioning request to use with curl
    * wallet propose output
    * {
    *     "id" : 1,
    *     "result" : {
    *     "account_id" : "rHzi7AJ6JS6x9GxAQENug4aXfofYzB1KGt",
    *     "key_type" : "secp256k1",
    *     "master_key" : "ITS AMEN CRAM OVA IRA BEE OMEN BARB ME TOUT FISH JAY",
    *     "master_seed" : "ss2Grp23bEdhjGVyVXbzkbNCVVpPo",
    *     "master_seed_hex" : "3F1058779F504AC80DE45198B07DE91E",
    *     "public_key" : "aBRk2GRn6WKiw3Rqp7BJm9Vj4uiYAZtLxazYwcHPRqF8ewxGKLAX",
    *     "public_key_hex" : "03F3B4304990F3ACC19CA1476D7748D0667221E27840948DF6B7EE267DFF0A5A6A",
    *     "status" : "success"
    *     }
    * }
    */

    @Test
    fun TestProvisioningMessage() {
        val phonenumber = "+447709618382"
        val wallet = VPay365Wallet("ss2Grp23bEdhjGVyVXbzkbNCVVpPo")
        val timestamp = Date().time
        val messagetosign = wallet.address + phonenumber + timestamp.toString()
        val signature = wallet.signMessage(messagetosign)

        val reply = FeelPayProvisioningRequest(
                address = wallet.address,
                telnumber = phonenumber,
                pubkey = wallet.publickey(),
                timestamp = timestamp.toLong(),
                signature = signature
        )

        println(Json.encode(reply))
    }

    @Test
    fun TestProvisioningReply() {
        val wallet = VPay365Wallet("ss2Grp23bEdhjGVyVXbzkbNCVVpPo")
        val smscode = "795022" // Change this on each request
        val messagetosign2 = wallet.address+smscode
        val signature2 = wallet.signMessage(messagetosign2)
        val reply2 = FeelPayProvisioningReply(
                address = wallet.address,
                signature = signature2
        )
        println (Json.encode(reply2))

    }

    @Test
    fun TestRNG() {
        val rng = SecureRandom()
        rng.setSeed(Date().time)

        var a = rng.nextInt()
        var b = (-39520162)%2147483647
        var c = (-39520162)
        var d: Int
        if (c>0)
            d = c
        else
            d = c+2147483647


    }

    // Withdrawal request
    // {
    //   "address": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
    //   "amount": 10.50,
    //   "currency": "CNY", or "BTC",
    //   "bankname": "Bank of China",
    //   "accountnumber": "1231230494949",
    //   "accountholder": "Leeroy Jenkins",
    //   "txhash" : "D93D89DCBF9320F6576E4A57C4E31983107A92D8A62C7FF50773CC217017088F",
    //   "signature": "XXXXbase64encodedsigXXX"
    // }
    @Test
    fun TestWithdrawalRequest() {
        val wallet = VPay365Wallet("ss2Grp23bEdhjGVyVXbzkbNCVVpPo")
        val amount = 10000F // which means 100.00
        val currency = "CNY"
        val bankname = "Bank of China"
        val accountnumber = "123123123123123"
        val accountholder = "Leeeeroy Jeeeeenkins"
        val txhash = "D93D89DCBF9320F6576E4A57C4E31983107A92D8A62C7FF50773CC217017088F"

        val messagetosign = wallet.address+amount.toString()+currency+bankname+accountnumber+accountholder+txhash
        val signature = wallet.signMessage(messagetosign)

        val reply = WithdrawalRequest (
                address = wallet.address,
                amount = amount,
                currency = currency,
                bankname = bankname,
                accountnumber = accountnumber,
                accountholder = accountholder,
                txhash = txhash,
                signature = signature
        )

        println(Json.encode(reply))
    }

    // Topup request
    // The client sends a JSON object in the following format
    // {
    //   "adddress": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
    //   "amount": 10000, (integer in cents)
    //   "currency": "CNY", .. or "BTC"
    //   "signature": "XXXXbase64encodedsigXXX"
    // }

    @Test
    fun TestTopupRequest() {
        val wallet = VPay365Wallet("ss2Grp23bEdhjGVyVXbzkbNCVVpPo")
        val amount = 10000 // which means 100.00
        val currency = "CNY"

        val messagetosign = wallet.address + amount.toString() + currency
        val signature = wallet.signMessage(messagetosign)

        var reply = FeelPayTopupRequest(
                address = wallet.address,
                amount = amount,
                currency = currency,
                signature = signature
        )

        println(Json.encode(reply))
    }

    // Push token request:
    //   "adddress": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
    //   "token: "dfgsdkfjgksdg...sdfgsdfgdsfg",
    //   "timestamp": "1500995339",
    //   "signature": "XXXXbase64encodedsigXXX"

    @Test
    fun TestPushtokenRequest(){
        val wallet = VPay365Wallet("ss2Grp23bEdhjGVyVXbzkbNCVVpPo")
        val token = "d720445218"
        val timestamp = Date().time

        val messagetosign = wallet.address+token+timestamp.toString()
        val signature = wallet.signMessage(messagetosign)

        val reply = FeelPayPushTokenRequest(
                address = wallet.address,
                token = token,
                timestamp = timestamp.toLong(),
                signature = signature
        )
        println(Json.encode(reply))
    }





    // Bitcoin address provisioning request
    // {
    //  "address": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
    //  "btcaddresses": [
    //      "BsMKtmYX41HRmMBLr7LKXcg1HKbgAXUi00",
    //      "BsMKtmYX41HRmMBLr7LKXcg1HKbgAXUi01",
    //      "BsMKtmYX41HRmMBLr7LKXcg1HKbgAXUi02"
    //   ],
    //  "timestamp": 1500995339,
    //  "signature": "XXXXbase64encodedsigXXX"
    // }
    @Test
    fun TestBitcoinProvisioningRequest(){
        val wallet = VPay365Wallet("ss2Grp23bEdhjGVyVXbzkbNCVVpPo")
        val btcaddresses = arrayOf("BsMKtmYX41HRmMBLr7LKXcg1HKbgAXUi04","BsMKtmYX41HRmMBLr7LKXcg1HKbgAXUi01", "BsMKtmYX41HRmMBLr7LKXcg1HKbgAXUi02")
        val timestamp = Date().time

        val messagetosign = wallet.address+btcaddresses[0]+btcaddresses[1]+btcaddresses[2]+timestamp.toString()
        val signature = wallet.signMessage(messagetosign)

        val reply = BitcoinRequest(
                address = wallet.address,
                btcaddresses = btcaddresses,
                timestamp = timestamp.toLong(),
                signature = signature
        )
        println(Json.encode(reply))
    }

    // Bitcoin generation request
    // {
    //   "address": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
    //   "timestamp": 1500995339,
    //   "signature": "XXXXbase64encodedsigXXX"
    // }
    @Test
    fun TestBitcoinGenerationRequest() {
        val wallet = VPay365Wallet("ss2Grp23bEdhjGVyVXbzkbNCVVpPo")
        val timestamp = Date().time
        val messagetosign = wallet.address+timestamp.toString()
        val signature = wallet.signMessage(messagetosign)
        val reply = BitcoinGenerationRequest (
                address = wallet.address,
                timestamp = timestamp.toLong(),
                signature = signature
        )
        println(Json.encode(reply))
    }



    @Test
    fun TestValidateAddress(){
        val wallet = VPay365Wallet("ss2Grp23bEdhjGVyVXbzkbNCVVpPo")
        assert(wallet.verifyAddress(wallet.address) == true)
    }


}