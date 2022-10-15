//
// Created by depry on 15.07.2017.
//


#include <jni.h>
#include "pointer.h"
#include <android/log.h>
#include <endian.h>

#include <ripple/protocol/Issue.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Issue_initialise(JNIEnv *env, jobject instance, jobject c, jobject a) {

    ripple::AccountID *accountID = getPointer<ripple::AccountID>(env, a);
    ripple::Currency *currency = getPointer<ripple::Currency>(env, c);

    ripple::Issue *wIssue = new ripple::Issue(*currency,
                                              *accountID);

    setPointer<ripple::Issue>(env, instance, wIssue);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_ripple_ripplelibpp_Issue_delete(JNIEnv *env, jobject instance) {

    ripple::Issue *issue = getPointer<ripple::Issue>(env, instance);
    setPointer<ripple::Issue>(env, instance, 0);

    delete issue;
}


