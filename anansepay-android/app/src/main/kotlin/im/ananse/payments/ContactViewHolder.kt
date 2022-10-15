package im.ananse.payments

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import im.ananse.payments.model.Contact

/**
 * Created by sena on 05/09/2017.
 */
class ContactViewHolder : RecyclerView.ViewHolder {

    constructor(view: View): super(view) {
        name = view.findViewById(R.id.nameTextField)
        address= view.findViewById(R.id.addressTextField)
    }

    var name: TextView
    var address: TextView

    lateinit var data: Contact

}