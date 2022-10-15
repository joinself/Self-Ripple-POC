package im.ananse.payments.model

import io.realm.RealmObject

/**
 * Created by sena on 13/09/2017.
 */
open class ServerInfo: RealmObject() {
    var fee: Long = 3000
}