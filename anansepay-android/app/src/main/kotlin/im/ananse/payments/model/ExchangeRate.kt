package im.ananse.payments.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.json.JSONObject

/**
 * Created by sena on 26/08/2017.
 */
open class ExchangeRate(): RealmObject() {

    @PrimaryKey @Required lateinit var currencyCode: String
    @Required lateinit var rates: String

    fun getRatesJsonObject(): JSONObject {
        return JSONObject(rates)
    }

}