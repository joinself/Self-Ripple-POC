package im.ananse.payments.model

import io.realm.RealmObject

/**
 * Created by sena on 28/08/2017.
 */
open class BankDetails(): RealmObject() {

    var name: String = ""
    var address: String = ""
    var bank: String = ""
    var accountNo: String = ""

}