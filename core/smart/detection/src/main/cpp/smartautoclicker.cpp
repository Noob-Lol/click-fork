/*
 * Copyright (C) 2025 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <android/log.h>
#include <android/bitmap.h>
#include <jni.h>
#include <opencv2/imgproc/imgproc.hpp>
#include <string>

#include "jni/jni.hpp"
#include "detector/detector.hpp"

using namespace smartautoclicker;

extern "C" {
    JNIEXPORT jlong JNICALL Java_com_nooblol_smartnoob_core_detection_NativeDetector_newDetector(
            JNIEnv *env,
            jobject self
    ) {
        return reinterpret_cast<jlong>(new Detector());
    }

    JNIEXPORT void JNICALL Java_com_nooblol_smartnoob_core_detection_NativeDetector_updateScreenMetrics(
            JNIEnv *env,
            jobject self,
            jstring metricsTag,
            jobject screenBitmap,
            jdouble detectionQuality
    ) {
        const char *tag = env->GetStringUTFChars(metricsTag, nullptr);

        getDetectorFromJavaRef(env, self)->setScreenMetrics(
                loadMatFromRGBA8888Bitmap(env, screenBitmap),
                detectionQuality,
                tag);

        env->ReleaseStringUTFChars(metricsTag, tag);
    }

    JNIEXPORT void JNICALL Java_com_nooblol_smartnoob_core_detection_NativeDetector_setScreenImage(
            JNIEnv *env,
            jobject self,
            jobject screenBitmap
    ) {
        getDetectorFromJavaRef(env, self)->setScreenImage(
                loadMatFromRGBA8888Bitmap(env, screenBitmap));
    }

    JNIEXPORT void JNICALL Java_com_nooblol_smartnoob_core_detection_NativeDetector_detect(
            JNIEnv *env,
            jobject self,
            jobject conditionBitmap,
            jint threshold,
            jobject result
    ) {
        setDetectionResult(env, result, getDetectorFromJavaRef(env, self)->detectCondition(
                loadMatFromRGBA8888Bitmap(env, conditionBitmap), threshold));
    }

    JNIEXPORT void JNICALL Java_com_nooblol_smartnoob_core_detection_NativeDetector_detectAt(
            JNIEnv *env,
            jobject self,
            jobject conditionBitmap,
            jint x,
            jint y,
            jint width,
            jint height,
            jint threshold,
            jobject result
    ) {
        setDetectionResult(env, result, getDetectorFromJavaRef(env, self)->detectCondition(
                loadMatFromRGBA8888Bitmap(env, conditionBitmap), x, y, width, height, threshold));
    }

    JNIEXPORT void JNICALL Java_com_nooblol_smartnoob_core_detection_NativeDetector_deleteDetector(
            JNIEnv *env,
            jobject self
    ) {
        delete getDetectorFromJavaRef(env, self);
    }
}
