//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <endian.h>

#include <ripple/basics/Slice.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Slice_initialise(JNIEnv *env, jobject instance) {

    ripple::Slice *slice = new ripple::Slice();
    setPointer<ripple::Slice>(env, instance, slice);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Slice_initialiseo(JNIEnv *env, jobject instance, jobject other) {

    ripple::Slice *otherSlice = getPointer<ripple::Slice>(env, other);
    ripple::Slice *slice = new ripple::Slice(*otherSlice);
    setPointer<ripple::Slice>(env, instance, slice);
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_ripple_ripplelibpp_Slice_data(JNIEnv *env, jobject instance) {

    ripple::Slice *slice = getPointer<ripple::Slice>(env, instance);

    jbyteArray arr = env->NewByteArray(slice->size());
    env->SetByteArrayRegion(arr, 0, slice->size(), (jbyte*)slice->data());

    return arr;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_ripple_ripplelibpp_Slice_size(JNIEnv *env, jobject instance) {

    ripple::Slice *slice = getPointer<ripple::Slice>(env, instance);

    return (jint) slice->size();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Slice_delete(JNIEnv *env, jobject instance) {

    ripple::Slice *slice = getPointer<ripple::Slice>(env, instance);
    setPointer<ripple::Slice>(env, instance, 0);

    delete slice;
}

