package im.ananse.gateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import im.ananse.gateway.wallet.VPay365Wallet
import io.vertx.core.*
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.net.JksOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.ext.web.client.WebClientOptions
import org.apache.commons.lang3.RandomStringUtils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import java.math.BigInteger
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet


class WebVerticle: AbstractVerticle() {

    internal val logger = LoggerFactory.getLogger("WebSocketVerticle")

    val CALLBACK_PATH_FEELPAY =         "/callbacks/feelpay"
    val CALLBACK_PATH_RIPPLE_GATEWAY =  "/callbacks/ripple/gateway"
    val CALLBACK_PATH_RIPPLE_CLIENTS =  "/callbacks/ripple/clients"
    val CALLBACK_PATH_BTC_GATEWAY =     "/callbacks/btc/gateway"
    val PROVISIONING_REQUEST =          "/provisioning/request"
    val PROVISIONING_SMS_VALIDATION =   "/provisioning/validate"
    val WITHDRAWAL_REQUEST =            "/provisioning/withdrawal"
    val TOPUP_REQUEST =                 "/topup/request"
    val RETURN_PATH_FEELPAY =           "/topup/return"
    val PUSHTOKEN_REGISTRATION =        "/provisioning/pushtoken"
    val BITCOIN_REGISTRATION =          "/provisioning/bitcoin"
    val BITCOIN_GENERATION =            "/provisioning/bitcoingen"
    val CALLBACK_PATH_BTCGENERATED =    "/callbacks/btc/bitcoingen"
    val CONSISTENCY_CHECK =             "/provisioning/check"

    var rippleAdminUrl: URL? = null
    var blockcypherUrl: URL? = null
    var blockcypherPaymentGenUrl: URL? = null

    var repository: VPay365Repository? = null
    var wallet : VPay365Wallet? = null

    var pushy : PushyAPI? = null

    var blockCypherHookList: MutableList<String> = mutableListOf()

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)
        // update config data
        logger.info(System.getenv())

        val baseUrl = System.getenv(BASE_URL)
        if (baseUrl != null && "" != baseUrl) config().put(BASE_URL, baseUrl)

        val baseAdminUrl = System.getenv(RIPPLE_ADMIN_URL)
        if (baseAdminUrl != null && "" != baseAdminUrl) config().put(RIPPLE_ADMIN_URL, baseAdminUrl)

        val port = System.getenv(CONFIG_HTTP_PORT)
        if (port != null) config().put(CONFIG_HTTP_PORT, port)

        val certificate_path = System.getenv(CONFIG_CERTIFICATE_PATH)
        if (certificate_path != null && "" != certificate_path) config().put(CONFIG_CERTIFICATE_PATH, certificate_path)

        val certificate_password = System.getenv(CONFIG_CERTIFICATE_PASSWORD)
        if (certificate_password != null && "" != certificate_password) config().put(CONFIG_CERTIFICATE_PASSWORD, certificate_password)

        val sendgrid_api_key = System.getenv(CONFIG_SENDGRID_API_KEY)
        if (sendgrid_api_key != null && "" != sendgrid_api_key) config().put(CONFIG_SENDGRID_API_KEY, sendgrid_api_key)

        val sendgrid_template_invite_friend = System.getenv(CONFIG_SENDGRID_TEMPLATE_INVITE_FRIEND)
        if (sendgrid_template_invite_friend != null && "" != sendgrid_template_invite_friend) config().put(CONFIG_SENDGRID_TEMPLATE_INVITE_FRIEND, sendgrid_template_invite_friend)

        val sendgrid_from_email = System.getenv(CONFIG_SENDGRID_FROM_EMAIL)
        if (sendgrid_from_email != null && "" != sendgrid_from_email) config().put(CONFIG_SENDGRID_FROM_EMAIL, sendgrid_from_email)

        val hot_wallet_address = System.getenv(CONFIG_HOT_WALLET_ADDRESS)
        if (hot_wallet_address != null && "" != hot_wallet_address) config().put(CONFIG_HOT_WALLET_ADDRESS, hot_wallet_address)

        val hot_wallet_secret = System.getenv(CONFIG_HOT_WALLET_SECRET)
        if (hot_wallet_secret != null && "" != hot_wallet_secret) config().put(CONFIG_HOT_WALLET_SECRET, hot_wallet_secret)

        val cold_wallet_address_btc = System.getenv(CONFIG_COLD_WALLET_ADDRESS_BTC)
        if (cold_wallet_address_btc != null && "" != cold_wallet_address_btc) config().put(CONFIG_COLD_WALLET_ADDRESS_BTC, cold_wallet_address_btc)

        val cold_wallet_address_cny = System.getenv(CONFIG_COLD_WALLET_ADDRESS_CNY)
        if (cold_wallet_address_cny != null && "" != cold_wallet_address_cny) config().put(CONFIG_COLD_WALLET_ADDRESS_CNY, cold_wallet_address_cny)

        val standby_wallet_address = System.getenv(CONFIG_STANDBY_WALLET_ADDRESS)
        if (standby_wallet_address != null && "" != standby_wallet_address) config().put(CONFIG_STANDBY_WALLET_ADDRESS, standby_wallet_address)

        // create MongoDB persistor default shared pool
        val mongouri = System.getenv(CONFIG_MONGODB_URL)
        if (mongouri != null && "" != mongouri) config().put(CONFIG_MONGODB_URL, mongouri)

        val mongodb = System.getenv(CONFIG_MONGODB_DATABASE)
        if (mongodb != null && "" != mongodb) config().put(CONFIG_MONGODB_DATABASE, mongodb)

        val twilio_sid = System.getenv(CONFIG_TWILIO_ACCOUNT_SID)
        if (twilio_sid != null && "" != twilio_sid) config().put(CONFIG_TWILIO_ACCOUNT_SID, twilio_sid)

        val twilio_auth_token = System.getenv(CONFIG_TWILIO_AUTH_TOKEN)
        if (twilio_auth_token != null && "" != twilio_auth_token) config().put(CONFIG_TWILIO_AUTH_TOKEN, twilio_auth_token)

        val twilio_number = System.getenv(CONFIG_TWILIO_NUMBER)
        if (twilio_number != null && "" != twilio_number) config().put(CONFIG_TWILIO_NUMBER, twilio_number)

        val pushy_apikey = System.getenv(CONFIG_PUSHY_APIKEY)
        if (pushy_apikey != null && "" != pushy_apikey) config().put(CONFIG_PUSHY_APIKEY, pushy_apikey)

        val feelpay_paymenturl = System.getenv(CONFIG_FEELPAY_PAYMENT_URL)
        if (feelpay_paymenturl != null && "" != feelpay_paymenturl) config().put(CONFIG_FEELPAY_PAYMENT_URL, feelpay_paymenturl)

        val blockcypher_token = System.getenv(CONFIG_BLOCKCYPHER_TOKEN)
        if (blockcypher_token != null && "" != blockcypher_token) config().put(CONFIG_BLOCKCYPHER_TOKEN, blockcypher_token)

        val blockcypher_chain = System.getenv(CONFIG_BLOCKCYPHER_CHAIN)
        if (blockcypher_chain != null && "" != blockcypher_chain) config().put(CONFIG_BLOCKCYPHER_CHAIN, blockcypher_chain)

        val blockcypher_api = System.getenv(CONFIG_BLOCKCYPHER_API)
        if (blockcypher_api != null && "" != blockcypher_api) config().put(CONFIG_BLOCKCYPHER_API, blockcypher_api)

        val btc_cold_wallet_address = System.getenv(CONFIG_BTC_COLD_WALLET_ADDRESS)
        if (btc_cold_wallet_address != null && "" != btc_cold_wallet_address) config().put(CONFIG_BTC_COLD_WALLET_ADDRESS, btc_cold_wallet_address)

        val btc_standby_wallet_address = System.getenv(CONFIG_BTC_STANDBY_WALLET_ADDRESS)
        if (btc_standby_wallet_address != null && "" != btc_standby_wallet_address) config().put(CONFIG_BTC_STANDBY_WALLET_ADDRESS, btc_standby_wallet_address)

        val btc_hot_wallet_address = System.getenv(CONFIG_BTC_HOT_WALLET_ADDRESS)
        if (btc_hot_wallet_address != null && "" != btc_hot_wallet_address) config().put(CONFIG_BTC_HOT_WALLET_ADDRESS, btc_hot_wallet_address)

        val btc_hot_public_key = System.getenv(CONFIG_BTC_HOT_PUBLIC_KEY)
        if (btc_hot_public_key != null && "" != btc_hot_public_key) config().put(CONFIG_BTC_HOT_PUBLIC_KEY, btc_hot_public_key)

        val btc_hot_private_key = System.getenv(CONFIG_BTC_HOT_PRIVATE_KEY)
        if (btc_hot_private_key != null && "" != btc_hot_private_key) config().put(CONFIG_BTC_HOT_PRIVATE_KEY, btc_hot_private_key)

        val anansepay_app_return_url = System.getenv(CONFIG_APP_RETURN_URL)
        if (anansepay_app_return_url != null && "" != anansepay_app_return_url) config().put(CONFIG_APP_RETURN_URL, anansepay_app_return_url)

    }

    @Throws(Exception::class)
    override fun stop(stopFuture: Future<Void>) {
        //mongoClient!!.close()
        repository!!.close()
        super.stop(stopFuture)
    }

    @Throws(Exception::class)
    override fun start(startFuture: Future<Void>?) {

        super.start(startFuture)
        val router = Router.router(vertx)
        repository = VPay365Repository(config().getString(CONFIG_MONGODB_URL),config().getString(CONFIG_MONGODB_DATABASE))
        Twilio.init(config().getString(CONFIG_TWILIO_ACCOUNT_SID),config().getString(CONFIG_TWILIO_AUTH_TOKEN))
        wallet = VPay365Wallet(config().getString(CONFIG_HOT_WALLET_SECRET))
        pushy = PushyAPI(config().getString(CONFIG_PUSHY_APIKEY))
        //This is needed to decode the incoming request body properly, otherwise the body is always null
        router.route().handler(BodyHandler.create())
        val rng = SecureRandom()
        rng.setSeed(Date().time)

        // Provisioning processing
        // The client sends a JSON object in the following format
        // {
        //   "adddress": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
        //   "telnumber": "+441234567890",
        //   "timestamp": 1500995339,
        //   "pubkey" : "xxxxxxxxxbase64encodedpubkey",
        //   "signature": "XXXXbase64encodedsigXXX"
        // }
        // This functions verifies:
        // a - That the pubkey and the address are related (the address can be calculated from the public key)
        // b - That the message has not been reused (the last used timestamp is saved)
        // c - That the concatenation of address+telnumber+timestamp has been signed with the public key related to that address
        //
        // If all is correct the function then:
        // 1 - Saves the user profile with all these details.
        // 2 - Generates a random 6 digit one time authenticator and sends it via Twilio SMS

        router.route(HttpMethod.POST, PROVISIONING_REQUEST).handler({ routingContext ->

            val response = routingContext.response()

            response.putHeader("content-type", "application/json")
            try {
                val requestBody = routingContext.bodyAsJson
                logger.info("Gateway - $PROVISIONING_REQUEST")
                // 1 - Verify signature on the provisioning request
                //logger.info("Gateway - $PROVISIONING_REQUEST")
                val userdb = verifyProvisioningRequest(requestBody)
                if (userdb != null) {
                    userdb.smscode = RandomStringUtils.randomNumeric(6)
                    // 2 - Create UserID in pending state
                    userdb.status = VPay365Status.PENDING
                    userdb.destinationtag = getDtag(rng)

                    repository!!.saveUser(userdb)
                    // 3 - Create and SMS code and send it via twilio
                    val rtn = sendTwilioSMS(userdb.prov_telnumber!!, userdb.smscode!!)
                    //logger.info("SMS CODE IS "+userdb.smscode.toString())
                    // 4 - Reply with a JSON message to the user
                    if (rtn != "ERROR") {
                        response.setStatusCode(200)
                        response.end(Json.encode(ProvisioningResponse()))
                    } else {
                        response.setStatusCode(401)
                        response.end(Json.encode(ProvisioningErrorSMS()))
                    }
                    // Write to the response and end it
                    //logger.info("Provisioning request done")
                } else {
                    //logger.info("Provisioning request failed")
                    response.setStatusCode(401)
                    response.end(Json.encode(ProvisioningError()))
                }
            } catch (e: io.vertx.core.json.DecodeException) {
                response.setStatusCode(400)
                response.end(Json.encode(ProvisioningError()))

            }

        })


        // SMS Validation
        // The client sends a JSON object in the following format
        // {
        //   "adddress": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
        //   "signature": "xxxxxbase64xxxxxx"
        // }
        // The signature is the signature of the (address+smscode)
        // The function does the following:
        // 1 - verify the that the signature has been signed with keypair related to the stored public key
        //     on the PROVISIONING_REQUEST call and the smscode selected previously on that call
        // 2 - If the provisioning status is set to VPay365ProvStatus.PENDING  the hot wallet sends
        //    35 XRP to this user. If a user reprovisions again (for whatever reason), the account will not receive
        //    new XRP.

        router.route(HttpMethod.POST, PROVISIONING_SMS_VALIDATION).handler({ routingContext ->

            val response = routingContext.response()

            response.putHeader("content-type", "application/json")
            try {
                val requestBody = routingContext.bodyAsJson
                logger.info("Gateway - $PROVISIONING_SMS_VALIDATION")
                // 1 - Verify the SMS code on the provisioning request
                //logger.info("Gateway - $PROVISIONING_SMS_VALIDATION")
                val user = verifyProvisioningSMSValidation(requestBody)
                if (user != null) {
                    if (user.status == VPay365Status.PENDING) {
                        logger.info("Provisioning SMS Validation has successfully verified")
                        user.smscode = null
                        user.telnumber = user.prov_telnumber
                        user.prov_telnumber = null
                        user.destinationtag = getDtag(rng)
                        user.status = VPay365Status.COMPLETE
                        repository!!.saveUser(user) // To do ensure destination tag is not yet used
                        // Has this user been provisioned with XRP
                        if (user.provisioningstatus == VPay365ProvStatus.PENDING) {
                            // Send the user 35 XRP
                            val options = WebClientOptions().setSsl(true)
                            sendProvisioningXRP(user, WebClient.create(vertx,options))
                            // Subscribe the server to transactions on the user
                            user.provisioningstatus = VPay365ProvStatus.COMPLETE
                            // Create random - and unique 32 bit destination tag
                            user.destinationtag = getDtag(rng)
                            repository!!.saveUser(user)

                            // Add user to the notification list
//                            val baseUrl = config().getString(BASE_URL)
//                            rippleAdminUrl = URL(config().getString(RIPPLE_ADMIN_URL))
//                            rippleAdminUrl?.let{
//                                registerWithRippleServer(URL(baseUrl+CALLBACK_PATH_RIPPLE_CLIENTS),
//                                        repository!!.getAllUsers(),
//                                        WebClient.create(vertx, options)) // user accounts
//                            }

                            response.setStatusCode(200)
                            response.end(Json.encode(SMSValidationResponse(dtag = user.destinationtag)))
                        } else {
                            user.destinationtag = getDtag(rng)
                            repository!!.saveUser(user)

//                            // Add user to the notification list
//                            val baseUrl = config().getString(BASE_URL)
//                            val options = WebClientOptions().setSsl(true)
//                            rippleAdminUrl = URL(config().getString(RIPPLE_ADMIN_URL))
//                            rippleAdminUrl?.let{
//                                registerWithRippleServer(URL(baseUrl+CALLBACK_PATH_RIPPLE_CLIENTS),
//                                        repository!!.getAllUsers(),
//                                        WebClient.create(vertx, options)) // user accounts
//                            }


                            response.setStatusCode(200)
                            response.end(Json.encode(SMSValidationDup(dtag = user.destinationtag)))
                        }

                    } else {
                        //logger.info("Provisioning SMS Validation already completed")
                        response.setStatusCode(401)
                        response.end(Json.encode(SMSValidationError()))
                    }

                    //logger.info("Provisioning SMS Validation done")
                } else {
                    //logger.info("Provisioning SMS Validation failed")
                    response.setStatusCode(401)
                    response.end(Json.encode(SMSValidationError()))
                }
            } catch (e: io.vertx.core.json.DecodeException) {
                response.setStatusCode(400)
                response.end(Json.encode(SMSValidationError()))

            }

        })


        // Push token registration
        // In order to register a push token, the client sends a JSON object in the following format
        // {
        //   "address": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
        //   "token: "dfgsdkfjgksdg...sdfgsdfgdsfg",
        //   "timestamp": 1500995339,
        //   "signature": "XXXXbase64encodedsigXXX"
        // }
        //
        // The signature is made of the address+timestamp+token concatenation
        // This function verifies and performs the following actions:
        // a - that the signature is correct and the address+timestamp+token has been signed with the publickey
        // b - That the timestamp is higher than the previous one stored on Mongo (protect against replay attacks)
        // c - Enter the token and the last token timestamp on Mongo and save the user

        router.route(HttpMethod.POST, PUSHTOKEN_REGISTRATION).handler({ routingContext ->

            val response = routingContext.response()
            response.putHeader("content-type", "application/json")
            try {
                val requestBody = routingContext.bodyAsJson
                logger.info("Gateway - $PUSHTOKEN_REGISTRATION")
                val user = verifyPushRegistration(requestBody)
                if (user!= null) {
                    logger.info("Pushtoken registration from client validated")
                    if (user.lasttokentimestamp !=null) {
                        val dbtokentime = user.lasttokentimestamp!!.toLong()
                        val newtimestamp = requestBody.getLong("timestamp")
                        if (newtimestamp> dbtokentime) {
                            user.pushtoken = requestBody.getString("token")
                            user.lasttokentimestamp = requestBody.getLong("timestamp")
                            repository!!.saveUser(user)
                            response.setStatusCode(200)
                            response.end(Json.encode(PushtokenResponse(dtag=user.destinationtag,
                                    pushtoken = user.pushtoken)))
                            return@handler
                        }
                        response.setStatusCode(401)
                        response.end(Json.encode(PushtokenError()))
                        return@handler
                    } else {
                        user.pushtoken = requestBody.getString("token")
                        user.lasttokentimestamp = requestBody.getLong("timestamp")
                        repository!!.saveUser(user)
                        response.setStatusCode(200)
                        response.end(Json.encode(PushtokenResponse(dtag=user.destinationtag,
                                pushtoken = user.pushtoken)))
                        return@handler
                    }


                } else {
                    response.setStatusCode(401)
                    response.end(Json.encode(PushtokenError()))
                    return@handler
                }
            } catch (e: io.vertx.core.json.DecodeException) {
                response.setStatusCode(400)
                response.end(Json.encode(PushtokenError()))

            }
        })

        // Withdrawal request
        // This function generates (or updates an existing) withdrawal request
        // In order to register the withdrawal, the client sends a JSON object in the following format
        // {
        //   "address": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
        //   "amount": 10.50,
        //   "currency": "CNY", or "BTC",
        //   "bankname": "Bank of China",
        //   "accountnumber": "1231230494949",
        //   "accountholder": "Leeroy Jenkins",
        //   "txhash" : "D93D89DCBF9320F6576E4A57C4E31983107A92D8A62C7FF50773CC217017088F",
        //   "signature": "XXXXbase64encodedsigXXX"
        // }
        // with the signature being made out of:
        // address+amount.toString()+currency+bankname+accountnumber+accountholder+txhash
        //
        router.route(HttpMethod.POST, WITHDRAWAL_REQUEST).handler({ routingContext ->
            val response = routingContext.response()
            response.putHeader("content-type", "application/json")
            try {
                val requestBody = routingContext.bodyAsJson
                logger.info("Gateway - $WITHDRAWAL_REQUEST")
                val user = verifyWithdrawalRequest(requestBody)
                if (user!= null) {
                    // 1 - Does the tx hash already exist on the withdrawal collection in mongo
                    val withdrawal = repository!!.getWithdrawalbyHash(requestBody.getString("txhash"))
                    if (withdrawal == null) {
                        // Create a withdrawal entry
                        val newwithdrawal = Withdrawal(
                                amount = requestBody.getFloat("amount"),
                                currency = Currencies.valueOf(requestBody.getString("currency")),
                                timecreated = Date(),
                                txhash = requestBody.getString("txhash"),
                                status = VPay365Status.PENDING,
                                userid = user._id!!,
                                bankname = requestBody.getString("bankname"),
                                accountholder = requestBody.getString("accountholder"),
                                accountnumber = requestBody.getString("accountnumber"),
                                timecompleted = null
                        )
                        repository!!.saveWithdrawal(newwithdrawal)
                        response.setStatusCode(201)
                        response.end(Json.encode(WithdrawalResponse(message = "Withdrawal created")))
                        return@handler
                    } else {
                        if (withdrawal.status == VPay365Status.PAYMENTRECEIVED) {
                            // This is a duplicate API call
                            response.setStatusCode(302)
                            response.end(Json.encode(WithdrawalResponse(message = "Withdrawal not modified")))
                            return@handler
                        }
                        if (withdrawal.timecompleted != null) { // If the transaction has already been seen on the chain
                            // Update this withdrawal entry with the bank information
                            withdrawal.accountholder = requestBody.getString("accountholder")
                            withdrawal.accountnumber = requestBody.getString("accountnumber")
                            withdrawal.bankname = requestBody.getString("bankname")
                            withdrawal.timecreated = Date()

                            // This withdrawal is already in the chain and API
                            withdrawal.status = VPay365Status.PAYMENTRECEIVED
                            val amountapi = requestBody.getFloat("amount")
                            val currencyapi = Currencies.valueOf(requestBody.getString("currency"))
                            if (withdrawal.amount != amountapi || withdrawal.currency != currencyapi) {
                                withdrawal.notes = "DIFF Chain amount ${withdrawal.amount} ${withdrawal.currency} and API amount ${amountapi} ${currencyapi}"
                            } else {
                                withdrawal.notes = "API and CHAIN values match"
                            }
                            repository!!.saveWithdrawal(withdrawal)

                            response.setStatusCode(202)
                            response.end(Json.encode(WithdrawalResponse(message = "Withdrawal updated")))
                            return@handler
                        } else {
                            response.setStatusCode(302)
                            response.end(Json.encode(WithdrawalResponse(message = "Withdrawal not modified")))
                            return@handler
                        }
                    }
                }
            } catch (e: io.vertx.core.json.DecodeException) {
                // Is this JSON? Nope
                response.setStatusCode(400)
                response.end(Json.encode(WithdrawalError(message = "Incorrect request format")))

            }
        })








        // Bitcoin address generation
        // This function generates a payment BTC address that forwards to the cold wallet using the
        // Blockcypher API
        // In order to get a bitcoin address, the client sends a JSON object in the following format
        // {
        //   "address": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
        //   "timestamp": 1500995339,
        //   "signature": "XXXXbase64encodedsigXXX"
        // }
        // with the signature being the signature of the address+timestamp


        router.route(HttpMethod.POST, BITCOIN_GENERATION).handler({ routingContext ->
            val response = routingContext.response()
            response.putHeader("content-type", "application/json")
            try {
                val requestBody = routingContext.bodyAsJson
                logger.info("Gateway - $BITCOIN_GENERATION")
                val user = verifyBitcoinGeneration(requestBody)
                if (user!= null) {

                    logger.info("Bitcoin generation from client validated")
                    if (user.bitcoinaddress == null) {
                        val options = WebClientOptions().setSsl(true)
                        // Call some blocking API that takes a significant amount of time to return
                        var httpresponse = generateBlockCypherPaymentAddressBlocking(user, WebClient.create(vertx, options))
                        if (httpresponse == null) {
                            response.setStatusCode(401)
                            response.end(Json.encode(BitcoinGenError(message="Unable to get address from API")))
                            return@handler
                        }
                        user.bitcoinaddress = httpresponse.getString("input_address")
                        user.blockcypher_callbackid = httpresponse.getString("id")
                        user.last_bitcoin_update_timestamp = requestBody.getLong("timestamp")
                        repository!!.saveUser(user)
                        response.setStatusCode(200)
                        response.end(Json.encode(BitcoinGenResponse(address = user.bitcoinaddress!!)))
                        return@handler
                    } else {
                        if (user.last_bitcoin_update_timestamp != null) {
                            if (user.last_bitcoin_update_timestamp!! >= requestBody.getLong("timestamp")) {
                                response.setStatusCode(401)
                                response.end(Json.encode(BitcoinGenError(message="Duplicated request")))
                                return@handler
                            }
                        }
                        user.last_bitcoin_update_timestamp=requestBody.getLong("timestamp")
                        repository!!.saveUser(user)
                        response.setStatusCode(200)
                        response.end(Json.encode(BitcoinGenResponse(address=user.bitcoinaddress!!)))
                        return@handler
                    }
                } else {
                    // User not found
                    response.setStatusCode(401)
                    response.end(Json.encode(BitcoinGenError(message="User not found")))
                    return@handler
                }
            } catch (e: io.vertx.core.json.DecodeException) {
                // Is this JSON? Nope
                response.setStatusCode(400)
                response.end(Json.encode(BitcoinGenError(message = "Incorrect request format")))

            }
        })


        router.route(HttpMethod.GET, CONSISTENCY_CHECK).handler({ routingContext ->
            val response = routingContext.response()
            response.putHeader("content-type", "application/json")

            logger.info("Gateway - $CONSISTENCY_CHECK")
            //Non-authenticated call - so far - just testing
            // Redo the subscriptions on the ripple server
            val baseUrl = config().getString(BASE_URL)
            rippleAdminUrl = URL(config().getString(RIPPLE_ADMIN_URL))
            val options = WebClientOptions().setSsl(true)
            val webClient = WebClient.create(vertx, options)

            rippleAdminUrl?.let{
                registerWithRippleServer(URL(
                        baseUrl+CALLBACK_PATH_RIPPLE_GATEWAY),
                        setOf(
                                User(config().getString(CONFIG_HOT_WALLET_ADDRESS), null, destinationtag = 123456),
                                User(config().getString(CONFIG_COLD_WALLET_ADDRESS_BTC), null, destinationtag = 123456),
                                User(config().getString(CONFIG_COLD_WALLET_ADDRESS_CNY), null, destinationtag = 123456),
                                User(config().getString(CONFIG_STANDBY_WALLET_ADDRESS), null, destinationtag = 123456)
                        ),
                        webClient) // hotwallet, coldwallet, standbywallet
                registerWithRippleServer(URL(baseUrl+CALLBACK_PATH_RIPPLE_CLIENTS),
                        repository!!.getAllUsers(),
                        webClient) // user accounts
                getTransactionsfromLedger(webClient)
            }




            // Delete unneeded payment forwards
            val fwaddrlist = getBlockCypherForwardingAddresses()
            if (fwaddrlist != null) {
                // 1 - For each element on the list
                for (i in 0..(fwaddrlist.list.size - 1)) {
                    val addr = fwaddrlist.get<JsonObject>(i)

                    // 1.a - check  getString("token") is the local blockcypher token, if not continue
                    if (addr.getString("token") != config().getString(CONFIG_BLOCKCYPHER_TOKEN)) continue
                    // 1.b - check  getString("callback_url") is contains local hostname, if not continue
                   if (!addr.getString("callback_url").contains(config().getString(BASE_URL))) continue
                    // 1.c - check getString("callback_url") is the local callback URL (/callbacks/btc/bitcoingen), if not delete from API
                    if (!addr.getString("callback_url").contains(CALLBACK_PATH_BTCGENERATED)) {
                        logger.error("DELETE wrong callback - $addr")
                        // delete from API
                        if (deleteBlockCypherForwardingAddress(addr.getString("id"))) {
                            // delete address from database if present
                            val user = repository!!.getUserbyBitcoinAddress(addr.getString("input_address"))
                            if (user !=null) {
                                user.bitcoinaddress = null
                                user.last_bitcoin_update_timestamp = null
                                logger.error("Removed BTC topup address from user ${user.address}")
                                repository!!.saveUser(user)
                            }
                        }
                        continue
                    }
                    // 1.d - check getString("destination") is the cold wallet, if not delete from API
                    if (addr.getString("destination") != config().getString(CONFIG_BTC_COLD_WALLET_ADDRESS)) {
                        // delete from API
                        logger.error("DELETE wrong coldwallet - $addr")
                        // delete from API
                        if (deleteBlockCypherForwardingAddress(addr.getString("id"))) {
                            // delete address from database if present
                            val user = repository!!.getUserbyBitcoinAddress(addr.getString("input_address"))
                            if (user !=null) {
                                user.bitcoinaddress = null
                                user.last_bitcoin_update_timestamp = null
                                logger.error("Removed BTC topup address from user ${user.address}")
                                repository!!.saveUser(user)
                            }
                        }
                        continue
                    }
                    // 1.e - check getString("input_address") exists on database, if not delete from API
                    if (repository!!.getUserbyBitcoinAddress(addr.getString("input_address"))== null) {
                        // delete from API
                        logger.error("DELETE not present on DB - $addr")
                        // delete from API
                        deleteBlockCypherForwardingAddress(addr.getString("id"))
                        continue
                    }
                    logger.error("CORRECT - $addr")
                }
            }

            //Delete out-of-date hooks
            val hookslist = getBlockCypherHooks()
            if (hookslist != null) {
                // 2 - For each element on the list
                for (i in 0..(hookslist.list.size - 1)) {
                    val hook = hookslist.get<JsonObject>(i)
                    // 2.a - check  getString("token") is the local blockcypher token, if not continue
                    if (hook.getString("token") != config().getString(CONFIG_BLOCKCYPHER_TOKEN)) continue
                    // 2.b - check  getString("callback_url") is contains local hostname, if not continue
                    if (!hook.getString("url").contains(config().getString(BASE_URL))) continue
                    // 2.c - check getString("url") is the local callback URL (/callbacks/btc/bitcoingen), if not delete from API
                    if (!hook.getString("url").contains(CALLBACK_PATH_BTC_GATEWAY)) {
                        logger.error("DELETE hook wrong callback - $hook")
                        // delete from API
                        deleteBlockCypherHook(hook.getString("id"))
                        continue
                    }
                    // 2.d - check getString("address") is one of the BTC wallets, if not delete from API
                    if (!(hook.getString("address") == config().getString(CONFIG_BTC_COLD_WALLET_ADDRESS) ||
                            hook.getString("address") == config().getString(CONFIG_BTC_STANDBY_WALLET_ADDRESS) ||
                            hook.getString("address") == config().getString(CONFIG_BTC_HOT_WALLET_ADDRESS))) {
                        logger.error("DELETE hook wrong BTC address - $hook")
                        // delete from API
                        deleteBlockCypherHook(hook.getString("id"))
                        continue
                    }
                    // 2.e - Check getString("id") is on the list of current hooks
                    if (!blockCypherHookList.contains(hook.getString("id"))) {
                        logger.error("DELETE hook not on current system - $hook")
                        // delete from API
                        deleteBlockCypherHook(hook.getString("id"))
                        continue
                    }
                    logger.error("CORRECT hook - $hook")
                }
            }
            response.setStatusCode(200)
            response.end()

        })




/*
        // Bitcoin allowed address registration
        // In order to register a list of bitcoin addresses as valid sources of BTC topup funds (max 5)
        // the client sends the following JSON
        // {
        //  "address": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
        //  "btcaddresses": [
        //      "BsMKtmYX41HRmMBLr7LKXcg1HKbgAXUi00",
        //      "BsMKtmYX41HRmMBLr7LKXcg1HKbgAXUi01",
        //      "BsMKtmYX41HRmMBLr7LKXcg1HKbgAXUi02"
        //   ],
        //  "timestamp": 1500995339,
        //  "signature": "XXXXbase64encodedsigXXX"
        // }
        //
        // The signature is made of the address+timestamp+btcaddresses(concat) concatenation
        // This function verifies and performs the following actions:
        // a - that the signature is correct and the address+timestamp+btcaddresses(concat) has been signed with the publickey
        // b - That the timestamp is higher than the previous one stored on Mongo (protect against replay attacks)
        // c - Enter the btcaddresses and the last btc update timestamp on Mongo and save the user
        //


        router.route(HttpMethod.POST, BITCOIN_REGISTRATION).handler({ routingContext ->

            val response = routingContext.response()
            response.putHeader("content-type", "application/json")
            try {
                val requestBody = routingContext.bodyAsJson
                logger.info("Gateway - $BITCOIN_REGISTRATION")
                val user = verifyBitcoinRegistration(requestBody)
                if (user!= null) {
                    logger.info("Bitcoin addresses from client validated")
                    if (user.last_bitcoin_update_timestamp !=null) {
                        val dbtokentime = user.last_bitcoin_update_timestamp!!.toLong()
                        val newtimestamp = requestBody.getLong("timestamp")
                        if (newtimestamp> dbtokentime) {
                            var btclist = requestBody.getJsonArray("btcaddresses")
                            var i = 0
                            while (i< btclist.size()) {
                                user.bitcoinaddresses[i]=btclist[i]
                                i++
                            }

                            user.last_bitcoin_update_timestamp = requestBody.getLong("timestamp")
                            repository!!.saveUser(user)
                            response.setStatusCode(200)
                            response.end(Json.encode(BitcoinResponse()))
                            return@handler
                        }
                        response.setStatusCode(401)
                        response.end(Json.encode(BitcoinError()))
                        return@handler
                    } else {
                        var btclist = requestBody.getJsonArray("btcaddresses")
                        var i = 0
                        while (i< btclist.size()) {
                            user.bitcoinaddresses[i]=btclist[i]
                            i++
                        }
                        user.last_bitcoin_update_timestamp = requestBody.getLong("timestamp")
                        repository!!.saveUser(user)
                        response.setStatusCode(200)
                        response.end(Json.encode(BitcoinResponse()))
                        return@handler
                    }


                } else {
                    response.setStatusCode(401)
                    response.end(Json.encode(BitcoinError()))
                    return@handler
                }
            } catch (e: io.vertx.core.json.DecodeException) {
                response.setStatusCode(400)
                response.end(Json.encode(BitcoinError()))

            }
        })
*/






        // Topup request
        // The client sends a JSON object in the following format
        // {
        //   "adddress": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
        //   "amount": 10000, (integer in cents)
        //   "currency": "CNY", .. or "BTC"
        //   "signature": "XXXXbase64encodedsigXXX"
        // }
        // This functions verifies:
        // a - That the concatenation of address+amount+currency has been signed with the public key related to that address
        // b - Log a pending entry on the topup collection
        // c - Construct and sign a URL for the FeelPay Internet Payment API
        // d - Deliver that URL and the POSTDATA string to the user in a JSON package
        // {
        //  "order" : "C00001xXXXXXXXX",
        //  "url": "http://121.201.38.37:8076/api/payment.aspx",
        //  "postdata": "....postdatastring......"
        // }


        router.route(HttpMethod.POST, TOPUP_REQUEST).handler({ routingContext ->

            val response = routingContext.response()
            response.putHeader("content-type", "application/json")
            try {
                val requestBody = routingContext.bodyAsJson
                logger.info("Gateway - $TOPUP_REQUEST")
                val user = verifyTopupRequest(requestBody)
                if (user!= null) {
                    logger.info("Topup request from client validated")
                    if (requestBody.getString("currency")=="CNY") {
                        // Generate FeelPay API Internet gateway URL and postdata
                        val feelpayresponse = generateFeelPayAPItopup(requestBody)

                        val topup = Topup (
                                order = feelpayresponse.order,
                                timecreated = Date(),
                                timecompleted = null,
                                status = VPay365Status.PENDING,
                                amount = requestBody.getInteger("amount"),
                                currency = Currencies.CNY,
                                userid = user._id!!,
                                txhash = null,
                                flowid = null,
                                remark = null
                        )
                        repository!!.saveTopup(topup)
                        response.setStatusCode(200)
                        response.end(Json.encode(feelpayresponse))
                        logger.info("Feelpay request topup")
                    } else {
                        response.setStatusCode(400)
                        response.end(Json.encode(TopupRequestError()))
                        logger.info ("Currency not yet supported - BTC??")
                    }


                } else {
                    logger.info ("Topup signature validation failure")
                    response.setStatusCode(400)
                    response.end(Json.encode(TopupRequestError()))
                }
            } catch (e: io.vertx.core.json.DecodeException) {
                logger.info ("Topup JSON decode failure")
                response.setStatusCode(400)
                response.end(Json.encode(TopupRequestError()))

            }
        })




        router.route(HttpMethod.POST, CALLBACK_PATH_FEELPAY).handler({ routingContext ->

            val response = routingContext.response()
            val parameters = routingContext.request().formAttributes()
            logger.info("Gateway - $CALLBACK_PATH_FEELPAY")
            if (verifyFeelPayCallBackMd5 (parameters)) {
                // 1 - Check the the topup in question is still pending on the database
                val topup = repository!!.getTopupbyOrder(parameters.get("BillNo"))
                if (topup != null) {
                    // Check the amount is the same
                    assert(topup.amount == parameters.get("Amount").toInt())
                    if (topup.status == VPay365Status.PAYMENTRECEIVED) {
                        if (topup.flowid == parameters.get("FlowId")) {
                            // This is a resend
                            logger.info("Feelpay gateway just resent tx with Flowid: "+topup.flowid+ "   IGNORING")
                            response.putHeader("content-type", "text/plain")
                            response.end("SUCCESS")
                        }
                    }
                    // Update parameters
                    topup.flowid=parameters.get("FlowId")
                    topup.timecompleted=Date()
                    topup.status = VPay365Status.PAYMENTRECEIVED
                    repository!!.saveTopup(topup)

                    // Get the user related to that order number
                    val user = repository!!.getUserbyId(topup.userid)
                    if (user != null) {
                        // Send the CNY amount to the user from the hot wallet
                        // get the ripple transaction hash
                        // update the topup entry
                        val options = WebClientOptions().setSsl(true)
                        sendTopupCNY(user,topup,WebClient.create(vertx, options))
                        // Send a push to the user, so the CNY amount can be refreshed
                        pushtoUserRippleAddress(user.address)
                    }

                    logger.info("Feelpay MD5 hash has successfully verified")
                } else {
                    logger.info("Feelpay callback without a database match")
                }
            }

            response.putHeader("content-type", "text/plain")
            response.end("SUCCESS")
            logger.info("Feelpay success callback done")

        })

        router.route(HttpMethod.POST, CALLBACK_PATH_RIPPLE_GATEWAY).handler({ routingContext ->

            val response = routingContext.response()
            response.putHeader("content-type", "application/json")

            val requestBody = routingContext.bodyAsJson

            if (requestBody.getString("method") == "event") {
                // This is an event we have to process
                val params = requestBody.get<JsonObject>("params")
                processGatewayEvent(params)
            }
            logger.info("Gateway - $CALLBACK_PATH_RIPPLE_GATEWAY")
            logger.info(requestBody)

            // Write to the response and end it
            response.end(Json.encode(DummyResponse(message = "Bro!", message2 = "Gateway")))
        })

        router.route(HttpMethod.POST, CALLBACK_PATH_RIPPLE_CLIENTS).handler({ routingContext ->

            val response = routingContext.response()
            response.putHeader("content-type", "application/json")

            val requestBody = routingContext.bodyAsJson
            if (requestBody.getString("method") == "event") {
                // This is an event we have to process
                val params = requestBody.get<JsonObject>("params")
                processClientEvent(params)
            }
            logger.info("Gateway - $CALLBACK_PATH_RIPPLE_CLIENTS")
            logger.info(requestBody)

            // Write to the response and end it
            response.end(Json.encode(DummyResponse(message = "Bro!", message2 = "Gateway")))
        })
        //
        // {
        // "block_hash":"00004832914fd72e62c7bb3eb01551fa6d530fafe8be4e563bfb979b8ad8da6e",
        // "block_height":1482820,
        // "block_index":1,
        // "hash":"99d093deded8571d6325a972dff1ca5e8c40cdf6a800e402728f7d971e26cd50",
        // "addresses":["CCrLmFzvwGkoCUe2za3tGokHEjXbj7Nig6","CFr99841LyMkyX5ZTGepY58rjXJhyNGXHf"],
        // "total":39052500,
        // "fees":10000,
        // "size":192,
        // "preference":"low",
        // "relayed_by":"127.0.0.1:36736",
        // "confirmed":"2017-09-09T16:55:42Z",
        // "received":"2017-09-09T16:55:15.597Z",
        // "ver":1,
        // "double_spend":false,
        // "vin_sz":1,
        // "vout_sz":2,
        // "confirmations":1,
        // "inputs":[{
        //      "prev_hash":"47aa7bbd72ab57aa5580b9c9397e0b1de841b5ad61e8b2bd8a08f2beb29f5ac0",
        //      "output_index":0,
        //      "script":"483045022100a5a81533cd1e2e3f0d2203022db697622e69b8c1b10180fe06faf37cb2d3ba980220422ff682653507b4746a8b12d98f1bd729e80fa55c2db866ddfce78e776b3a4301",
        //      "output_value":39062500,
        //      "sequence":4294967295,
        //      "addresses":["CFr99841LyMkyX5ZTGepY58rjXJhyNGXHf"],
        //      "script_type":"pay-to-pubkey",
        //      "age":1482801
        //      }],
        // "outputs":[{
        //      "value":1000000,
        //      "script":"76a914d85e83603bb4c5b1aabcc70de660c4614fffe82c88ac",
        //      "addresses":["CCrLmFzvwGkoCUe2za3tGokHEjXbj7Nig6"],
        //      "script_type":"pay-to-pubkey-hash"
        //      },
        //      {
        //      "value":38052500,
        //      "script":"76a914f93d302789520e8ca07affb76d4ba4b74ca3b3e688ac"
        //      ,"addresses":["CFr99841LyMkyX5ZTGepY58rjXJhyNGXHf"],
        //      "script_type":"pay-to-pubkey-hash"
        //      }]
        // }

        router.route(HttpMethod.POST, CALLBACK_PATH_BTC_GATEWAY).handler({ routingContext ->

            val response = routingContext.response()
            response.putHeader("content-type", "application/json")

            val requestBody = routingContext.bodyAsJson
            logger.info("BTC Gateway - $CALLBACK_PATH_BTC_GATEWAY")
            logger.info(requestBody)

            if (repository!!.getTopupbyOrder(requestBody.getString("hash")) !=null) {
                logger.info ("Duplicated transaction")
                response.end()
                return@handler

            } else {
                // Get the user this topup is aimed at: input address should be registered in the system
                var addrlist = requestBody.getJsonArray("addresses")
                var user: User? = null
                for (add in addrlist) {
                    user = repository!!.getUserbyBitcoinAddress(add.toString())
                    if (user != null) break
                }
                if (user == null) {
                    logger.info("Transaction not from any known user")
                    response.end()
                } else {
                    // Get the amount
                    var outputs = requestBody.getJsonArray("outputs").getJsonObject(0)
                    for (out in outputs) {
                        var addressesdest = outputs.getJsonArray("addresses")
                        for (add in addressesdest) {
                            if (add == config().getString(CONFIG_BTC_COLD_WALLET_ADDRESS)) {
                                logger.info("Creating topup")
                                if (repository!!.getTopupbyOrder(requestBody.getString("hash"))== null) {
                                    val topup = Topup(
                                            order = requestBody.getString("hash"),
                                            timecreated = Date(),
                                            timecompleted = null,
                                            status = VPay365Status.PENDING,
                                            amount = outputs.getInteger("value"),
                                            currency = Currencies.BTC,
                                            userid = user._id!!,
                                            txhash = null,
                                            flowid = null,
                                            remark = null
                                    )
                                    repository!!.saveTopup(topup)
                                    val options = WebClientOptions().setSsl(true)
                                    sendTopupBTC(user, topup, WebClient.create(vertx, options))
                                    pushtoUserRippleAddress(user.address)
                                    return@handler
                                }
                            }
                        }
                    }




                    logger.info("Transaction for a known user - topping up")
                    response.end()
                    return@handler
                }
            }
        })

        // Event sent by the
        //{
        //"value": 100000000,
        //"input_address": "16uKw7GsQSzfMaVTcT7tpFQkd7Rh9qcXWX",
        //"destination": "15qx9ug952GWGTNn7Uiv6vode4RcGrRemh",
        //"input_transaction_hash": "39bed5d...",
        //"transaction_hash": "1aa6103..."
        //}

        router.route(HttpMethod.POST, CALLBACK_PATH_BTCGENERATED).handler({ routingContext ->

            val response = routingContext.response()
            response.putHeader("content-type", "application/json")
            var user: User?
            val requestBody = routingContext.bodyAsJson
            logger.info("BTC Gateway - $CALLBACK_PATH_BTCGENERATED")
            logger.info(requestBody)

            if (repository!!.getTopupbyOrder(requestBody.getString("transaction_hash")) !=null) {
                logger.info ("Duplicated transaction")
                response.end()
                return@handler

            } else {
                // Get the user this topup is aimed at: input address should be registered in the system
                var address = requestBody.getString("input_address")
                user = repository!!.getUserbyBitcoinAddress(address)
                if (user == null) {
                    logger.info("Transaction not from any known user")
                    response.end()
                    return@handler
                } else {
                    // Get the amount in satoshis
                    var amount = requestBody.getInteger("value")
                    logger.info("Creating topup")
                    if (repository!!.getTopupbyOrder(requestBody.getString("transaction_hash")) == null) {
                        val topup = Topup(
                                order = requestBody.getString("transaction_hash"),
                                timecreated = Date(),
                                timecompleted = Date(),
                                status = VPay365Status.COMPLETE,
                                amount = amount,
                                currency = Currencies.BTC,
                                userid = user!!._id!!,
                                txhash = null,
                                flowid = null,
                                remark = null
                        )
                        repository!!.saveTopup(topup)
                        val options = WebClientOptions().setSsl(true)
                        sendTopupBTC(user!!, topup, WebClient.create(vertx, options))
                        pushtoUserRippleAddress(user!!.address)
                        response.end()
                        return@handler
                    }
                }




                    logger.info("Transaction for a known user - topping up")
                    response.end()
                    return@handler
                }
        })

        //
        // This is the endpoint where the users end up after a payment procedure
        // The return URL is called BEFORE the notify URL,The transaction will still be pending.
        // this will have to redirect to another page that will refresh this topup state
        // until the success code is displayed
        router.route(HttpMethod.POST, RETURN_PATH_FEELPAY).handler({ routingContext ->

            val response = routingContext.response()
            val parameters = routingContext.request().formAttributes()
            logger.info("Gateway - $RETURN_PATH_FEELPAY")
            logger.info("Parameters: "+parameters.toString())


            if (verifyFeelPayCallBackMd5(parameters)){
                val order = parameters.get("BillNo")
                val topup = repository!!.getTopupbyOrder(order)
                if (topup != null) {
                    //The topup may not have completed yet, mark that the user has completed the flow
                    topup.status= VPay365Status.USERCOMPLETED
                    repository!!.saveTopup(topup)
                    //val returnstr = topup.toString()
                    val userstring = "success"
                    val returnstr = "<html><meta http-equiv=\"refresh\" content=\"0;url="+config().getString(CONFIG_APP_RETURN_URL)+"?result="+userstring+"\" /></html>"
                    response.putHeader("content-type", "text/html")
                    response.end(returnstr)
                    logger.error("Topup order ${topup.order} of ${topup.amount} ${topup.currency} SUCCESSFUL")
                } else {
                    val userstring = "failure"
                    val returnstr = "<html><meta http-equiv=\"refresh\" content=\"0;url="+config().getString(CONFIG_APP_RETURN_URL)+"?result="+userstring+"\" /></html>"
                    response.putHeader("content-type", "text/html")
                    response.end(returnstr)
                    logger.error("Topup order $order NOT FOUND")
                }
            } else {
                val userstring = "failure"
                val returnstr = "<html><meta http-equiv=\"refresh\" content=\"0;url="+config().getString(CONFIG_APP_RETURN_URL)+"?result="+userstring+"\" /></html>"
                response.putHeader("content-type", "text/html")
                response.end(returnstr)
                logger.error("Topup order MALFORMED")
            }
        })


        val deploymentOptions = DeploymentOptions()
                .setConfig(config())
                .setInstances(1)

        val certificatePath = config().getString(CONFIG_CERTIFICATE_PATH)
        val certificatePassword = config().getString(CONFIG_CERTIFICATE_PASSWORD)

        var terminateTLS = false
        if (certificatePassword != null && !certificatePassword.isEmpty() &&
                certificatePath != null && !certificatePath.isEmpty()) {
            terminateTLS = true
        }

        val httpServerOptions = HttpServerOptions()
        val httpServer: HttpServer
        if (terminateTLS) {
            logger.info("Setup SSL - path=" + certificatePath!!)
            httpServerOptions.setKeyStoreOptions(JksOptions()
                    .setPath(certificatePath)
                    .setPassword(certificatePassword)).isSsl = true
        }

        httpServerOptions.isTcpNoDelay = true
        httpServerOptions.idleTimeout = 90
        httpServerOptions.maxWebsocketFrameSize = 2621440

        vertx.createHttpServer(httpServerOptions).requestHandler({router.accept(it)}).listen(config().getString(CONFIG_HTTP_PORT).toInt())

        val baseUrl = config().getString(BASE_URL)
        rippleAdminUrl = URL(config().getString(RIPPLE_ADMIN_URL))
        val options = WebClientOptions().setSsl(true)
        val webClient = WebClient.create(vertx, options)

        rippleAdminUrl?.let{
            registerWithRippleServer(URL(
                    baseUrl+CALLBACK_PATH_RIPPLE_GATEWAY),
                    setOf(
                            User(config().getString(CONFIG_HOT_WALLET_ADDRESS), null, destinationtag = 123456),
                            User(config().getString(CONFIG_COLD_WALLET_ADDRESS_BTC), null, destinationtag = 123456),
                            User(config().getString(CONFIG_COLD_WALLET_ADDRESS_CNY), null, destinationtag = 123456),
                            User(config().getString(CONFIG_STANDBY_WALLET_ADDRESS), null, destinationtag = 123456)
                    ),
                    webClient) // hotwallet, coldwallet, standbywallet
            registerWithRippleServer(URL(baseUrl+CALLBACK_PATH_RIPPLE_CLIENTS),
                    repository!!.getAllUsers(),
                    webClient) // user accounts
            getTransactionsfromLedger(webClient)
        }


        var blockCypherReply: JsonObject?
        blockcypherUrl = URL(config().getString(CONFIG_BLOCKCYPHER_API)+config().getString(CONFIG_BLOCKCYPHER_CHAIN)+"/hooks?token="+config().getString(CONFIG_BLOCKCYPHER_TOKEN))

        blockCypherReply = registerWithBlockCypherServer(
                URL(baseUrl + CALLBACK_PATH_BTC_GATEWAY),
                config().getString(CONFIG_BTC_COLD_WALLET_ADDRESS),
                webClient)
        if (blockCypherReply != null) {
            blockCypherHookList.add(blockCypherReply.getString("id"))
        }
        blockCypherReply = registerWithBlockCypherServer(
                URL(baseUrl + CALLBACK_PATH_BTC_GATEWAY),
                config().getString(CONFIG_BTC_STANDBY_WALLET_ADDRESS),
                webClient)

        if (blockCypherReply != null) {
            blockCypherHookList.add(blockCypherReply.getString("id"))
        }
        blockCypherReply = registerWithBlockCypherServer(
                URL(baseUrl + CALLBACK_PATH_BTC_GATEWAY),
                config().getString(CONFIG_BTC_HOT_WALLET_ADDRESS),
                webClient)

        if (blockCypherReply != null) {
            blockCypherHookList.add(blockCypherReply.getString("id"))
        }


        blockcypherPaymentGenUrl = URL (config().getString(CONFIG_BLOCKCYPHER_API)+config().getString(CONFIG_BLOCKCYPHER_CHAIN)+"/payments?token="+config().getString(CONFIG_BLOCKCYPHER_TOKEN))

    }





    fun verifyFeelPayCallBackMd5 (parameters: MultiMap): Boolean {
        val flowid:String = parameters.get("FlowId")
        val billno:String = parameters.get("BillNo")
        val currency:String = parameters.get("Currency")
        val amount:String = parameters.get("Amount")
        val status:String = parameters.get("Status")
        val bank:String = parameters.get("Bank")
        val remark:String = parameters.get("Remark")
        val origmd5:String = parameters.get("MD5info")
        val key = config().getString(CONFIG_FEELPAY_MD5KEY)

        val concatstring = flowid+billno+currency+amount+status+bank+remark+key
        val digest = MessageDigest.getInstance("MD5")
        digest.reset()
        digest.update(concatstring.toByteArray())
        val calchash =  String.format("%032x",BigInteger(1,digest.digest()))

        return origmd5.contentEquals(calchash)
    }

    fun pushtoUserRippleAddress(address: String) {
        // 1 - get the user object from mongo
        val user = repository!!.getUserByRippleAddress(address)
        if (user != null) {
            // 2 - if user is on the mongo database  - get the pushtoken
            if (user.pushtoken != null) {
                val message = mutableMapOf<String, String>()
                message["message"]="New VPay365 Transactions available"
                // 3 - if the pushtoken is not empty, do a push
                logger.info("Sending push to ${address} with token ${user.pushtoken}")
                val pushrequest = PushyPushRequest(
                        message,
                        user.pushtoken,
                        null
                )
                try {
                    pushy!!.sendPush(pushrequest)
                } catch (e: Throwable) {
                    logger.info("Problem pushing to pushtoken ${user.pushtoken}: ${e.message}")
                }
            }
        }
    }

    fun sendTwilioSMS (telnumber: String, code: String) : String {
        val body:String = "VPay365 verification code: "+code
        try {
            val msg: Message = Message.creator(PhoneNumber(telnumber), PhoneNumber(config().getString(CONFIG_TWILIO_NUMBER)), body).create()
            return msg.sid
        } catch (e: Throwable){
            logger.info("Problem sending SMS via Twilio")
            return "ERROR"
        }
    }

    fun verifyProvisioningRequest (params: JsonObject): User? {
        try {
            val address = params.getString("address")
            val telnumber = params.getString("telnumber")
            val timecreated = Date(params.getLong("timestamp"))
            val pubkey = params.getString("pubkey")
            val signature = params.getString("signature")

            //var wallet = VPay365Wallet(config().getString(CONFIG_HOT_WALLET_SECRET))

            // Validate that the public key and the address are related
            if (wallet!!.validatePublicKey(pubkey, address)) {

                var user = repository!!.getUserByRippleAddress(address)
                if (user == null) {
                    user = User(address = address,
                            prov_telnumber = telnumber,
                            timecreated = timecreated,
                            last_provreq_timestamp = params.getLong("timestamp"),
                            pubkey = pubkey)
                } else {
                    // Re-sending provisioning requests  - same timestamp - is not allowed
                    if (user.last_provreq_timestamp == params.getLong("timestamp")) {
                        return null
                    }
                    user.prov_telnumber = telnumber
                    user.last_provreq_timestamp = params.getLong("timestamp")
                }

                val concatstring = user.address + user.prov_telnumber + params.getLong("timestamp")
                if (wallet!!.verifyMessage(concatstring, signature, pubkey))
                    return user
                else
                    return null
            }
            return null
        } catch (e: Throwable) {
            return null
        }

    }
    fun generateFeelPayAPItopup (params: JsonObject): FeelPayResponse {
        val amount = params.getInteger("amount")
        val currency = 1
        val GoodsDescription = amount.toString()+"CNY"
        val GoodsSubject ="VPay365_credit"
        val merchantid = config().getString(CONFIG_FEELPAY_MERCHANTID)
        val order = merchantid + RandomStringUtils.randomNumeric(14)
        val notifyurl = config().getString(BASE_URL)+CALLBACK_PATH_FEELPAY
        val returnurl = config().getString(BASE_URL)+RETURN_PATH_FEELPAY
        val consumetype=1
        val key = config().getString(CONFIG_FEELPAY_MD5KEY)


        val concatstring = merchantid+order+currency.toString()+amount.toString()+returnurl+notifyurl+key
        val digest = MessageDigest.getInstance("MD5")
        digest.reset()
        digest.update(concatstring.toByteArray())
        val signature =  String.format("%032x",BigInteger(1,digest.digest())).toUpperCase()


        val response = FeelPayResponse(
                url = config().getString(CONFIG_FEELPAY_PAYMENT_URL),
                postdata = "MerNo="+merchantid+"&BillNo="+order+"&Amount="+amount.toString()+"&Currency="+currency.toString()+
                        "&GoodsDescription="+GoodsDescription+"&GoodsSubject="+GoodsSubject+"&ReturnURL="+returnurl+
                        "&NotifyUrl="+notifyurl+"&ConsumeType="+consumetype.toString()+"&Md5info="+signature,
                order = order
        )
        return response


    }


    fun verifyPushRegistration (params: JsonObject): User? {
        val address = params.getString ("address")
        val token = params.getString("token")
        val timestamp = params.getLong("timestamp").toString()
        val signature = params.getString ("signature")

        val user = repository!!.getUserByRippleAddress(address)
        if (user == null) return null

        val concatstring = address+token+timestamp
        if (wallet!!.verifyMessage(concatstring,signature,user.pubkey!!))
            return user

        return null
    }

    fun verifyWithdrawalRequest (params: JsonObject): User? {
        val address = params.getString ("address")
        val amount = params.getFloat("amount").toString()
        val currency = params.getString("currency")
        val bankname = params.getString("bankname")
        val accountnumber = params.getString("accountnumber")
        val accountholder = params.getString("accountholder")
        val txhash = params.getString("txhash")
        val signature = params.getString ("signature")
        val user = repository!!.getUserByRippleAddress(address)
        if (user == null) return null
        val concatstring = address+amount+currency+bankname+accountnumber+accountholder+txhash
        if (wallet!!.verifyMessage(concatstring,signature,user.pubkey!!))
            return user
        return null
    }



    fun verifyBitcoinGeneration (params: JsonObject): User? {
        val address = params.getString ("address")
        val timestamp = params.getLong("timestamp").toString()
        val signature = params.getString ("signature")
        val user = repository!!.getUserByRippleAddress(address)
        if (user == null) return null

        val concatstring = address+timestamp
        if (wallet!!.verifyMessage(concatstring,signature,user.pubkey!!))
            return user
        return null
    }
/*
    fun verifyBitcoinRegistration (params: JsonObject): User? {
        val address = params.getString ("address")
        val btcaddresses = params.getJsonArray("btcaddresses")
        val timestamp = params.getLong("timestamp").toString()
        val signature = params.getString ("signature")
        val btcsize = btcaddresses.size()
        if (btcsize > 5) return null
        val user = repository!!.getUserByRippleAddress(address)
        if (user == null) return null

        var concatbtc: String = ""
        var i = 0
        while (i<btcsize) {
            concatbtc = concatbtc + btcaddresses[i]
            i++
        }

        val concatstring = address+concatbtc+timestamp
        if (wallet!!.verifyMessage(concatstring,signature,user.pubkey!!))
            return user

        return null
    }*/



    fun verifyTopupRequest (params: JsonObject): User? {

        val address = params.getString ("address")
        val amount = params.getInteger("amount")
        val currency = params.getString("currency")
        val signature = params.getString ("signature")


        val user = repository!!.getUserByRippleAddress(address)
        if (user == null) return null

        val concatstring = address+amount.toString()+currency
        if (wallet!!.verifyMessage(concatstring,signature,user.pubkey!!))
            return user

        return null
    }



    fun verifyProvisioningSMSValidation (params: JsonObject): User? {

        val signature = params.getString ("signature")
        val address = params.getString ("address")

        val user = repository!!.getUserByRippleAddress(address)
        //var wallet = VPay365Wallet(config().getString(CONFIG_HOT_WALLET_SECRET))

        if (user == null) return null
        val concatstring = user.address+user.smscode
        if (wallet!!.verifyMessage(concatstring,signature,user.pubkey!!))
            return user

        return null
    }

    // curl -X DELETE "https://api.blockcypher.com/v1/btc/main/hooks/<ID>?token=<TOKEN>
    fun deleteBlockCypherHook (id:String): Boolean {
        logger.info("BlockCypher Hook delete")
        var deleteUrl = URL (config().getString(CONFIG_BLOCKCYPHER_API)+config().getString(CONFIG_BLOCKCYPHER_CHAIN)+"/hooks/"+id+"?token="+config().getString(CONFIG_BLOCKCYPHER_TOKEN))
        val client = HttpClients.createDefault();
        val httpDelete = HttpDelete(deleteUrl.toURI())
        val response = client.execute(httpDelete)
        val statusCode = response.getStatusLine().getStatusCode()

        when (statusCode) {
            204 -> {
                logger.error("Hook $id DELETED")
                return true
            }
            else ->{
                logger.error("Can't delete hook $id from API")
                return false
            }
        }
    }



    // curl -X DELETE "https://api.blockcypher.com/v1/btc/main/payments/<ID>?token=<TOKEN>
    fun deleteBlockCypherForwardingAddress (id:String): Boolean {
        logger.info("BlockCypher Payment Forwarding delete")
        var deleteUrl = URL (config().getString(CONFIG_BLOCKCYPHER_API)+config().getString(CONFIG_BLOCKCYPHER_CHAIN)+"/payments/"+id+"?token="+config().getString(CONFIG_BLOCKCYPHER_TOKEN))
        val client = HttpClients.createDefault();
        val httpDelete = HttpDelete(deleteUrl.toURI())
        val response = client.execute(httpDelete)
        val statusCode = response.getStatusLine().getStatusCode()

        when (statusCode) {
            204 -> {
                logger.error("Payment forwarding $id DELETED")
                return true
            }
            else ->{
                logger.error("Can't delete payment forwarding $id from API")
                return false
            }
        }
    }

    // curl -X GET  "https://api.blockcypher.com/v1/bcy/test/hooks?token=<token>
    fun getBlockCypherHooks (): JsonArray? {
        var hooksUrl = URL (config().getString(CONFIG_BLOCKCYPHER_API)+config().getString(CONFIG_BLOCKCYPHER_CHAIN)+"/hooks?token="+config().getString(CONFIG_BLOCKCYPHER_TOKEN))
        var port: Int = hooksUrl!!.port
        if (port.equals(-1)) port = hooksUrl!!.defaultPort

        val client = HttpClients.createDefault();
        val httpGet = HttpGet(hooksUrl!!.toURI())
        httpGet.setHeader("Accept", "application/json")
        val response = client.execute(httpGet)
        var hookslist :JsonArray? = null
        val statusCode = response.getStatusLine().getStatusCode()

        when (statusCode) {
            200 -> {
                hookslist= JsonArray(IOUtils.toString(response.entity.content, Charset.forName("UTF-8")))
            }
            else ->{
                logger.error("Failed Blockcypher Payment Address Request")
            }
        }
        client.close()
        return hookslist
    }

    // curl -X GET  "https://api.blockcypher.com/v1/bcy/test/payments?token=<token>"
    fun getBlockCypherForwardingAddresses (): JsonArray? {
        logger.info("BlockCypher Payment Forwarding check")
        var port: Int = blockcypherPaymentGenUrl!!.port
        if (port.equals(-1)) port = blockcypherPaymentGenUrl!!.defaultPort

        val client = HttpClients.createDefault();
        val httpGet = HttpGet(blockcypherPaymentGenUrl!!.toURI())
        httpGet.setHeader("Accept", "application/json")
        val response = client.execute(httpGet)
        var fwaddresslist :JsonArray? = null
        val statusCode = response.getStatusLine().getStatusCode()

        when (statusCode) {
            200 -> {
                fwaddresslist= JsonArray(IOUtils.toString(response.entity.content, Charset.forName("UTF-8")))
            }
            else ->{
                logger.error("Failed Blockcypher Payment Address Request")
            }
        }

        client.close()

        return fwaddresslist

    }


    fun generateBlockCypherPaymentAddressBlocking (user:User, webClient: WebClient) : JsonObject? {
        val requestObject = BlockCypherPaymentCreateEndpoint(
                destination = config().getString(CONFIG_BTC_COLD_WALLET_ADDRESS),
                callback_url = config().getString(BASE_URL) + CALLBACK_PATH_BTCGENERATED
        )
        logger.info("BTC get paymentaddress: \n${Json.encode(requestObject)}")
        var port: Int = blockcypherPaymentGenUrl!!.port
        if (port.equals(-1)) port = blockcypherPaymentGenUrl!!.defaultPort

        val client = HttpClients.createDefault();
        val httpPost = HttpPost(blockcypherPaymentGenUrl!!.toURI())

        val mapper = ObjectMapper()
        val entity = StringEntity(mapper.writeValueAsString(requestObject))
        httpPost.setEntity(entity)
        httpPost.setHeader("Accept", "application/json")
        httpPost.setHeader("Content-type", "application/json")

        val response = client.execute(httpPost)
        var httpresponse: JsonObject? = null

        val statusCode = response.getStatusLine().getStatusCode()

        when (statusCode) {
            201 -> {
                httpresponse= JsonObject(IOUtils.toString(response.entity.content, Charset.forName("UTF-8")))
            }
            else ->{
                logger.error("Failed Blockcypher Payment Address Request")
            }
        }

        client.close()

        return httpresponse
    }



/*
    fun generateBlockCypherPaymentAddress (user:User, webClient: WebClient) : String?{
        val requestObject = BlockCypherPaymentCreateEndpoint(
                destination = config().getString(CONFIG_BTC_COLD_WALLET_ADDRESS),
                callback_url = config().getString(BASE_URL)+CALLBACK_PATH_BTCGENERATED
        )
        logger.info("BTC get paymentaddress: \n${Json.encode(requestObject)}")
        var returned = false
        var port: Int = blockcypherPaymentGenUrl!!.port
        if (port.equals(-1)) port = blockcypherPaymentGenUrl!!.defaultPort
        // This has to execute in a way that completes before the function returns
        // executeBlocking??
        webClient.post(port, blockcypherPaymentGenUrl!!.host, blockcypherPaymentGenUrl!!.file).sendJson(requestObject, { response ->
            val result = response.result()
            if (response.succeeded() && result != null) {
                val responseJson = result.bodyAsJsonObject()
                // Add to user object and save
                logger.info("Response received:\n$responseJson")
                user.bitcoinaddress = responseJson.getString("input_address")
                user.blockcypher_callbackid = responseJson.getString("id")
                repository!!.saveUser(user)
                returned = true
            } else {
                if (result != null) {
                    user.bitcoinaddress = null
                    user.blockcypher_callbackid = null
                    repository!!.saveUser(user)
                    val bodyString = result.bodyAsString()
                    logger.error(bodyString)
                    returned = true
                } else {
                    user.bitcoinaddress = null
                    user.blockcypher_callbackid = null
                    repository!!.saveUser(user)
                    logger.error("Registration request for event subscription failed with null result")
                    returned = true
                }
            }
        })
        return user.bitcoinaddress
    }
*/





    fun sendTopupBTC (user: User, topup: Topup,  webClient: WebClient) {
        val amountstr = (topup.amount.toFloat()/100000000F).toString()
        val requestObject = RippleBTCTransaction(params = listOf(
                BTCPaymentParams(
                        secret = config().getString(CONFIG_HOT_WALLET_SECRET),
                        tx_json = BTCTxParams(
                                Account = config().getString(CONFIG_HOT_WALLET_ADDRESS),
                                Destination = user.address,
                                Amount = BTCAmount(
                                        issuer = config().getString(CONFIG_COLD_WALLET_ADDRESS_BTC),
                                        value = amountstr
                                )
                        )
                )
        )
        )

        logger.info("BTC Provisioning request :\n${Json.encode(requestObject)}")

        var port: Int = rippleAdminUrl!!.port
        if (port.equals(-1)) port = rippleAdminUrl!!.defaultPort

        webClient.post(port, rippleAdminUrl!!.host, rippleAdminUrl!!.path).sendJson(requestObject, { response ->
            val result = response.result()
            if (response.succeeded() && result != null) {
                val responsejson = result.bodyAsJsonObject()
                try {
                    logger.info("Response received:\n$responsejson")
                    topup.txhash = responsejson.getJsonObject("result").getJsonObject("tx_json").getString("hash")
                    topup.rippleresult = responsejson.getJsonObject("result").getString("engine_result")
                    topup.ripplelog = responsejson.toString()
                    if (topup.rippleresult == "tesSUCCESS") topup.status = VPay365Status.COMPLETE
                    else topup.status = VPay365Status.ERRORFLAG

                } catch  (e: Throwable) {
                    topup.status = VPay365Status.ERRORFLAG
                    topup.ripplelog = responsejson.toString()
                }

                repository!!.saveTopup(topup)
            } else {
                if (result !=null) {
                    val responsejson = result.bodyAsJsonObject()
                    logger.error(responsejson)
                } else {
                    logger.error("BTC Provisioning request encountered an error: NULL response result")
                }
            }
        })

    }



    // In this function the hot wallet will send the requested amount of CNYto the account related to the user object passed
    // The amount is an integer, which is denominated in CNY cents
    // On the ripple command line client we have:
    //  {
    //     "TransactionType": "Payment",
    //     "Account":"rKSVZEQWXKudnBnqtRs43mDagkNSyivs7",
    //     "Destination":"rhMxfXzr3BNZixMXiirz9TkcCBdRhrmV6W",
    //     "Amount": {
    //           "currency": "CNY",
    //           "value":"100",
    //           "issuer":"rJHscEPC5HmzLBu7uy1QeqGe6XDk2iaxCx"
    //            }
    //   }


    fun sendTopupCNY (user: User, topup: Topup,  webClient: WebClient) {

        val amountstr = (topup.amount.toFloat()/100).toString()
        val requestObject = RippleCNYTransaction(params = listOf(
                CNYPaymentParams(
                        secret = config().getString(CONFIG_HOT_WALLET_SECRET),
                        tx_json = CNYTxParams(
                                Account = config().getString(CONFIG_HOT_WALLET_ADDRESS),
                                Destination = user.address,
                                Amount = CNYAmount(
                                        issuer = config().getString(CONFIG_COLD_WALLET_ADDRESS_CNY),
                                        value = amountstr
                                )
                        )
                )
        )
        )

        logger.info("CNY Provisioning request :\n${Json.encode(requestObject)}")

        var port: Int = rippleAdminUrl!!.port
        if (port.equals(-1)) port = rippleAdminUrl!!.defaultPort

        webClient.post(port, rippleAdminUrl!!.host, rippleAdminUrl!!.path).sendJson(requestObject, { response ->
            val result = response.result()
            if (response.succeeded() && result != null) {
                val responsejson = result.bodyAsJsonObject()
                try {
                    logger.info("Response received:\n$responsejson")
                    topup.txhash = responsejson.getJsonObject("result").getJsonObject("tx_json").getString("hash")
                    topup.rippleresult = responsejson.getJsonObject("result").getString("engine_result")
                    topup.ripplelog = responsejson.toString()
                    if (topup.rippleresult == "tesSUCCESS") topup.status = VPay365Status.COMPLETE
                    else topup.status = VPay365Status.ERRORFLAG

                } catch  (e: Throwable) {
                    topup.status = VPay365Status.ERRORFLAG
                    topup.ripplelog = responsejson.toString()
                }

                repository!!.saveTopup(topup)
            } else {
                if (result !=null) {
                    val responsejson = result.bodyAsJsonObject()
                    logger.error(responsejson)
                } else {
                    logger.error("CNY Provisioning request encountered an error: NULL response result")
                }
            }
        })

    }



    // In this function the hot wallet will send 35 XRP to the account related to the user object passed
    fun sendProvisioningXRP (user: User, webClient: WebClient) {

        val requestObject = RippleXRPTransaction(params = listOf(
                XRPPaymentParams(
                        secret = config().getString(CONFIG_HOT_WALLET_SECRET),
                        tx_json = XRPTxParams(
                                        Account = config().getString(CONFIG_HOT_WALLET_ADDRESS),
                                        Destination = user.address
                        )
                )
        )
        )

        logger.info("XRP Provisioning request :\n${Json.encode(requestObject)}")

        var port: Int = rippleAdminUrl!!.port
        if (port.equals(-1)) port = rippleAdminUrl!!.defaultPort

        webClient.post(port, rippleAdminUrl!!.host, rippleAdminUrl!!.path).sendJson(requestObject, { response ->
            val result = response.result()
            if (response.succeeded() && result != null) {
                val bodyString = result.bodyAsString()
                logger.info("Response received\n$bodyString")
            } else {
                if (result != null) {
                    val bodyString = result.bodyAsString()
                    logger.error(bodyString)
                } else {
                    logger.error("XRP Provisioning request encountered an error: NULL response result")
                }
            }
        })

    }


    fun processClientEvent (params: JsonObject) {
        if (params.get<String>("type") == "transaction") {
            val txdetails = params.get<JsonObject>("transaction")
            // We will only need to push to the destination as the source user app will be aware of the transaction
            // and can refresh automatically
            val destination = txdetails.getString("Destination")
            pushtoUserRippleAddress(destination)
        }

    }

    fun processGatewayEvent (params: JsonObject) {
        if (params.get<String>("type") == "transaction") {
            val txdetails = params.get<JsonObject>("transaction")
            if (txdetails.containsKey("DestinationTag")) {
                val user = repository!!.getUserByDestinationTag(txdetails.getInteger("DestinationTag"))
                val msecsinceepoch: Long = (txdetails.getInteger("date").toLong() + 946684800L) * 1000
                if (user != null) {
                    var withdrawal = repository!!.getWithdrawalbyHash(txdetails.getString("hash"))
                    if (withdrawal == null) {
                        // This withdrawal has arrived from the gateway before it did from the client
                        // We need to create it
                        val newwithdrawal = Withdrawal(
                                amount = txdetails.get<JsonObject>("Amount").getString("value").toFloat(),
                                currency = Currencies.valueOf(txdetails.get<JsonObject>("Amount").getString("currency")),
                                timecompleted = Date(),
                                txhash = txdetails.getString("hash"),
                                status = VPay365Status.PENDING,
                                userid = user._id!!,
                                timecreated = null
                        )
                        repository!!.saveWithdrawal(newwithdrawal)
                        logger.info("Detected a withdrawal from ${newwithdrawal.userid}  for ${newwithdrawal.amount} ${newwithdrawal.currency} using transaction ${newwithdrawal.txhash} at ${newwithdrawal.timecreated}")
                    } else {
                        // This withdrawal is already in the system
                        // has this been seen on the chain?
                        if (withdrawal.timecompleted == null) { // This would be one created by the API
                            withdrawal.status = VPay365Status.PAYMENTRECEIVED
                            withdrawal.timecompleted = Date()
                            val amountchain = txdetails.get<JsonObject>("Amount").getString("value").toFloat()
                            val currency = Currencies.valueOf(txdetails.get<JsonObject>("Amount").getString("currency"))
                            if (withdrawal.amount != amountchain || withdrawal.currency != currency) {
                                withdrawal.notes = "DIFF API amount ${withdrawal.amount} ${withdrawal.currency} and CHAIN amount ${amountchain} ${currency}"
                            } else {
                                withdrawal.notes = "API and CHAIN values match"
                            }
                            repository!!.saveWithdrawal(withdrawal)
                        } else {
                            // This is just a duplicated withdrawal event from the gateway, ignore
                        }
                    }
                }
            } else {
                // This is not a withdrawal as the transfer does not have a destination tag
                // Currently just log it
                logger.info("Detected a transaction from the gateway: ${txdetails}")
            }
        }
    }

    fun registerWithBlockCypherServer(callbackUrl: URL, account: String, webClient: WebClient): JsonObject? {
        val event = "confirmed-tx"
        val requestObject = BlockCypherEventRequest(address=account,url=callbackUrl.toString())
        logger.info("BlockCypher event request to ${blockcypherUrl.toString()} :\n${Json.encode(requestObject)}")
        var port: Int = blockcypherUrl!!.port
        if (port.equals(-1)) port = blockcypherUrl!!.defaultPort


        val client = HttpClients.createDefault();
        val httpPost = HttpPost(blockcypherUrl!!.toURI())

        val mapper = ObjectMapper()
        val entity = StringEntity(mapper.writeValueAsString(requestObject))
        httpPost.setEntity(entity)
        httpPost.setHeader("Accept", "application/json")
        httpPost.setHeader("Content-type", "application/json")

        val response = client.execute(httpPost)
        var httpresponse: JsonObject? = null

        val statusCode = response.getStatusLine().getStatusCode()

        when (statusCode) {
            201 -> {
                httpresponse= JsonObject(IOUtils.toString(response.entity.content, Charset.forName("UTF-8")))
            }
            else ->{
                logger.error("Failed Blockcypher Payment Address Request")
            }
        }

        return httpresponse


/*        webClient.post(port, blockcypherUrl!!.host, blockcypherUrl!!.file).sendJson(requestObject, { response ->
            val result = response.result()
            if (response.succeeded() && result != null ) {
                val bodyString = result.bodyAsString()
                logger.info("Response received\n$bodyString")
            } else {
                if (result != null) {
                    val bodyString = result.bodyAsString()
                    logger.error(bodyString)
                } else {
                    logger.error("Registration request for event subscription failed with null result")
                }
            }
        })*/
    }

    fun registerWithRippleServer(callbackUrl: URL, accounts: Collection<User>, webClient: WebClient) {


        val uuid = getUUID()

        val addresses = HashSet<String>()
        for (account in accounts) {
            addresses.add(account.address)
        }

        if (addresses.size >0 ) {

            val requestObject = RippleTransactionSubscribe(params = listOf(
                    SubscriptionParams(
                            id = uuid.toString(),
                            url = callbackUrl.toString(),
                            accounts = addresses
                    )
            )

            )

            logger.info("Registration request :\n${Json.encode(requestObject)}")

            var port: Int = rippleAdminUrl!!.port
            if (port.equals(-1)) port = rippleAdminUrl!!.defaultPort

            webClient.post(port, rippleAdminUrl!!.host, rippleAdminUrl!!.path).sendJson(requestObject, { response ->
                val result = response.result()
                if (response.succeeded() && result != null ) {
                    val bodyString = result.bodyAsString()
                    logger.info("Response received\n$bodyString")
                } else {
                    if (result != null) {
                        val bodyString = result.bodyAsString()
                        logger.error(bodyString)
                    } else {
                        logger.error("Registration request for event subscription failed with null result")
                    }
                }
            })
        } else {
            logger.info ("No accounts to register on the server")
        }

    }

    fun getUUID(): UUID {
        return UUID.randomUUID()
    }


    fun getDtag(rng : SecureRandom): Int {
        var d:Int = 0
        var user: User? = null
        do {
            d = rng.nextInt()
            if (d <= 0) d += 2147483647
            user = repository!!.getUserByDestinationTag(d)
        } while (user != null)
        return d
    }

    fun sendNotification(msgData: JsonObject) {
        vertx.eventBus().send(NOTIFICATION_SERVICE, msgData)
    }


    fun getTransactionsfromLedger (webClient: WebClient) {
        val gwaddress  = listOf(config().getString(CONFIG_HOT_WALLET_ADDRESS),
                config().getString(CONFIG_COLD_WALLET_ADDRESS_CNY),
                config().getString(CONFIG_COLD_WALLET_ADDRESS_BTC),
                config().getString(CONFIG_STANDBY_WALLET_ADDRESS)
        )
        var port: Int = rippleAdminUrl!!.port
        if (port==-1) port = rippleAdminUrl!!.defaultPort
        for (add in gwaddress) {
            val requestObject = RippleTXQuery(params = listOf(RippleTXQueryParams(account = add)))

            webClient.post(port, rippleAdminUrl!!.host, rippleAdminUrl!!.path).sendJson(requestObject, { response ->
                val result = response.result()
                if (response.succeeded() && result != null) {
                    val bodyJson = result.bodyAsJsonObject()
                    val txs = bodyJson.getJsonObject("result").getJsonArray("transactions") as Iterable<JsonObject>
                    for (tx in txs)
                        if (tx.getBoolean("validated") == true)
                            processGwTransaction(tx.getJsonObject("tx"))
                } else {
                    if (result != null) {
                        val bodyString = result.bodyAsString()
                        logger.error(bodyString)
                    } else {
                        logger.error("Query to the ripple server for ledger transactions has failed with null result")
                    }
                }
            })
        }

    }

    fun processGwTransaction(tx: JsonObject) {
        if (tx.getString("Account") == config().getString(CONFIG_HOT_WALLET_ADDRESS)) return // Do not process tx from hot wallet
        if (tx.getString("TransactionType") != "Payment") return // Do not process TrustSet
        if (repository!!.getWithdrawalbyHash(tx.getString("hash")) == null) {
            //logger.info("TX NOT in DB: $tx")
            val user = repository!!.getUserByRippleAddress(tx.getString("Account"))
            if (user != null) {
                if (user.destinationtag == tx.getInteger("DestinationTag")) {
                    val txtime = (tx.getInteger("date").toLong() + 946684800L)*1000

                    val w= Withdrawal(
                            amount = tx.get<JsonObject>("Amount").getString("value").toFloat(),
                            currency = Currencies.valueOf(tx.get<JsonObject>("Amount").getString("currency")),
                            timecreated = Date(txtime),
                            txhash = tx.getString("hash"),
                            status = VPay365Status.PENDING,
                            userid = user._id!!,
                            timecompleted = null)
                    repository!!.saveWithdrawal(w)
                } else {
                    logger.error("Transaction from known user ${user.address} but wrong dtag: ${tx}")
                }
            } else {
                logger.error("Transaction from unknown user: ${tx}")
            }
        } else {
            //logger.info("TX IN DB: $tx")
        }
    }
}