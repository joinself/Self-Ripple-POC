package im.ananse.payments

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import im.ananse.payments.model.Contact
import im.ananse.payments.model.Type
import im.ananse.payments.model.VPayTransaction
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import org.jetbrains.anko.textColor
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by sena on 05/09/2017.
 */

val rippleEpoch = BigInteger.valueOf(946684800L * 1000)
val dateFormat = SimpleDateFormat("dd-MM-yyyy")

fun getUnixTimeFromRippleTime(rippleTime: Long): Date {

    val rippleInt = BigInteger.valueOf(rippleTime) * BigInteger.valueOf(1000)
    val unixTime = rippleEpoch + rippleInt

    val unixDate = Date(unixTime.toLong())

    return unixDate
}

class TransactionRecyclerViewAdapter : RealmRecyclerViewAdapter<VPayTransaction, TransactionViewHolder> {

    val TAG = "RecyclerViewAdapter"

    constructor(data: OrderedRealmCollection<VPayTransaction>): super(data, true) {
        setHasStableIds(false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)

        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {

        val realm = Realm.getDefaultInstance()

        val transaction = getItem(position)
        holder.data = transaction!!
        val resources = holder.itemView.context.resources

        var contact: Contact? = null

        when (transaction.transactionType) {
            Type.Topup.toString() -> {
                if(transaction.validated) {
                    holder.description.text = resources.getText(R.string.topup)
                } else {
                    holder.description.text = resources.getText(R.string.unvalidated_topup)
                }
                holder.name.text = ""
                holder.amount.text = holder.data.amount
                holder.amount.textColor = resources.getColor(R.color.green_vpay)
                holder.currencyCode.text = holder.data.currencyCode
                holder.currencyCode.textColor = resources.getColor(R.color.green_vpay)
            }
            Type.Incoming.toString() -> {
                if (transaction.validated) {
                    holder.description.text = resources.getText(R.string.credit)
                } else {
                    holder.description.text = resources.getText(R.string.unvalidated_credit)
                }
                contact = realm.where(Contact::class.java).equalTo("address", holder.data.account).findFirst()
                Log.i(TAG, "contacts: ${contact} ${contact}")
                holder.name.text = contact?.name ?: holder.data.account
                holder.name.textColor = resources.getColor(android.R.color.black)
                holder.amount.text = holder.data.amount
                holder.amount.textColor = resources.getColor(R.color.green_vpay)
                holder.currencyCode.text = holder.data.currencyCode
                holder.currencyCode.textColor = resources.getColor(R.color.green_vpay)
            }

            Type.Outgoing.toString() -> {
                if (transaction.validated) {
                    holder.description.text = resources.getText(R.string.debit)
                } else {
                    holder.description.text = resources.getText(R.string.unvalidated_debit)
                }
                contact = realm.where(Contact::class.java).equalTo("address", holder.data.destinationAccount).findFirst()
                Log.i(TAG, "contacts: ${contact}")
                holder.name.text = contact?.name ?: holder.data.destinationAccount
                holder.name.textColor = resources.getColor(android.R.color.black)
                holder.amount.text = holder.data.amount
                holder.amount.textColor = resources.getColor(R.color.maroon_vpay)
                holder.currencyCode.text = holder.data.currencyCode
                holder.currencyCode.textColor = resources.getColor(R.color.maroon_vpay)
            }
            Type.Withdrawal.toString() -> {
                if (transaction.validated) {
                    holder.description.text = resources.getText(R.string.withdrawal)
                } else {
                    holder.description.text = resources.getText(R.string.unvalidated_withdrawal)
                }
                holder.name.text = "" //contact?.name ?: holder.data.destinationAccount
                holder.name.textColor = resources.getColor(R.color.maroon_vpay)
                holder.amount.text = holder.data.amount
                holder.amount.textColor = resources.getColor(R.color.maroon_vpay)
                holder.currencyCode.text = holder.data.currencyCode
                holder.currencyCode.textColor = resources.getColor(R.color.maroon_vpay)
            }
        }

        holder.date.text = dateFormat.format(getUnixTimeFromRippleTime(holder.data.timestamp))
        holder.amount.text = holder.data.amount

        realm.close()
    }

}