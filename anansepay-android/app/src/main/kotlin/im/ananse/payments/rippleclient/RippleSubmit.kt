
package im.ananse.payments.rippleclient

/**
 * Created by sena on 02/09/2017.
 */
class RippleSubmitRequest(txBlob:String) {

    val method: String = "submit"
    val params = ArrayList<Any>()

    init {
        params.add(RippleTxBlob(txBlob))
    }

}

data class RippleTxBlob(val tx_blob: String)