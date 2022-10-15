package im.ananse.admin.controllers


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import im.ananse.admin.WebApplication
import im.ananse.admin.model.RippleAccountInfoRequest
import im.ananse.admin.model.RippleAccountLinesRequest
import im.ananse.admin.model.RippleGatewayBalancesRequest
import im.ananse.gateway.*
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.ModelAndView
import java.math.BigDecimal
import java.util.*
import java.util.logging.Logger
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession


@Controller
class RootController(val aPayRepository: VPay365Repository, val webApplication: WebApplication) {

    private val logger = Logger.getLogger(javaClass.name)

    val mapper = ObjectMapper()

    @GetMapping("", "", "/index", "/home")
    fun unprotected(request: HttpServletRequest): String {

        logger.info(request.requestURI)

        return "redirect:/admin/home"
    }

    @GetMapping("/admin/home")
    fun adminHome(request: HttpServletRequest, session: HttpSession, model: ModelMap): ModelAndView {

        logger.info(request.requestURI)

        val hotWalletDetail = JSONObject(getAccountLines(webApplication.hotWalletAddress!!))
        val hotWalletArray = hotWalletDetail.getJSONObject("result").getJSONArray("lines")
        val hotWalletList = ArrayList<Balance>()
        hotWalletList.add(mapper.readValue(hotWalletArray.getJSONObject(0).toString(), Balance::class.java))
        hotWalletList.add(mapper.readValue(hotWalletArray.getJSONObject(1).toString(), Balance::class.java))
        val hotWalletAccountInfo = JSONObject(getAccountInfo(webApplication.hotWalletAddress!!)).getJSONObject("result")
        val hotWalletXrpDropBalance = hotWalletAccountInfo.getJSONObject("account_data").getString("Balance")
        val hotWalletXrpBalance = BigDecimal(hotWalletXrpDropBalance).divide(BigDecimal(1000000)).toLong()

        val standbyWalletDetail = JSONObject(getAccountLines(webApplication.standbyWalletAddress!!))
        val standByWalletArray = standbyWalletDetail.getJSONObject("result").getJSONArray("lines")
        val standByWalletList = ArrayList<Balance>()
        standByWalletList.add(mapper.readValue(standByWalletArray.getJSONObject(0).toString(), Balance::class.java))
        standByWalletList.add(mapper.readValue(standByWalletArray.getJSONObject(1).toString(), Balance::class.java))
        val standbyWalletAccountInfo = JSONObject(getAccountInfo(webApplication.standbyWalletAddress!!)).getJSONObject("result")
        val standbyWalletXrpDropBalance = standbyWalletAccountInfo.getJSONObject("account_data").getString("Balance")
        val standbyWalletXrpBalance = BigDecimal(standbyWalletXrpDropBalance).divide(BigDecimal(1000000)).toLong()

        val coldWalletDetail = JSONObject(getGatewayBalances(webApplication.coldWalletAddress!!))
        val coldWalletObligations = coldWalletDetail.getJSONObject("result").getJSONObject("obligations")
        val coldWalletBtcObligation = try { coldWalletObligations.getString("BTC") } catch (e:JSONException) {null}
        val coldWalletCnyObligation = try { coldWalletObligations.getString("CNY") } catch (e:JSONException) {null}

//        val coldWalletList = ArrayList<Balance>()
//        coldWalletList.add(mapper.readValue(coldWalletObligationsArray.getJSONObject(0).toString(), Balance::class.java))
//        coldWalletList.add(mapper.readValue(coldWalletObligationsArray.getJSONObject(1).toString(), Balance::class.java))
        val coldWalletAccountInfo = JSONObject(getAccountInfo(webApplication.coldWalletAddress!!)).getJSONObject("result")
        val coldWalletXrpDropBalance = coldWalletAccountInfo.getJSONObject("account_data").getString("Balance")
        val coldWalletXrpBalance = BigDecimal(coldWalletXrpDropBalance).divide(BigDecimal(1000000)).toLong()

        val userList = aPayRepository.getAllUsers()

        model.put("hotWalletList", hotWalletList)
        model.put("hotWalletXrp", hotWalletXrpBalance)
        model.put("standByWalletList", standByWalletList)
        model.put("standByWalletXrp", standbyWalletXrpBalance)
//        model.put("coldWalletList", coldWalletList)
        model.put("coldWalletXrp", coldWalletXrpBalance)
        model.put("coldWalletBtcObligation", coldWalletBtcObligation)
        model.put("coldWalletCnyObligation", coldWalletCnyObligation)

        model.put("userList", userList)

        model.put("rippleExplorerBaseUrl", webApplication.rippleExplorerBaseUrl)

        return ModelAndView("adminhome", model)
    }

    @GetMapping("/admin/pendingwithdrawals")
    fun adminPendingWithdrawals(request: HttpServletRequest, session: HttpSession, model: ModelMap): ModelAndView {

        logger.info(request.requestURI)

        val pendingWithdrawals = aPayRepository.getAllPendingWithdrawals()

        model.put("withdrawalList", pendingWithdrawals)
        model.put("repository", aPayRepository)

        return ModelAndView("adminpendingwithdrawals", model)

    }

    @GetMapping("/admin/processwithdrawal")
    fun adminProcessWithdrawals(@RequestParam hash:String, @RequestParam action: String, request: HttpServletRequest, session: HttpSession, model: ModelMap): ModelAndView {

        logger.info(request.requestURI)

        val withdrawal = aPayRepository.getWithdrawalbyHash(hash)

        if (withdrawal != null) {

            when (action) {
                "a" -> {
                    withdrawal.status = VPay365Status.COMPLETE
                    withdrawal.timecompleted = Date()
                }
                "r" -> {
                    logger.info("rejecting transaction")
                    val user = aPayRepository.getUserbyId(withdrawal.userid)
                    rejectWithdrawal(user = user!!, withdrawal = withdrawal)
                }
            }


            aPayRepository.saveWithdrawal(withdrawal)
        }

//        model.put("withdrawalList", pendingWithdrawals)

        return ModelAndView("redirect:/admin/pendingwithdrawals", model)

    }



    fun getAccountLines(rippleAddress: String):String {

        val restTemplate = RestTemplate()
//        restTemplate.messageConverters.add(MessageConve())

        val uri = webApplication.rippleServerUrl

        val response = restTemplate.postForObject(uri, RippleAccountLinesRequest(rippleAddress), String::class.java)

        logger.info(response.toString())

        return response.toString()

    }

    fun getAccountInfo(rippleAddress: String):String {

        val restTemplate = RestTemplate()
//        restTemplate.messageConverters.add(MessageConve())

        val uri = webApplication.rippleServerUrl

        val response = restTemplate.postForObject(uri, RippleAccountInfoRequest(rippleAddress), String::class.java)

        logger.info(response.toString())

        return response.toString()

    }

    fun getGatewayBalances(rippleAddress: String):String {

        val restTemplate = RestTemplate()
//        restTemplate.messageConverters.add(MessageConve())

        val uri = webApplication.rippleServerUrl

        logger.info("GetGatewayBalancesRequest : \n ${mapper.writeValueAsString(RippleGatewayBalancesRequest(rippleAddress, webApplication.hotWalletAddress!!, webApplication.standbyWalletAddress!!))}")

        val response = restTemplate.postForObject(uri, RippleGatewayBalancesRequest(rippleAddress, webApplication.hotWalletAddress!!, webApplication.standbyWalletAddress!!), String::class.java)

        logger.info(response.toString())

        return response.toString()

    }

    fun rejectWithdrawal (user: User, withdrawal: Withdrawal) {
        var amountstr: String? = null

        var requestObject: Any? = null

        when (withdrawal.currency) {

            Currencies.BTC -> {

                amountstr = (withdrawal.amount).toString()

                requestObject = RippleBTCTransaction(
                        params = listOf(
                                BTCPaymentParams(
                                        secret = webApplication.hotWalletSecret!!,
                                        tx_json = BTCTxParams(
                                                Account = webApplication.hotWalletAddress!!,
                                                Destination = user.address,
                                                Amount = BTCAmount(
                                                        issuer = webApplication.coldWalletAddress!!,
                                                        value = amountstr
                                                )
                                        )
                                )
                        )
                )
            }

            Currencies.CNY -> {

                // TODO fix the amount for CNY
                amountstr = (withdrawal.amount).toString()

                requestObject = RippleCNYTransaction(
                        params = listOf(
                                CNYPaymentParams(
                                        secret = webApplication.hotWalletSecret!!,
                                        tx_json = CNYTxParams(
                                                Account = webApplication.hotWalletAddress!!,
                                                Destination = user.address,
                                                Amount = CNYAmount(
                                                        issuer = webApplication.coldWalletAddress!!,
                                                        value = amountstr
                                                )
                                        )
                                )
                        )
                )
            }
        }

        logger.info("Sending rejected withdrawal credit to user:\n${mapper.writeValueAsString(requestObject)}")

        val restTemplate = RestTemplate()
        val response = restTemplate.postForObject(webApplication.rippleServerUrl, requestObject, String::class.java)

//        val responseObject = JSONObject(response) // Intentionally not creating object previously delaying deserialisatin to here

        logger.info("Response : \n${mapper.writeValueAsString(response)}")

        if (!response.contains("SUCCESS")) {
            throw Exception("Error rejecting withdrawal. Please check and try again")
        }

        withdrawal.status = VPay365Status.ERRORFLAG
        withdrawal.timecompleted = Date()


        /// -----------------------------------
//        webClient.post(port, rippleAdminUrl!!.host, rippleAdminUrl!!.path).sendJson(requestObject, { response ->
//            val result = response.result()
//            if (response.succeeded() && result != null) {
//                val responsejson = result.bodyAsJsonObject()
//                try {
//                    logger.info("Response received:\n$responsejson")
//                    withdrawal.txhash = responsejson.getJsonObject("result").getJsonObject("tx_json").getString("hash")
//                    withdrawal.rippleresult = responsejson.getJsonObject("result").getString("engine_result")
//                    withdrawal.ripplelog = responsejson.toString()
//                    if (withdrawal.rippleresult == "tesSUCCESS") withdrawal.status = VPay365Status.COMPLETE
//                    else withdrawal.status = VPay365Status.ERRORFLAG
//
//                } catch  (e: Throwable) {
//                    withdrawal.status = VPay365Status.ERRORFLAG
//                    withdrawal.ripplelog = responsejson.toString()
//                }
//
//                repository!!.saveTopup(withdrawal)
//            } else {
//                if (result !=null) {
//                    val responsejson = result.bodyAsJsonObject()
//                    logger.error(responsejson)
//                } else {
//                    logger.error("BTC Provisioning request encountered an error: NULL response result")
//                }
//            }
//        })

    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
open class Balance() {
    var balance: String? = null
    var currency: String? = null
}

