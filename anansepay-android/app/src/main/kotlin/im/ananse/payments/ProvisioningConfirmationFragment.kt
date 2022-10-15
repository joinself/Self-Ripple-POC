package im.ananse.payments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_provision_confirm.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * A placeholder fragment containing a simple view.
 */
class ProvisioningConfirmationFragment : Fragment(), AnkoLogger {

    lateinit var activity: ProvisioningActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        activity = getActivity() as ProvisioningActivity

        return inflater.inflate(R.layout.fragment_provision_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonResend.setOnClickListener {
            it.isEnabled = false
            activity.provisioningRequest(it, activity.profile!!.wallet!!, activity.profile.toString())
        }

        confirmEditText.setOnEditorActionListener { textView, eventId, keyEvent ->

            when (eventId) {
                EditorInfo.IME_ACTION_DONE -> {
                    activity.confirmRequest(textView, textView.text)
                }
                else -> {
                    info("Unhandled IME_ACTION")
                }
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()

        confirmEditText.requestFocus()

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}