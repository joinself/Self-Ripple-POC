package im.ananse.payments.model

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

/**
 * Created by sena on 28/08/2017.
 */
open class Contact(): RealmObject() {

    @PrimaryKey @Required var address: String = ""
    @Index var name: String = ""

}