LOCAL_PATH := $(call my-dir)
$(call import-add-path,$(LOCAL_PATH)/../static_libs)

include $(CLEAR_VARS)

LOCAL_MODULE    := OpusEncoder

LOCAL_SRC_FILES := de_stuttgart_hdm_opuswalkietalkie_OpusEncoder.c

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog 
LOCAL_STATIC_LIBRARIES := opus
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE    := OpusDecoder

LOCAL_SRC_FILES := de_stuttgart_hdm_opuswalkietalkie_OpusDecoder.c

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog 
LOCAL_STATIC_LIBRARIES := opus
include $(BUILD_SHARED_LIBRARY)

$(call import-module,opus) 