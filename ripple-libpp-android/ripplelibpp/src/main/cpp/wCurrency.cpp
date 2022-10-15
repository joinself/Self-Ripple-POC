//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <android/log.h>
#include <endian.h>

#include <ripple/protocol/UintTypes.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Currency_initialise(JNIEnv *env, jobject instance) {

    ripple::Currency *currency = new ripple::base_uint<160, ripple::detail::CurrencyTag>();
    setPointer<ripple::Currency >(env, instance, currency);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Currency_delete(JNIEnv *env, jobject instance) {

    ripple::Currency *currency = getPointer<ripple::Currency >(env, instance);
    setPointer<ripple::Currency >(env, instance, 0);

    delete currency;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ripple_ripplelibpp_Currency_toString(JNIEnv *env, jclass type, jobject c) {

    ripple::Currency *currency = getPointer<ripple::Currency >(env, c);
    std::string returnValue = ripple::to_string(*currency);

    return env->NewStringUTF(returnValue.c_str());
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_Currency_toCurrency(JNIEnv *env, jclass type, jstring s_) {

    const char *s = env->GetStringUTFChars(s_, 0);
    std::string strValue(s);

    ripple::Currency currency = ripple::to_currency(strValue);
    ripple::Currency *wCurrency = new ripple::base_uint<160, ripple::detail::CurrencyTag>(currency);

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/Currency");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wCurrency));

    env->ReleaseStringUTFChars(s_, s);

    return obj;
}
