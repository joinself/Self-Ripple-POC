package im.ananse.payments.gatewayclient

/**
 * Created by sena on 30/08/2017.
 */
open class ProvisioningRequest() {

    lateinit var address: String
    lateinit var telnumber: String
    var timestamp: Long = 0
    lateinit var pubkey: String
    lateinit var signature: String

}