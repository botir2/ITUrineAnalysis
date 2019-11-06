//
// Created by AHQLab on 2018-11-06. developer Song Seokwoo
//

#include <jni.h>
#include <string>

#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
//#include <opencv2/imgproc/imgproc.hpp>
//#include <android/asset_manager_jni.h>

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_com_ahqlab_hodooopencv_activity_TestActivity_findContourNative(JNIEnv *env, jobject instance,
                                                                    jlong matAddressInput,
                                                                    jlong matAddressResult,
                                                                    jstring imgAddr_) {
    const char *imgAddr = env->GetStringUTFChars(imgAddr_, 0);

    // TODO



//    env->ReleaseStringUTFChars(imgAddr_, imgAddr);
//    Mat &matInput = *(Mat *)matAddressInput;
//    Mat &matResult = *(Mat *)matAddressResult;
//    Mat grayMat, input;
//    string baseDir("/storage/emulated/0/");
//    baseDir.append(imgAddr);
//    matResult = imread(imgAddr, IMREAD_COLOR);
}
