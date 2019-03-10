//
// Created by xhp on 2019/3/10.
//
#include <string>
#include "YuvUtils.h"
#include "libyuv.h"

void rotateI420(jbyte *src_i420_data, jint width, jint height, jbyte *dst_i420_data, jint degree);

JNIEXPORT void JNICALL Java_com_test_x264encoderdemo_YuvUtils_yuv420Rotate90
  (JNIEnv *env, jobject obj, jbyteArray src_i420_data, jbyteArray dst_i420_data, jint width, jint height, jint degree) {
    jbyte *src_data = env->GetByteArrayElements(src_i420_data, NULL);
    jbyte *dst_data = env->GetByteArrayElements(dst_i420_data, NULL);
    // 旋转
    rotateI420(src_data, width, height, dst_data, degree);

    env->ReleaseByteArrayElements(src_i420_data, src_data, 0);
    env->ReleaseByteArrayElements(dst_i420_data, dst_data, 0);
}

/*
void nv21ToI420(jbyte *src_nv21_data, jint width, jint height, jbyte *src_i420_data) {
    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *src_nv21_y_data = src_nv21_data;
    jbyte *src_nv21_vu_data = src_nv21_data + src_y_size;

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_y_size + src_u_size;


    libyuv::NV21ToI420((const uint8 *) src_nv21_y_data, width,
                       (const uint8 *) src_nv21_vu_data, width,
                       (uint8 *) src_i420_y_data, width,
                       (uint8 *) src_i420_u_data, width >> 1,
                       (uint8 *) src_i420_v_data, width >> 1,
                       width, height);
}
*/

void rotateI420(jbyte *src_i420_data, jint width, jint height, jbyte *dst_i420_data, jint degree) {
    jint src_i420_y_size = width * height;
    jint src_i420_u_size = (width >> 1) * (height >> 1);

    jbyte *src_i420_y_data = src_i420_data;
    jbyte *src_i420_u_data = src_i420_data + src_i420_y_size;
    jbyte *src_i420_v_data = src_i420_data + src_i420_y_size + src_i420_u_size;

    jbyte *dst_i420_y_data = dst_i420_data;
    jbyte *dst_i420_u_data = dst_i420_data + src_i420_y_size;
    jbyte *dst_i420_v_data = dst_i420_data + src_i420_y_size + src_i420_u_size;

    if (degree == libyuv::kRotate90 || degree == libyuv::kRotate270) {
        libyuv::I420Rotate((const unsigned char *) src_i420_y_data, width,
                           (const unsigned char *) src_i420_u_data, width >> 1,
                           (const unsigned char *) src_i420_v_data, width >> 1,
                           (unsigned char *) dst_i420_y_data, height,
                           (unsigned char *) dst_i420_u_data, height >> 1,
                           (unsigned char *) dst_i420_v_data, height >> 1,
                           width, height,
                           (libyuv::RotationMode) degree);
    }
}

