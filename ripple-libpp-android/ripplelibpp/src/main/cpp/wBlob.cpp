//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <android/log.h>
#include <endian.h>

#include <ripple/protocol/AccountID.h>
#include <ripple/protocol/PublicKey.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Blob_initialise(JNIEnv *env, jobject instance) {

    ripple::Blob *blob = new std::vector <unsigned char>();
    setPointer<std::vector <unsigned char>>(env, instance, blob);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Blob_delete(JNIEnv *env, jobject instance) {

    ripple::Blob *blob = getPointer<std::vector <unsigned char>>(env, instance);
    setPointer<std::vector <unsigned char>>(env, instance, 0);

    delete blob;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_ripple_ripplelibpp_Blob_getRawData(JNIEnv *env, jobject instance) {

    jbyteArray arr = nullptr;
//    auto const id = sttx->getTransactionID();
//
//    jbyteArray arr = env->NewByteArray(id.size());
//    env->SetByteArrayRegion(arr, 0, id.size(), (jbyte *) id.data());

    return arr;

}
