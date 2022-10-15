package im.ananse.payments

import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import im.ananse.payments.model.Profile
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_backup.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.AnkoLogger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class BackupActivity : AppCompatActivity(), AnkoLogger {

    lateinit var realm: Realm
    lateinit var profile: Profile
    lateinit var dialog: AlertDialog
    lateinit var clipBoardMananger: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val builder = AlertDialog.Builder(this);
        builder.setTitle(R.string.action_backup)
                .setItems(R.array.backup_options, DialogInterface.OnClickListener { dialog, which ->

                    when (which) {
                        0 -> {
                            val sendIntent = Intent()
                            sendIntent.action = Intent.ACTION_SEND
                            sendIntent.putExtra(Intent.EXTRA_TEXT, profile.seed)
                            sendIntent.type = "text/plain"
                            startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.send_to)))
                        }
                        1 -> {
                            val barcodeBitmap = QRCode.from(profile.seed).withSize(250, 250).bitmap()
                            val qrCodeFile = File(cacheDir, "backupcode.png")

                            var out: FileOutputStream? = null
                            try {
                                out = FileOutputStream(qrCodeFile)
                                barcodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                try {
                                    if (out != null) {
                                        out.close()
                                    }
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }

                            }

                            val uri = FileProvider.getUriForFile(this, BuildConfig.FILEPROVIDER, qrCodeFile)

                            val shareIntent = Intent()
                            shareIntent.action = Intent.ACTION_SEND
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            shareIntent.type = "image/PNG"
                            startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
                        }
                    }

                })

        dialog = builder.create();

        clipBoardMananger = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        buttonBackupCode.setOnClickListener {
            dialog.show()
        }
    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
        profile = realm.where(Profile::class.java).findFirst()!!
    }

    override fun onPause() {

        realm.close()
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }

        }
        return super.onOptionsItemSelected(item);
    }

}