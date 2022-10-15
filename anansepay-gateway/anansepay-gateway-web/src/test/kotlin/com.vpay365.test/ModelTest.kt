package im.ananse.test


import im.ananse.gateway.*
import io.vertx.core.logging.LoggerFactory
import java.util.*
import org.junit.Test
import org.apache.commons.lang3.RandomStringUtils
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom


class ModelTest {
    internal val logger = LoggerFactory.getLogger("ModelTest")
    var repository = VPay365Repository("localhost:27017","anansepay")


    fun getDtag(rng : SecureRandom): Int {
        var d:Int = 0
        var user: User? = null
        do {
            d = rng.nextInt()
            if (d < 0) d += 2147483647
            user = repository!!.getUserByDestinationTag(d)
        } while (user != null)
        return d
    }


    @Test
    fun DTagTest() {
        var rng = SecureRandom()
        rng.setSeed(Date().time)
        var i:Int = 0
        while (i<200) {
            i++
            assert(getDtag(rng)>=0)
        }
    }

    @Test
    fun InitialTest (){
        val userlist = repository.getAllUsers()
        assert(userlist.size == 1)
        val user = repository.getUserByDestinationTag(123456)
        assert(user!=null)
        assert(user!!.destinationtag == 123456)
        assert(user.address == "rhMxfXzr3BNZixMXiirz9TkcCBdRhrmV6W")
        val user2 = repository.getUserByRippleAddress("rhMxfXzr3BNZixMXiirz9TkcCBdRhrmV6W")
        assert(user2!=null)
        assert(user2!!.destinationtag == 123456)
        assert(user2.address == "rhMxfXzr3BNZixMXiirz9TkcCBdRhrmV6W")
       // user2.destinationtag=1234567
        //repository.saveUser(user2)
        //val user3 = repository.getUserByDestinationTag(123456)
        //assert(user3 == null )


        val withdrawallist = repository.getWithdrawalsbyUser(user = user2)
        assert(withdrawallist.isNotEmpty())
    }

    var testaddresses = listOf(
        "rn3yATyTvtP4MRf68AW2TwYKACKbSJmJmK",
        "rBkedyc2mSCYz5XQG75xvZzGvoRv25Q4bi",
        "rah7SXpr6nou4ARWAhk3sBxnAes782WpkZ",
        "rGgcGUDYizSXNGUBmhz5K7B9mDuSmjhJRN",
        "rQry3rPJ24u7Syvq5sPaRrsc6pMnDHcMFY",
        "rUZGJi2RWjVA3XLxSdBEeHt41kjcUXpeqj",
        "rWjqy2srXN7gpQ99HGS8FxtZwdrnHhbwy",
        "rhg3XPwsMTLJij34S1pdPLhwtgqFxJoahb",
        "r3T3g47SeyxH5jBWt7zt5DsYmq2trWTmgV",
        "rwszZ4wfQ5qxYdmv8BJSZ99nv265SShYgd"
    )

    fun createRandomPushtoken(): String {
        return RandomStringUtils.randomAlphanumeric(16)
    }

    fun createRandomTxHash(): String {
        val randomstring = RandomStringUtils.randomAlphanumeric(60)
        val digest = MessageDigest.getInstance("SHA-512")
        digest.reset()
        digest.update(randomstring.toByteArray())
        val calchash =  String.format("%064x", BigInteger(1,digest.digest()))
        return calchash.substring(0,31)
    }

    // 6 digit destination tag
    fun createRandomDestinationTag(): Int {
        val randomgenerator=Random()
        val dtag: Int = (1000000 * randomgenerator.nextDouble()).toInt()
        return dtag
    }

    fun createRandomOrderId(): String{
        return RandomStringUtils.randomNumeric(18)
    }

    fun createRandomFlowId(): String {
        return RandomStringUtils.randomNumeric(18)
    }

    @Test
    fun clearDatabase() {
        repository.run {
            deleteDatabase()
            createSchema()
        }
    }

    @Test
    fun synthenticDatabaseTest() {
        clearDatabase()
        // 1 - add all new users
        testaddresses
                .map {
                    User(
                            address = it,
                            pushtoken = createRandomPushtoken(),
                            destinationtag = createRandomDestinationTag(),
                            pubkey = "erwertwertwertert"
                    )
                }
                .forEach { repository.saveUser(it) }
        // 2 - Pick a random user and add a withdrawal or a topup in pending state

        var i:Int = 0
        while (i<100) {
            i++
            val randomgenerator = Random()
            var index: Int = (10 * randomgenerator.nextDouble()).toInt()
            if (index == 10) index = 9
            val randomuser = repository.getUserByRippleAddress(testaddresses[index])
            //  3- Add a withdrawal
            if ((index+i)%2 == 0) {

                val withdrawal = Withdrawal(
                        amount = ((randomgenerator.nextFloat() * 1000).toInt()).toFloat() / 100,
                        currency = Currencies.CNY,
                        userid = randomuser!!._id!!,
                        //timecreated = Timestamp((Date().toInstant().epochSecond.toLong()) * 1000),
                        timecreated = Date(),
                        timecompleted = null,
                        txhash = null,
                        status = VPay365Status.PENDING
                )
                logger.info("Adding a pending withdrawal for user "+randomuser.address)
                repository.saveWithdrawal(withdrawal)
            } else {
                // 4 - Add a topup
                val topup = Topup(
                        order = createRandomOrderId(),
                        //timecreated = Timestamp((Date().toInstant().epochSecond.toLong()) * 1000),
                        timecreated = Date(),
                        timecompleted = null,
                        status = VPay365Status.PENDING,
                        amount = (((randomgenerator.nextFloat() * 1000).toInt()).toFloat() / 100).toInt(),
                        currency = Currencies.CNY,
                        userid = randomuser!!._id!!,
                        txhash = null,
                        flowid = null,
                        remark = null
                )
                logger.info("Adding a pending topup for user "+randomuser.address)
                repository.saveTopup(topup)
            }
        }


        // 6 - Pick a random user again
        i = 0
        while (i<100) {
            i++
            val randomgenerator = Random()
            var index: Int = (10 * randomgenerator.nextDouble()).toInt()
            if (index == 10) index = 9
            val randomuser = repository.getUserByRippleAddress(testaddresses[index])

            if ((index +i) %2 == 0) {
                // 7a - Get all topups for that user
                val topuplist = repository.getTopupsbyUser(randomuser!!)
                // 8a - Get a random one
                if (topuplist.isNotEmpty()) {
                    val range = topuplist.indices
                    val tindex = ((range.last - range.first) * randomgenerator.nextDouble()).toInt() + range.first
                    val topup = topuplist.elementAt(tindex)
                    // 9a - If not completed, complete and save
                    if (topup.status == VPay365Status.PENDING) {
                        topup.status = VPay365Status.COMPLETE
                        //topup.timecompleted = Timestamp((Date().toInstant().epochSecond.toLong()) * 1000)
                        topup.timecompleted = Date()
                        topup.txhash = createRandomTxHash()
                        topup.flowid = createRandomFlowId()
                        logger.info("Completed topup for " + randomuser.address + " with order " + topup.order + " and amount: " + topup.amount + " " + topup.currency.name)
                        repository.saveTopup(topup)
                    }
                }
            } else {
                // 7b - Get all withdrawals for that user
                val withdrawallist = repository.getWithdrawalsbyUser(randomuser!!)
                if (withdrawallist.isNotEmpty()) {
                    // 8b - Get a random one
                    val range = withdrawallist.indices
                    val windex = ((range.last - range.first) * randomgenerator.nextDouble()).toInt() + range.first
                    val withdrawal = withdrawallist.elementAt(windex)
                    // 9b - If not completed, complete and save
                    if (withdrawal.status == VPay365Status.PENDING) {
                        withdrawal.status = VPay365Status.COMPLETE
                        //withdrawal.timecompleted = Timestamp((Date().toInstant().epochSecond.toLong()) * 1000)
                        withdrawal.timecompleted = Date()
                        withdrawal.txhash = createRandomTxHash()
                        logger.info("Completed withdrawal for " + randomuser.address + " with date " + withdrawal.timecreated + " and amount: " + withdrawal.amount + " " + withdrawal.currency.name)
                        repository.saveWithdrawal(withdrawal)
                    }
                }
            }
        }

        // 10 - Check pending withdrawals for random users
        i = 0
        while (i<20) {
            i++
            val randomgenerator = Random()
            var index: Int = (10 * randomgenerator.nextDouble()).toInt()
            if (index == 10) index = 9
            val randomuser = repository.getUserByRippleAddress(testaddresses[index])
            val pendingwithdrawals = repository.getPendingWithdrawalsbyUser(randomuser!!)
            for ((amount, currency, _, timecreated) in pendingwithdrawals) {
                logger.info("Pending withdrawal for user " + randomuser.address + " time: " + timecreated + " amount: " + amount + " " + (currency.name))
            }
        }

        // 11 - Check pending topups for random users
        i = 0
        while (i<20) {
            i++
            val randomgenerator = Random()
            var index: Int = (10 * randomgenerator.nextDouble()).toInt()
            if (index == 10) index = 9
            val randomuser = repository.getUserByRippleAddress(testaddresses[index])
            val pendingtopups = repository.getPendingTopupsbyUser(randomuser!!)
            for ((order, _, _, _, amount, currency) in pendingtopups) {
                logger.info("Pending topup for user " + randomuser.address + " order: " + order + " amount: " + amount + " " + (currency.name))
            }
        }


    }



}
