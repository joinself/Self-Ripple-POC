package im.ananse.payments.rippleclient

/**
 * Created by sena on 02/09/2017.
 */
class RippleAccountInfoRequest(account: String) {

    val method: String = "account_info"
    val params = ArrayList<Any>()

    init {
        params.add(RippleAccountInfoParams(account))
    }
}

data class RippleAccountInfoParams(val account: String)