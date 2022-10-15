package im.ananse.payments

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import im.ananse.payments.model.ExchangeRate

/**
 * Created by sena on 05/09/2017.
 */
class ExchangeRateViewHolder : RecyclerView.ViewHolder {

    constructor(view: View): super(view) {
        currency = view.findViewById(R.id.currencyNameTextField)
        cnyRate = view.findViewById(R.id.cnyRateTextField)
        mBtcRate = view.findViewById(R.id.mBtcRateTextField)
        currencyCode = view.findViewById(R.id.currencyTextField)
    }

    var currency: TextView
    var cnyRate: TextView
    var mBtcRate: TextView
    var currencyCode: TextView

    lateinit var data: ExchangeRate


}