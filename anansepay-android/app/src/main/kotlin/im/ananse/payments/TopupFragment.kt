package im.ananse.payments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RadioButton
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper
import im.ananse.payments.network.RequestQueueSingleton
import im.ananse.gateway.wallet.FeelPayTopupRequest
import im.ananse.gateway.wallet.VPay365Wallet
import io.realm.Realm
import io.realm.RealmChangeListener
import kotlinx.android.synthetic.main.fragment_topup.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast
import org.json.JSONObject


class TopupFragment : Fragment(), AnkoLogger {

    lateinit var activity: MainActivity
    var currencyCode: String = "CNY"
    lateinit var clipBoardMananger: ClipboardManager

    val realmListener = object : RealmChangeListener<Realm> {
        override fun onChange(realm: Realm) {
            topupButton.setText(activity.profile?.btcTopupAddress)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        activity = getActivity() as MainActivity

        return inflater.inflate(R.layout.fragment_topup, container, false)

        activity.realm.addChangeListener(realmListener)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radio_btc.onClick { view ->

            onRadioButtonClicked(view!!)
        }

        radio_cny.onClick { view ->

            onRadioButtonClicked(view!!)
        }

        topupButton.onClick { view ->
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.getWindowToken(), 0)
            postTopupRequest(activity, amountEditTextField.text.toString(), view!!)
        }

        btcTopUpAddressButton.setOnClickListener { view ->
            clipBoardMananger.primaryClip = ClipData.newPlainText("Bitcoin Address", activity.profile?.btcTopupAddress)
            Snackbar.make(view, R.string.topup_copied_to_clipboard, Snackbar.LENGTH_SHORT).show()
        }

        clipBoardMananger = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onResume() {
        super.onResume()

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.hideFam()

        if (activity.profile?.btcTopupAddress != null && radio_btc.isChecked) {
//            activity.startService(intentFor<IntentService>().setAction(action_register_btc_address).setData(Uri.parse(registeredBtcAddress)))

//            registerBtcAddress(registeredBtcAddress!!)

            btcIntructionsTextField.setText(R.string.send_bitcoin_instruction)
            btcTopUpAddressButton.setText(activity.profile?.btcTopupAddress)
            btcTopUpAddressButton.visibility = View.VISIBLE
            btcTopUpAddressButton.setTransformationMethod(null);
        } else {
            btcIntructionsTextField.setText(R.string.btc_transfer_text)
            btcTopUpAddressButton.visibility = View.GONE
        }

        info("Registered btc address : ${activity.profile?.btcTopupAddress}")
    }

    fun onRadioButtonClicked(view: View) {
        // Is the button now checked?
        val checked = (view as RadioButton).isChecked

        // Check which radio button was clicked
        when (view.getId()) {
            R.id.radio_cny-> {
                if (checked) {
                    currencyCode = "CNY"
                    toast("cny")
                    amountEditTextField.visibility = View.VISIBLE
                    btcIntructionsTextField.visibility = View.GONE
                }
            }
            R.id.radio_btc -> {
                if (checked) {
                    currencyCode = "BTC"
                    toast("btc")
                    amountEditTextField.visibility = View.GONE
                    btcIntructionsTextField.visibility = View.VISIBLE
                }
            }
        }
    }

    fun postTopupRequest(context: Context, amount: String, view: View) {

        val vPay365Wallet = VPay365Wallet(activity.profile!!.seed!!)
        val messageToSign = activity.profile!!.wallet!!.address + amount + currencyCode
        val signature = vPay365Wallet.signMessage(messageToSign)

        val topupRequestObject = FeelPayTopupRequest(activity.profile!!.wallet!!.address!!, amount.toInt(), currencyCode, signature)

        val mapper = ObjectMapper()
        val requestJson = JSONObject(mapper.writeValueAsString(topupRequestObject))
        info("topupRequestJson\n$requestJson")

        val topupCodeRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.GATEWAY_SERVER_URL}/topup/request", requestJson, object : Response.Listener<JSONObject> {

            override fun onResponse(response: JSONObject) {
                info(response.toString())

                val order = response["order"] as String
                val url = response["url"] as String
                val postData = response["postdata"] as String

                val webSettings = feelpayWebview.settings
                webSettings.javaScriptEnabled = true
                webSettings.mixedContentMode

                feelpayWebview.visibility = View.VISIBLE

                feelpayWebview.webViewClient = object: WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        info(url)
                        if (url.startsWith("anansepay", true)) {
                            startActivity(intentFor<MainActivity>().setData(Uri.parse(url)).singleTop().clearTop().clearTask().newTask())
                            return true
                        } else {
                            return false
                        }
                    }

                }

                feelpayWebview.postUrl(url, postData.toByteArray())

            }
        }, object : Response.ErrorListener {

            override fun onErrorResponse(error: VolleyError) {
                Snackbar.make(view!!, R.string.error_problem_topup, Snackbar.LENGTH_LONG).show()
                error(error)
                view.isEnabled = true
            }
        })

        RequestQueueSingleton.getInstance(activity.applicationContext).addToRequestQueue(topupCodeRequest)
        topupButton.isEnabled = false
    }

    override fun onPause() {
        super.onPause()

        activity.realm.removeChangeListener(realmListener)
    }

}