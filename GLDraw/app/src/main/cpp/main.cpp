#include <jni.h>
#include <string>

#include <android/log.h>
#include <math.h>
#include <GLES3/gl3.h>

#define DEBUG 1
#define LOG_TAG "APP_PANYI"
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#if DEBUG
#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#else
#define ALOGV(...)
#endif

static void printGlString(const char* name, GLenum s) {
    const char* v = (const char*)glGetString(s);
    ALOGV("GL %s: %s\n", name, v);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_xinlan_gldraw_nat_NativeRenderJNI_init(JNIEnv *env, jclass type) {
    printGlString("Version", GL_VERSION);
    printGlString("Vendor", GL_VENDOR);
    printGlString("Renderer", GL_RENDERER);
    printGlString("Extensions", GL_EXTENSIONS);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xinlan_gldraw_nat_NativeRenderJNI_resize(JNIEnv *env, jclass type, jint width, jint height) {

}

extern "C"
JNIEXPORT void JNICALL
Java_com_xinlan_gldraw_nat_NativeRenderJNI_update(JNIEnv *env, jclass type) {

}