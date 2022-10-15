//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <endian.h>

#include <ripple/protocol/PublicKey.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_PublicKey_initialise(JNIEnv *env, jobject instance) {

    ripple::PublicKey *pKey = new ripple::PublicKey();
    setPointer<ripple::PublicKey>(env, instance, pKey);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_PublicKey_initialiseo(JNIEnv *env, jobject instance, jobject other) {

    ripple::PublicKey *otherKey = getPointer<ripple::PublicKey>(env, other);
    ripple::PublicKey *publicKey = new ripple::PublicKey(*otherKey);
    setPointer<ripple::PublicKey>(env, instance, publicKey);
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_ripple_ripplelibpp_PublicKey_data(JNIEnv *env, jobject instance) {

    ripple::PublicKey *publicKey = getPointer<ripple::PublicKey>(env, instance);

    jbyteArray arr = env->NewByteArray(publicKey->size());
    env->SetByteArrayRegion(arr, 0, publicKey->size(), (jbyte*)publicKey->data());

    return arr;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ripple_ripplelibpp_PublicKey_size(JNIEnv *env, jobject instance) {

    ripple::PublicKey *publicKey = getPointer<ripple::PublicKey>(env, instance);

    return (jint) publicKey->size();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_PublicKey_delete(JNIEnv *env, jobject instance) {

    ripple::PublicKey *publicKey = getPointer<ripple::PublicKey>(env, instance);
    setPointer<ripple::PublicKey>(env, instance, 0);

    delete publicKey;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ripple_ripplelibpp_PublicKey_toBase58(JNIEnv *env, jclass type, jint tokenType,
                                               jobject pk) {

    ripple::PublicKey *publicKey = getPointer<ripple::PublicKey>(env, pk);
    std::string returnValue = ripple::toBase58((ripple::TokenType) tokenType, *publicKey);


    return env->NewStringUTF(returnValue.c_str());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_PublicKey_parseBase58(JNIEnv *env, jclass type, jint tokenType,
                                                  jstring s_) {
    const char *s = env->GetStringUTFChars(s_, 0);
    std::string strValue(s);

    auto publicKey = ripple::parseBase58<ripple::PublicKey>((ripple::TokenType) tokenType, strValue);
    ripple::PublicKey *wPublicKey = new ripple::PublicKey(publicKey.get());

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/PublicKey");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wPublicKey));

    env->ReleaseStringUTFChars(s_, s);

    return obj;
}
