package com.example.ripplelibppdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ripple.ripplelibpp.AccountID;
import com.ripple.ripplelibpp.Buffer;
import com.ripple.ripplelibpp.Credentials;
import com.ripple.ripplelibpp.Currency;
import com.ripple.ripplelibpp.Issue;
import com.ripple.ripplelibpp.KeyType;
import com.ripple.ripplelibpp.Pair;
import com.ripple.ripplelibpp.PublicKey;
import com.ripple.ripplelibpp.RippleLibPP;
import com.ripple.ripplelibpp.STAmount;
import com.ripple.ripplelibpp.STTx;
import com.ripple.ripplelibpp.SecretKey;
import com.ripple.ripplelibpp.Seed;
import com.ripple.ripplelibpp.Serializer;
import com.ripple.ripplelibpp.TxFlags;
import com.ripple.ripplelibpp.TxType;

import junit.framework.Assert;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "RippleJavaTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView) findViewById(R.id.label)).setText(RippleLibPP.getVersion());

	test();
    }

    public String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    boolean exerciseSingleSign() {
        boolean result;

        result = demonstrateSigning(KeyType.SECP256K1,
                "alice", "rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn");

        result = result & demonstrateSigning(KeyType.ED25519,
                "alice", "r9mC1zjD9u5SJXw56pdPhxoDSHaiNcisET");

        // Genesis account w/ not-so-secret key.
        // Never hardcode a real secret key.
        result = result & demonstrateSigning(KeyType.SECP256K1,
                "snoPBrXtMeMyMHUVTgbuqAfg1SUTb", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

        try {
            STTx.deserialize("");
            result = false;
        } catch (IllegalArgumentException e) {
        }

        Assert.assertEquals(true, result);
        Log.i(TAG, (result ?
                  "All single signing checks pass.\n" :
                  "Some single signing checks fail.\n"));

        return result;
    }

    public boolean demonstrateSigning(int keyType, String seedStr, String expectedAccount) {

        Seed seed = Seed.parseGenericSeed(seedStr);
        Assert.assertNotNull(seed);
        Pair<PublicKey, SecretKey> keypair = SecretKey.generateKeyPair(keyType, seed);
        AccountID id = AccountID.calcAccountID(keypair.getFirst());
        Assert.assertEquals(true, expectedAccount.equals(AccountID.toBase58(id)));

        AccountID destination = AccountID.parseBase58("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
        Assert.assertNotNull(destination);
        AccountID gateway1 = AccountID.parseBase58("rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq");
        Assert.assertNotNull(gateway1);
        AccountID gateway2 = AccountID.parseBase58("razqQKzJRdB4UxFPWf5NEpEG3WMkmwgcXA");
        Assert.assertNotNull(gateway2);

        String sKeyType = "invalid";
        if (keyType == KeyType.SECP256K1) sKeyType = "secp256k1";
        else if (keyType == KeyType.ED25519) sKeyType = "ed25519";

        Log.i(TAG, "\n" + sKeyType + " secret \"" + seedStr
                + "\" generates secret key \"" + Seed.toBase58(seed)
                + "\" and public key \"" + AccountID.toBase58(id) + "\"\n");

        STAmount sfFee = new STAmount(100);
        STAmount sfAmount = new STAmount(new Issue(Currency.toCurrency("USD"), gateway1), 1234, 5);
        STAmount sfSendMax = new STAmount(new Issue(Currency.toCurrency("CNY"), gateway2), 56789, 7);

        STTx noopTx = STTx.getBuilder().setTxType(TxType.TT_PAYMENT).setAccount(id).setFee(sfFee).setFlags(TxFlags.tfFullyCanonicalSig)
                .setSequence(18).setSigningPublicKey(keypair.getFirst()).setAmount(sfAmount)
                .setDestination(destination).setSendMax(sfSendMax).build();

        Log.i(TAG, "\nBefore signing: \n" + noopTx.getStyledJsonString(0) + "\n" +
                "Serialized: " + noopTx.getStyledJsonString(0, true) + "\n");

        noopTx.sign(keypair.getFirst(), keypair.getSecond());

        String serialized = STTx.serialize(noopTx);
        Log.i(TAG, "\nAfter signing: \n" + noopTx.getStyledJsonString(0) + "\n" +
                "Serialized: " + serialized + "\n");

        STTx deserialized = STTx.deserialize(serialized);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(true, Arrays.equals(deserialized.getTransactionID(), noopTx.getTransactionID()));

        Log.i(TAG, "Deserialized: " +
                deserialized.getStyledJsonString(0) + "\n");

        Pair<Boolean, String> check1 = noopTx.checkSign(false);

        Log.i(TAG, "Check 1: " + (check1.getFirst() ? "Good" : "Bad!"));
        Assert.assertEquals(true, check1.getFirst().booleanValue());

        boolean check2 = noopTx.verify(keypair.getFirst(), true);
        Log.i(TAG, "Check 2: " + (check2 ? "Good" : "Bad!"));

        return check1.getFirst() && check2;
    }

    boolean exerciseMultiSign() {
        // Create credentials for the folks involved in the transaction.
        Credentials alice = new Credentials("alice");
        Credentials billy = new Credentials("billy");
        Credentials carol = new Credentials("carol");

        // Create a transaction on alice's account.  alice doesn't sign it.
        STTx tx = STTx.buildMultisignTx(alice.id(), 2, 100);
        Log.i(TAG, "\nBefore signing: " + tx.getStyledJsonString(0) + "\n");

        // billy and carol sign alice's transaction for her.
        boolean allPass = tx.multisign(billy);
        Assert.assertEquals(true, allPass);
        Log.i(TAG, "\nMultisigned JSON: " + tx.getStyledJsonString(0, false) + "\n");
        Log.i(TAG, "\nMultisigned blob: " + tx.getStyledJsonString(0, true) + "\n");

        allPass &= tx.multisign(carol);
        Assert.assertEquals(true, allPass);
        Log.i(TAG, "\nMultisigned JSON: " + tx.getStyledJsonString(0, false) + "\n");
        Log.i(TAG, "\nMultisigned blob: " + tx.getStyledJsonString(0, true) + "\n");

        return allPass;
    }

    void test() {

        Log.i(TAG, "ripple-libpp_demo version  " + RippleLibPP.getVersion());

        // Demonstrate single signing.
        boolean allPass = exerciseSingleSign();

        // Demonstrate multisigning.
        allPass &= exerciseMultiSign();

        Assert.assertEquals(true, allPass);
        Log.i(TAG, (allPass ? "All checks pass.\n" : "Some checks fail.\n"));
    }
}
