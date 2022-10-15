//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <endian.h>

#include <ripple/basics/Buffer.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Buffer_initialise(JNIEnv *env, jobject instance) {

    ripple::Buffer *buffer = new ripple::Buffer();
    setPointer<ripple::Buffer>(env, instance, buffer);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Buffer_initialiseo(JNIEnv *env, jobject instance, jobject other) {

    ripple::Buffer *otherBuf = getPointer<ripple::Buffer>(env, other);
    ripple::Buffer *buf = new ripple::Buffer(*otherBuf);
    setPointer<ripple::Buffer>(env, instance, buf);
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_ripple_ripplelibpp_Buffer_data(JNIEnv *env, jobject instance) {

    ripple::Buffer *buffer = getPointer<ripple::Buffer>(env, instance);

    jbyteArray arr = env->NewByteArray(buffer->size());
    env->SetByteArrayRegion(arr, 0, buffer->size(), (jbyte*)buffer->data());

    return arr;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ripple_ripplelibpp_Buffer_size(JNIEnv *env, jobject instance) {

    ripple::Buffer *buffer = getPointer<ripple::Buffer>(env, instance);

    return (jint) buffer->size();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Buffer_delete(JNIEnv *env, jobject instance) {

    ripple::Buffer *buffer = getPointer<ripple::Buffer>(env, instance);
    setPointer<ripple::Buffer>(env, instance, 0);

    delete buffer;
}

