package im.ananse.payments.rippleclient

/**
 * Created by sena on 11/09/2017.
 */
open class RippleAccountLinesRequest(account: String) {

    val method = "account_lines"
    val params = ArrayList<RippleAccountLinesParams>()

    init {
        params.add(RippleAccountLinesParams(account))
    }

}

data class RippleAccountLinesParams(val account:String, val ledger: String = "current")