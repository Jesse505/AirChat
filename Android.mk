TOP_LOCAL_PATH:= $(call my-dir)
LOCAL_PATH:= $(TOP_LOCAL_PATH)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng samples

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4-zyf adhoc_manager glide-3.7.0

LOCAL_SRC_FILES := $(call all-subdir-java-files) \
 
LOCAL_JNI_SHARED_LIBRARIES := libbv16_jni \
			libg722_jni \
			libgsm_jni  \
			libOSNetworkSystem \
			libspeex_jni

LOCAL_PACKAGE_NAME := AdhocNetwork
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

# ============================================================
##################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := adhoc_manager:lib/adhoc_manager.jar\
	android-support-v4-zyf:lib/android-support-v4.jar\
	glide-3.7.0:lib/glide-3.7.0.jar

include $(BUILD_MULTI_PREBUILT)

# Also build all of the sub-targets under this one: the shared library.
include $(call all-makefiles-under,$(LOCAL_PATH))

