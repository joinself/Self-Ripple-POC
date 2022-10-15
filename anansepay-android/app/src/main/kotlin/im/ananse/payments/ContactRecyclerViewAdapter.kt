package im.ananse.payments

import android.view.LayoutInflater
import android.view.ViewGroup
import im.ananse.payments.model.Contact
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

/**
 * Created by sena on 05/09/2017.
 */

class ContactRecyclerViewAdapter: RealmRecyclerViewAdapter<Contact, ContactViewHolder> {

    val TAG = "CRecyclerViewAdapter"

    constructor(data: OrderedRealmCollection<Contact>): super(data, true) {

        setHasStableIds(false)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder{

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)

        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {

        val contact = getItem(position)
        holder.data = contact!!


        holder.address.text = contact.address
        if (!contact.name.isNullOrBlank()) {
            holder.name.text = contact.name
        } else {
            holder.name.text = ""
        }
    }

}