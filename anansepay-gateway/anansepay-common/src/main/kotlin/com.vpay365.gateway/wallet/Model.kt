package im.ananse.gateway.wallet

data class FeelPayProvisioningRequest(
        var address: String,
        var telnumber: String,
        var timestamp: Long,
        var pubkey: String,
        var signature: String
)

data class FeelPayProvisioningReply(
        var address: String,
        var signature: String
)

data class FeelPayTopupRequest(
        val address: String,
        val amount: Int,
        val currency: String,
        val signature: String
)

data class FeelPayPushTokenRequest(
        val address: String,
        val token: String,
        val timestamp: Long,
        val signature: String
)

data class BitcoinRequest (
        val address: String,
        val btcaddresses: Array<String>,
        val timestamp: Long,
        val signature: String
)

data class BitcoinGenerationRequest (
        val address: String,
        val timestamp: Long,
        val signature: String
)

data class WithdrawalRequest (
        val address: String,
        val amount: Float,
        val currency: String,
        val bankname: String,
        val accountnumber: String,
        val accountholder: String,
        val txhash:String,
        val signature: String
)
