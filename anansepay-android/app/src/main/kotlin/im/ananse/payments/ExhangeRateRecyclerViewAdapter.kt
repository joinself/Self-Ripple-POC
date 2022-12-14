package im.ananse.payments

import android.view.LayoutInflater
import android.view.ViewGroup
import im.ananse.payments.model.ExchangeRate
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import org.json.JSONObject
import java.text.DecimalFormat

/**
 * Created by sena on 05/09/2017.
 */

class ExchangeRateRecyclerViewAdapter: RealmRecyclerViewAdapter<ExchangeRate, ExchangeRateViewHolder> {

    val TAG = "XRRecyclerViewAdapter"
    lateinit var currencyFormat: DecimalFormat
    lateinit var mBtcFormat: DecimalFormat

    constructor(data: OrderedRealmCollection<ExchangeRate>): super(data, true) {
        setHasStableIds(false)

        currencyFormat = DecimalFormat("###.##")
        mBtcFormat = DecimalFormat("###.#####")

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeRateViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.exchange_rate_item, parent, false)

        return ExchangeRateViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExchangeRateViewHolder, position: Int) {

        val rate = getItem(position)
        holder.data = rate!!
        val ratesObject = rate!!.getRatesJsonObject().getJSONObject("rates")

        holder.currencyCode.text = "1 ${holder.data.currencyCode}"

        if (ratesObject.has("CNY")) {
            holder.cnyRate.text = currencyFormat.format(ratesObject.getDouble("CNY"))
        } else {
            holder.cnyRate.text = ""
        }

        if (ratesObject.has("BTC")) {
            holder.mBtcRate.text = mBtcFormat.format(ratesObject.getDouble("BTC")*1000)
        } else {
            holder.mBtcRate.text = ""
        }

        holder.currency.text = currencyList.getString(holder.data.currencyCode)
    }

}

val currencyList = JSONObject("{\n" +
        "    \"AED\": \"United Arab Emirates Dirham\",\n" +
        "    \"AFN\": \"Afghan Afghani\",\n" +
        "    \"ALL\": \"Albanian Lek\",\n" +
        "    \"AMD\": \"Armenian Dram\",\n" +
        "    \"ANG\": \"Netherlands Antillean Guilder\",\n" +
        "    \"AOA\": \"Angolan Kwanza\",\n" +
        "    \"ARS\": \"Argentine Peso\",\n" +
        "    \"AUD\": \"Australian Dollar\",\n" +
        "    \"AWG\": \"Aruban Florin\",\n" +
        "    \"AZN\": \"Azerbaijani Manat\",\n" +
        "    \"BAM\": \"Bosnia-Herzegovina Convertible Mark\",\n" +
        "    \"BBD\": \"Barbadian Dollar\",\n" +
        "    \"BDT\": \"Bangladeshi Taka\",\n" +
        "    \"BGN\": \"Bulgarian Lev\",\n" +
        "    \"BHD\": \"Bahraini Dinar\",\n" +
        "    \"BIF\": \"Burundian Franc\",\n" +
        "    \"BMD\": \"Bermudan Dollar\",\n" +
        "    \"BND\": \"Brunei Dollar\",\n" +
        "    \"BOB\": \"Bolivian Boliviano\",\n" +
        "    \"BRL\": \"Brazilian Real\",\n" +
        "    \"BSD\": \"Bahamian Dollar\",\n" +
        "    \"BTC\": \"Bitcoin\",\n" +
        "    \"BTN\": \"Bhutanese Ngultrum\",\n" +
        "    \"BWP\": \"Botswanan Pula\",\n" +
        "    \"BYN\": \"Belarusian Ruble\",\n" +
        "    \"BZD\": \"Belize Dollar\",\n" +
        "    \"CAD\": \"Canadian Dollar\",\n" +
        "    \"CDF\": \"Congolese Franc\",\n" +
        "    \"CHF\": \"Swiss Franc\",\n" +
        "    \"CLF\": \"Chilean Unit of Account (UF)\",\n" +
        "    \"CLP\": \"Chilean Peso\",\n" +
        "    \"CNH\": \"Chinese Yuan (Offshore)\",\n" +
        "    \"CNY\": \"Chinese Yuan\",\n" +
        "    \"COP\": \"Colombian Peso\",\n" +
        "    \"CRC\": \"Costa Rican Col??n\",\n" +
        "    \"CUC\": \"Cuban Convertible Peso\",\n" +
        "    \"CUP\": \"Cuban Peso\",\n" +
        "    \"CVE\": \"Cape Verdean Escudo\",\n" +
        "    \"CZK\": \"Czech Republic Koruna\",\n" +
        "    \"DJF\": \"Djiboutian Franc\",\n" +
        "    \"DKK\": \"Danish Krone\",\n" +
        "    \"DOP\": \"Dominican Peso\",\n" +
        "    \"DZD\": \"Algerian Dinar\",\n" +
        "    \"EGP\": \"Egyptian Pound\",\n" +
        "    \"ERN\": \"Eritrean Nakfa\",\n" +
        "    \"ETB\": \"Ethiopian Birr\",\n" +
        "    \"EUR\": \"Euro\",\n" +
        "    \"FJD\": \"Fijian Dollar\",\n" +
        "    \"FKP\": \"Falkland Islands Pound\",\n" +
        "    \"GBP\": \"British Pound Sterling\",\n" +
        "    \"GEL\": \"Georgian Lari\",\n" +
        "    \"GGP\": \"Guernsey Pound\",\n" +
        "    \"GHS\": \"Ghanaian Cedi\",\n" +
        "    \"GIP\": \"Gibraltar Pound\",\n" +
        "    \"GMD\": \"Gambian Dalasi\",\n" +
        "    \"GNF\": \"Guinean Franc\",\n" +
        "    \"GTQ\": \"Guatemalan Quetzal\",\n" +
        "    \"GYD\": \"Guyanaese Dollar\",\n" +
        "    \"HKD\": \"Hong Kong Dollar\",\n" +
        "    \"HNL\": \"Honduran Lempira\",\n" +
        "    \"HRK\": \"Croatian Kuna\",\n" +
        "    \"HTG\": \"Haitian Gourde\",\n" +
        "    \"HUF\": \"Hungarian Forint\",\n" +
        "    \"IDR\": \"Indonesian Rupiah\",\n" +
        "    \"ILS\": \"Israeli New Sheqel\",\n" +
        "    \"IMP\": \"Manx pound\",\n" +
        "    \"INR\": \"Indian Rupee\",\n" +
        "    \"IQD\": \"Iraqi Dinar\",\n" +
        "    \"IRR\": \"Iranian Rial\",\n" +
        "    \"ISK\": \"Icelandic Kr??na\",\n" +
        "    \"JEP\": \"Jersey Pound\",\n" +
        "    \"JMD\": \"Jamaican Dollar\",\n" +
        "    \"JOD\": \"Jordanian Dinar\",\n" +
        "    \"JPY\": \"Japanese Yen\",\n" +
        "    \"KES\": \"Kenyan Shilling\",\n" +
        "    \"KGS\": \"Kyrgystani Som\",\n" +
        "    \"KHR\": \"Cambodian Riel\",\n" +
        "    \"KMF\": \"Comorian Franc\",\n" +
        "    \"KPW\": \"North Korean Won\",\n" +
        "    \"KRW\": \"South Korean Won\",\n" +
        "    \"KWD\": \"Kuwaiti Dinar\",\n" +
        "    \"KYD\": \"Cayman Islands Dollar\",\n" +
        "    \"KZT\": \"Kazakhstani Tenge\",\n" +
        "    \"LAK\": \"Laotian Kip\",\n" +
        "    \"LBP\": \"Lebanese Pound\",\n" +
        "    \"LKR\": \"Sri Lankan Rupee\",\n" +
        "    \"LRD\": \"Liberian Dollar\",\n" +
        "    \"LSL\": \"Lesotho Loti\",\n" +
        "    \"LYD\": \"Libyan Dinar\",\n" +
        "    \"MAD\": \"Moroccan Dirham\",\n" +
        "    \"MDL\": \"Moldovan Leu\",\n" +
        "    \"MGA\": \"Malagasy Ariary\",\n" +
        "    \"MKD\": \"Macedonian Denar\",\n" +
        "    \"MMK\": \"Myanma Kyat\",\n" +
        "    \"MNT\": \"Mongolian Tugrik\",\n" +
        "    \"MOP\": \"Macanese Pataca\",\n" +
        "    \"MRO\": \"Mauritanian Ouguiya\",\n" +
        "    \"MUR\": \"Mauritian Rupee\",\n" +
        "    \"MVR\": \"Maldivian Rufiyaa\",\n" +
        "    \"MWK\": \"Malawian Kwacha\",\n" +
        "    \"MXN\": \"Mexican Peso\",\n" +
        "    \"MYR\": \"Malaysian Ringgit\",\n" +
        "    \"MZN\": \"Mozambican Metical\",\n" +
        "    \"NAD\": \"Namibian Dollar\",\n" +
        "    \"NGN\": \"Nigerian Naira\",\n" +
        "    \"NIO\": \"Nicaraguan C??rdoba\",\n" +
        "    \"NOK\": \"Norwegian Krone\",\n" +
        "    \"NPR\": \"Nepalese Rupee\",\n" +
        "    \"NZD\": \"New Zealand Dollar\",\n" +
        "    \"OMR\": \"Omani Rial\",\n" +
        "    \"PAB\": \"Panamanian Balboa\",\n" +
        "    \"PEN\": \"Peruvian Nuevo Sol\",\n" +
        "    \"PGK\": \"Papua New Guinean Kina\",\n" +
        "    \"PHP\": \"Philippine Peso\",\n" +
        "    \"PKR\": \"Pakistani Rupee\",\n" +
        "    \"PLN\": \"Polish Zloty\",\n" +
        "    \"PYG\": \"Paraguayan Guarani\",\n" +
        "    \"QAR\": \"Qatari Rial\",\n" +
        "    \"RON\": \"Romanian Leu\",\n" +
        "    \"RSD\": \"Serbian Dinar\",\n" +
        "    \"RUB\": \"Russian Ruble\",\n" +
        "    \"RWF\": \"Rwandan Franc\",\n" +
        "    \"SAR\": \"Saudi Riyal\",\n" +
        "    \"SBD\": \"Solomon Islands Dollar\",\n" +
        "    \"SCR\": \"Seychellois Rupee\",\n" +
        "    \"SDG\": \"Sudanese Pound\",\n" +
        "    \"SEK\": \"Swedish Krona\",\n" +
        "    \"SGD\": \"Singapore Dollar\",\n" +
        "    \"SHP\": \"Saint Helena Pound\",\n" +
        "    \"SLL\": \"Sierra Leonean Leone\",\n" +
        "    \"SOS\": \"Somali Shilling\",\n" +
        "    \"SRD\": \"Surinamese Dollar\",\n" +
        "    \"SSP\": \"South Sudanese Pound\",\n" +
        "    \"STD\": \"S??o Tom?? and Pr??ncipe Dobra\",\n" +
        "    \"SVC\": \"Salvadoran Col??n\",\n" +
        "    \"SYP\": \"Syrian Pound\",\n" +
        "    \"SZL\": \"Swazi Lilangeni\",\n" +
        "    \"THB\": \"Thai Baht\",\n" +
        "    \"TJS\": \"Tajikistani Somoni\",\n" +
        "    \"TMT\": \"Turkmenistani Manat\",\n" +
        "    \"TND\": \"Tunisian Dinar\",\n" +
        "    \"TOP\": \"Tongan Pa'anga\",\n" +
        "    \"TRY\": \"Turkish Lira\",\n" +
        "    \"TTD\": \"Trinidad and Tobago Dollar\",\n" +
        "    \"TWD\": \"New Taiwan Dollar\",\n" +
        "    \"TZS\": \"Tanzanian Shilling\",\n" +
        "    \"UAH\": \"Ukrainian Hryvnia\",\n" +
        "    \"UGX\": \"Ugandan Shilling\",\n" +
        "    \"USD\": \"United States Dollar\",\n" +
        "    \"UYU\": \"Uruguayan Peso\",\n" +
        "    \"UZS\": \"Uzbekistan Som\",\n" +
        "    \"VEF\": \"Venezuelan Bol??var Fuerte\",\n" +
        "    \"VND\": \"Vietnamese Dong\",\n" +
        "    \"VUV\": \"Vanuatu Vatu\",\n" +
        "    \"WST\": \"Samoan Tala\",\n" +
        "    \"XAF\": \"CFA Franc BEAC\",\n" +
        "    \"XAG\": \"Silver Ounce\",\n" +
        "    \"XAU\": \"Gold Ounce\",\n" +
        "    \"XCD\": \"East Caribbean Dollar\",\n" +
        "    \"XDR\": \"Special Drawing Rights\",\n" +
        "    \"XOF\": \"CFA Franc BCEAO\",\n" +
        "    \"XPD\": \"Palladium Ounce\",\n" +
        "    \"XPF\": \"CFP Franc\",\n" +
        "    \"XPT\": \"Platinum Ounce\",\n" +
        "    \"YER\": \"Yemeni Rial\",\n" +
        "    \"ZAR\": \"South African Rand\",\n" +
        "    \"ZMW\": \"Zambian Kwacha\",\n" +
        "    \"ZWL\": \"Zimbabwean Dollar\"\n" +
        "}")