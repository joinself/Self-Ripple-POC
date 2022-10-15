package im.ananse.payments.model

import io.realm.DynamicRealm


/**
 * Created by sena on 28/09/2017.
 */
class RealmMigration: RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {

        // DynamicRealm exposes an editable schema
        val schema = realm.getSchema()
        var transientVersion = oldVersion

        if (transientVersion == 0L) {
            val serverInforSchema = schema.get("ServerInfo")
                    if (serverInforSchema!!.hasField("fee")) {
                        serverInforSchema.removeField("fee")
                                .addField("fee", Long::class.java)
                    }

            val profileSchema = schema.get("Profile")
            if(!profileSchema!!.hasField("btcTopupAddress")) {
                profileSchema.addField("btcTopupAddress", String::class.java)
            }

            transientVersion.inc()
        }

        if (transientVersion == 1L) {

            val profileSchema = schema.get("Profile")

                    if (profileSchema!!.hasField("destinationTag")) {
                        profileSchema.removeField("destinationTag")
                    }

            val walletSchema = schema.get("Wallet")

                    if (walletSchema!!.hasField("destinationTag")) {
                        walletSchema.removeField("destinationTag")
                                .addField("destinationTag", String::class.java)
                    }


            transientVersion.inc()
        }

        if (transientVersion == 2L) {

            val profileSchema = schema.get("Profile")
            if (profileSchema?.hasField("rippleSequenceNo")!!) {
                profileSchema.removeField("rippleSequenceNo")
            }

            if (schema.contains("VPayTransaction")) {
                schema.remove("VPayTransaction")
            }

                schema.remove("VPayTransaction")

                schema.create("VPayTransaction")
                        .addField("account", String::class.java)
                        .setRequired("account", true)
                        .addField("destinationAccount", String::class.java)
                        .setRequired("destinationAccount", true)
                        .addField("txHash", String::class.java)
                        .addPrimaryKey("txHash")
                        .setRequired("txHash", true)
                        .addField("timestamp", Long::class.java)
                        .addIndex("timestamp")
                        .addField("amount", String::class.java)
                        .setRequired("amount", true)
                        .addField("currencyCode", String::class.java)
                        .setRequired("currencyCode", true)
                        .addField("transactionType", String::class.java)
                        .setRequired("transactionType", true)
                        .addField("txId", Long::class.java)

            transientVersion.inc()
        }

        if (transientVersion == 3L) {

            val profileSchema = schema.get("Profile")
            if (profileSchema?.hasField("rippleSequenceNo")!!) {
                profileSchema.removeField("rippleSequenceNo")
            }
            if (profileSchema.hasField("lastSyncedLedger")) {
                profileSchema.removeField("lastSyncedLedger")
                profileSchema.addField("lastSyncedLedger", Int::class.java)
                profileSchema.setNullable("lastSyncedLedger", true)
            }

            if (schema.contains("VPayTransaction")) {
                schema.remove("VPayTransaction")
            }

                schema.create("VPayTransaction")
                        .addField("account", String::class.java)
                        .setRequired("account", true)
                        .addField("destinationAccount", String::class.java)
                        .setRequired("destinationAccount", true)
                        .addField("txHash", String::class.java)
                        .addPrimaryKey("txHash")
                        .setRequired("txHash", true)
                        .addField("timestamp", Long::class.java)
                        .addIndex("timestamp")
                        .addField("amount", String::class.java)
                        .setRequired("amount", true)
                        .addField("currencyCode", String::class.java)
                        .setRequired("currencyCode", true)
                        .addField("transactionType", String::class.java)
                        .setRequired("transactionType", true)
//                        .addField("txId", Long::class.java)

            if (schema.contains("ExchangeRate")) {
                schema.remove("ExchangeRate")
            }
            schema.create("ExchangeRate")
                .addField("currencyCode", String::class.java)
                .setRequired("currencyCode", true)
                .addPrimaryKey("currencyCode")
                .addField("rates", String::class.java)
                .setRequired("rates", true)

            if (schema.contains("Contact")) {
                schema.remove("Contact")
            }
            schema.create("Contact")
                    .addField("address", String::class.java)
                    .setRequired("address", true)
                    .addPrimaryKey("address")
                    .addField("name", String::class.java)
                    .addIndex("name")


//            var counterSchema = schema.get("TransactionCounter")
//            if (counterSchema == null) {
//                counterSchema = schema.create("TransactionCounter")
//            }
//            counterSchema.addField("currentCount", Long::class.java)

            transientVersion.inc()


        }

        if (transientVersion == 4L) {

            val profileSchema = schema.get("Profile")
            if (profileSchema?.hasField("lastSyncedLedger")!!) {
                profileSchema.removeField("lastSyncedLedger")
                profileSchema.addField("lastSyncedLedger", Int::class.java)
                profileSchema.setNullable("lastSyncedLedger", true)
            } else {
                profileSchema.addField("lastSyncedLedger", Int::class.java)
                profileSchema.setNullable("lastSyncedLedger", true)
            }
            if (profileSchema.hasField("destinationTag")) {
                profileSchema.removeField("destinationTag")
            }
            if (profileSchema.hasField("rippleSequenceNo")) {
                profileSchema.removeField("rippleSequenceNo")
            }

            val walletSchema = schema.get("Wallet")
            if (walletSchema?.hasField("destinationTag")!!) {
                walletSchema.removeField("destinationTag")
                walletSchema.addField("destinationTag", String::class.java)
            }

            if (schema.contains("ExchangeRate")) {
                schema.remove("ExchangeRate")
            }
            schema.create("ExchangeRate")
                    .addField("currencyCode", String::class.java)
                    .setRequired("currencyCode", true)
                    .addPrimaryKey("currencyCode")
                    .addField("rates", String::class.java)
                    .setRequired("rates", true)

            if (schema.contains("Contact")) {
                schema.remove("Contact")
            }
            schema.create("Contact")
                    .addField("address", String::class.java)
                    .setRequired("address", true)
                    .addPrimaryKey("address")
                    .addField("name", String::class.java)
                    .addIndex("name")

            if (schema.contains("VPayTransaction")) {
                schema.remove("VPayTransaction")
            }

            schema.create("VPayTransaction")
                    .addField("account", String::class.java)
                    .setRequired("account", true)
                    .addField("destinationAccount", String::class.java)
                    .setRequired("destinationAccount", true)
                    .addField("txHash", String::class.java)
                    .addPrimaryKey("txHash")
                    .setRequired("txHash", true)
                    .addField("timestamp", Long::class.java)
                    .addIndex("timestamp")
                    .addField("amount", String::class.java)
                    .setRequired("amount", true)
                    .addField("currencyCode", String::class.java)
                    .setRequired("currencyCode", true)
                    .addField("transactionType", String::class.java)
                    .setRequired("transactionType", true)


            transientVersion.inc()
        }

        if (transientVersion == 5L) {

            if (schema.contains("VPayTransaction")) {
                schema.remove("VPayTransaction")
            }

            schema.create("VPayTransaction")
                    .addField("account", String::class.java)
                    .setRequired("account", true)
                    .addField("destinationAccount", String::class.java)
                    .setRequired("destinationAccount", true)
                    .addField("txHash", String::class.java)
                    .addPrimaryKey("txHash")
                    .setRequired("txHash", true)
                    .addField("timestamp", Long::class.java)
                    .addIndex("timestamp")
                    .addField("amount", String::class.java)
                    .setRequired("amount", true)
                    .addField("currencyCode", String::class.java)
                    .setRequired("currencyCode", true)
                    .addField("transactionType", String::class.java)
                    .setRequired("transactionType", true)
                    .addField("validated", Boolean::class.java)

        }
    }

}