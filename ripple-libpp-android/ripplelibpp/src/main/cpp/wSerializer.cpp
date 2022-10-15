//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <android/log.h>
#include <endian.h>

#include <ripple/protocol/Serializer.h>
#include <ripple/protocol/STAccount.h>
#include <ripple/protocol/STTx.h>
#include <ripple/protocol/Sign.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Serializer_initialise(JNIEnv *env, jobject instance, jint n) {

    ripple::Serializer *serializer = new ripple::Serializer(n);
    setPointer<ripple::Serializer>(env, instance, serializer);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Serializer_delete(JNIEnv *env, jobject instance) {

    ripple::Serializer *serializer = getPointer<ripple::Serializer>(env, instance);
    setPointer<ripple::Serializer>(env, instance, 0);

    delete serializer;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_ripple_ripplelibpp_Serializer_data(JNIEnv *env, jobject instance) {

    ripple::Serializer *serializer = getPointer<ripple::Serializer>(env, instance);

    jbyteArray arr = env->NewByteArray(serializer->size());
    env->SetByteArrayRegion(arr, 0, serializer->size(), (jbyte*)serializer->data());

    return arr;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ripple_ripplelibpp_Serializer_size(JNIEnv *env, jobject instance) {

    ripple::Serializer *serializer = getPointer<ripple::Serializer>(env, instance);

    return (jint) serializer->size();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_ripple_ripplelibpp_Serializer_add32(JNIEnv *env, jobject instance, jint i) {

    ripple::Serializer *serializer = getPointer<ripple::Serializer>(env, instance);

    return (jlong) serializer->add32(i);

}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_Serializer_getData(JNIEnv *env, jobject instance) {

    ripple::Serializer *serializer = getPointer<ripple::Serializer>(env, instance);
    ripple::Blob blob = serializer->getData();

    ripple::Blob *wBlob = new std::vector <unsigned char>(blob);

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/Blob");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wBlob));

    return obj;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_Serializer_buildMultiSigningData(JNIEnv *env, jclass type, jobject tx,
                                                             jobject signingID) {

    ripple::AccountID *aID = getPointer<ripple::AccountID>(env, signingID);
    ripple::STTx *sttx = getPointer<ripple::STTx>(env, tx);

    ripple::Serializer s = ripple::buildMultiSigningData(*sttx, *aID);
    ripple::Serializer *wS = new ripple::Serializer(s);

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/Serializer");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wS));

    return obj;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_ripple_ripplelibpp_Serializer_slice(JNIEnv *env, jobject instance) {

    ripple::Serializer *serializer = getPointer<ripple::Serializer>(env, instance);
    ripple::Slice *wSlice = new ripple::Slice(serializer->slice());

    jclass clazz = env->FindClass("com/ripple/ripplelibpp/Slice");
    jmethodID constructor = env->GetMethodID(clazz, "<init>", "(J)V");
    jobject obj = env->NewObject(clazz, constructor, reinterpret_cast<jlong>(wSlice));

    return obj;
}



