//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <endian.h>

#include <ripple/protocol/STAmount.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_STAmount_initialise(JNIEnv *env, jobject instance, jlong mantissa,
                                                jboolean negative) {

    ripple::STAmount *amount = new ripple::STAmount(mantissa, negative);
    setPointer<ripple::STAmount>(env, instance, amount);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_STAmount_initialise_1issue(JNIEnv *env, jobject instance, jobject issue,
                                                       jlong mantissa, jlong exponent) {

    ripple::Issue *issueIn = getPointer<ripple::Issue>(env, issue);
    ripple::STAmount *amount = new ripple::STAmount(*issueIn, mantissa, exponent);
    setPointer<ripple::STAmount>(env, instance, amount);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_STAmount_delete(JNIEnv *env, jobject instance) {

    ripple::STAmount *amount = getPointer<ripple::STAmount>(env, instance);
    setPointer<ripple::STAmount>(env, instance, 0);

    delete amount;
}
