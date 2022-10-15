package im.ananse.payments.model

import io.realm.RealmObject

/**
 * Created by sena on 27/08/2017.
 */
open class Wallet() : RealmObject() {

    var address: String? = null
    var publicKey: String? = null
    var privateKey: String? = null
    var destinationTag: String? = null

}