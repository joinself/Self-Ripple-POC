package im.ananse.payments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.ananse.payments.model.VPayTransaction
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import org.jetbrains.anko.support.v4.intentFor


/**
 * A placeholder fragment containing a simple view.
 */
class TransactionListFragment : Fragment() {

    lateinit var activity: MainActivity
    lateinit var realm: Realm
    lateinit var transactions: RealmResults<VPayTransaction>
    lateinit var recyclerAdapter: RealmRecyclerViewAdapter<VPayTransaction, TransactionViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = getActivity() as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.fragment_transaction_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
        transactions = realm.where(VPayTransaction::class.java).sort("timestamp", Sort.DESCENDING).findAll()

        transactionRecyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerAdapter = TransactionRecyclerViewAdapter(transactions)
        transactionRecyclerView.adapter = recyclerAdapter

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        activity.showFam()

        activity.startService(intentFor<IntentService>().setAction(action_ripple_get_transaction_data))

    }

    override fun onPause() {

        realm.close()

        super.onPause()
    }
}