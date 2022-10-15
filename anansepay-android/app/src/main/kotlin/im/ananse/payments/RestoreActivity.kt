package im.ananse.payments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.ripple.ripplelibpp.*
import im.ananse.payments.model.Profile
import im.ananse.payments.model.Wallet
import im.ananse.gateway.wallet.VPay365Wallet
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_restore.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.contentView
import org.jetbrains.anko.info

class RestoreActivity : AppCompatActivity(), AnkoLogger {

    lateinit var intentIntegrator: IntentIntegrator
    lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restore)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setBeepEnabled(true)
        intentIntegrator.setOrientationLocked(false)

        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.action_restore)
                .setPositiveButton(R.string.action_restore, DialogInterface.OnClickListener {dialog, which ->
                    Snackbar.make(restoreButton, "Restoring", Snackbar.LENGTH_LONG).show()

                    createNewWalletFromSeed(restoreCodeField.text.toString())
                })
                .setNegativeButton(("Cancel"), DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                })
                .setMessage(R.string.secondary_restore_warning)

        restoreButton.setOnClickListener {
            dialog.show()
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_restore, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            R.id.action_scan -> {
                intentIntegrator.initiateScan()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Snackbar.make(contentView!!, R.string.cancelled, Snackbar.LENGTH_LONG).show()
            } else {
                restoreCodeField.setText(result.contents.trim())
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun createNewWalletFromSeed(seed: String) {

        val vPay365Wallet = VPay365Wallet(seed.trim())

        val nativeSeed = Seed.parseBase58(seed)
        val keyPair = SecretKey.generateKeyPair(KeyType.SECP256K1, nativeSeed)
        val accountId = AccountID.calcAccountID(keyPair.first)
        val address = AccountID.toBase58(accountId)

        val profile = Profile()
        profile.seed = seed
        profile.wallet = Wallet()
        profile.wallet!!.address = address
        profile.wallet!!.publicKey = PublicKey.toBase58(KeyType.SECP256K1, keyPair.first)

        if (vPay365Wallet.verifyAddress(profile!!.wallet!!.address!!.trim())) { //
            Toast.makeText(this, "Successful restore", Toast.LENGTH_LONG).show()
            info("Instance Count : ${Realm.getGlobalInstanceCount(Realm.getDefaultConfiguration())}")
            if (Realm.deleteRealm(Realm.getDefaultConfiguration())) {
                val realm = Realm.getDefaultInstance()
                realm.beginTransaction()
                realm.copyToRealm(profile)
                realm.commitTransaction()
                realm.close()

                finishAffinity()
            } else {
                Snackbar.make(restoreButton, R.string.restore_error, Snackbar.LENGTH_LONG).show()
            }

            val newRealm = Realm.getDefaultConfiguration()
        } else {
            Snackbar.make(restoreButton, R.string.restore_error, Snackbar.LENGTH_LONG).show()
        }
    }

}