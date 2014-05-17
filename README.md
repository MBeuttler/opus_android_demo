opus_android_demo
=================

This demo application was part of one of my student projects.
The idea was to use the opus audio codec to build a walkie-talkie demo app on android.
I had problems getting the opus encoding work and as there was less and less time to work on the project I finaly gave up.

A short time ago someone got in touch with me referencing my code. He asked me to put it on GitHub as it did help him in some way and it could may be useful for others too.

The project so far does only work in some parts. Unfortunately opus encoding and decoding does not.

What does work:
  - Running the android application
  - Compiling the native sources
  - Calling the native methods
  - Recording and playing none encoded audio

What does not work:
  - Recording and playing using the opus codec
  

Setup instructions as I used them for my workspace and device. You may need to customize them to your setup:

1. Download ndk and opus source:
						https://developer.android.com/tools/sdk/ndk/index.html#Downloads
						http://downloads.xiph.org/releases/opus/opus-1.0.1.tar.gz
2. make standalone toolchain: 
						~/android-ndk-r8b/build/tools/make-standalone-toolchain.sh --platform=android-14 --install-dir=/tmp/my_toolchain
3. define path variables: 
						export PATH=/tmp/my_toolchain/bin/:$PATH
						export CC=arm-linux-androideabi-gcc
						export CXX=arm-linux-androideabi-g++
4. configure opus makefile:
						./configure --host=arm-linux-androideabi --prefix=/tmp/libOpus
5. compile opus sources:
						make
						make install

This should give you the precombiled opus sources under /tmp/libOpus.
Those files are included with the project Android.mk found under '/static_libs/opus' to load the prebuild library:

```
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := opus
LOCAL_SRC_FILES := lib/libopus.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(PREBUILT_STATIC_LIBRARY) 
```

And '/jni' to define the local modules and reference the prebuild libary:

```
LOCAL_PATH := $(call my-dir)
$(call import-add-path,$(LOCAL_PATH)/../static_libs)

include $(CLEAR_VARS)

LOCAL_MODULE    := OpusEncoder

LOCAL_SRC_FILES := de_stuttgart_hdm_opuswalkietalkie_OpusEncoder.c

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog 
LOCAL_STATIC_LIBRARIES := opus
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE    := OpusDecoder

LOCAL_SRC_FILES := de_stuttgart_hdm_opuswalkietalkie_OpusDecoder.c

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog 
LOCAL_STATIC_LIBRARIES := opus
include $(BUILD_SHARED_LIBRARY)

$(call import-module,opus) 
```

I no longer have the time to work on this project, but maybe the code so far helps someone else or even inspires someone to finish the work.


Further sources:

Using the android toolchain as standalone compiler: https://gitorious.org/mingw-android-ndk/mingw-android-ndk/blobs/be60a6a54b4436c4b5e50edfdf63c4b00866ae1e/docs/STANDALONE-TOOLCHAIN.html

Opus Codec: http://www.opus-codec.org

Opus Spec (RFC6716): https://tools.ietf.org/html/rfc6716#sectionL2.1.1

Setting up Automatic NDK Build in Eclipse: http://mobilepearls.com/labs/ndk-builder-in-eclipse/

Android NDK: https://developer.android.com/tools/sdk/ndk/index.html
