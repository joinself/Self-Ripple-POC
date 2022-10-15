//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <android/log.h>
#include <endian.h>

#include <ripple/protocol/AccountID.h>
#include <ripple/protocol/PublicKey.h>

const std::string byteToHexString(const u_char *buf, const int length);

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_AccountID_initialise(JNIEnv *env, jobject instance) {

    ripple::AccountID *aID = new ripple::base_uint<160, ripple::detail::AccountIDTag>();
    setPointer<ripple::AccountID>(env, instance, aID);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_AccountID_delete(JNIEnv *env, jobject instance) {

    ripple::AccountID *aID = getPointer<ripple::AccountID>(env, instance);
    setPointer<ripple::AccountID>(env, instance, 0);

    delete aID;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ripple_ripplelibpp_AccountID_toBase58(JNIEnv *env, jclass type, jobject v) {

    ripple::AccountID *accountID = getPointer<ripple::AccountID>(env, v);
    std::string returnValue = ripple::toBase58(*accountID);

    return env->NewStringUTF(returnValue.c_str());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_AccountID_parseBase58(JNIEnv *env, jclass type, jstring s_) {
    const char *s = env->GetStringUTFChars(s_, 0);
    std::string strValue(s);

    auto accountID = ripple::parseBase58<ripple::AccountID >(strValue);
    ripple::AccountID *wAccountID = new ripple::base_uint<160, ripple::detail::AccountIDTag>(accountID.get());

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/AccountID");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wAccountID));

    env->ReleaseStringUTFChars(s_, s);

    return obj;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_AccountID_parseHex(JNIEnv *env, jclass type, jstring s_) {
    const char *s = env->GetStringUTFChars(s_, 0);
    std::string strValue(s);

    auto accountID = ripple::parseHex<ripple::AccountID>(strValue);
    ripple::AccountID *wAccountID = new ripple::base_uint<160, ripple::detail::AccountIDTag>(accountID.get());

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/AccountID");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wAccountID));

    env->ReleaseStringUTFChars(s_, s);

    return obj;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_AccountID_parseHexOrBase58(JNIEnv *env, jclass type, jstring s_) {
    const char *s = env->GetStringUTFChars(s_, 0);
    std::string strValue(s);

    auto accountID = ripple::parseHexOrBase58<ripple::AccountID>(strValue);
    ripple::AccountID *wAccountID = new ripple::base_uint<160, ripple::detail::AccountIDTag>(accountID.get());

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/AccountID");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wAccountID));

    env->ReleaseStringUTFChars(s_, s);

    return obj;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_AccountID_calcAccountID(JNIEnv *env, jclass type, jobject pk) {

    ripple::PublicKey *publicKey = getPointer<ripple::PublicKey>(env, pk);
    auto accountID = ripple::calcAccountID(*publicKey);
    ripple::AccountID *wAccountID = new ripple::base_uint<160, ripple::detail::AccountIDTag>(accountID);

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/AccountID");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wAccountID));

    return obj;

}
