package im.ananse.gateway

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRawValue
import com.mongodb.MongoClient
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import io.vertx.core.logging.LoggerFactory
import org.litote.kmongo.*
import java.util.*

data class DummyResponse(val message: String,
                         val message2: String = "Bro!")
data class ProvisioningResponse ( val message: String  = "SMS notification sent")
data class ProvisioningError ( val message: String  = "Request incorrect")
data class ProvisioningErrorSMS ( val message: String  = "Incorrect telephone number")
data class PushtokenResponse ( val message: String  = "Push token updated",
                               val dtag: Int? = null,
                               val pushtoken: String? = null)
data class PushtokenError ( val message: String  = "Request incorrect")
data class SMSValidationResponse ( val message: String  = "Account updated and provisioned",
                                   val dtag: Int? )
data class SMSValidationError ( val message: String  = "SMS validation failed")
data class SMSValidationDup ( val message: String  = "Account updated",
                              val dtag: Int?)
data class TopupRequestError ( val message: String  = "Topup request validation error")
data class BitcoinResponse (val message:String = "Bitcoin addresses updated")
data class BitcoinError (val message:String = "Error updating Bitcoin addresses")

data class BitcoinGenResponse (val address:String)
data class BitcoinGenError (val message:String)

data class WithdrawalResponse (val message: String = "Withdrawal created")
data class WithdrawalError (val message: String = "Error creating withdrawal")


data class FeelPayResponse (
        val order: String,
        val url: String,
        val postdata: String
)

data class BlockCypherEventRequest(val event:String = "confirmed-tx",
                                   val address:String,
                                   val url: String)


// Ripple
@JsonIgnoreProperties(ignoreUnknown = true)
data class RippleTransactionSubscribe(val method:String = "subscribe",
                                      val params: List<SubscriptionParams>
                                )

data class SubscriptionParams(val accounts: Set<String> = HashSet<String>(),
                              val id: String = "WatchClient",
                              val command: String = "subscribe",
                              val method: String = "subscribe",
                              val url: String
                              )
/* XRP JSON transaction format
{
	"method": "submit",
	"params": [{
		"offline": false,
		"secret": "sncsyaBV1Fu1D5phfVw7nx9sjGzLS",
		"tx_json": {
			"Account": "rLyxXdsUybCVmpYPcmG4EMLBvdw7R2qizc",
			"Amount": 2700000000,
			"Destination": "rddjedtKH4aQscQbJwgUMYQtfkzqpiw5i",
			"TransactionType": "Payment"
		},
		"fee_mult_max": 1000
	}]
}
 */

data class RippleXRPTransaction (val method:String = "submit",
                                 val params: List<XRPPaymentParams>)

data class XRPPaymentParams(var secret: String,
                            var offline: Boolean = false,
                            var fee_mult_max: Int = 1000,
                            var tx_json: XRPTxParams)


data class XRPTxParams @JsonCreator constructor(
        @param:JsonProperty("Account") @get:JsonProperty("Account") var Account: String? = null,
        @param:JsonProperty("Destination") @get:JsonProperty("Destination") var Destination: String? = null,
        @param:JsonProperty("Amount") @get:JsonProperty("Amount") var Amount: Long = 35000000,
        @param:JsonProperty("Fee") @get:JsonProperty("Fee") var Fee: Long = 500,
        @param:JsonProperty("TransactionType") @get:JsonProperty("TransactionType") var TransactionType: String = "Payment"
)


data class RippleCNYTransaction (val method:String = "submit",
                                 val params: List<CNYPaymentParams>)

data class CNYPaymentParams (var secret: String,
                             var offline: Boolean = false,
                             var fee_mult_max: Int = 1000,
                             var tx_json: CNYTxParams)

data class CNYTxParams @JsonCreator constructor (
        @param:JsonProperty("Account") @get:JsonProperty("Account") var Account: String? = null,
        @param:JsonProperty("Destination") @get:JsonProperty("Destination") var Destination: String? = null,
        @param:JsonProperty("Amount") @get:JsonProperty("Amount") var Amount: CNYAmount,
        @param:JsonProperty("Fee") @get:JsonProperty("Fee") var Fee: Long = 500,
        @param:JsonProperty("TransactionType") @get:JsonProperty("TransactionType") var TransactionType: String = "Payment"
)

data class CNYAmount (val currency : String = "CNY",
                      val issuer: String,
                      val value: String)


data class RippleBTCTransaction (val method:String = "submit",
                                 val params: List<BTCPaymentParams>)

data class BTCPaymentParams (var secret: String,
                             var offline: Boolean = false,
                             var fee_mult_max: Int = 1000,
                             var tx_json: BTCTxParams)

data class BTCTxParams @JsonCreator constructor (
        @param:JsonProperty("Account") @get:JsonProperty("Account") var Account: String? = null,
        @param:JsonProperty("Destination") @get:JsonProperty("Destination") var Destination: String? = null,
        @param:JsonProperty("Amount") @get:JsonProperty("Amount") var Amount: BTCAmount,
        @param:JsonProperty("Fee") @get:JsonProperty("Fee") var Fee: Long = 500,
        @param:JsonProperty("TransactionType") @get:JsonProperty("TransactionType") var TransactionType: String = "Payment"
)

data class BTCAmount (val currency : String = "BTC",
                      val issuer: String,
                      val value: String)







enum class Currencies {
    BTC,
    CNY,
    XRP
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class RippleAmount(val currency: Currencies,
                        val issuer: String,
                        val value: String)

enum class RippleTransactionType {
    Payment,
    OfferCreate,
    OfferCancel,
    TrustSet,
    AccountSet,
    SetRegularKey,
    SignerListSet,
    EscrowCreate,
    EscrowFinish,
    EscrowCancel,
    PaymentChannelCreate,
    PaymentChannelFund,
    PaymentChannelClaim
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class RippleTX(val Account: String,
                    val Amount: RippleAmount,
                    val Destination: String,
                    val DestinationTag: String,
                    val TransactionType: RippleTransactionType)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RippleTransactionPayment(val secret: String,
                                    val tx_json: RippleTX
                                    )



/*
{
    "method": "account_tx",
    "params": [
        {
            "account": "rJHscEPC5HmzLBu7uy1QeqGe6XDk2iaxCx",
            "binary": false,
            "count": false,
            "descending": false,
            "forward": false,
            "ledger_index_max": -1,
            "ledger_index_min": -1,
            "limit": 10,
            "offset": 1
        }
    ]
}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RippleTXQueryParams ( val account: String,
                                 val binary: Boolean = false,
                                 val count: Boolean = false,
                                 val forward: Boolean = false,
                                 val ledger_index_max : Int = -1,
                                 val ledger_index_min : Int = -1)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RippleTXQuery ( val method: String = "account_tx",
                           val params: List<RippleTXQueryParams>)


// Blockcypher payment endpoint
// {"destination":"15qx9ug952GWGTNn7Uiv6vode4RcGrRemh","callback_url": "https://my.domain.com/callbacks/new-pay"}
@JsonIgnoreProperties(ignoreUnknown = true)
data class BlockCypherPaymentCreateEndpoint (   val destination: String,
                                                val callback_url: String)



//Mongo data classes (collections)

enum class VPay365Status {
    PENDING,
    COMPLETE,
    PAYMENTRECEIVED,
    ERRORFLAG,
    USERCOMPLETED
}
enum class VPay365ProvStatus {
    PENDING,
    COMPLETE
}


data class BankDetails ( val name : String,
                         val bank: String,
                         val accountnumber: String)


data class User (val address: String,
                 val pubkey : String?,
                 var pushtoken: String? = null,
                 var lasttokentimestamp: Long? = null,
                 var destinationtag: Int? = null,
                 var telnumber : String? = null,
                 var prov_telnumber: String? = null,
                 var last_provreq_timestamp: Long? = null,
                 var smscode: String? = null,
                 var status: VPay365Status = VPay365Status.PENDING,
                 var provisioningstatus: VPay365ProvStatus = VPay365ProvStatus.PENDING,
                 var timecreated: Date? = null,
                 val _id: org.bson.types.ObjectId? = null,
                 val bankdetails: Set<BankDetails>? = null,
                 //var bitcoinaddresses: Array<String?> = arrayOfNulls<String>(5),
                 var bitcoinaddress: String? = null,
                 var blockcypher_callbackid: String? = null,
                 var last_bitcoin_update_timestamp: Long? = null)

data class Topup (val order: String,
                  val timecreated: Date,
                  var timecompleted: Date?,
                  var status: VPay365Status,
                  val amount: Int,
                  val currency: Currencies,
                  val userid: org.bson.types.ObjectId,
                  var txhash: String?,
                  var flowid: String?,
                  var remark: String?,
                  val _id: org.bson.types.ObjectId? = null,
                  var rippleresult: String? = null,
                  var ripplelog: String? = null
                  )

data class Withdrawal (val amount: Float,
                       val currency: Currencies,
                       val userid: org.bson.types.ObjectId,
                       var timecreated: Date? = null, // API time
                       var timecompleted: Date? = null, // Chain time
                       var txhash: String?,
                       var status: VPay365Status,
                       var bankname: String?  = null,
                       var accountnumber: String?  = null,
                       var accountholder: String? = null,
                       var notes: String? = null,
                       val _id: org.bson.types.ObjectId? = null
                       )



class VPay365Repository constructor(mongourl: String, mongodatabase: String ) {
    internal val logger = LoggerFactory.getLogger("Repository")
    var mongoClient: MongoClient? = null
    var mongoDb: String? = null
    var database: MongoDatabase? = null
    init {
        mongoClient = KMongo.createClient(mongourl)
        mongoDb = mongodatabase
        database = mongoClient?.getDatabase(mongoDb)

        createSchema()


    }

    fun close() {
        mongoClient!!.close()
    }

    fun createSchema() {
        val usercol = database!!.getCollection(MONGO_USER)
        usercol.createIndex("{ address: 1 }", IndexOptions().unique(true))
        usercol.createIndex("{ destinationtag: 1 }", IndexOptions().unique(true))
        usercol.createIndex("{ bitcoinaddress: 1 }", IndexOptions())

        val topupcol = database!!.getCollection(MONGO_TOPUP)
        topupcol.createIndex("{ order: 1 }", IndexOptions().unique(true))
        topupcol.createIndex("{ timecreated: 1 }")
        topupcol.createIndex("{ currency: 1 }")
        topupcol.createIndex("{ userid: 1 }")
        topupcol.createIndex("{ txhash: 1 }", IndexOptions())
        topupcol.createIndex("{ flowid: 1 }", IndexOptions())

        val withdrawalcol = database!!.getCollection(MONGO_WITHDRAWAL)
        withdrawalcol.createIndex("{ timecreated: 1 }")
        withdrawalcol.createIndex("{ currency: 1 }")
        withdrawalcol.createIndex("{ userid: 1 }")
        withdrawalcol.createIndex("{ txhash: 1 }")
    }


    fun deleteDatabase() {
        database!!.getCollection(MONGO_USER).drop()
        database!!.getCollection(MONGO_TOPUP).drop()
        database!!.getCollection(MONGO_WITHDRAWAL).drop()
    }

    fun getUserbyId (id: org.bson.types.ObjectId) : User? {
        try{
            val user=database!!.getCollection<User>(MONGO_USER).findOne("{_id: ObjectId('${id}')}")
            return user
        } catch (e:Throwable) {
            return null
        }


    }
    fun getUserbyBitcoinAddress (address: String): User? {
        try {
            val user = database!!.getCollection<User>(MONGO_USER).findOne("{ bitcoinaddress: '${address}' }")
            return user
        } catch (e: Throwable) {
            return null
        }
    }

    fun getUserByRippleAddress(address: String): User? {
        try {
            val user = database!!.getCollection<User>(MONGO_USER).findOne("{ address: '${address}' }")
            return user
        } catch (e: Throwable) {
            return null
        }
    }

    fun getUserByDestinationTag(dtag: Int): User? {
        try {
            val user = database!!.getCollection<User>(MONGO_USER).findOne("{ destinationtag: ${dtag} }")
            return user
        } catch (e: Throwable) {
            return null
        }
    }

    fun getAllUsers(): Set<User> {
        val users = HashSet<User>()
        database!!.getCollection<User>(MONGO_USER).find().forEach<User>({ user ->
            users.add(user)
        })
        return users
    }

    fun saveUser (user: User) {
        try {
            database!!.getCollection<User>(MONGO_USER).save(user)
        } catch (e: MongoWriteException) {
            logger.error("saveUser "+e.localizedMessage)
        }
    }

    fun getWithdrawalsbyUser (user:User) : Set<Withdrawal> {
        val withdrawals = HashSet<Withdrawal>()
        database!!.getCollection<Withdrawal>(MONGO_WITHDRAWAL).find("{userid: ObjectId('${user._id}')}").forEach<Withdrawal>( { withdrawal ->
            withdrawals.add(withdrawal)
        })
        return withdrawals
    }

    fun getWithdrawalbyHash (txhash:String) : Withdrawal? {
        return database!!.getCollection<Withdrawal>(MONGO_WITHDRAWAL).findOne("{txhash: '${txhash}'}")
    }

    fun getPendingWithdrawalsbyUser (user:User) : Set<Withdrawal> {
        val withdrawals = HashSet<Withdrawal>()
        database!!.getCollection<Withdrawal>(MONGO_WITHDRAWAL).find("{userid: ObjectId('${user._id}'), status: 'PENDING'}").forEach<Withdrawal>( { withdrawal ->
            withdrawals.add(withdrawal)
        })
        return withdrawals
    }


    fun getAllPendingWithdrawals () : Set<Withdrawal> {
        val withdrawals = HashSet<Withdrawal>()
        database!!.getCollection<Withdrawal>(MONGO_WITHDRAWAL).find("{status: 'PENDING'}").forEach<Withdrawal>( { withdrawal ->
            withdrawals.add(withdrawal)
        })
        return withdrawals
    }



    fun saveWithdrawal (withdrawal: Withdrawal) {
        try {
            database!!.getCollection<Withdrawal>(MONGO_WITHDRAWAL).save(withdrawal)
        } catch (e: MongoWriteException) {
            logger.error("saveWithdrawal "+e.localizedMessage)
        }
    }


    fun getTopupbyOrder (order: String):Topup? {
        try {
            val topup = database!!.getCollection<Topup>(MONGO_TOPUP).findOne("{order: '${order}'}")
            return topup
        } catch (e: Throwable) {
            return null
        }

    }


    fun getTopupsbyUser (user:User): Set<Topup> {
        val topups = HashSet<Topup>()
        database!!.getCollection<Topup>(MONGO_TOPUP).find("{userid: ObjectId('${user._id}')}").forEach<Topup>({ topup ->
            topups.add(topup)
        })
        return topups
    }

    fun getPendingTopupsbyUser (user:User): Set<Topup> {
        val topups = HashSet<Topup>()
        database!!.getCollection<Topup>(MONGO_TOPUP).find("{userid: ObjectId('${user._id}'), status: 'PENDING'}").forEach<Topup>({ topup ->
            topups.add(topup)
        })
        return topups
    }


    fun saveTopup (topup: Topup) {
        try {
            database!!.getCollection<Topup>(MONGO_TOPUP).save(topup)
        } catch (e: MongoWriteException) {
            logger.error("saveTopup " + e.localizedMessage)
        }
    }

}
