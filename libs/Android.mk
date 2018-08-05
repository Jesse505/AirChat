

LOCAL_PATH := $(my-dir)
########################
include $(CLEAR_VARS)

LOCAL_MODULE := libbv16_jni.so

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE_CLASS := SHARED_LIBRARIES

LOCAL_SRC_FILES := libbv16_jni.so

include $(BUILD_PREBUILT)

########################

include $(CLEAR_VARS)

LOCAL_MODULE := libg722_jni.so

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE_CLASS := SHARED_LIBRARIES

LOCAL_SRC_FILES := libg722_jni.so

include $(BUILD_PREBUILT)

########################

include $(CLEAR_VARS)

LOCAL_MODULE := libgsm_jni.so

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE_CLASS := SHARED_LIBRARIES

LOCAL_SRC_FILES := libgsm_jni.so

include $(BUILD_PREBUILT)

########################

include $(CLEAR_VARS)

LOCAL_MODULE := libOSNetworkSystem.so

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE_CLASS := SHARED_LIBRARIES

LOCAL_SRC_FILES := libOSNetworkSystem.so

include $(BUILD_PREBUILT)

########################

include $(CLEAR_VARS)

LOCAL_MODULE := libspeex_jni.so

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE_CLASS := SHARED_LIBRARIES

LOCAL_SRC_FILES := libspeex_jni.so

include $(BUILD_PREBUILT)

########################

