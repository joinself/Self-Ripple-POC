//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <endian.h>

#include <ripple/protocol/PublicKey.h>
#include <ripple/protocol/STAmount.h>
#include <ripple/protocol/STTx.h>
#include <ripple/protocol/STAccount.h>
#include <ripple/basics/StringUtilities.h>
#include <ripple/protocol/HashPrefix.h>
#include <ripple/protocol/TxFlags.h>
#include <ripple/protocol/Sign.h>
#include <ripple/protocol/st.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_STTx_initialise(JNIEnv *env, jobject instance, jint txType_, jobject sfAccount_,
                                            jobject sfFee_, jlong sfFlags_, jint sfSequence_,
                                            jobject sfSigningPubKey_, jobject sfAmount_,
                                            jobject sfDestination_, jobject sfSendMax_) {

    ripple::AccountID *_sfAccount = getPointer<ripple::AccountID >(env, sfAccount_);
    ripple::STAmount *_sfFee = getPointer<ripple::STAmount>(env, sfFee_);
    ripple::PublicKey *_publicKey = getPointer<ripple::PublicKey>(env, sfSigningPubKey_);
    ripple::STAmount *_sfAmount = getPointer<ripple::STAmount>(env, sfAmount_);
    ripple::AccountID *_sfDestination = getPointer<ripple::AccountID >(env, sfDestination_);
    ripple::STAmount *_sfSendMax = getPointer<ripple::STAmount>(env, sfSendMax_);

    ripple::STTx *sttx = new ripple::STTx(static_cast<ripple::TxType >(txType_), [&](auto& obj) {
        obj[ripple::sfAccount] = (ripple::base_uint<160, ripple::detail::AccountIDTag>)*_sfAccount;
        obj[ripple::sfFee] = ripple::STAmount {*_sfFee};
        obj[ripple::sfFlags] = sfFlags_;
        obj[ripple::sfSequence] = sfSequence_;
        obj[ripple::sfSigningPubKey] = *_publicKey;

        obj[ripple::sfAmount] = *_sfAmount;
        obj[ripple::sfDestination] = *_sfDestination;
        obj[ripple::sfSendMax] = *_sfSendMax;
    });

    setPointer<ripple::STTx>(env, instance, sttx);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_STTx_delete(JNIEnv *env, jobject instance) {

    ripple::STTx *sttx = getPointer<ripple::STTx>(env, instance);
    setPointer<ripple::STTx>(env, instance, 0);

    delete sttx;

}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ripple_ripplelibpp_STTx_getStyledJsonString__IZ(JNIEnv *env, jobject instance,
                                                         jint options, jboolean binary) {

    ripple::STTx *sttx = getPointer<ripple::STTx>(env, instance);
    std::string returnValue = sttx->getJson(options, binary).toStyledString();

    return env->NewStringUTF(returnValue.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_STTx_sign(JNIEnv *env, jobject instance, jobject publicKey,
                                      jobject secretKey) {

    ripple::STTx *sttx = getPointer<ripple::STTx>(env, instance);
    ripple::PublicKey *publicKey1 = getPointer<ripple::PublicKey>(env, publicKey);
    ripple::SecretKey *secretKey1 = getPointer<ripple::SecretKey>(env, secretKey);

    sttx->sign(*publicKey1, *secretKey1);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ripple_ripplelibpp_STTx_serialize(JNIEnv *env, jclass type, jobject tx) {

    ripple::STTx *sttx = getPointer<ripple::STTx>(env, tx);
    std::string returnValue = ripple::strHex(sttx->getSerializer().peekData());

    return env->NewStringUTF(returnValue.c_str());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_STTx_deserialize(JNIEnv *env, jclass type, jstring blob_) {

    const char *blob = env->GetStringUTFChars(blob_, 0);

    auto ret{ ripple::strUnHex(blob) };

    if (!ret.second || !ret.first.size()) {
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "transaction not valid hex");
        return nullptr;
    }

    ripple::SerialIter sitTrans{ ripple::makeSlice(ret.first) };
    // Can Throw
    std::shared_ptr<const ripple::STTx> sttx = std::make_shared<ripple::STTx const>(std::ref(sitTrans));
    const ripple::STTx *tx = sttx.get();
    ripple::STTx *sTx = new ripple::STTx(*tx);

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/STTx");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(sTx));

    env->ReleaseStringUTFChars(blob_, blob);

    return obj;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_ripple_ripplelibpp_STTx_getTransactionID(JNIEnv *env, jobject instance) {

    ripple::STTx *sttx = getPointer<ripple::STTx>(env, instance);
    auto const id = sttx->getTransactionID();

    jbyteArray arr = env->NewByteArray(id.size());
    env->SetByteArrayRegion(arr, 0, id.size(), (jbyte *) id.data());

    return arr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_STTx_checkSign(JNIEnv *env, jobject instance, jboolean allowMultiSign) {

    ripple::STTx *sttx = getPointer<ripple::STTx>(env, instance);

    std::pair<bool, std::string> result = sttx->checkSign(allowMultiSign);

    jclass clazzBoolean = env->FindClass("java/lang/Boolean");
    jmethodID constructorBoolean = env->GetMethodID(clazzBoolean, "<init>", "(Z)V");
    jobject objBoolean = env->NewObject(clazzBoolean, constructorBoolean, result.first);

    jclass clazzString = env->FindClass("java/lang/String");
    jmethodID constructorString = env->GetMethodID(clazzString, "<init>", "(Ljava/lang/String;)V");
    jobject objString = env->NewObject(clazzString, constructorString, env->NewStringUTF(result.second.c_str()));

    jclass clazzPair = env->FindClass("com/ripple/ripplelibpp/Pair");
    jmethodID constructorPair = env->GetMethodID(clazzPair, "<init>", "()V");
    jmethodID setFirst = env->GetMethodID(clazzPair, "setFirst", "(Ljava/lang/Object;)V");
    jmethodID setSecond = env->GetMethodID(clazzPair, "setSecond", "(Ljava/lang/Object;)V");
    jobject pairObject = env->NewObject(clazzPair, constructorPair);
    env->CallVoidMethod(pairObject, setFirst, objBoolean);
    env->CallVoidMethod(pairObject, setSecond, objString);

    return pairObject;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_STTx_addWithoutSigningFields(JNIEnv *env, jobject instance, jobject s) {

    ripple::Serializer *serializer = getPointer<ripple::Serializer>(env, s);
    ripple::STTx *sttx = getPointer<ripple::STTx>(env, instance);

    sttx->addWithoutSigningFields(*serializer);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_ripple_ripplelibpp_STTx_verify(JNIEnv *env, jobject instance, jobject publicKey,
                                        jboolean mustBeFullyCanonical) {

    ripple::STTx *sttx = getPointer<ripple::STTx>(env, instance);
    ripple::PublicKey *pKey = getPointer<ripple::PublicKey>(env, publicKey);

    ripple::STTx tx = *sttx;
    auto const& signatureSlice = tx[ripple::sfTxnSignature];
    ripple::Blob const data = [&]
    {
        // This is a copy of the static `getSigningData` function body
        // which is needed by `verify`.
        ripple::Serializer s;
        s.add32(ripple::HashPrefix::txSign);
        tx.addWithoutSigningFields(s);
        return s.getData();
    }();

    // STTx::checkSign calls `verify` indirectly via `checkSingleSign`
    return (jboolean)verify(*pKey, ripple::makeSlice(data), signatureSlice, true);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_STTx_buildMultisignTx(JNIEnv *env, jclass type, jobject id, jint seq,
                                                  jint fee) {

    ripple::AccountID *aID = getPointer<ripple::AccountID>(env, id);

    u_int32_t seq_ = (u_int32_t)seq;
    u_int32_t fee_ = (u_int32_t)fee;

    ripple::STTx tx {ripple::ttACCOUNT_SET,
                 [aID, seq_, fee_] (auto& obj)
                 {
                     obj[ripple::sfAccount] = *aID;
                     obj[ripple::sfFlags] = ripple::tfFullyCanonicalSig;
                     obj[ripple::sfFee] = ripple::STAmount {fee_};               // Must be already filled in
                     obj[ripple::sfSequence] = seq_;                             // Must be already filled in
                     obj[ripple::sfSigningPubKey] = ripple::Slice {nullptr, 0}; // Must be present and empty
                 }};


    ripple::STTx *sTx = new ripple::STTx(tx);

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/STTx");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(sTx));

    return obj;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_ripple_ripplelibpp_STTx_multisign(JNIEnv *env, jobject instance, jobject id, jobject pk, jobject sk) {

    ripple::STTx *sttx = getPointer<ripple::STTx>(env, instance);
    ripple::AccountID *aID = getPointer<ripple::AccountID>(env, id);
    ripple::PublicKey *publicKey = getPointer<ripple::PublicKey>(env, pk);
    ripple::SecretKey *secretKey = getPointer<ripple::SecretKey>(env, sk);

    ripple::Serializer s = ripple::buildMultiSigningData(*sttx, *aID);
    auto const multisig = ripple::sign (*publicKey, *secretKey, s.slice());

    // Make the signer object that we'll inject into the array.
    ripple::STObject element (ripple::sfSigner);
    element[ripple::sfAccount] = *aID;
    element[ripple::sfSigningPubKey] = *publicKey;
    element[ripple::sfTxnSignature] = multisig;

    // If a Signers array does not yet exist make one.
    if (! sttx->isFieldPresent (ripple::sfSigners))
        sttx->setFieldArray (ripple::sfSigners, {});

    // Insert the signer into the array.
    ripple::STArray& signers {sttx->peekFieldArray (ripple::sfSigners)};
    signers.emplace_back (std::move (element));

    // Sort the Signers array by Account.  If it is not sorted when submitted
    // to the network then it will be rejected.
    std::sort (signers.begin(), signers.end(),
               [](ripple::STObject const& a, ripple::STObject const& b)
               {
                   return (a[ripple::sfAccount] < b[ripple::sfAccount]);
               });

    // Verify that the signature is valid.
    bool const pass = sttx->checkSign(true).first;

    return (jboolean )pass;

}