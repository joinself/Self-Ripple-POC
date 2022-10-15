//
// Created by depry on 15.07.2017.
//

#include <jni.h>

inline jfieldID getPointerField(JNIEnv *env, jobject obj)
{
    jclass c = env->GetObjectClass(obj);
    return env->GetFieldID(c, "nativePtr", "J");
}

inline template <typename T>
T *getPointer(JNIEnv *env, jobject obj)
{
    jlong pointer = env->GetLongField(obj, getPointerField(env, obj));
    return reinterpret_cast<T *>(pointer);
}

inline template <typename T>
void setPointer(JNIEnv *env, jobject obj, T *t)
{
    jlong pointer = reinterpret_cast<jlong >(t);
    env->SetLongField(obj, getPointerField(env, obj), pointer);
}
