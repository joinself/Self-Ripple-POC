package im.ananse.payments.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Created by sena on 24/09/2017.
 */

val ANDROID_KEYSTORE = "AndroidKeyStore"
val AES_MODE = "AES/GCM/NoPadding"
val KEY_ALIAS = "im.ananse.key"

val SALT_FILE = "saltFile"


fun initKeyStore(): KeyStore {

    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
    keyStore.load(null)

    if (!keyStore.containsAlias(KEY_ALIAS)) {

        val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        keyGenerator.init(
                KeyGenParameterSpec.Builder(KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build());
        val key = keyGenerator.generateKey();

        val cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, key);

    }

    return keyStore
}

fun getEncryptionKey() {
    // The key can also be obtained from the Android Keystore any time as follows:
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
    keyStore.load(null);
    val key = keyStore.getKey("key2", null)
}

//    fun getSalt(): ByteArray {
//
//        val saltFile = File(filesDir, SALT_FILE)
//        if (saltFile.exists()) {
//            return saltFile.readBytes()
//        }
//
//    }

@Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
fun generateKey(passphraseOrPin: CharArray, salt: ByteArray): SecretKey {
    // Number of PBKDF2 hardening rounds to use. Larger values increase
    // computation time. You should select a value that causes computation
    // to take >100ms.
    val iterations = 1000

    // Generate a 256-bit key
    val outputKeyLength = 256

    val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val keySpec = PBEKeySpec(passphraseOrPin, salt, iterations, outputKeyLength)
    return secretKeyFactory.generateSecret(keySpec)
}

class Utils {
}