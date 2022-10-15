package im.ananse.payments

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import im.ananse.payments.model.Profile
import io.realm.Realm
import io.realm.RealmChangeListener
import kotlinx.android.synthetic.main.fragment_account_balance.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.support.v4.intentFor
import java.util.*




/**
 * A placeholder fragment containing a simple view.
 */
class AccountBalanceFragment : Fragment() {

    lateinit var realm: Realm
    lateinit var barcodeBitmap: Bitmap

    val realmListener = object : RealmChangeListener<Realm> {
        override fun onChange(realm: Realm) {
            val profile = realm.where(Profile::class.java).findFirst()
            balance_cny?.text = profile?.cnyBalance?.amount.toString()
            balance_btc?.text = profile?.btcBalance?.amount.toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_account_balance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
        val profile = realm.where(Profile::class.java).findFirst()
//        barcodeBitmap = createBarcodeBitmap(profile?.wallet?.address!!, 40, 200)
        barcodeBitmap = QRCode.from("https://ananse.im/c?a=${profile?.wallet?.address}").withSize(250, 250).bitmap();

        barcodeImageView.setImageBitmap(barcodeBitmap)

        realm.addChangeListener(realmListener)

        activity?.startService(intentFor<IntentService>().setAction(action_refresh_trust_lines))
    }

    @Deprecated("Using QRGen now")
    @Throws(WriterException::class)
    private fun createBarcodeBitmap(data: String, width: Int, height: Int): Bitmap {
        val writer = MultiFormatWriter()
        val finalData = Uri.encode(data)

        // Use 1 as the height of the matrix as this is a 1D Barcode.
        val bm = writer.encode(finalData, BarcodeFormat.CODE_128, width, 1)
        val bmWidth = bm.width

        val imageBitmap = Bitmap.createBitmap(bmWidth, height, Bitmap.Config.ARGB_8888)

        for (i in 0..bmWidth - 1) {
            // Paint columns of width 1
            val column = IntArray(height)
            Arrays.fill(column, if (bm.get(i, 0)) Color.BLACK else Color.WHITE)
            imageBitmap.setPixels(column, 0, 1, i, 0, 1, height)
        }

        return imageBitmap
    }

    override fun onPause() {
        super.onPause()

        realm.removeChangeListener(realmListener)
        realm.close()
    }

    override fun onDestroy() {
        super.onDestroy()


    }
}
