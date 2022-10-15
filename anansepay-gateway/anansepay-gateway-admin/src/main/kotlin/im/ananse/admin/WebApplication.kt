package im.ananse.admin


import im.ananse.gateway.VPay365Repository
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.context.annotation.Bean
import java.util.logging.Logger


fun main(args: Array<String>) {
    SpringApplication.run(WebApplication::class.java, *args)
}

@SpringBootApplication
@ServletComponentScan
@ConfigurationProperties
class WebApplication() {

    private val logger = Logger.getLogger(javaClass.name)

    var mongoDbUrl: String? = null
    var mongoDbDatabase: String? = null
    var rippleServerUrl: String? = null

    var hotWalletAddress: String? = null
    var hotWalletSecret: String? = null
    var standbyWalletAddress: String? = null
    var coldWalletAddress: String? = null

    var adminPassword: String? = null

    var rippleExplorerBaseUrl: String? = null

    @Bean
    fun vPay365Repository(): VPay365Repository {
        return VPay365Repository(mongoDbUrl!!, mongoDbDatabase!!)
    }
}