
LOCAL_PATH := $(call my-dir)
#$(warning  LOCAL_PATH: $(LOCAL_PATH))

export PATH:= $(LOCAL_PATH)/tools:../tools:$(LOCAL_PATH)/tools/compile:../tools/compile:$(PATH)

cmd-strip = $(TOOLCHAIN_PREFIX)strip --strip-all -x $1
TXZ_CPP_COMPILE_FLAGS = -Wall -Werror \
                                      -Wno-error=unused-parameter \
                                      -Wno-error=unused-variable \
                                      -Wno-error=unused-but-set-variable \
									  -Wno-error=implicit-function-declaration \
                                      -O3 -fvisibility=hidden\
                                      -std=c++11\
                                      -DNDEBUG

TXZ_C_COMPILE_FLAGS = -Wall -Werror \
                                      -Wno-error=unused-parameter \
                                      -Wno-error=unused-variable \
                                      -Wno-error=unused-but-set-variable \
									  -Wno-error=implicit-function-declaration \
                                      -O3 -fvisibility=hidden\
                                      -DNDEBUG
									  

##################################################################################

UTIL_SRC_DIR_LIST = \
	$(LOCAL_PATH)/libUtil 

UTIL_INC_DIR_LIST = \
	$(LOCAL_PATH)/libUtil \
	$(LOCAL_PATH)/rapidjson/include 

include $(CLEAR_VARS)

#依赖库
LOCAL_LDLIBS := \
	 -llog
	 
#依赖目录
LOCAL_C_INCLUDES += . \
	$(UTIL_INC_DIR_LIST)

#生成CPP源码列表
CPP_SRC_LIST = $(wildcard $(addsuffix /*.cpp,$(UTIL_SRC_DIR_LIST)))

LOCAL_CFLAGS := $(TXZ_CPP_COMPILE_FLAGS) -fexceptions 
LOCAL_MODULE    := TXZUtil
LOCAL_SRC_FILES := $(CPP_SRC_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_SHARED_LIBRARY)


##################################################################################



OPUS_SRC_DIR_LIST = \
	$(LOCAL_PATH)/audios/opus/src \
	$(LOCAL_PATH)/audios/opus/include \
	$(LOCAL_PATH)/audios/opus/celt \
	$(LOCAL_PATH)/audios/opus/silk \
	$(LOCAL_PATH)/audios/opus/silk/float 

OPUS_INC_DIR_LIST = \
	$(OPUS_SRC_DIR_LIST) 
	
OPUS_COMPILE_FLAGS = -Wno-error=maybe-uninitialized -DNONTHREADSAFE_PSEUDOSTACK=1 -DOPUS_BUILD=1


include $(CLEAR_VARS)

#依赖目录
LOCAL_C_INCLUDES += . \
	$(OPUS_INC_DIR_LIST)

#生成CPP源码列表
C_SRC_LIST = $(wildcard $(addsuffix /*.c,$(OPUS_SRC_DIR_LIST)))

LOCAL_CFLAGS := $(TXZ_C_COMPILE_FLAGS) -fexceptions $(OPUS_COMPILE_FLAGS)
LOCAL_MODULE    := opus
LOCAL_SRC_FILES := $(C_SRC_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_STATIC_LIBRARY)


##################################################################################


OGG_SRC_DIR_LIST = \
	$(LOCAL_PATH)/audios/libogg/src \
	$(LOCAL_PATH)/audios/libogg/include

OGG_INC_DIR_LIST = \
	$(OGG_SRC_DIR_LIST) 
	
OGG_COMPILE_FLAGS = 

include $(CLEAR_VARS)

#依赖目录
LOCAL_C_INCLUDES += . \
	$(OGG_INC_DIR_LIST)

#生成CPP源码列表
C_SRC_LIST = $(wildcard $(addsuffix /*.c,$(OGG_SRC_DIR_LIST)))

LOCAL_CFLAGS := $(TXZ_C_COMPILE_FLAGS) -fexceptions $(OGG_COMPILE_FLAGS)
LOCAL_MODULE    := ogg
LOCAL_SRC_FILES := $(C_SRC_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_STATIC_LIBRARY)


##################################################################################


SPEEX_SRC_DIR_LIST = \
	$(LOCAL_PATH)/audios/speex/include \
	$(LOCAL_PATH)/audios/speex/libspeex

SPEEX_INC_DIR_LIST = \
	$(SPEEX_SRC_DIR_LIST) 
	
SPEEX_COMPILE_FLAGS = -DFLOATING_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H

include $(CLEAR_VARS)

#依赖目录
LOCAL_C_INCLUDES += . \
	$(SPEEX_INC_DIR_LIST)

#生成CPP源码列表
C_SRC_LIST = $(wildcard $(addsuffix /*.c,$(SPEEX_SRC_DIR_LIST)))

LOCAL_CFLAGS := $(TXZ_C_COMPILE_FLAGS) -fexceptions $(SPEEX_COMPILE_FLAGS)
LOCAL_MODULE    := speex
LOCAL_SRC_FILES := $(C_SRC_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_STATIC_LIBRARY)


##################################################################################


OPUSTOOL_SRC_DIR_LIST = \
	$(LOCAL_PATH)/audios/opus-tools/src 

OPUSTOOL_INC_DIR_LIST = \
	$(OPUS_INC_DIR_LIST) \
	$(OGG_INC_DIR_LIST) \
	$(SPEEX_INC_DIR_LIST) \
	$(OPUSTOOL_SRC_DIR_LIST) 
	
OPUSTOOL_COMPILE_FLAGS = -DFLOATING_POINT -DSPX_RESAMPLE_EXPORT=""

include $(CLEAR_VARS)

#依赖目录
LOCAL_C_INCLUDES += . \
	$(OPUSTOOL_INC_DIR_LIST)

#生成CPP源码列表
C_SRC_LIST = $(wildcard $(addsuffix /*.c,$(OPUSTOOL_SRC_DIR_LIST)))

LOCAL_CFLAGS := $(TXZ_C_COMPILE_FLAGS) -fexceptions $(OPUSTOOL_COMPILE_FLAGS)
LOCAL_MODULE    := opustool
LOCAL_SRC_FILES := $(C_SRC_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_STATIC_LIBRARY)


##################################################################################


AUDIO_SRC_DIR_LIST = \
	$(LOCAL_PATH)/libAudio 

AUDIO_INC_DIR_LIST = \
	$(OPUS_INC_DIR_LIST) \
	$(OGG_INC_DIR_LIST) \
	$(OPUSTOOL_INC_DIR_LIST) \
	$(LOCAL_PATH)/libAudio

include $(CLEAR_VARS)

#依赖库
LOCAL_LDLIBS := \
	 -llog
	 
LOCAL_STATIC_LIBRARIES := libspeex libopus libogg libopustool

#依赖目录
LOCAL_C_INCLUDES += . \
	$(AUDIO_INC_DIR_LIST)

#生成CPP源码列表
CPP_SRC_LIST = $(wildcard $(addsuffix /*.cpp,$(AUDIO_SRC_DIR_LIST)))

LOCAL_CFLAGS := $(TXZ_CPP_COMPILE_FLAGS) -fexceptions 
LOCAL_MODULE    := TXZAudio
LOCAL_SRC_FILES := $(CPP_SRC_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_SHARED_LIBRARY)

##################################################################################



OPUSTOOLBIN_SRC_DIR_LIST = \
	$(LOCAL_PATH)/audios/opus-tools/src/bin \
	$(LOCAL_PATH)/audios/opus-tools/src/bin/opusdec

OPUSTOOLBIN_INC_DIR_LIST = \
	$(OPUS_INC_DIR_LIST) \
	$(OGG_INC_DIR_LIST) \
	$(SPEEX_INC_DIR_LIST) \
	$(OPUSTOOL_SRC_DIR_LIST) \
	$(OPUSTOOLBIN_SRC_DIR_LIST) \
	
OPUSTOOLBIN_COMPILE_FLAGS = $(OPUSTOOL_COMPILE_FLAGS)

include $(CLEAR_VARS)

#依赖目录
LOCAL_C_INCLUDES += . \
	$(OPUSTOOLBIN_INC_DIR_LIST)

#生成CPP源码列表
C_SRC_LIST = $(wildcard $(addsuffix /*.c,$(OPUSTOOLBIN_SRC_DIR_LIST)))

LOCAL_CFLAGS := $(TXZ_C_COMPILE_FLAGS) -fexceptions $(OPUSTOOLBIN_COMPILE_FLAGS)
LOCAL_MODULE    := opusdec
LOCAL_SRC_FILES := $(C_SRC_LIST:$(LOCAL_PATH)/%=%)

LOCAL_STATIC_LIBRARIES := libspeex libopus libogg libopustool

include $(BUILD_EXECUTABLE) 
