package im.ananse.payments.model

import com.ripple.crypto.ecdsa.Seed
import im.ananse.gateway.wallet.VPay365Wallet
import io.realm.RealmObject

/**
 * Created by sena on 26/08/2017.
 */
open class Profile(): RealmObject() {

    var wallet: Wallet? = Wallet()
    var pushToken: String? = null
    var pushTokenTimestamp: Long = 0
    var seed: String? = null
    var phone: String? = null
    var cnyBalance: Balance? = Balance("CNY", 0.0)
    var btcBalance: Balance? = Balance("BTC", 0.0)
    var xrpBalance: Balance? = Balance("XRP", 0.0)
    var lastSyncedLedger: Int? = null

    var btcTopupAddress: String? = null // model 1

    fun getVPay365Wallet(): VPay365Wallet {
        return VPay365Wallet(Seed.fromBase58(seed).toString())
    }

}