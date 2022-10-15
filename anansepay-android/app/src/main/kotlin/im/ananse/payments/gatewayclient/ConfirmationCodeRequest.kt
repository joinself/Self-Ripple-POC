package im.ananse.payments.gatewayclient

/**
 * Created by sena on 30/08/2017.
 */
open class ConfirmationCodeRequest() {

    lateinit var address: String
    lateinit var signature: String

}