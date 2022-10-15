package im.ananse.payments

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import im.ananse.payments.model.VPayTransaction

/**
 * Created by sena on 05/09/2017.
 */
class TransactionViewHolder : RecyclerView.ViewHolder{

    constructor(view: View): super(view) {
        description = view.findViewById(R.id.descriptionTextField)
        name = view.findViewById(R.id.nameTextField)
        amount = view.findViewById(R.id.amountTextField)
        currencyCode = view.findViewById(R.id.currencyCode)
        date = view.findViewById(R.id.dateTextField)
    }

    var description: TextView
    var name: TextView
    var amount: TextView
    var currencyCode: TextView
    var date: TextView

    lateinit var data: VPayTransaction


}