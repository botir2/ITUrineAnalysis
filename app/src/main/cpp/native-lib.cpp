//
// Created by AHQLab on 2018-11-06. developer Song Seokwoo
//

#include <jni.h>
#include <string>

#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <android/log.h>

#define  LOG_TAG    "your-log-tag"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace cv;
using namespace std;

extern "C" JNIEXPORT jstring

JNICALL
Java_com_ahqlab_hodooopencv_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_ahqlab_hodooopencv_MainActivity_convertRGBtoGray(JNIEnv *env, jobject instance,
                                                          jlong matAddrInput, jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;

    cvtColor(matInput, matResult, CV_RGBA2GRAY);
}extern "C"
JNIEXPORT void JNICALL
Java_com_ahqlab_hodooopencv_MainActivity_convertBinary(JNIEnv *env, jobject instance,
                                                       jlong matGray) {

    // TODO
//    adaptiveThreshold(matGray, matGray, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 40);
//    threshold(matGray, matGray, 125, 255, THRESH_BINARY_INV | THRESH_OTSU);
}extern "C"
JNIEXPORT void JNICALL
Java_com_ahqlab_hodooopencv_activity_CameraActivity_convertBinary(JNIEnv *env, jobject instance,
                                                                  jlong matGray) {

    // TODO
}extern "C"
JNIEXPORT void JNICALL
Java_com_ahqlab_hodooopencv_activity_CameraActivity_convertRGBtoGray(JNIEnv *env, jobject instance,
                                                                     jlong matAddrInput,
                                                                     jlong matAddrResult) {

    // TODO
    Java_com_ahqlab_hodooopencv_MainActivity_convertRGBtoGray(env, instance, matAddrInput, matAddrResult);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_ahqlab_hodooopencv_activity_TestActivity_contourProcessing(JNIEnv *env, jobject instance,
                                                                    jlong matResultAddr,
                                                                    jstring fileName_) {
    const char *fileName = env->GetStringUTFChars(fileName_, 0);

    // TODO

    env->ReleaseStringUTFChars(fileName_, fileName);

    Mat &matResult = *(Mat *)matResultAddr;
    Mat matInput, grayMat, contoursMat, wrappingMat;
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    Point2f inputQuad[4];
    Point2f outputQuad[4];

    string baseDir("/storage/emulated/0/");
    baseDir.append(fileName);
    const char *pathDir = baseDir.c_str();

    matInput = imread(pathDir, IMREAD_COLOR);
    cvtColor(matInput, matInput, CV_BGR2RGB); //색상 반전
    grayMat = matInput.clone();
    cvtColor(matInput, grayMat, CV_RGB2GRAY);

    GaussianBlur(grayMat, grayMat, Size(11, 11), 2);
    contoursMat = matInput.clone();
    Canny(grayMat, contoursMat, 10, 100);
    dilate(contoursMat, contoursMat, Mat(), Point(-1, -1), 1); //노이즈 제거
//    return;
//    threshold(grayMat, contoursMat, 90, 255, THRESH_BINARY_INV | THRESH_OTSU);//이진화 처리

    findContours(contoursMat, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE); //윤곽선 검출

    vector<Point2f> approx;
    LOGE("contours.size() : %d", contours.size());
    for(int i = 0; i < contours.size(); i++) {
        approxPolyDP(Mat(contours[i]), approx, 0.1 * arcLength(Mat(contours[i]), true), true);
        Rect rect = boundingRect(Mat(contours[i]));
        LOGE("rect.width : %d", rect.width);

        if (  rect.width > 180 ) {
//        if ( fabs(contourArea(Mat(approx))) > 300 ) {
            int size = approx.size();
            matResult = matInput.clone();

//                Point point1 = approxCurve.toArray()[0]; //오른쪽위
//                Point point2 = approxCurve.toArray()[1]; //왼쪽위
//                Point point3 = approxCurve.toArray()[2]; //왼쪽아래
//                Point point4 = approxCurve.toArray()[3]; //오른쪽아래
//                mInputMat.put(0, 0, point2.x, point2.y, point1.x, point1.y, point4.x, point4.y, point3.x, point3.y);
//                mOutputMat.put(0, 0, 0, 0, mImage.cols() - 1, 0, mImage.cols() - 1, mImage.rows() - 1, 0, mImage.rows() - 1);
//            for ( int j = 0; j < approx.size(); j++ ) {
//                circle(matResult, approx[i], 50, Scalar(255, 0, 0), -1);
//                putText(matResult, std::to_string(j + 1), approx[i], FONT_HERSHEY_SCRIPT_SIMPLEX, 4, Scalar(255, 0, 0), 3);
//            }
//            return;

            inputQuad[0] = Point2f(approx[1].x, approx[1].y);
            inputQuad[1] = Point2f(approx[0].x, approx[0].y);
            inputQuad[2] = Point2f(approx[3].x, approx[3].y);
            inputQuad[3] = Point2f(approx[2].x, approx[2].y);

            outputQuad[0] = Point2f(0, 0);
            outputQuad[1] = Point2f(matInput.cols - 1, 0);
            outputQuad[2] = Point2f(matInput.cols - 1 , matInput.rows - 1);
            outputQuad[3] = Point2f(0, matInput.rows - 1);
            wrappingMat = getPerspectiveTransform(inputQuad, outputQuad);
            warpPerspective(matInput, matResult, wrappingMat, Size(1500, 1000));
//            drawContours(matResult, contours, i, Scalar(0, 0, 255), -1);
        }
    }
//    if ( countNonZero(matResult ) < 1) matResult = matInput.clone();

//    cvtColor(matInput, matResult, CV_RGBA2GRAY);
}