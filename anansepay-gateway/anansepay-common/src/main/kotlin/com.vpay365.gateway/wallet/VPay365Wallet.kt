package im.ananse.gateway.wallet

import com.ripple.crypto.ecdsa.ECDSASignature
import com.ripple.crypto.ecdsa.IKeyPair
import com.ripple.crypto.ecdsa.SECP256K1
import com.ripple.crypto.ecdsa.Seed
import com.ripple.encodings.base58.B58
import com.ripple.utils.HashUtils
import org.ripple.bouncycastle.crypto.params.ECPublicKeyParameters
import org.ripple.bouncycastle.crypto.signers.ECDSASigner
import org.ripple.bouncycastle.util.encoders.Base64

class VPay365Wallet (secret: String)  {
    var secret: String
    var keypair: IKeyPair? = null
    var publickey:  String
    var address: String


    init  {
        this.secret = secret
        val seed = Seed.fromBase58(this.secret)
        this.keypair = seed.keyPair()
        this.publickey  = this.keypair!!.canonicalPubHex()
        this.address = getAddressfromPubKey(this.keypair!!.canonicalPubBytes())
    }


    fun getAddressfromPubKey(pubkey: ByteArray): String {

        val b58codec = B58("rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz")
        val payload = HashUtils.SHA256_RIPEMD160(pubkey)
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
        return b58codec.encodeToString(address)
    }

    fun verifyAddress(address:String): Boolean {
        val b58codec = B58("rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz")
        val address_binary = b58codec.decode(address)

        val payload=address_binary.copyOfRange(0,21)
        val sha = HashUtils.doubleDigest(payload)
        if (sha[0]==address_binary[21] && sha[1]==address_binary[22] && sha[2]==address_binary[23] && sha[3] == address_binary[24]) {
            return true
        }

        return false
    }

    fun publickey (): String {
        return Base64.toBase64String(this.keypair!!.canonicalPubBytes())
    }

    fun signMessage(message:String): String {
        val sig = keypair!!.signMessage(message.toByteArray())
        return Base64.toBase64String(sig)
    }

    fun verifyMessage(message: String, signature: String): Boolean {
        try {
            val sig = Base64.decode(signature);
            return keypair!!.verifySignature(message.toByteArray(), sig)
        } catch (e: Throwable) {
            return false
        }
    }

    fun verifyMessage(message: String, signature: String, pubkey: String): Boolean {
        try {
            val hash = HashUtils.halfSha512(message.toByteArray())
            val decodedsig = ECDSASignature.decodeFromDER(Base64.decode(signature))
            val signer = ECDSASigner()
            val point = SECP256K1.curve().decodePoint(Base64.decode(pubkey))
            val params = ECPublicKeyParameters(point, SECP256K1.params())
            signer.init(false, params)
            return (signer.verifySignature(hash, decodedsig.r, decodedsig.s))
        } catch (e : Throwable) {
            return false
        }
    }

    fun validatePublicKey(pubkey: String, address: String): Boolean {
        try {
            return (getAddressfromPubKey(Base64.decode(pubkey)) == address)
        } catch (e: Throwable) {
            return false
        }
    }

}