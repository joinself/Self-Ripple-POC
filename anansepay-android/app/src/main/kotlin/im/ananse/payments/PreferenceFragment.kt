package im.ananse.payments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.github.orangegangsters.lollipin.lib.managers.AppLock
import com.github.orangegangsters.lollipin.lib.managers.LockManager
import org.jetbrains.anko.support.v4.defaultSharedPreferences



/**
 * Created by sena on 10/09/2017.
 */

val prefKeyRegisteredBtcAddress = "pref_key_authorised_btc_address"

class PreferenceFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var activity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = getActivity() as MainActivity
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.hideFam()

        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        when (key) {
            resources.getString(R.string.pref_key_screen_lock_timeout) -> {

                val lockManager = LockManager.getInstance()
                lockManager.appLock.logoId = R.mipmap.ic_launcher

                val desiredTimeout = sharedPreferences.getString(resources.getString(R.string.pref_key_screen_lock_timeout), "60000")

                if (!desiredTimeout.equals("0")) {
                    lockManager.appLock.timeout = desiredTimeout.toLong()
                    if (!lockManager.isAppLockEnabled) {
                        lockManager.enableAppLock(activity, CustomPinActivity::class.java)
                        val intent = Intent(activity, CustomPinActivity::class.java)
                        intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK)
                        startActivityForResult(intent, requestCodeEnable)
                    }
                } else {
                    lockManager.disableAppLock()
                }
            }
        }

    }

    override fun onPause() {

        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onPause()
    }

}