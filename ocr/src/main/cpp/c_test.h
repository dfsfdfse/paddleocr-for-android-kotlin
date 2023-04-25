//
// Created by realcool on 2023/4/25.
//
#pragma once

#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>
#include "common.h"

using namespace cv;

inline void bitmap_mat(JNIEnv *env, jobject &bitmap, Mat &mat) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    Mat &dst = mat;

    try {
        //LOGD("nBitmapToMat");
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        dst.create(info.height, info.width, CV_8UC4);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            //LOGD("nBitmapToMat: RGBA_8888 -> CV_8UC4");
            LOGE("bitmap: width: %d, height: %d", info.width, info.height);
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            //LOGD("nBitmapToMat: RGB_565 -> CV_8UC4");
            LOGE("bitmap: width: %d, height: %d", info.width, info.height);
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //LOGE("nBitmapToMat catched cv::Exception: %s", e.what());
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //LOGE("nBitmapToMat catched unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

inline void mat_bitmap(JNIEnv *env, Mat &mat, jobject &bitmap) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    Mat &src = mat;

    try {
        //LOGD("nMatToBitmap");
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(src.dims == 2 && info.height == (uint32_t) src.rows &&
                  info.width == (uint32_t) src.cols);
        CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (src.type() == CV_8UC1) {
                //LOGD("nMatToBitmap: CV_8UC1 -> RGBA_8888");
                cvtColor(src, tmp, COLOR_GRAY2RGBA);
            } else if (src.type() == CV_8UC3) {
                //LOGD("nMatToBitmap: CV_8UC3 -> RGBA_8888");
                cvtColor(src, tmp, COLOR_RGB2RGBA);
            } else if (src.type() == CV_8UC4) {
                //LOGD("nMatToBitmap: CV_8UC4 -> RGBA_8888");
                src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if (src.type() == CV_8UC1) {
                //LOGD("nMatToBitmap: CV_8UC1 -> RGB_565");
                cvtColor(src, tmp, COLOR_GRAY2BGR565);
            } else if (src.type() == CV_8UC3) {
                //LOGD("nMatToBitmap: CV_8UC3 -> RGB_565");
                cvtColor(src, tmp, COLOR_RGB2BGR565);
            } else if (src.type() == CV_8UC4) {
                //LOGD("nMatToBitmap: CV_8UC4 -> RGB_565");
                cvtColor(src, tmp, COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //LOGE("nMatToBitmap catched cv::Exception: %s", e.what());
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //LOGE("nMatToBitmap catched unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

inline std::vector<int> detect(Mat temp, Mat origin) {
    LOGE("进入了detect方法了");
    clock_t start, end1, end2, end3;
    start = clock();
    std::vector<KeyPoint> key1, key2;
    Mat rt, ro;
    Ptr<SIFT> sift = SIFT::create();
    sift->detectAndCompute(temp, noArray(), key1, rt);
    sift->detectAndCompute(origin, noArray(), key2, ro);
    end1 = clock();
    LOGE("The time was: %f\n", (double) (end1 - start) / CLOCKS_PER_SEC);
    std::vector<std::vector<DMatch>> matches;
    Ptr<DescriptorMatcher> matcher = DescriptorMatcher::create(DescriptorMatcher::FLANNBASED);
    float nndrRatio = 0.75f;
    matcher->knnMatch(rt, ro, matches, 2);
    std::vector<DMatch> good_matches;
    for (int i = 0; i < matches.size(); ++i) {
        if (matches[i][0].distance < nndrRatio * matches[i][1].distance) {
            good_matches.push_back(matches[i][0]);
        }
    }
    std::vector<int> result;
    if (good_matches.size() >= 4) {
        std::vector<Point2f> k1, k2;

        for (auto &match: good_matches) {
            k1.push_back(key1[match.queryIdx].pt);
            k2.push_back(key1[match.trainIdx].pt);
        }
        Mat res = findHomography(k1, k2, RANSAC);
        end2 = clock();
        LOGE("The time was: %f\n", (double) (end2 - start) / CLOCKS_PER_SEC);
        std::vector<Point2f> corners(4);
        std::vector<Point2f> scene_corners(4);

        LOGE("cols: %d,rows: %d, size: %D", temp.cols, temp.rows, temp.size);
        corners[0] = Point2f(0, 0);
        corners[1] = Point2f(temp.cols, 0);
        corners[2] = Point2f(temp.cols, temp.rows);
        corners[3] = Point2f(0, temp.rows);
        perspectiveTransform(corners, scene_corners, res);
        LOGE("cols: %d,rows: %d, size: %D", temp.cols, temp.rows,temp.size);
        for (int i = 0; i < scene_corners.size(); i++) {
            LOGE("x: %f,y: %f", scene_corners[i].x, scene_corners[i].y);
            result.push_back(scene_corners[i].x);
            result.push_back(scene_corners[i].y);
        }
    }
    end3 = clock();
    LOGE("The time was: %f\n", (double) (end3 - start) / CLOCKS_PER_SEC);
    return result;
}

