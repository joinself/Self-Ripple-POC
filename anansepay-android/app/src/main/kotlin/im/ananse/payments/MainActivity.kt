package im.ananse.payments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v13.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.github.clans.fab.FloatingActionMenu
import com.github.orangegangsters.lollipin.lib.PinCompatActivity
import com.github.orangegangsters.lollipin.lib.managers.AppLock
import com.github.orangegangsters.lollipin.lib.managers.LockManager
import com.google.zxing.integration.android.IntentIntegrator
import im.ananse.payments.model.Profile
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import me.pushy.sdk.Pushy
import org.jetbrains.anko.*
import java.security.KeyStore
import java.util.*


val pref_key_salt = "prefKeySalt"

val dayInMilliseconds = 1000*60*60*24

val KEY_ADDRESS = "a"
val KEY_NAME = "n"
val KEY_AMOUNT = "v"
val KEY_CURRENCY = "m"

val ACTION_SET_PASSCODE = "ACTION_SET_PASSCODE"
val ACTION_DISABLE_PASSCODE = "ACTION_DISABLE_PASSCODE"

val QR_SCAN_REQUEST_CODE = 49374

class MainActivity : PinCompatActivity(), AnkoLogger {

    lateinit var intentIntegrator: IntentIntegrator
    lateinit var realm: Realm
    var profile: Profile? = null
    lateinit var fam: FloatingActionMenu

    lateinit private var keyStore: KeyStore

    val externalWritePermissionRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        initialPasscodeCheck()

        Pushy.listen(this)

        // Check whether the user has granted us the READ/WRITE_EXTERNAL_STORAGE permissions
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request both READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE so that the
            // Pushy SDK will be able to persist the device token in the external storage
            ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setBeepEnabled(true)
        intentIntegrator.setOrientationLocked(false)

        handleIntent(intent)

        fam = findViewById(R.id.fam)

        action_topup.setOnClickListener() { view ->

            val fragment = TopupFragment()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.mainContentFragmentContainer, fragment)
            fragmentTransaction.addToBackStack("topup")

            fragmentTransaction.commit()

        }

        action_withdraw.setOnClickListener() { view ->

            val fragment = WithdrawalFragment()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.mainContentFragmentContainer, fragment)
            fragmentTransaction.addToBackStack("withdrawal")

            fragmentTransaction.commit()

        }

        action_transfer.setOnClickListener() { view ->


            val fragment = TransferFragment()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.mainContentFragmentContainer, fragment)
            fragmentTransaction.addToBackStack("transfer")

            fragmentTransaction.commit()

        }

        action_request_transaction.setOnClickListener() { view ->

            val fragment = RequestTransferFragment()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.mainContentFragmentContainer, fragment)
            fragmentTransaction.addToBackStack("requestTransaction")

            fragmentTransaction.commit()

        }

    }

    fun initialPasscodeCheck() {

//        if (defaultSharedPreferences.contains(pref_key_salt)) {
//            info("No salt or passcode set")
//
//        }

        val lockManager = LockManager.getInstance()
        val appLock = lockManager.appLock
        val desiredTimeout = defaultSharedPreferences.getString(resources.getString(R.string.pref_key_screen_lock_timeout), "60000")
        if (!desiredTimeout.equals("0")) {
            lockManager.enableAppLock(this, CustomPinActivity::class.java)
            lockManager.appLock.logoId = R.mipmap.ic_launcher
        }
    }


    fun handleIntent(intent: Intent) {

        info("MainActivity from Launcher")
        val fragment = TransactionListFragment()



        when (intent.action) {
            Intent.ACTION_MAIN, ACTION_TRANSFER, Intent.ACTION_VIEW -> {
                info("MainActivity MAIN TRANSFER OR VIEW ACTIONS")

                var fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.add(R.id.mainContentFragmentContainer, fragment)
                fragmentTransaction.commit()

                when (intent.action) {
                    ACTION_TRANSFER, Intent.ACTION_VIEW -> {
                        fragmentTransaction = supportFragmentManager.beginTransaction()

                        val transferFragment = TransferFragment()
                        val args = Bundle()
                        args.putString(KEY_ADDRESS, intent.data.getQueryParameter(KEY_ADDRESS))
                        args.putString(KEY_NAME, intent.data.getQueryParameter(KEY_NAME))
                        args.putString(KEY_AMOUNT, intent.data.getQueryParameter(KEY_AMOUNT))
                        args.putString(KEY_CURRENCY, intent.data.getQueryParameter(KEY_CURRENCY))
                        transferFragment.arguments = args

                        fragmentTransaction = supportFragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.mainContentFragmentContainer, transferFragment)
                        fragmentTransaction.addToBackStack("transfer")
                        fragmentTransaction.commit()
                    }
                }


            }

            ACTION_ADD_CONTACT -> {
                info("MainActivity Pre-Fill Contact")
            }
            ACTION_SET_PASSCODE -> {
                val lockManager = LockManager.getInstance()
                val appLock = lockManager.appLock
                val intent = Intent(this, CustomPinActivity::class.java)
                if (appLock != null && appLock.isPasscodeSet) {
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN)
                    startActivity(intent)
                } else {
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK)
                    startActivityForResult(intent, requestCodeEnable)
                }
            }
            ACTION_DISABLE_PASSCODE -> {
                val lockManager = LockManager.getInstance()
//                lockManager.enableAppLock(activity, MainActivity::class.java)
                val appLock = lockManager.appLock
                val intent = Intent(this, CustomPinActivity::class.java)
                if (appLock != null && appLock.isPasscodeSet) {
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.DISABLE_PINLOCK)
                    startActivity(intent)
                } else {
                    lockManager.disableAppLock()
                    defaultSharedPreferences.edit().putString(resources.getString(R.string.pref_key_screen_lock_timeout), "0").apply()
                    finish()
                }
            }
            else -> {
                info("Deep Navigation Not Required")
            }
        }

    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
        profile = realm.where(Profile::class.java).findFirst()
        info("Ripple address : ${profile?.wallet?.address}")
        if (profile == null) {
            realm.close()
            debug("Realm closed")
            startActivity(intentFor<ProvisioningActivity>().singleTop().clearTop().newTask())
            finishAffinity()
        } else {
            // Check for a device pushtoken. Register if it is unset or older than 24h.
            val pushToken = profile?.pushToken
            val tokenTimestamp = profile!!.pushTokenTimestamp
            if (pushToken == null || (tokenTimestamp < Date().time - dayInMilliseconds)) {
                startService(intentFor<IntentService>().setAction(action_pushreg))
            }

            startService(intentFor<IntentService>().setAction(action_ripple_get_transaction_data))

            when (intent.data?.getQueryParameter("result")) {
                "success" -> {
                    Snackbar.make(action_topup, R.string.topup_success_snack, Snackbar.LENGTH_LONG)
                }
                "failure" -> {
                    Snackbar.make(action_topup, R.string.topup_failure_snack, Snackbar.LENGTH_LONG)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_profile -> {
                val fragment = ProfileFragment()
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.mainContentFragmentContainer, fragment)
                fragmentTransaction.addToBackStack("profile")
                fragmentTransaction.commit()
                true
            }
            R.id.action_scan -> {
                intentIntegrator.initiateScan()
                true
            }
            R.id.action_settings -> {
                val fragment = PreferenceFragment()
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.mainContentFragmentContainer, fragment)
                fragmentTransaction.addToBackStack("preferences")
                fragmentTransaction.commit()
                true
            }
            R.id.action_exchange_rates-> {
                val fragment = ExchangeRateListFragment()
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.mainContentFragmentContainer, fragment)
                fragmentTransaction.addToBackStack("exchangeRates")
                fragmentTransaction.commit()
                true
            }
            R.id.action_contacts-> {
                val fragment = ContactListFragment()
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.mainContentFragmentContainer, fragment)
                fragmentTransaction.addToBackStack("contacts")
                fragmentTransaction.commit()
                true
            }
            R.id.action_add_contact -> {
                val fragment = AddContactFragment()
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.mainContentFragmentContainer, fragment)
                fragmentTransaction.addToBackStack("addContact")
                fragmentTransaction.commit()
                true
            }
            R.id.action_refresh -> {
                startService(intentFor<IntentService>().setAction(action_refresh_trust_lines))
                startService(intentFor<IntentService>().setAction(action_ripple_get_transaction_data))
                true
            }
            R.id.action_backup -> {
                startActivity(intentFor<BackupActivity>())
                true
            }
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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        info("MainActivity.onActivityResult")

        when (requestCode) {
            requestCodeEnable -> {
                Log.i("MainActivity", "Passcode Set")
                val desiredTimeout = defaultSharedPreferences.getString(resources.getString(R.string.pref_key_screen_lock_timeout), null)
                if (desiredTimeout == null) {
                    defaultSharedPreferences.edit().putString(resources.getString(R.string.pref_key_screen_lock_timeout), "60000").apply()
                }
                val lockManager = LockManager.getInstance()
//                lockManager.enableAppLock(this, MainActivity::class.java)
//                lockManager.appLock.logoId = R.mipmap.ic_launcher
                lockManager.appLock.timeout = desiredTimeout.toLong()
            }
//            requestCodeDisable -> {
//                Log.i("MainActivity", "Disabling Pincode")
//                if (resultCode == -1) {
//                    val lockManager = LockManager.getInstance()
//                    val appLock = lockManager.appLock
//                    if (appLock != null) {
//                        appLock.disable()
//                    }
//                }
//            }
            requestCodeConfirm -> {

            }
            QR_SCAN_REQUEST_CODE -> {
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result != null) {
                    if (result.contents == null) {
                        Snackbar.make(contentView!!, R.string.cancelled, Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(contentView!!, "Scanned", Snackbar.LENGTH_LONG).show()
                        info("QR output : ${result.contents.toString()}")
                        if (result.contents.startsWith("https://www.ananse.im")) {
                            startActivity(intentFor<MainActivity>().setAction(ACTION_TRANSFER).setData(Uri.parse(result.contents)))
                        } else {
                            Snackbar.make(contentView!!, R.string.invalid_barcode, Snackbar.LENGTH_LONG).show()
                        }

                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
//            else -> {
//
//            }
        }

    }

    fun showFam() {
        fam.visibility = View.VISIBLE
    }

    fun hideFam() {
        fam.visibility = View.GONE
    }

    override fun onPause() {

        realm.close()

        super.onPause()
    }

}
