package im.ananse.payments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper
import im.ananse.payments.model.Profile
import im.ananse.payments.network.RequestQueueSingleton
import im.ananse.payments.rippleclient.RippleAccountInfoRequest
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_withdrawal.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.json.JSONObject
import java.math.BigDecimal

class WithdrawalFragment : Fragment(), AnkoLogger, TextWatcher {

    lateinit var activity: MainActivity
    var currencyCode: String = "BTC"
    lateinit var profile: Profile
    lateinit var realm: Realm
    lateinit var mapper: ObjectMapper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        activity = getActivity() as MainActivity
        realm = Realm.getDefaultInstance()
        profile = realm.where(Profile::class.java).findFirst() as Profile
        mapper = ObjectMapper()

        return inflater.inflate(R.layout.fragment_withdrawal, container, false)

    }

    fun validate(): Boolean {

        if (amountEditTextField.text.isNullOrBlank() or
                bankEditTextField.text.isNullOrBlank() or
                accountNoEditTextField.text.isNullOrBlank() or
                accountHolderEditTextField.text.isNullOrBlank()) {
            return false
        } else {
            return true
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radio_btc.setOnClickListener { radioButton ->

            onRadioButtonClicked(radioButton!!)
        }

        radio_cny.setOnClickListener { radioButton ->

            onRadioButtonClicked(radioButton!!)
        }

        withdrawButton.setOnClickListener { button ->
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(button.getWindowToken(), 0)
            val amount = BigDecimal(amountEditTextField.text.toString())

            val accountInfoRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.RIPPLE_SERVER_URL}/", JSONObject(mapper.writeValueAsString(RippleAccountInfoRequest(profile.wallet?.address!!))), object : Response.Listener<JSONObject> {

                override fun onResponse(response: JSONObject) {

                    debug("Ripple Account Info\n${response.toString()}")
                    val result = response.getJSONObject("result")
                    val status = result.getString("status")
                    if (status.equals("success")) {
                        val accountData = result.getJSONObject("account_data")
                        val sequence = accountData.getInt("Sequence")
                        info("account_data sequence : $sequence")

                        getRippleServerFee(activity.applicationContext, {
                            postTransferRequest(currencyCode, getcurrencyIssuingAddress(currencyCode), amount, sequence, withdrawButton, true, bankEditTextField.text.toString(), accountHolderEditTextField.text.toString(), accountNoEditTextField.text.toString())
                        })

                    } else {
                        Log.w(javaClass.name, "Problem getting Ripple server_info")
                    }

                }
            }, object : Response.ErrorListener {

                override fun onErrorResponse(error: VolleyError) {
                    error("Problem geting Ripple account_info\n${error}")
                }
            })

            accountInfoRequest.retryPolicy = DefaultRetryPolicy(30000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            RequestQueueSingleton.getInstance(activity.applicationContext).addToRequestQueue(accountInfoRequest)

        }

        bankEditTextField.setText(R.string.bitcoin)
        bankEditTextField.visibility = View.GONE

        accountNoEditTextField.setHint(R.string.btc_account)
        accountNoEditTextField.visibility = View.VISIBLE

        accountHolderEditTextField.setText("na")
        accountHolderEditTextField.visibility = View.GONE

        amountEditTextField.addTextChangedListener(this)
        bankEditTextField.addTextChangedListener(this)
        accountNoEditTextField.addTextChangedListener(this)
        accountHolderEditTextField.addTextChangedListener(this)

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

                    bankEditTextField.setText("")
                }
            }
            R.id.radio_btc -> {
                if (checked) {
                    currencyCode = "BTC"
                    info("BTC")

                    bankEditTextField.setText(R.string.bitcoin)
                    bankEditTextField.visibility = View.GONE

                    accountNoEditTextField.setHint(R.string.btc_account)
                    accountNoEditTextField.visibility = View.VISIBLE

                    accountHolderEditTextField.setText("na")
                    accountHolderEditTextField.visibility = View.GONE
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun afterTextChanged(p0: Editable?) {
        withdrawButton.isEnabled = validate()
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}