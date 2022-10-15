package im.ananse.payments

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper
import im.ananse.payments.model.Balance
import im.ananse.payments.model.Profile
import im.ananse.payments.model.ServerInfo
import im.ananse.payments.model.VPayTransaction
import im.ananse.payments.network.RequestQueueSingleton
import im.ananse.payments.rippleclient.RippleAccountLinesRequest
import im.ananse.payments.rippleclient.RippleAccountTxRequest
import im.ananse.payments.rippleclient.RippleFeeRequest
import im.ananse.gateway.wallet.BitcoinGenerationRequest
import im.ananse.gateway.wallet.FeelPayPushTokenRequest
import im.ananse.gateway.wallet.VPay365Wallet
import io.realm.Realm
import me.pushy.sdk.Pushy
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.json.JSONObject
import java.util.*


val action_pushreg = "pushReg"
//val action_ripple_gateway_set_trustlines = "gatewaySetTrustLines"
val action_ripple_get_transaction_data = "rippleGetTransactionData"
val action_refresh_trust_lines = "refreshTrustLines"

val OPENEXCHANGE_COMMAND_LATEST="latest.json"

class IntentService: IntentService("IntentService"), AnkoLogger {

    val TAG = "IntentService"

    var mapper = ObjectMapper()

    override fun onHandleIntent(intent: Intent) {

//        var serverInfo = realm.where(ServerInfo::class.java).findFirst() ?: getRippleServerFee(applicationContext, noOperation)

        val extras = intent.extras

        mapper = ObjectMapper()

        when (intent.action) {
            action_pushreg -> {
                Log.i(javaClass.name, action_pushreg)

                val realm = Realm.getDefaultInstance()

                try {
                    val profile = realm.where(Profile::class.java).findFirst()

                    if (profile != null) {
                        realm.beginTransaction()
                        profile.pushToken = Pushy.register(this)
                        profile.pushTokenTimestamp = Date().time
                        realm.commitTransaction()
                        registerPushToken(profile.pushToken!!, profile.seed!!)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during Pushreg")
                    e.printStackTrace()
                } finally {
                    if (!realm.isClosed) {
                        realm.close()
                    }
                }

            }
            action_ripple_get_transaction_data -> {

                Log.i(javaClass.name, action_ripple_get_transaction_data)

                val realm = Realm.getDefaultInstance()
                var profile = realm.where(Profile::class.java).findFirst()

                if (profile != null) {
                    val lastSyncedLedger = profile.lastSyncedLedger
                    if (true) { // TODO optimise
                        info("Profile.lastSyncedLedger is null. Retrieving from Ripple Network")
//                        getRippleServerInfo(true)
//                        getRippleServerState(applicationContext, refreshTransactions())
                        getRippleServerFee(applicationContext, refreshTransactions)
                    }
                } else {
//                    getRippleServerState(applicationContext, null)
                    getRippleServerFee(applicationContext, noOperation)
                }

                realm.close()

            }
            action_refresh_trust_lines -> {

                refreshTrustLines()
                requestTopUpAddress()

            }
            else -> throw IllegalArgumentException("Unsupported ACTION : ${intent.action}")


        }
    }


    val refreshTransactions =  {

        val realm = Realm.getDefaultInstance()

        val profile = realm.where(Profile::class.java).findFirst()
        val currentAddress = profile?.wallet?.address

        var accountTxRequest: RippleAccountTxRequest? = null

        if (true) {
//        if (profile.lastSyncedLedger == null) {
            info("Syncing transactions from beginning of time")
            accountTxRequest = RippleAccountTxRequest(profile?.wallet!!.address!!, -1, -1)
        } else {
            info("Syncing since : ${profile?.lastSyncedLedger}")
            accountTxRequest = RippleAccountTxRequest(profile?.wallet!!.address!!, profile.lastSyncedLedger!!, -1)
        }

        val requestJson = mapper.writeValueAsString(accountTxRequest)
        info("RippleAccountTxRequest\n${requestJson}")

        val refreshTransactionRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.RIPPLE_SERVER_URL}/", JSONObject(requestJson), object : Response.Listener<JSONObject> {

            override fun onResponse(response: JSONObject) {

                info("Transactions : ${response.toString()}")

                val threadRealm = Realm.getDefaultInstance()

                // Persist Transactions Locally
                threadRealm.beginTransaction()
                persistTransactionsLocally(threadRealm, currentAddress!!, response)
                threadRealm.commitTransaction()
                // Update last synced Ledger Value

                threadRealm.close()

            }
        }, object : Response.ErrorListener {

            override fun onErrorResponse(error: VolleyError) {
                error("Problem geting Ripple server_info\n${error}")
            }
        })

        realm.close()

        refreshTransactionRequest.retryPolicy = DefaultRetryPolicy(30000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(refreshTransactionRequest)
    }

    fun refreshTrustLines() {

        val realm = Realm.getDefaultInstance()
        val profile = realm.where(Profile::class.java).findFirst()

        if (profile != null) {
            profile.wallet?.address?.run {

                val trustlineRequestObject = RippleAccountLinesRequest(profile.wallet!!.address!!)
                val trustLinesRequestJson = mapper.writeValueAsString(trustlineRequestObject)
                info("trustLinesRequestJson\n${trustLinesRequestJson}")


                val trustLinesRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.RIPPLE_SERVER_URL}/", JSONObject(trustLinesRequestJson), object : Response.Listener<JSONObject> {

                    override fun onResponse(response: JSONObject) {

                        val localRealm = Realm.getDefaultInstance()
                        val localProfile = localRealm.where(Profile::class.java).findFirst()

                        debug("Ripple Server Info\n${response.toString()}")
                        val result = response.getJSONObject("result")
                        val status = result.getString("status")
                        if (status.equals("success")) {
                            val lines = result.getJSONArray("lines")
                            localRealm.beginTransaction()
                            for(i in 0..lines.length()-1) {
                                    val line = lines.getJSONObject(i)
                                val currencyCode = line.getString("currency")
                                when (currencyCode) {
                                    "CNY" -> {
                                        val balance = localRealm.createObject(Balance::class.java)
                                        balance.currencyCode = currencyCode
                                        balance.amount = line.getString("balance").toDouble()
                                        localProfile?.cnyBalance = balance
                                    }
                                    "BTC" -> {
                                        val balance = localRealm.createObject(Balance::class.java)
                                        balance.currencyCode = currencyCode
                                        balance.amount = line.getString("balance").toDouble()
                                        localProfile?.btcBalance = balance
                                    }
                                }
                            }

                        } else {
                            Log.w(javaClass.name, "Problem getting Ripple server_info")
                        }
                        localRealm.commitTransaction()
                        localRealm.close()
                    }
                }, object : Response.ErrorListener {

                    override fun onErrorResponse(error: VolleyError) {
                        error("Problem getting Ripple server_info\n${error}")
                    }
                })

                trustLinesRequest.retryPolicy = DefaultRetryPolicy(30000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(trustLinesRequest)
            }
        }

        realm.close()
    }

    fun registerPushToken(pushToken: String, seed: String){

        val wallet = VPay365Wallet(seed)
        val token = pushToken
        val timestamp = Date().time

        val messagetosign = wallet.address+token+timestamp.toString()
        val signature = wallet.signMessage(messagetosign)

        val pushTokenRegistrationRequestObject = FeelPayPushTokenRequest(
                address = wallet.address,
                token = token,
                timestamp = timestamp,
                signature = signature
        )

        val pushTokenRegistrationRequestJson = mapper.writeValueAsString(pushTokenRegistrationRequestObject)

        val pushTokenRegistratiomRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.GATEWAY_SERVER_URL}/provisioning/pushtoken", JSONObject(pushTokenRegistrationRequestJson), object : Response.Listener<JSONObject> {

            override fun onResponse(response: JSONObject) {

                info("pushToken registered\n${response.toString()}")

                val threadRealm = Realm.getDefaultInstance()
                val profile = threadRealm.where(Profile::class.java).findFirst()

                threadRealm.beginTransaction()
                profile?.wallet?.destinationTag = response.getString("dtag")
                threadRealm.commitTransaction()
                threadRealm.close()

            }
        }, object : Response.ErrorListener {

            override fun onErrorResponse(error: VolleyError) {
                error("Problem Registering pushToken\n${error}")
            }
        })

        pushTokenRegistratiomRequest.retryPolicy = DefaultRetryPolicy(30000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(pushTokenRegistratiomRequest)
    }

    fun requestTopUpAddress(): String? {

        val mapper = ObjectMapper()

        val realm = Realm.getDefaultInstance()
        val profile = realm.where(Profile::class.java).findFirst()

        if (profile != null && profile.seed != null) {

            val wallet = VPay365Wallet(profile.seed!!)
            val timestamp = Date().time
            val messagetosign = wallet.address+timestamp.toString()
            val signature = wallet.signMessage(messagetosign)

            val btcAddressRequestObject = BitcoinGenerationRequest(
                    address = wallet.address,
                    timestamp = timestamp.toLong(),
                    signature = signature
            )

            val btcAddressRequestJson = mapper.writeValueAsString(btcAddressRequestObject)
            info("btcAddressRequestJson : ${btcAddressRequestJson}")

            val btcAddressRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.GATEWAY_SERVER_URL}/provisioning/bitcoingen", JSONObject(btcAddressRequestJson), object : Response.Listener<JSONObject> {

                override fun onResponse(response: JSONObject) {

                    info("BTC Address registered\n${response.toString()}")

                    val threadRealm = Realm.getDefaultInstance()
                    val profile = threadRealm.where(Profile::class.java).findFirst()
                    threadRealm.beginTransaction()
                    profile?.btcTopupAddress = response.getString("address")
                    threadRealm.commitTransaction()
                    threadRealm.close()

                }
            }, object : Response.ErrorListener {

                override fun onErrorResponse(error: VolleyError) {
                    error("Problem Getting BTC Top-Up Address\n${error}")
                }
            })

            btcAddressRequest.retryPolicy = DefaultRetryPolicy(30000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(btcAddressRequest)
        }

        realm.close()

        return null

    }

}

fun getRippleServerFee(applicationContext: Context, operation: (() -> Unit)) {

    val feeRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.RIPPLE_SERVER_URL}/", JSONObject(ObjectMapper().writeValueAsString(RippleFeeRequest())), object : Response.Listener<JSONObject> {

        override fun onResponse(response: JSONObject) {

            Log.d("getRippleServerFee", "Ripple Server Fee\n${response.toString()}")
            val result = response.getJSONObject("result")
            val drops = result.getJSONObject("drops")
            val medianFee = drops.getDouble("median_fee")

            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            val serverInfo = realm.where(ServerInfo::class.java).findFirst()?:realm.createObject(ServerInfo::class.java)
            serverInfo.fee = medianFee.toLong()
            realm.commitTransaction()
            realm.close()

            operation()

        }
    }, object : Response.ErrorListener {

        override fun onErrorResponse(error: VolleyError) {
            Log.e("IntentService", "Problem geting Ripple server_info\n${error.networkResponse?.toString()}")
        }
    })

    feeRequest.retryPolicy = DefaultRetryPolicy(30000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(feeRequest)
}

val noOperation = {}

fun persistTransactionsLocally(realm: Realm, currentUserAddress: String, resultJson: JSONObject) {

    val result = resultJson.getJSONObject("result")
    val status = result.getString("status")
    if (status.equals("success")) {
        Log.i("IntentService", "Transactions Successfully retrieved. Attempting local persistence")

        val transactions = result.getJSONArray("transactions")
        var transaction: JSONObject? = null
        for (i in (0 until transactions.length() -1)) {
            transaction = transactions.getJSONObject(i)
            val tx = transaction.getJSONObject("tx")
            val account = tx.getString("Account")
            val transactionType = tx.getString("TransactionType")
            val validated = transaction.getBoolean("validated")

            if (transactionType.equals("Payment")) {
                val amount = tx.getJSONObject("Amount")
                val date = tx.getLong("date")
                val txHash = tx.getString("hash")
                val destinationAddress = tx.getString("Destination")
                val currencyCode = amount.getString("currency")
                val value = amount.getString("value")
                val localTransaction = VPayTransaction(currentUserAddress, account, destinationAddress, txHash, date, value, currencyCode, validated)
                realm.copyToRealmOrUpdate(localTransaction)
            } else {
                Log.i("IntentService", "Unsupported TransactionType : ${transactionType}")
            }
        }
    }

}