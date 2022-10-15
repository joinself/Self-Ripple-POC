//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include <android/log.h>
#include "pointer.h"
#include <endian.h>

#include <ripple/protocol/SecretKey.h>

#define RIPPLE_TEST_TAG		"RippleLibPP_test"

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_SecretKey_initialise(JNIEnv *env, jobject instance) {

    ripple::SecretKey *pKey = new ripple::SecretKey();
    setPointer(env, instance, pKey);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_SecretKey_initialiseo(JNIEnv *env, jobject instance, jobject other) {

    ripple::SecretKey *otherKey = getPointer<ripple::SecretKey>(env, other);
    ripple::SecretKey *secretKey = new ripple::SecretKey(*otherKey);
    setPointer<ripple::SecretKey>(env, instance, secretKey);
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_ripple_ripplelibpp_SecretKey_data(JNIEnv *env, jobject instance) {

    ripple::SecretKey *secretKey = getPointer<ripple::SecretKey>(env, instance);

    jbyteArray arr = env->NewByteArray(secretKey->size());
    env->SetByteArrayRegion(arr, 0, secretKey->size(), (jbyte*)secretKey->data());

    return arr;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ripple_ripplelibpp_SecretKey_size(JNIEnv *env, jobject instance) {

    ripple::SecretKey *secretKey = getPointer<ripple::SecretKey>(env, instance);

    return (jint) secretKey->size();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_SecretKey_delete(JNIEnv *env, jobject instance) {

    ripple::SecretKey *secretKey = getPointer<ripple::SecretKey>(env, instance);
    setPointer<ripple::SecretKey>(env, instance, 0);

    delete secretKey;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ripple_ripplelibpp_SecretKey_toBase58(JNIEnv *env, jclass type, jint tokenType,
                                               jobject sk) {

    ripple::SecretKey *secretKey = getPointer<ripple::SecretKey>(env, sk);
    std::string returnValue = ripple::toBase58((ripple::TokenType) tokenType, *secretKey);


    return env->NewStringUTF(returnValue.c_str());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_SecretKey_parseBase58(JNIEnv *env, jclass type, jint tokenType,
                                                  jstring s_) {
    const char *s = env->GetStringUTFChars(s_, 0);
    std::string strValue(s);

    auto secretKey = ripple::parseBase58<ripple::SecretKey>((ripple::TokenType) tokenType, strValue);
    ripple::SecretKey *wSecretKey = new ripple::SecretKey(secretKey.get());

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/SecretKey");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wSecretKey));

    env->ReleaseStringUTFChars(s_, s);

    return obj;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_SecretKey_generateKeyPair(JNIEnv *env, jclass type, jint keyType,
                                                        jobject seed) {

    ripple::KeyType kType;

    switch (keyType) {
        case 0:
            kType = ripple::KeyType::secp256k1;
            break;
        case 1:
            kType = ripple::KeyType::ed25519;
            break;
        default:
            kType = ripple::KeyType::invalid;
    }

    ripple::Seed *sk = getPointer<ripple::Seed>(env, seed);
    auto const keypair = ripple::generateKeyPair(kType, *sk);

    ripple::PublicKey *wPublicKey = new ripple::PublicKey(keypair.first);
    ripple::SecretKey *wSecretKey = new ripple::SecretKey(keypair.second);

    jclass clazzPublicKey = env->FindClass("com/ripple/ripplelibpp/PublicKey");
    jmethodID constructorPublicKey = env->GetMethodID(clazzPublicKey, "<init>", "(J)V");
    jobject objPublicKey = env->NewObject(clazzPublicKey, constructorPublicKey, reinterpret_cast<jlong>(wPublicKey));

    jclass clazzSecretKey = env->FindClass("com/ripple/ripplelibpp/SecretKey");
    jmethodID constructorSecretKey = env->GetMethodID(clazzSecretKey, "<init>", "(J)V");
    jobject objSecretKey = env->NewObject(clazzSecretKey, constructorSecretKey, reinterpret_cast<jlong>(wSecretKey));

    jclass clazzPair = env->FindClass("com/ripple/ripplelibpp/Pair");
    jmethodID constructorPair = env->GetMethodID(clazzPair, "<init>", "()V");
    jmethodID setFirst = env->GetMethodID(clazzPair, "setFirst", "(Ljava/lang/Object;)V");
    jmethodID setSecond = env->GetMethodID(clazzPair, "setSecond", "(Ljava/lang/Object;)V");
    jobject keyPairObject = env->NewObject(clazzPair, constructorPair);
    env->CallVoidMethod(keyPairObject, setFirst, objPublicKey);
    env->CallVoidMethod(keyPairObject, setSecond, objSecretKey);

    return keyPairObject;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_SecretKey_sign(JNIEnv *env, jclass type, jobject pk, jobject sk,
                                           jobject m) {

    ripple::SecretKey *secretKey = getPointer<ripple::SecretKey>(env, sk);
    ripple::PublicKey *publicKey = getPointer<ripple::PublicKey>(env, pk);
    ripple::Slice *slice = getPointer<ripple::Slice>(env, m);

    ripple::Buffer buffer = ripple::sign(*publicKey, *secretKey, *slice);
    ripple::Buffer *wB = new ripple::Buffer(buffer);

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/Buffer");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wB));

    return obj;
}

