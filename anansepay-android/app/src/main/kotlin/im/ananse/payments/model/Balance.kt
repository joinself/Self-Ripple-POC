package im.ananse.payments.model

import io.realm.RealmObject

/**
 * Created by sena on 01/09/2017.
 */
open class Balance(): RealmObject() {

    var currencyCode: String? = null
    var amount: Double = 0.0
    var hasTrustLine: Boolean = false

    constructor(currencyCode: String, amount: Double): this() {

        this.currencyCode = currencyCode
        this.amount = amount

    }
}