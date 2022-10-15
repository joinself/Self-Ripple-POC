package im.ananse.payments

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import com.github.orangegangsters.lollipin.lib.managers.AppLockActivity
import uk.me.lewisdeane.ldialogs.BaseDialog
import uk.me.lewisdeane.ldialogs.CustomDialog

/**
 * Created by sena on 05/10/2017.
 */


/**
 * ENABLE_PINLOCK type, uses at firt to define the password
 */
val ENABLE_PINLOCK = 0
/**
 * DISABLE_PINLOCK type, uses to disable the system by asking the current password
 */
val DISABLE_PINLOCK = 1
/**
 * CHANGE_PIN type, uses to change the current password
 */
val CHANGE_PIN = 2
/**
 * CONFIRM_PIN type, used to confirm the new password
 */
val CONFIRM_PIN = 3
/**
 * UNLOCK_PIN type, uses to ask the password to the user, in order to unlock the im.ananse.payments
 */
val UNLOCK_PIN = 4

/**
 * EXTRA_TYPE, uses to pass to the [com.github.orangegangsters.lollipin.lib.managers.AppLockActivity]
 * to determine in which type it musts be started.
 */
val EXTRA_TYPE = "type"

val requestCodeEnable = 11
val requestCodeConfirm = 12
val requestCodeDisable = 13

class CustomPinActivity: AppLockActivity() {

    override fun showForgotDialog() {
        val res = resources
        // Create the builder with required paramaters - Context, Title, Positive Text
        val builder = CustomDialog.Builder(this,
                res.getString(R.string.forgot_pincode_message),
                res.getString(R.string.ok))
        builder.content(res.getString(R.string.forgot_code_wipe_warning))
        builder.negativeText(res.getString(R.string.cancel))

        //Set theme
        builder.darkTheme(false)
        builder.typeface(Typeface.DEFAULT)
        builder.positiveColor(res.getColor(R.color.gold_vpay)) // int res, or int colorRes parameter versions available as well.
        builder.negativeColor(res.getColor(R.color.gold_vpay))
        builder.rightToLeft(false) // Enables right to left positioning for languages that may require so.
        builder.titleAlignment(BaseDialog.Alignment.CENTER)
        builder.buttonAlignment(BaseDialog.Alignment.CENTER)
        builder.setButtonStacking(false)

        //Set text sizes
        //        builder.titleTextSize((int) res.getDimension(R.dimen.activity_dialog_title_size));
        //        builder.contentTextSize((int) res.getDimension(R.dimen.activity_dialog_content_size));
        //        builder.positiveButtonTextSize((int) res.getDimension(R.dimen.activity_dialog_positive_button_size));
        //        builder.negativeButtonTextSize((int) res.getDimension(R.dimen.activity_dialog_negative_button_size));

        //Build the dialog.
        val customDialog = builder.build()
        customDialog.setCanceledOnTouchOutside(false)
        customDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        customDialog.setClickListener(object : CustomDialog.ClickListener {
            override fun onConfirmClick() {
                Toast.makeText(applicationContext, "Yes", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelClick() {
                Toast.makeText(applicationContext, "Cancel", Toast.LENGTH_SHORT).show()
            }
        })

        // Show the dialog.
        customDialog.show()
    }

    override fun onPinFailure(attempts: Int) {

    }

    override fun onPinSuccess(attempts: Int) {

    }

    override fun getPinLength(): Int {
        return super.getPinLength()//you can override this method to change the pin length from the default 4
    }

}