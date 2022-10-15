package im.ananse.payments

import android.app.Application
import android.util.Log
import com.android.volley.VolleyLog
import com.bugsnag.android.Bugsnag
import com.github.orangegangsters.lollipin.lib.managers.LockManager
import com.google.android.fix.PRNGFixes
import com.ripple.ripplelibpp.RippleLibPP
import im.ananse.payments.model.RealmMigration
import io.realm.Realm
import io.realm.RealmConfiguration
import org.jetbrains.anko.*


/**
 * Created by sena on 25/08/2017.
 */


fun getcurrencyIssuingAddress(currencyCode: String): String {
    when (currencyCode) {
        "CNY" -> {
            return BuildConfig.CNY_ISSUING_WALLET_ADDRESS
        }
        "BTC" -> {
            return BuildConfig.BTC_ISSUING_WALLET_ADDRESS
        }
        else -> throw IllegalArgumentException("Unsupported Currency Code : $currencyCode")
    }
}

open class VPay365Application: Application(), AnkoLogger {

//    private lateinit var deviceToken: String
//    private lateinit var salt: String
//    private lateinit var profile: Profile
//    private lateinit var realm: Realm

    override fun onCreate() {

        super.onCreate()

        val lockManager = LockManager.getInstance()
        val desiredTimeout = defaultSharedPreferences.getString(resources.getString(R.string.pref_key_screen_lock_timeout), "60000")
        if (!desiredTimeout.equals("0")) {
            lockManager.enableAppLock(this, CustomPinActivity::class.java)
            lockManager.appLock.logoId = R.mipmap.ic_launcher
        }

        VolleyLog.DEBUG = BuildConfig.DEBUG
        info("VolleyLog.debug : ${BuildConfig.DEBUG}")

        debug(RippleLibPP.getVersion())

        Log.i(javaClass.simpleName, "Initialising BugSnag")
        Bugsnag.init(this);

        Log.i(javaClass.simpleName, "Applying PRNG Fixes for Android Crypto")
        PRNGFixes.apply()
        Realm.init(this)

        Realm.setDefaultConfiguration(
                RealmConfiguration.Builder()
                        .schemaVersion(5)
                        .migration(RealmMigration()).build())

        Log.d(javaClass.simpleName, "Get Ripple Ledger and Refresh Transacitons")
        startService(intentFor<IntentService>().setAction(action_ripple_get_transaction_data))

        Log.d(javaClass.simpleName, "Register Push Notification Service")
        // Register with Pushy if necessary (Intent is called using Anko)
        startService(intentFor<IntentService>().setAction(action_pushreg))

    }

}