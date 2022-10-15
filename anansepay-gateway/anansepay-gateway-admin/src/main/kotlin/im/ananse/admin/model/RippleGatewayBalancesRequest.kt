package im.ananse.admin.model

/**
 * Created by sena on 02/09/2017.
 */
class RippleGatewayBalancesRequest(account: String, hotwallet: String, standbyWallet: String) {

    val method: String = "gateway_balances"
    val params = ArrayList<Any>()

    init {
        params.add(RippleGatewayBalancesParams(account, listOf(hotwallet, standbyWallet)))
    }
}

data class RippleGatewayBalancesParams(val account: String, val hotwallet: List<String>)