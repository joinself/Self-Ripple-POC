package im.ananse.payments.rippleclient

/**
 * Created by sena on 11/09/2017.
 */
open class RippleAccountTxRequest(account: String, ledger_index_min: Int, ledger_index_max: Int) {

    val method = "account_tx"
    val params = ArrayList<RippleAccountTxParams>()

    init {
        params.add(RippleAccountTxParams(account, ledger_index_min, ledger_index_max))
    }

}

data class RippleAccountTxParams(val account:String, val ledger_index_min:Int, val ledger_index_max: Int)