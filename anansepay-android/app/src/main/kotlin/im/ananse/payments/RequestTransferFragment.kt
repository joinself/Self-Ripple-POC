package im.ananse.payments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import com.fasterxml.jackson.databind.ObjectMapper
import im.ananse.payments.model.Profile
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_request_transfer.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk25.coroutines.onClick




class RequestTransferFragment: Fragment(), AnkoLogger, TextWatcher {

    val TAG = "RTransferFragment"

    lateinit var activity: MainActivity
    var currencyCode: String = "BTC"
    lateinit var profile: Profile
    lateinit var realm: Realm
    lateinit var mapper: ObjectMapper
    lateinit var barCodeText: String
    lateinit var barcodeBitmap: Bitmap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        activity = getActivity() as MainActivity
        realm = Realm.getDefaultInstance()
        profile = realm.where(Profile::class.java).findFirst()!!
        mapper = ObjectMapper()

        realm = Realm.getDefaultInstance()

        return inflater.inflate(R.layout.fragment_request_transfer, container, false)

//        activity.realm.addChangeListener(realmListener)

    }

    fun validate(): Boolean {

        if (amountEditTextField.text.isNullOrBlank()) {
            return false
        } else {
            return true
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO
        radio_btc.onClick { view ->

            onRadioButtonClicked(view!!)
        }
        // TODO
        radio_cny.onClick { view ->

            onRadioButtonClicked(view!!)
        }
        // TODO

        shareButton.onClick { view ->
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.getWindowToken(), 0)

            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, barCodeText)
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));

            info("Outgoing Share")

        }

        amountEditTextField.addTextChangedListener(this)
        amountEditTextField.setOnEditorActionListener { textView, eventId, keyEvent ->

            when (eventId) {
                EditorInfo.IME_ACTION_DONE -> {
                    validate()
                    updateQRcode()
                }
                else -> {
                    info("Unhandled IME_ACTION")
                }
            }
            false
        }
    }

    fun updateQRcode() {

        barCodeText  = "https://www.ananse.im/p?a=${profile.wallet!!.address}&v=${amountEditTextField.text}&m=${currencyCode}" // TODO handle CNY
        barcodeBitmap = QRCode.from(barCodeText).withSize(250, 250).bitmap();
        barcodeImageView.setImageBitmap(barcodeBitmap)

    }

    override fun onResume() {
        super.onResume()

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.hideFam()

    }

    fun onRadioButtonClicked(view: View) {
        // Is the button now checked?
        val checked = (view as RadioButton).isChecked

        // Check which radio button was clicked
        when (view.getId()) {
            R.id.radio_cny -> {
                if (checked) {
                    currencyCode = "CNY"
                    info("CNY")
                }
            }
            R.id.radio_btc -> {
                if (checked) {
                    currencyCode = "BTC"
                    info("BTC")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {

        realm.close()

        super.onDestroy()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun afterTextChanged(p0: Editable?) {
        shareButton.isEnabled = validate()
        updateQRcode()

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}