**anansepay Gateway Client API**

The client apps will interact with the gateway using REST API calls. In order 
to verify the identity of the user all API calls will be signed with the Ripple
keypair related to the account for that user.

anansepay uses Ripple keypairs on the Koblitz curves with secp256k1 parameters and 
ECDSA signatures exclusively. Support for ED25519 (based on Curve25519) with Schnorr 
signatures is limited on client libraries and can not still be used at the time of 
writing (July 2017).

However the system should move to ED25519 in the future once support is stable.

The gateway allows four functions:

 - **User provisioning** (using the Twilio SDK for two factor authentication)
 - **Push notification token update** (using the Pushy SDK)
 - **Topup**: Using the FeelPay API or BlockCypher BTC payment forwarding API.
 - **Withdrawal** : The withdrawal request gets entered into a ledger for manual processing.


Current provisioning gateways:

 - Production:   https://anansepay-gateway.ananse.im
 - Staging:      https://stage-anansepay-gateway.ananse.im
 
 Note that transactions will have to be sent directly to a JSON-RPC Ripple server. We run two 
 systems to that effect:
 - Production rippled: https://prod-ripple.ananse.im
 - Staging rippled (on Ripple TestNet): https://stage-ripple.ananse.im

**User Provisioning**

The client app needs to be able to generate a Ripple wallet using the curves defined 
above. The ripple secret should **never** leave the client app with any API, so the 
client SDK needs to be able to sign transactions **locally**, not relying on the server
to sign transactions on its behalf.

For the provisioning, the flow outlined here ties the ripple address, the secp256k1 used to 
generate the address, and a telephone number under the control of the user.

1- **Provisioning request**: This is a POST request to **/provisioning/request** with the following JSON
object as the POST body. This results on an SMS code (6 digits) sent to the request telephone number.
```
        {
          "adddress": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
          "telnumber": "+441234567890",
          "timestamp": "1500995339",
          "pubkey" : "xxxxxxxxxbase64encodedpubkey",
          "signature": "XXXXbase64encodedsigXXX"
        } 
```
The address is the ripple address, the telephone number is a normal E.164 identifier, the timestamp is
a string version of the unix timestamp (seconds only), the pubkey is the base64 encoded secp256k1 public
key related to the ripple address and the signature is the ECDSA signature of the concatenated string of
address+telnumber+timestamp. The ECDSA generation is explained on RippleSignTest.TestProvisioningMessage().

The system does verify that the address belongs to the public key sent and that the signature has been
signed with the relevant public key. The SMS is sent via Twilio.

2-  **Provisioning SMS verification**: This is a POST request to **/provisioning/validate** with the following
JSON object as the body. If all goes well and the account has not been provisioned before, this results
on 35 XRP being sent to the account from the hot wallet.
```
       {
          "adddress": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
          "signature": "xxxxxbase64xxxxxx"
       }
``` 
The address is the ripple address and the signature is the ECDSA signature of the concatenated string of
address+smscode. The ECDSA generation is explained on RippleSignTest.TestProvisioningReply().
If the account has been previously provisioned the 35XRP is not sent.

The reply to the provisioning sms verification (if successful) will contain the newly assigned destination tag
required to perform withdrawals. The destination tag is a 32 bit number, randomly assigned each time a user completes
provisioning. It should be kept within the client app alongside the rest of the cryptographic material for that 
address.


```
{"message":"Account updated","dtag":311902533}
```



**Push notification token update**

The client app can be notified via push notifications whenever new transactions have been sent to it. This is a
useful feature as it will prevent users from having to refresh whenever a transaction has been submitted to the
ripple ledger.

The push notification update is a single step as a POST request to **/provisioning/pushtoken** with the following
JSON object as the POST body.
```
        {
          "adddress": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
          "token: "dfgsdkfjgksdg...sdfgsdfgdsfg",
          "timestamp": "1500995339",
          "signature": "XXXXbase64encodedsigXXX"
        }
```

The address is the ripple address, the token is the pushtoken received upon client registration on the Pushy
API, the timestamp is a string version of the unix timestamp (seconds only) and the signature is the ECDSA
signature of the concatenated string address+token+timestamp. That signature generation is explained on 
RippleSignTest.TestPushtokenRequest().

NOTE: Currently only a single token is allowed per address. Multi-device is not considered yet, but could be 
added without much difficulty.


**Bitcoin topup address generation**

To topup Bitcoin, the gateway will generate a single per-user temporary Bitcoin address using the BlockCypher API.
The address will be wired up to transfer funds (minus a 5000 satoshis fee) to the BTC cold wallet used by the system.
Transfers with less than 5000 satoshis (0.05 mBTC) will not work.

The generation is done via a POST request to **/provisioning/bitcoingen** with the following JSON object as the POST
body.
```
        {
          "address":"rHzi7AJ6JS6x9GxAQENug4aXfofYzB1KGt",
          "timestamp":1508124950845,
          "signature":"MEQCIF1B6C6YxjHAKclsb9Of0ZMEwQussNaq6Qjxc5BXsMkUAiB8eX8r0ZT/JbyotqE9h7nw2REeItfrVA6VNJ2qFIGIgg=="
        }
```

The response is a 200 OK with a JSON reply containing the bitcoin address. A 401 if the user or signatures are 
incorrect and a 400 if the JSON is malformed.

NOTE: The Staging server runs on the BlockCypher Test  BCY chain, not on the main bitcoin chain.

**Credit topup**

- CNY topup

To topup the anansepay credit with CNY, the user needs to complete a FeelPay API internet payment flow. The FeelPay API
flow is completely user side and starts with a POST query to the FeelPay API endpoint with a set of POST parameters
configured and signed by the anansepay gateway. Those parameters (and the current payment URI) are obtained from the 
credit topup request API endpoint.

The credit topup request is a single step POST request to /topup/request with the following JSON as the POST body.
```
        {
          "adddress": "rGZG674DSZJfoY8abMPSgChxZTJZEhyMRm",
          "amount": 10000, (integer in cents)
          "currency": "CNY",
          "signature": "XXXXbase64encodedsigXXX"
        }
```
The address is the ripple address, the amount is the number of CNY in cents (amount /100  is the number of yuan to be 
credited) and the signature is the ECDSA signature of the concatenated string address+amount.toString()+currency. The 
signature generation is explained on RippleSignTest.TestTopupRequest().


The function returns a JSON object that has to be parsed by the client app the JSON object contains: the order number
that uniquely references this topup request, the payment URL to send the FeelPay API POST request to and the postdata 
string with all the parameters needed to start the payment process.

```
        {
          "order" : "C00001xXXXXXXXX",
          "url": "http://121.201.38.37:8076/api/payment.aspx",
          "postdata": "....postdatastring......"
        }


```

If the credit flow completes, the user will be redirected to the topup return page (/topup/return). This page just 
returns to the user the state of the order.This redirection however happens **before** the FeelPay API server 
notifies the gateway of the transaction completion, so at the time of the return the transaction is in USERCOMPLETED
stage. 

The /topup/return page result should return to the main app screen. The callback from the server will trigger a push
notification indicating the need for an app refresh with the transaction result.


 - Bitcoin topup
 
 Bitcoin topups are handled automatically by the system. A Bitcoin transfer to the per-user address generated by
 the /provisioning/bitcoingen endpoint will automatically be accounted on the users account using Blockcypher 
 notifications.
 
**Withdrawal**

Withdrawals are just ripple payments of anansepay issued tokens towards the issuer address. The payment will need
to contain the DestinationTag sent to the user during the provisioning verification step (this is a 32 bit number).

However, in order to avoid problems with missed notifications from the rippled server, a withdrawal needs to
be registered with the system. The withdrawal registration request is a POST request to the **/provisioning/withdrawal**
endpoint with a JSON object in the following format.

```
   {
     "address":"rHzi7AJ6JS6x9GxAQENug4aXfofYzB1KGt",
     "amount":10000.0,
     "currency":"CNY",
     "bankname":"Bank of China",
     "accountnumber":"123123123123123",
     "accountholder":"Leeeeroy Jeeeeenkins",
     "txhash":"D93D89DCBF9320F6576E4A57C4E31983107A92D8A62C7FF50773CC217017088F",
     "signature":"MEQCIDQQz/sKkNfbMzaRq3FVUaKsuOJi/imz7ax12Utj/OCxAiBylWHLHvEJgcapI3Vcl/C6pJQ++UJmKlytKaAgSOA8xQ=="
   }
```

The txhash is the ripple hash of the transaction of the relevant BTC or CNY tokens from the user's wallet to
the system cold wallet. This transaction has to be performed first before calling this endpoint.
 
The signature calculation is done using the relevant RippleSignTest code (Android) or the C++ anansepayWallet class.

The transactions will be processed manually by the system administrators.


An example of such transaction from a user is here:

```$xslt
# /opt/ripple/bin/rippled submit ss2Grp23bEdhjGVyVXbzkbNCVVpPo  '{"TransactionType":"Payment","Account":"rHzi7AJ6JS6x9GxAQENug4aXfofYzB1KGt","Destination":"rJHscEPC5HmzLBu7uy1QeqGe6XDk2iaxCx", "DestinationTag": "311902533" ,"Amount":{"currency":"CNY","value":"0.05","issuer":"rJHscEPC5HmzLBu7uy1QeqGe6XDk2iaxCx"}}'
Loading: "/etc/opt/ripple/rippled.cfg"
2017-Aug-02 10:17:38 HTTPClient:NFO Connecting to 127.0.0.1:5005

{
   "id" : 1,
   "result" : {
      "engine_result" : "tesSUCCESS",
      "engine_result_code" : 0,
      "engine_result_message" : "The transaction was applied. Only final in a validated ledger.",
      "status" : "success",
      "tx_blob" : "120000228000000024000000062E1297414561D411C37937E08000000000000000000000000000434E590000000000BD82F874DA37521172244834C1F1039C6B43A95B68400000000000000A732103F3B4304990F3ACC19CA1476D7748D0667221E27840948DF6B7EE267DFF0A5A6A7446304402203B8EC30B60D28FBFFD568DE424AC6945C7F3ED19EDF3FD4437B71435382014DB02206A2A02B90B3B34B2D3ED836CA304519AD4D210DB2B891ADF90404953E5D07FCF8114BA73070EFA4B7F6AB6B14E0F9C9B9BE7FBB2631C8314BD82F874DA37521172244834C1F1039C6B43A95B",
      "tx_json" : {
         "Account" : "rHzi7AJ6JS6x9GxAQENug4aXfofYzB1KGt",
         "Amount" : {
            "currency" : "CNY",
            "issuer" : "rJHscEPC5HmzLBu7uy1QeqGe6XDk2iaxCx",
            "value" : "0.05"
         },
         "Destination" : "rJHscEPC5HmzLBu7uy1QeqGe6XDk2iaxCx",
         "DestinationTag" : 311902533,
         "Fee" : "10",
         "Flags" : 2147483648,
         "Sequence" : 6,
         "SigningPubKey" : "03F3B4304990F3ACC19CA1476D7748D0667221E27840948DF6B7EE267DFF0A5A6A",
         "TransactionType" : "Payment",
         "TxnSignature" : "304402203B8EC30B60D28FBFFD568DE424AC6945C7F3ED19EDF3FD4437B71435382014DB02206A2A02B90B3B34B2D3ED836CA304519AD4D210DB2B891ADF90404953E5D07FCF",
         "hash" : "94F5D7BC0A6C9053C27566951EC96BB07C6173030BB54BA1670C0E2F1A842AAD"
      }
   }
}

```

This will normally trigger an event. The server will be notified and log a withdrawal request. If these 
are missed the administrators can look up the txhash manually to verify the withdrawal.

