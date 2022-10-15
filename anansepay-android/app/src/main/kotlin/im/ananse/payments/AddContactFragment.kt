package im.ananse.payments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fasterxml.jackson.databind.ObjectMapper
import im.ananse.payments.model.Contact
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_add_contact.*
import org.jetbrains.anko.AnkoLogger

val ACTION_ADD_CONTACT = "actionAddContact"

class AddContactFragment: Fragment(), AnkoLogger, TextWatcher {

    val TAG = "AddContactFragment"

    lateinit var activity: MainActivity
    lateinit var mapper: ObjectMapper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        activity = getActivity() as MainActivity

        return inflater.inflate(R.layout.fragment_add_contact, container, false)

    }

    fun validate(): Boolean {

        if (recipientAutoCompleteEditTextField.text.isNullOrBlank()) {
            return false
        } else {
            return true
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipientAutoCompleteEditTextField.addTextChangedListener(this)

        saveButton.setOnClickListener {

            val threadRealm = Realm.getDefaultInstance()

            val contact = Contact()
            contact.address = recipientAutoCompleteEditTextField.text.toString()
            if (!nameAutoCompleteEditTextField.text.isNullOrBlank()) {
                contact.name = nameAutoCompleteEditTextField.text.toString()
            }

            threadRealm.beginTransaction()
            threadRealm.copyToRealmOrUpdate(contact)
            threadRealm.commitTransaction()
            threadRealm.close()

            activity.supportFragmentManager.popBackStack()
        }

    }

    override fun onResume() {
        super.onResume()

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.hideFam()

    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun afterTextChanged(p0: Editable?) {
        saveButton.isEnabled = validate()
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}