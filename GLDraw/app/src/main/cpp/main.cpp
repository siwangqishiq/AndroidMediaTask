#include <jni.h>
#include <string>
#include "common.h"
#include "HelloTrigle.h"

static IExe *exe;

extern "C"
JNIEXPORT void JNICALL
Java_com_xinlan_gldraw_nat_NativeRenderJNI_init(JNIEnv *env, jclass type) {
    printGlString("Version", GL_VERSION);
    printGlString("Vendor", GL_VENDOR);
    printGlString("Renderer", GL_RENDERER);
    printGlString("Extensions", GL_EXTENSIONS);
    ALOGE("init");

    if(exe!= nullptr){
        delete exe;
        exe = nullptr;
    }

    exe = new HelloTrigle();
    exe->init();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xinlan_gldraw_nat_NativeRenderJNI_resize(JNIEnv *env, jclass type, jint width, jint height) {
    ALOGE("resize");
    exe->resize(width ,height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xinlan_gldraw_nat_NativeRenderJNI_update(JNIEnv *env, jclass type) {
    //ALOGE("draw frame");
    exe->update();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xinlan_gldraw_nat_NativeRenderJNI_destory(JNIEnv *env, jclass type) {
    ALOGE("desorty");
    exe->destory();
}
