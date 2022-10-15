#include <jni.h>
#include <string>

#include <endian.h>

#include <android/log.h>

#include <ripple/protocol/AccountID.h>
#include <ripple/protocol/digest.h>
#include <ripple/protocol/HashPrefix.h>
#include <ripple/protocol/JsonFields.h>
#include <ripple/protocol/Sign.h>
#include <ripple/protocol/st.h>
#include <ripple/protocol/TxFlags.h>
#include <ripple/basics/StringUtilities.h>
#include <ripple/json/to_string.h>
#include <ripple-libpp/version.hpp>
#include <algorithm>

#define RIPPLE_TEST_TAG		"RippleLibPP_test"

// FOR DEBUG
const std::string byteToHexString(const u_char *buf, const int length) {
    char output[(length * 2) + 1];

    char *ptr = &output[0];
    int i;

    for (i = 0; i < length; i++)
    {
        ptr += sprintf (ptr, "%02X", buf[i]);
    }

    return std::string(output);
}

std::string serialize(ripple::STTx const& tx)
{
using namespace ripple;

return strHex(tx.getSerializer().peekData());
}

std::shared_ptr<ripple::STTx const> deserialize(std::string blob)
{
    using namespace ripple;

    auto ret{ strUnHex(blob) };

    if (!ret.second || !ret.first.size())
        Throw<std::runtime_error>("transaction not valid hex");

    SerialIter sitTrans{ makeSlice(ret.first) };
    // Can Throw
    return std::make_shared<STTx const>(std::ref(sitTrans));
}

//------------------------------------------------------------------------------

bool demonstrateSigning(ripple::KeyType keyType, std::string seedStr,
                        std::string expectedAccount)
{
    using namespace ripple;

    auto const seed = parseGenericSeed(seedStr);
    assert(seed);
    auto const keypair = generateKeyPair(keyType, *seed);
    auto const id = calcAccountID(keypair.first);
    assert(toBase58(id) == expectedAccount);

    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "\n%s secret \"%s\n generates secret key \"%s\" and public key \"%s\"\n",
                        to_string(keyType), seedStr.c_str(), toBase58(*seed).c_str(), toBase58(id).c_str());

    auto const destination = parseBase58<AccountID>(
            "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
    assert(destination);
    auto const gateway1 = parseBase58<AccountID>(
            "rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq");
    assert(gateway1);
    auto const gateway2 = parseBase58<AccountID>(
            "razqQKzJRdB4UxFPWf5NEpEG3WMkmwgcXA");
    assert(gateway2);
    STTx noopTx(ttPAYMENT,
                [&](auto& obj)
                {
                    // General transaction fields
                    obj[sfAccount] = id;
                    obj[sfFee] = STAmount{ 100 };
                    obj[sfFlags] = tfFullyCanonicalSig;
                    obj[sfSequence] = 18;
                    obj[sfSigningPubKey] = keypair.first.slice();
                    // Payment-specific fields
                    obj[sfAmount] = STAmount(Issue(to_currency("USD"), *gateway1),
                                             1234, 5);
                    obj[sfDestination] = *destination;
                    obj[sfSendMax] = STAmount(Issue(to_currency("CNY"), *gateway2),
                                              56789, 7);
                });

    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "\nBefore signing: \n%s\nSerialized: %s\n",
                        noopTx.getJson(0).toStyledString().c_str(),
                        noopTx.getJson(0, true)[jss::tx].toStyledString().c_str());

    noopTx.sign(keypair.first, keypair.second);

    auto const serialized = serialize(noopTx);

    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "\nAfter signing: \n%s\nSerialized: %s\n",
                        noopTx.getJson(0).toStyledString().c_str(),
                        serialized.c_str());

    auto const deserialized = deserialize(serialized);
    assert(deserialized);
    assert(deserialized->getTransactionID() == noopTx.getTransactionID());

    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "\nDeserialized: %s\n",
                        deserialized->getJson(0).toStyledString().c_str());

    auto const check1 = noopTx.checkSign(false);

    std::string status1 = (check1.first ? "Good" : "Bad!");
    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "Check 1: %s\n", status1.c_str());
    assert(check1.first);
//
    // Use the function primitives, which are hidden by the `STTx`
    // interface, to check the signature again and verify that
    // `STTx` is working properly.
    auto const& signatureSlice = noopTx[sfTxnSignature];
    Blob const data = [&]
    {
        // This is a copy of the static `getSigningData` function body
        // which is needed by `verify`.
        Serializer s;
        s.add32(HashPrefix::txSign);
        noopTx.addWithoutSigningFields(s);
        return s.getData();
    }();

    // STTx::checkSign calls `verify` indirectly via `checkSingleSign`
    auto const check2 = verify(
            keypair.first,
            makeSlice(data),
            signatureSlice,
            true);

//    std::string status2 = (check2 ? "Good" : "Bad!");
//    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "Check 2:: %s\n", status2.c_str());

    return check1.first && check2;
}

bool exerciseSingleSign ()
{
    std::vector<bool> passes;

    passes.emplace_back(demonstrateSigning(ripple::KeyType::secp256k1,
                                           "alice", "rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"));

    passes.emplace_back(demonstrateSigning(ripple::KeyType::ed25519,
                                           "alice", "r9mC1zjD9u5SJXw56pdPhxoDSHaiNcisET"));

    // Genesis account w/ not-so-secret key.
    // Never hardcode a real secret key.
    passes.emplace_back(demonstrateSigning(ripple::KeyType::secp256k1,
                                           "snoPBrXtMeMyMHUVTgbuqAfg1SUTb", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"));

    {
        passes.emplace_back(false);
        try
        {
            auto const empty = deserialize("");
        }
        catch (std::runtime_error e)
        {
            passes.back() = true;
            assert(e.what() == std::string("transaction not valid hex"));
        }
    }

    auto const allPass = std::all_of(passes.begin(), passes.end(),
                                     [](auto p) { return p; });
    assert(allPass);
    std::string status = (allPass ?
                  "All single signing checks pass.\n" :
                  "Some single signing checks fail.\n");

    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "%s", status.c_str());
 
    return allPass;
}

//------------------------------------------------------------------------------

// Demonstrate multisigning.

// Helper function that asserts if for some reason we can't create a seed.
ripple::Seed getSeed (std::string const& seedText)
{
    // WARNING!
    // Never use ripple::parseGenericSeed() for secure code.  Call
    // ripple::randomSeed() instead, since it is cryptographically secure.
    boost::optional<ripple::Seed> const possibleSeed {
            ripple::parseGenericSeed (seedText)};

    // Should not be necessary in production code, since you used
    // ripple::randomSeed().  Right?
    assert (possibleSeed);

    return *possibleSeed;
}

// Holds identifying information for an account.
class Credentials
{
    std::string const name_;
    ripple::KeyType const keyType_;
    ripple::Seed const seed_;
    std::pair<ripple::PublicKey, ripple::SecretKey> const keys_;
    ripple::AccountID const id_;

public:
    Credentials (
            std::string name,
            ripple::KeyType keyType = ripple::KeyType::secp256k1)
            : name_ (name)
            , keyType_ (keyType)
            , seed_ (getSeed (name_))
            , keys_ (ripple::generateKeyPair (keyType_, seed_))
            , id_ (ripple::calcAccountID (keys_.first))
    {
    }

    std::string const& name() const { return name_; }
    ripple::KeyType const& keyType() const { return keyType_; }
    ripple::Seed const& seed() const { return seed_; }
    ripple::SecretKey const& secretKey() const { return keys_.second; }
    ripple::PublicKey const& publicKey() const { return keys_.first; }
    ripple::AccountID const& id() const { return id_; }
};

// Build a transaction that can be multisigned.  All fields must be filled in,
// including sequence and fee, before any signatures are applied.  If the
// contents of the transaction are modified then any previously provided
// multi-signatures will become invalid.
ripple::STTx buildMultisignTx (
        ripple::AccountID const& id, std::uint32_t seq, std::uint32_t fee)
{
    using namespace ripple;

    STTx noopTx {ttACCOUNT_SET,
                 [id, seq, fee] (auto& obj)
                 {
                     obj[sfAccount] = id;
                     obj[sfFlags] = tfFullyCanonicalSig;
                     obj[sfFee] = STAmount {fee};               // Must be already filled in
                     obj[sfSequence] = seq;                     // Must be already filled in
                     obj[sfSigningPubKey] = Slice {nullptr, 0}; // Must be present and empty
                 }};

    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "\nBefore signing: %s\n", noopTx.getJson(0, false).toStyledString().c_str());

    return noopTx;
}

// Apply one multi-signature to the supplied transaction.  The signer
// provides their AccountID, PublicKey, and SecretKey.
bool multisign (ripple::STTx& tx, Credentials const& signer)
{
    using namespace ripple;

    // Get the TxnSignature.
    Serializer s = buildMultiSigningData (tx, signer.id());

    auto const multisig = ripple::sign (
            signer.publicKey(), signer.secretKey(), s.slice());

    // Make the signer object that we'll inject into the array.
    STObject element (sfSigner);
    element[sfAccount] = signer.id();
    element[sfSigningPubKey] = signer.publicKey();
    element[sfTxnSignature] = multisig;

    // If a Signers array does not yet exist make one.
    if (! tx.isFieldPresent (sfSigners))
        tx.setFieldArray (sfSigners, {});

    // Insert the signer into the array.
    STArray& signers {tx.peekFieldArray (sfSigners)};
    signers.emplace_back (std::move (element));

    // Sort the Signers array by Account.  If it is not sorted when submitted
    // to the network then it will be rejected.
    std::sort (signers.begin(), signers.end(),
               [](STObject const& a, STObject const& b)
               {
                   return (a[sfAccount] < b[sfAccount]);
               });

    // Verify that the signature is valid.
    bool const pass = tx.checkSign(true).first;
    assert (pass);

    // To submit multisigned JSON to the network use this RPC command:
    // $ rippled submit_multisigned '<all JSON>'

    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "\nMultisigned JSON: %s\n", tx.getJson(0, false).toStyledString().c_str());

    // Alternatively, to submit the multisigned blob to the network:
    //  1. Extract the hex string (including the quotes) following "tx"
    //  2. Then use this RPC command:
    //     $ rippled submit <quoted hex string>

    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "\nMultisigned blob: %s\n", tx.getJson(0, true).toStyledString().c_str());

    return pass;
}

// Outlines a multisign process where:
//  1. A multi-signable transaction is built.
//  2. That transaction is signed by one signer.
//  3. The transaction is signed by a different signer.
bool exerciseMultiSign()
{
    using namespace ripple;

    // Create credentials for the folks involved in the transaction.
    Credentials const alice {"alice"};
    Credentials const billy {"billy"};
    Credentials const carol {"carol"};

    // Create a transaction on alice's account.  alice doesn't sign it.
    STTx tx {buildMultisignTx (alice.id(), 2, 100)};

    // billy and carol sign alice's transaction for her.
    bool allPass = multisign (tx, billy);
    allPass &= multisign (tx, carol);

    return allPass;
}

//------------------------------------------------------------------------------

void test ()
{

    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, "ripple-libpp_demo version  %s", RIPPLE_LIBPP_VERSION_STRING);

    // Demonstrate single signing.
    auto allPass = exerciseSingleSign();

    // Demonstrate multisigning.
    allPass &= exerciseMultiSign();

    assert(allPass);
    __android_log_print(ANDROID_LOG_INFO, RIPPLE_TEST_TAG, (allPass ? "All checks pass.\n" : "Some checks fail.\n"));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_RippleLibPP_startTest(JNIEnv *env, jobject instance) {
    test();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ripple_ripplelibpp_RippleLibPP_getVersion(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = RIPPLE_LIBPP_VERSION_STRING;
    return env->NewStringUTF(hello.c_str());
}



