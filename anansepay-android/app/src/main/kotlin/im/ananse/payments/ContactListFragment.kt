package im.ananse.payments

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import im.ananse.payments.model.Contact
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_contact_list.*
import org.jetbrains.anko.support.v4.intentFor

class ContactListFragment : Fragment() {

    lateinit var activity: MainActivity
    lateinit var realm: Realm
    lateinit var contacts: RealmResults<Contact>
    lateinit var recyclerAdapter: RealmRecyclerViewAdapter<Contact, ContactViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = getActivity() as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {


        return inflater.inflate(R.layout.fragment_contact_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactListRecyclerView.addOnItemTouchListener(RecyclerItemClickListener(activity, contactListRecyclerView, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                //handle click events here
                val contact = recyclerAdapter.getItem(position)
                startActivity(intentFor<MainActivity>().setAction(ACTION_TRANSFER).setData(Uri.parse("https://www.ananse.im/c?a=${contact?.address}&n=${contact?.name}")))
            }

            override fun onItemLongClick(view: View, position: Int) {
                //handle longClick if any
            }
        }));

        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_contact, menu)
    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
        contacts = realm.where(Contact::class.java).sort("name", Sort.ASCENDING).findAll()

        contactListRecyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerAdapter = ContactRecyclerViewAdapter(contacts)
        contactListRecyclerView.adapter = recyclerAdapter

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.hideFam()

    }

    override fun onPause() {

        realm.close()

        super.onPause()


    }
}