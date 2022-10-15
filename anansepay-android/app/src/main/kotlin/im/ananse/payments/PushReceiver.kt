package im.ananse.payments

/**
 * Created by sena on 15/09/2017.
 */
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import org.jetbrains.anko.AnkoLogger

class PushReceiver : BroadcastReceiver(), AnkoLogger{
    override fun onReceive(context: Context, intent: Intent) {

        val intent = Intent()
        intent.setAction(action_refresh_trust_lines)
        intent.setClass(context, IntentService::class.java)

        Toast.makeText(context, "VPay Updated", Toast.LENGTH_SHORT)

        context.startService(intent)
    }
}