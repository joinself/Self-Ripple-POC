package im.ananse.payments.model

import im.ananse.payments.BuildConfig
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

/**
 * Created by sena on 03/09/2017.
 */

open class VPayTransaction(): RealmObject(){

    @Required lateinit var account: String
    @Required lateinit var destinationAccount: String
    @Required @PrimaryKey lateinit var txHash: String
    @Index var timestamp: Long = 0
    @Required lateinit var amount: String
    @Required lateinit var currencyCode: String
    @Required lateinit var transactionType: String
    var validated: Boolean = false
//    @Required var txId: Long? = null

    constructor(currentUserAddrress: String, account: String, destinationAccount: String, txHash: String, timestamp: Long, amount: String, currencyCode: String, validated: Boolean):this() {
        this.account = account
        this.destinationAccount = destinationAccount
        this.txHash = txHash
//        this.txId = BigInteger(txHash, 16).toLong()
        this.timestamp = timestamp
        this.amount = amount
        this.currencyCode = currencyCode
        this.validated = validated

        initType(currentUserAddrress)
    }

    /*
    1. The client only shows payments and ignores everything else
    2. Topups are identified as topups because they come fromthe Hot Wallet
        for a currency though they are just normal payments. (edited)
    3. Sent Payments between users come from the user’s own Ripple Address to another user address
    4. Received Payments come from a sending user’s address to the receiving user’s address.
        Or to the users own ripple address for incoming payment
    5   Withdrawals are to the issuing address
     */
    fun initType(currentUserAddrress: String) {
        // Setting Type
        if (destinationAccount.equals(currentUserAddrress)) {
            if (account.equals(BuildConfig.RIPPLE_HOT_WALLET_ADDRESS)) {
                transactionType = Type.Topup.toString()
            } else {
                transactionType = Type.Incoming.toString()
            }
        } else {
            if (destinationAccount.equals(BuildConfig.CNY_ISSUING_WALLET_ADDRESS) or destinationAccount.equals(BuildConfig.BTC_ISSUING_WALLET_ADDRESS)) {
                transactionType = Type.Withdrawal.toString()
            } else {
                transactionType = Type.Outgoing.toString()
            }
        }
    }
}

enum class Type {
    Topup,
    Outgoing,
    Incoming,
    Withdrawal
}