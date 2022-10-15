package im.ananse.payments.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley


/**
 * Created by sena on 30/08/2017.
 */
class RequestQueueSingleton() {

    private var mRequestQueue: RequestQueue? = null
    private var mCtx: Context? = null

    private constructor(context: Context): this(){

        mCtx = context
        mRequestQueue = getRequestQueue()

    }

    fun getRequestQueue(): RequestQueue {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx?.getApplicationContext())
        }
        return mRequestQueue!!
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        getRequestQueue().add(req)
    }

    companion object {

        private var mInstance: RequestQueueSingleton? = null

        @Synchronized
        fun getInstance(context: Context): RequestQueueSingleton {
            if (mInstance == null) {
                mInstance = RequestQueueSingleton(context)
            }
            return mInstance as RequestQueueSingleton
        }
    }

}