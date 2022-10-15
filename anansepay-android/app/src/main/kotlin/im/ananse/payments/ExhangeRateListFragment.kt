package im.ananse.payments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import im.ananse.payments.model.ExchangeRate
import im.ananse.payments.network.RequestQueueSingleton
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_exchange_rate_list.*

val supportedCurrencies = listOf("CNY","BTC","USD","SGD","GBP","EUR","AED","CAD")

class ExchangeRateListFragment : Fragment() {

    val TAG = "RateListFragment"

    lateinit var activity: MainActivity
    lateinit var realm: Realm
    lateinit var rates: RealmResults<ExchangeRate>
    lateinit var recyclerAdapter: RealmRecyclerViewAdapter<ExchangeRate, ExchangeRateViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = getActivity() as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.fragment_exchange_rate_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()

        realm = Realm.getDefaultInstance()
        rates = realm.where(ExchangeRate::class.java).sort("currencyCode", Sort.ASCENDING).findAll()

        exchangeRateRecyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerAdapter = ExchangeRateRecyclerViewAdapter(rates)
        exchangeRateRecyclerView.adapter = recyclerAdapter

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.hideFam()

        for (currencyCode in supportedCurrencies) {
            refreshExchangeRates(currencyCode)
        }

    }

    fun refreshExchangeRates(baseCurrencyCode: String) {

        val currencyRequest = StringRequest(Request.Method.GET, "${BuildConfig.OPENEXCHANGE_BASE_URL}/$OPENEXCHANGE_COMMAND_LATEST?app_id=${BuildConfig.OPENEXCHANGE_APPID}&base=${baseCurrencyCode}&symbols=CNY,BTC,USD,SGD,GBP,EUR,AED,CAD\n", object : Response.Listener<String> {

            override fun onResponse(response: String) {

                val threadRealm = Realm.getDefaultInstance()

                val exchangeRate = ExchangeRate()
                exchangeRate.currencyCode = baseCurrencyCode
                exchangeRate.rates = response

                threadRealm.beginTransaction()
                threadRealm.copyToRealmOrUpdate(exchangeRate)
                threadRealm.commitTransaction()
                threadRealm.close()
            }

        }, Response.ErrorListener {
            Log.e(TAG, "Error encountered fetching exchange rates")
        })

        currencyRequest.retryPolicy = DefaultRetryPolicy(30000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        RequestQueueSingleton.getInstance(activity.applicationContext).addToRequestQueue(currencyRequest)

    }

    override fun onPause() {

        realm.close()
        super.onPause()
    }
}