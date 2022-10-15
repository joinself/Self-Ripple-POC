package im.ananse.payments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.AnkoLogger

class ProfileFragment : Fragment(), AnkoLogger {

    lateinit var activity: MainActivity
    lateinit var clipBoardMananger: ClipboardManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        activity = getActivity() as MainActivity

        return inflater.inflate(R.layout.fragment_profile, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        copyAddressButton.setText(activity.profile?.wallet?.address)
        copyAddressButton.setOnClickListener { view ->
            clipBoardMananger.primaryClip = ClipData.newPlainText("Vpay365 Address", activity.profile?.wallet?.address)
            Snackbar.make(view, "Top-Up Address copied to clipboard", Snackbar.LENGTH_SHORT).show()
        }

        clipBoardMananger = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    }

    override fun onResume() {
        super.onResume()

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.hideFam()

    }

}