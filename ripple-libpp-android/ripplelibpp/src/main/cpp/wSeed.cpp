//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <endian.h>

#include <ripple/protocol/Seed.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Seed_initialiseo(JNIEnv *env, jobject instance, jobject other) {

    ripple::Seed *otherKey = getPointer<ripple::Seed>(env, other);
    ripple::Seed *secretKey = new ripple::Seed(*otherKey);
    setPointer<ripple::Seed>(env, instance, secretKey);
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_ripple_ripplelibpp_Seed_data(JNIEnv *env, jobject instance) {

    ripple::Seed *seed = getPointer<ripple::Seed>(env, instance);

    jbyteArray arr = env->NewByteArray(seed->size());
    env->SetByteArrayRegion(arr, 0, seed->size(), (jbyte*)seed->data());

    return arr;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ripple_ripplelibpp_Seed_size(JNIEnv *env, jobject instance) {

    ripple::Seed *seed = getPointer<ripple::Seed>(env, instance);

    return (jint) seed->size();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Seed_delete(JNIEnv *env, jobject instance) {

    ripple::Seed *secretKey = getPointer<ripple::Seed>(env, instance);
    setPointer<ripple::Seed>(env, instance, 0);

    delete secretKey;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_Seed_generateSeed(JNIEnv *env, jclass type, jstring passPhrase_) {

    const char *s = env->GetStringUTFChars(passPhrase_, 0);
    std::string strValue(s);

    ripple::Seed seed = ripple::generateSeed(strValue);
    ripple::Seed *wSeed = new ripple::Seed(seed);

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/Seed");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "()V");
    jobject obj = env->NewObject(clazz, constructor);
    setPointer<ripple::Seed>(env, obj, wSeed);

    env->ReleaseStringUTFChars(passPhrase_, s);

    return obj;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_Seed_parseBase58(JNIEnv *env, jclass type, jstring s_) {

    const char *s = env->GetStringUTFChars(s_, 0);
    std::string strValue(s);

    auto seed = ripple::parseBase58<ripple::Seed>(strValue);
    ripple::Seed *wSeed = new ripple::Seed(seed.get());

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/Seed");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "()V");
    jobject obj = env->NewObject(clazz, constructor);
    setPointer<ripple::Seed>(env, obj, wSeed);

    env->ReleaseStringUTFChars(s_, s);

    return obj;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_Seed_parseGenericSeed(JNIEnv *env, jclass type, jstring str_) {

    const char *s = env->GetStringUTFChars(str_, 0);
    std::string strValue(s);

    auto seed = ripple::parseGenericSeed(strValue);
    ripple::Seed *wSeed = new ripple::Seed(seed.get());

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/Seed");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "()V");
    jobject obj = env->NewObject(clazz, constructor);
    setPointer<ripple::Seed>(env, obj, wSeed);

    env->ReleaseStringUTFChars(str_, s);

    return obj;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ripple_ripplelibpp_Seed_seedAs1751(JNIEnv *env, jclass type, jobject seed) {

    ripple::Seed *seed1 = getPointer<ripple::Seed>(env, seed);
    std::string returnValue = ripple::seedAs1751(*seed1);

    return env->NewStringUTF(returnValue.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ripple_ripplelibpp_Seed_toBase58(JNIEnv *env, jclass type, jobject seed) {

    ripple::Seed *seed1 = getPointer<ripple::Seed>(env, seed);
    std::string returnValue = ripple::toBase58(*seed1);

    return env->NewStringUTF(returnValue.c_str());
}

