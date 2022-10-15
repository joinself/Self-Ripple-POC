package im.ananse.payments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.ripple.ripplelibpp.*
import im.ananse.payments.gatewayclient.ConfirmationCodeRequest
import im.ananse.payments.gatewayclient.ProvisioningRequest
import im.ananse.payments.model.Profile
import im.ananse.payments.model.ServerInfo
import im.ananse.payments.model.Wallet
import im.ananse.payments.network.RequestQueueSingleton
import im.ananse.payments.rippleclient.RippleSubmitRequest
import im.ananse.gateway.wallet.VPay365Wallet
import io.realm.Realm
import org.jetbrains.anko.*
import org.json.JSONObject
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*




class ProvisioningActivity : AppCompatActivity(), AnkoLogger {

    lateinit var realm: Realm
    lateinit var profile: Profile
    lateinit var mapper: ObjectMapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profile = Profile()
        profile.wallet = getCandidateWallet()

//        realm.beginTransaction()
//        realm.commitTransaction()

        setContentView(R.layout.activity_provision)

//        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue()

        val fragment = ProvisioningFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.provisionFragmentContainer, fragment)
//        fragmentTransaction.addToBackStack("provision")
        fragmentTransaction.commit()

        mapper = ObjectMapper()

    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
    }

    fun getCandidateWallet(): Wallet {

        val random = SecureRandom()
        val seed = Seed.generateSeed(BigInteger(160, random).toString(32 ))
        profile.seed = Seed.toBase58(seed)
        val vPay365Wallet = VPay365Wallet(profile.seed!!)
//        val keyPair = walletManager.generateKeypair(profile.seed!!)
        val wallet = Wallet()
//        wallet.privateKey = SecretKey.toBase58(KeyType.SECP256K1, keyPair.second)
        wallet.publicKey = vPay365Wallet.publickey()
//        val accountId = AccountID.calcAccountID(keyPair.first)
        wallet.address = vPay365Wallet.address

        return wallet
    }

    fun provisioningRequest(view: View, wallet: Wallet, phoneNumber: String) {

        profile.phone = phoneNumber

        val vPay365Wallet = profile.getVPay365Wallet()

        val provisioningRequestObject = ProvisioningRequest()
        provisioningRequestObject.address = wallet.address!!
        provisioningRequestObject.pubkey = wallet.publicKey!!
        provisioningRequestObject.timestamp = Date().time
        provisioningRequestObject.signature = vPay365Wallet.signMessage("${wallet.address}$phoneNumber${provisioningRequestObject.timestamp}")
        provisioningRequestObject.telnumber = phoneNumber


        val nativeSeed = Seed.parseBase58(profile.seed)
        val keyPair = SecretKey.generateKeyPair(KeyType.SECP256K1, nativeSeed)
//        val publicKey = com.ripple.ripplelibpp.PublicKey.toBase58(KeyType.SECP256K1, keyPair.first)
        val secretKey = SecretKey.toBase58(KeyType.SECP256K1, keyPair.second)
        val accountId = AccountID.calcAccountID(keyPair.first)
        val address = AccountID.toBase58(accountId)
        info("native address : ")

        val requestJson = JSONObject(mapper.writeValueAsString(provisioningRequestObject))
        info("provisioingRequestJSON\n$requestJson")

        val provisioningRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.GATEWAY_SERVER_URL}/provisioning/request", requestJson, object : Response.Listener<JSONObject> {

            override fun onResponse(response: JSONObject) {
//                mTxtDisplay.setText("Response: " + response.toString())
                debug(response.toString())

                val confirmationFragment = ProvisioningConfirmationFragment()
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.provisionFragmentContainer, confirmationFragment)
                fragmentTransaction.addToBackStack("confirm")
                fragmentTransaction.commit()

            }
        }, object : Response.ErrorListener {

            override fun onErrorResponse(error: VolleyError) {
                Snackbar.make(contentView!!, R.string.error_problem_provisioning, Snackbar.LENGTH_LONG).show()
                view.isEnabled = true
                error(error)
            }
        })

        provisioningRequest.retryPolicy = DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(provisioningRequest)

    }

    fun confirmRequest(view: View, confirmationCode: CharSequence) {

        val vPay365Wallet = profile.getVPay365Wallet()

        val confirmationCodeRequestObject = ConfirmationCodeRequest()
        confirmationCodeRequestObject.address = profile.wallet?.address!!
        confirmationCodeRequestObject.signature = vPay365Wallet.signMessage("${profile.wallet?.address}$confirmationCode")

        val requestJson = JSONObject(mapper.writeValueAsString(confirmationCodeRequestObject))
        info("confirmationCodeRequestJSON\n$requestJson")

        val confirmationCodeRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.GATEWAY_SERVER_URL}/provisioning/validate", requestJson, object : Response.Listener<JSONObject> {

            override fun onResponse(response: JSONObject) {
                info(response.toString())

                val dtag = response.getString("dtag")

                val threadRealm = Realm.getDefaultInstance()

                profile.wallet?.destinationTag = dtag
                threadRealm.beginTransaction()
                threadRealm.copyToRealm(profile)
                threadRealm.commitTransaction()
                threadRealm.close()

                Log.d(javaClass.simpleName, "Get Ripple Ledger")
                startService(intentFor<IntentService>().setAction(action_ripple_get_transaction_data))

                val cnyTrustlinesJson = createTrustLinesRequest("CNY", 1, 10000)
                val btcTrustlinesJson = createTrustLinesRequest("BTC", 2, 10)

                trustlineRequests(listOf(cnyTrustlinesJson, btcTrustlinesJson))
            }
        }, object : Response.ErrorListener {

            override fun onErrorResponse(error: VolleyError) {
                Snackbar.make(contentView!!, R.string.error_problem_provisioning, Snackbar.LENGTH_LONG).show()
                view.isEnabled = true
                error(error)
            }
        })

        confirmationCodeRequest.retryPolicy = DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(confirmationCodeRequest)

    }

    fun createTrustLinesRequest(currencyCode: String, sequenceNo: Int, value: Long): String {

        val seed = Seed.parseBase58(profile.seed)
        val keypair = SecretKey.generateKeyPair(KeyType.SECP256K1, seed)

        var issuerAccountId: AccountID? = null
        when (currencyCode) {
            "CNY" -> {issuerAccountId = AccountID.parseBase58(BuildConfig.CNY_ISSUING_WALLET_ADDRESS)}
            "BTC" -> {issuerAccountId = AccountID.parseBase58(BuildConfig.BTC_ISSUING_WALLET_ADDRESS)}
            else -> throw IllegalArgumentException("Unsupported Currency")
        }

        val realm = Realm.getDefaultInstance()
        val serverInfo = realm.where(ServerInfo::class.java).findFirst()
        var sfFee:STAmount? = null
        if (serverInfo?.fee != null) {
            sfFee = STAmount(serverInfo.fee!!)
        } else {
            sfFee = STAmount(3000)
        }

         // TODO Make this dynamic
        val sfAmount = STAmount(Issue(Currency.toCurrency(currencyCode), issuerAccountId), value, 1)

        val trustLineTx = STTx.getBuilder().setTxType(TxType.TT_TRUST_SET)
                .setAccount(AccountID.parseBase58(profile.wallet!!.address))
                .setSigningPublicKey(keypair.getFirst()).setLimitAmount(sfAmount)
                .setFee(sfFee) // TBD API artifact
                .setSequence(sequenceNo) // TBD API artifact
                .build()

        info("Before signing: \n${trustLineTx.getStyledJsonString(0)}\nSerialized: ${trustLineTx.getStyledJsonString(0, true)}")

        trustLineTx.sign(keypair.getFirst(), keypair.getSecond())
        info("signed output fullText : \n${trustLineTx.fullText}")

        val trustLineTxBlob = STTx.serialize(trustLineTx)

        return mapper.writeValueAsString(RippleSubmitRequest(trustLineTxBlob))

    }

    fun trustlineRequests(jsonRequests: List<String>) {

        for (requestJson in jsonRequests) {
            val trustLineRequest = JsonObjectRequest(Request.Method.POST, "${BuildConfig.RIPPLE_SERVER_URL}/", JSONObject(requestJson), object : Response.Listener<JSONObject> {

                override fun onResponse(response: JSONObject) {
                    info("trustLineRequestResponse\n${response.toString()}")

                    Snackbar.make(contentView!!, "Currency Successfully configured", Snackbar.LENGTH_LONG).show()
                }
            }, object : Response.ErrorListener {

                override fun onErrorResponse(error: VolleyError) {
                    Snackbar.make(contentView!!, R.string.error_problem_provisioning, Snackbar.LENGTH_LONG).show()
                    error(error)
                }
            })

            trustLineRequest.retryPolicy = DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            RequestQueueSingleton.getInstance(applicationContext).addToRequestQueue(trustLineRequest)

            startActivity(intentFor<MainActivity>().singleTop().clearTop().clearTask().newTask())
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_provision, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_restore -> {
                startActivity(intentFor<RestoreActivity>())
                true
            }
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {

        realm.close()

        super.onPause()
    }

}