LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
#LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
#LOCAL_EXPORT_C_INCLUDE_DIRS := $(LOCAL_PATH)/include
LOCAL_MODULE := google_yuv
LOCAL_SRC_FILES := $(LOCAL_PATH)/../jniLibs/$(TARGET_ARCH_ABI)/libyuv.so

include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := YuvUtils.cpp
common_CFLAGS := -Wall -fexceptions
LOCAL_CFLAGS += $(common_CFLAGS)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARY := libyuv

LOCAL_MODULE := yuvutils
include $(BUILD_SHARED_LIBRARY)