# anansepay-gateway

## Configuration
Update configuration in `anansepay-gateway-web/src/main/conf/config.json` with this content

```javascript
{
  "base_url": "",
  "base_admin_url": "",
  "http_port": 8085,
  "certificate_path": "",
  "certificate_password": "",
  "sendgrid_api_key": "",
  "sendgrid_template_invite_friend": "",
  "sendgrid_from_email": "noreply@ananse.im",
  "hot_wallet_address": "",
  "hot_wallet_secret": ""
  "cold_wallet_address": "",
  "standby_wallet_address": "",
  "mongodb_url": "mongodb://localhost:27017/anansepay"
  "mongodb_database": "anansepay",
  "feelpay_md5key": "",
  "feelpay_merchant": "",
  "feelpay_payment_url": "",
  "pushy_apikey": ""
}
```

Or set __Environment variables__ with the following keys
- `base_url`
- `base_admin_url`
- `http_port`
- `certificate_path OPTIONAL`
- `certificate_password OPTIONAL`
- `sendgrid_api_key`
- `sendgrid_template_invite_friend`
- `sendgrid_from_email`
- `hot_wallet_address`
- `hot_wallet_secret`
- `cold_wallet_address`
- `standby_wallet_address`
- `mongodb_url`
- `mongodb_database`
- `feelpay_md5key`
- `feelpay_merchant`
- `feelpay_payment_url`
- `pushy_apikey`


## Build
```bash
./gradlew :anansepay-gateway-web:shadowJar

java -jar anansepay-gateway-web/build/libs/anansepay-gateway-web-1.0-SNAPSHOT-fat.jar -conf anansepay-gateway-web/src/main/conf/config.json
```

- Run directly

```
./gradlew :anansepay-gateway-web:run
./gradlew :anansepay-gateway-web:run --debug-jvm # debug mode, attach process in IntelliJ IDEA
```
