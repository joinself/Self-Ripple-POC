package im.ananse.payments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import com.ripple.core.coretypes.Amount
import com.ripple.encodings.base58.EncodingFormatException
import com.ripple.ripplelibpp.*
import com.ripple.ripplelibpp.STTx
import im.ananse.payments.model.Contact
import im.ananse.payments.model.Profile
import im.ananse.payments.model.ServerInfo
import im.ananse.payments.network.RequestQueueSingleton
import im.ananse.payments.rippleclient.RippleAccountInfoRequest
import im.ananse.payments.rippleclient.RippleSubmitRequest
import im.ananse.gateway.wallet.VPay365Wallet
import im.ananse.gateway.wallet.WithdrawalRequest
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_transfer.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger

val ACTION_TRANSFER = "actionTransfer"

fun calculateExponent(value: BigDecimal): Int {
        return -Amount.MAXIMUM_IOU_PRECISION + value.precision() - value.scale();
    }

fun bigIntegerIOUMantissa(value: BigDecimal): BigInteger {
    return exactBigIntegerScaledByPowerOfTen(-calculateExponent(value), value).abs();
}

// fun bigIntegerDrops(value: BigDecimal): BigInteger {
//     return exactBigIntegerScaledByPowerOfTen(MAXIMUM_NATIVE_SCALE, value);
// }

fun exactBigIntegerScaledByPowerOfTen(n: Int, value: BigDecimal): BigInteger {
    return value.scaleByPowerOfTen(n).toBigIntegerExact();
}

val postTransferRequest = { currencyCode: String, destinationAddress: String, amount: BigDecimal, sequence: Int, view: View, isWithdrawal: Boolean, bankName: String?, accountHolder: String?, accountNumber: String? ->

    val transferRequestJson = createTransferRequest(currencyCode, destinationAddress, amount, sequence, isWithdrawal)

    val transferRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.RIPPLE_SERVER_URL}/", JSONObject(transferRequestJson), object : Response.Listener<JSONObject> {

        override fun onResponse(response: JSONObject) {
            Log.i("TransferFragment", "transferRequestResponse\n${response.toString()}")

            if (isWithdrawal) {

                val threadRealm = Realm.getDefaultInstance()
                val profile = threadRealm.where(Profile::class.java).findFirst()

                val wallet = VPay365Wallet(profile?.seed!!)
                val amount = amount.toFloat()
                val currency = currencyCode
                val bankname = bankName
                val accountnumber = accountNumber
                val accountholder = accountHolder
                val txhash = response.getJSONObject("result").getJSONObject("tx_json").getString("hash")

                val messagetosign = wallet.address+amount.toString()+currency+bankname+accountnumber+accountholder+txhash
                val signature = wallet.signMessage(messagetosign)

                val withdrawalRegistrationRequestObject = WithdrawalRequest (
                        address = wallet.address,
                        amount = amount,
                        currency = currency,
                        bankname = bankname!!,
                        accountnumber = accountnumber!!,
                        accountholder = accountholder!!,
                        txhash = txhash,
                        signature = signature
                )

                val withdrawalRegistrationRequestJson = ObjectMapper().writeValueAsString(withdrawalRegistrationRequestObject)

                val withdrawalRegistrationRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.GATEWAY_SERVER_URL}/provisioning/withdrawal", JSONObject(withdrawalRegistrationRequestJson), object : Response.Listener<JSONObject> {

                    override fun onResponse(response: JSONObject) {

                        Log.i("TransferFragment", "Withdrawal registered\n${response.toString()}")

                        val threadRealm2 = Realm.getDefaultInstance()

                        threadRealm2.beginTransaction()
//                        threadProfile?.wallet?.destinationTag = response.getString("dtag")
                        threadRealm2.commitTransaction()
                        threadRealm2.close()

                    }
                }, object : Response.ErrorListener {

                    override fun onErrorResponse(error: VolleyError) {
                        error("Problem Registering Withdrawal with Gateway\n${error}")
                    }
                })

                threadRealm.close()

                withdrawalRegistrationRequest.retryPolicy = DefaultRetryPolicy(30000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                RequestQueueSingleton.getInstance(view.context.applicationContext).addToRequestQueue(withdrawalRegistrationRequest)
            }

            Snackbar.make(view, R.string.request_submitted, Snackbar.LENGTH_LONG).show()
        }
    }, object : Response.ErrorListener {

        override fun onErrorResponse(error: VolleyError) {
            Snackbar.make(view, R.string.error_submitting_transaction, Snackbar.LENGTH_LONG).show()
            error(error)
        }
    })

    transferRequest.retryPolicy = DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    RequestQueueSingleton.getInstance(view.context.applicationContext).addToRequestQueue(transferRequest)

    val intent = Intent(view.context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    view.context.startActivity(intent)

//    startActivity(intentFor<MainActivity>().singleTop().clearTop().clearTask().newTask()) // TODO Should happen on response?
}

fun createTransferRequest(currencyCode: String, destinationAddress: String, amount: BigDecimal, sequence: Int, isWithdrawal: Boolean):String {

    val realm = Realm.getDefaultInstance()
    val profile = realm.where(Profile::class.java).findFirst()
    val wallet = profile?.wallet
    val destinationTag = wallet?.destinationTag

    val seed = Seed.parseBase58(profile?.seed)
    val keypair = SecretKey.generateKeyPair(KeyType.SECP256K1, seed)

    val originatingAccountID = AccountID.parseBase58(profile?.wallet!!.address)
    val destinationAccountId = AccountID.parseBase58(destinationAddress)

    val exponent = calculateExponent(amount)
    val mantissa = bigIntegerIOUMantissa(amount)

    Log.i("TransferFragment", "mantissa : $mantissa\nexponent : $exponent")

    val serverInfo = realm.where(ServerInfo::class.java).findFirst()

//        val feeExponent = calculateExponent(BigDecimal(serverInfo.fee!!))
//        val feeMantissa = bigIntegerIOUMantissa(BigDecimal.valueOf(serverInfo.fee!!))
    val sfFee = STAmount(serverInfo!!.fee)
    Log.i("TransferFragment","Dynamic Fee : $sfFee")

    var issuerAccountId: AccountID? = null
    when (currencyCode) {
        "CNY" -> {issuerAccountId = AccountID.parseBase58(BuildConfig.CNY_ISSUING_WALLET_ADDRESS)}
        "BTC" -> {issuerAccountId = AccountID.parseBase58(BuildConfig.BTC_ISSUING_WALLET_ADDRESS)}
        else -> throw IllegalArgumentException("Unsupported Currency")
    }

    val sfAmount = STAmount(Issue(Currency.toCurrency(currencyCode), issuerAccountId), mantissa.toLong(), exponent.toLong())
//        val sfLimit = STAmount(Issue(Currency.toCurrency(currencyCode), issuerAccountId), value.unscaledValue().toLong()*2, value.scale().toLong())

    val nextRippleSequenceNumber = sequence
    Log.i("TransferFragment", "Next Sequence number : $nextRippleSequenceNumber")

    val transferTxBuilder = STTx.getBuilder().setTxType(TxType.TT_PAYMENT)
            .setAccount(originatingAccountID)
            .setFee(sfFee)
            .setFlags(TxFlags.tfFullyCanonicalSig)
            .setSequence(nextRippleSequenceNumber)
            .setSigningPublicKey(keypair.first)
            .setAmount(sfAmount)
            .setDestination(destinationAccountId)

    if (isWithdrawal) {
        transferTxBuilder.setDestinationTag(destinationTag?.toLong()!!)
    } else {
        Log.d("TransferFragment", "Not Withdrawal")
    }

    val transferTx = transferTxBuilder.build()

    Log.i("TransferFragment", "Before signing: \n${transferTx.getStyledJsonString(0)}\nSerialized: ${transferTx.getStyledJsonString(0, true)}")

    transferTx.sign(keypair.getFirst(), keypair.getSecond())
    Log.i("TransferFragment", "signed output fullText : \n${transferTx.fullText}")

    val trustLineTxBlob = STTx.serialize(transferTx)

    if (!realm.isClosed) {
        realm.close()
    }

    return ObjectMapper().writeValueAsString(RippleSubmitRequest(trustLineTxBlob))
}

class TransferFragment : Fragment(), AnkoLogger, TextWatcher {

    val TAG = "TransferFragment"

    lateinit var activity: MainActivity
    var currencyCode: String = "BTC"
    lateinit var profile: Profile
    lateinit var realm: Realm
    lateinit var mapper: ObjectMapper
    lateinit var suggestions: List<Contact>
    lateinit var vPayWallet: VPay365Wallet

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        activity = getActivity() as MainActivity

        return inflater.inflate(R.layout.fragment_transfer, container, false)

//        activity.realm.addChangeListener(realmListener)

    }

    fun validate(): Boolean {

        if (recipientAutoCompleteEditTextField.text.isNullOrBlank() or amountEditTextField.text.isNullOrBlank()) {
            return false
        } else {
            return true
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            val address = arguments!!.getString(KEY_ADDRESS)
            address?.let {recipientAutoCompleteEditTextField.setText(address.trim())}
            val name = arguments!!.getString(KEY_NAME)
            name?.let {labelAutoCompleteEditTextField.setText(name.trim())}
            val amount = arguments!!.getString(KEY_AMOUNT)
            amount?.let {amountEditTextField.setText(amount.trim())}
            // TODO CURRENCIES
        }

        radio_btc.setOnClickListener {
            onRadioButtonClicked(view!!)
        }

        radio_cny.setOnClickListener {
            onRadioButtonClicked(view!!)
        }
        // TODO
        transferButton.setOnClickListener {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.getWindowToken(), 0)
            val amount = BigDecimal(amountEditTextField.text.toString().trim())

            try {
                if (!labelAutoCompleteEditTextField.text.isNullOrBlank() and vPayWallet.verifyAddress(recipientAutoCompleteEditTextField.text.toString())) {
                    val contact = Contact()
                    contact.address = recipientAutoCompleteEditTextField.text.toString().trim()
                    contact.name = labelAutoCompleteEditTextField.text.toString().trim()
                    realm.beginTransaction()
                    realm.copyToRealmOrUpdate(contact)
                    realm.commitTransaction()
                } else {
                    Snackbar.make(labelAutoCompleteEditTextField, R.string.invalid_vpay_address, Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: EncodingFormatException) {
                Snackbar.make(labelAutoCompleteEditTextField, R.string.invalid_vpay_address, Snackbar.LENGTH_SHORT).show()
            }


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
                            postTransferRequest(currencyCode, recipientAutoCompleteEditTextField.text.toString().trim(), amount, sequence, transferButton, false, null, null, null)
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
            getRippleServerFee(activity.applicationContext, {
                RequestQueueSingleton.getInstance(activity.applicationContext).addToRequestQueue(accountInfoRequest)
            })

        }

        recipientAutoCompleteEditTextField.addTextChangedListener(this)
        amountEditTextField.addTextChangedListener(this)

        transferButton.isEnabled = validate()

    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
        profile = realm.where(Profile::class.java).findFirst() as Profile
        mapper = ObjectMapper()

        realm = Realm.getDefaultInstance()
        suggestions = realm.where(Contact::class.java).sort("name").findAll()

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.hideFam()

        vPayWallet = VPay365Wallet(profile.seed!!)

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

        realm.close()

        super.onPause()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun afterTextChanged(p0: Editable?) {
        transferButton.isEnabled = validate()

        if (labelAutoCompleteEditTextField.text.isNullOrEmpty()) {
            transferButton.text = resources.getText(R.string.action_transfer)
        } else {
            transferButton.text = resources.getText(R.string.transfer_and_save)
        }
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}