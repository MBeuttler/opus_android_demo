/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Manuel Beuttler
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


#include <de_stuttgart_hdm_opuswalkietalkie_OpusEncoder.h>
#include <string.h>
#include <android/log.h>
#include <opus/opus.h>
#include <stdio.h>

char logMsg[255];
OpusEncoder *enc;

//Config
opus_int32 SAMPLING_RATE;
int CHANNELS;
int APPLICATION_TYPE = OPUS_APPLICATION_VOIP;
int FRAME_SIZE;
const int MAX_PAYLOAD_BYTES = 4000;
//--

/*
 * Class:     de_stuttgart_hdm_opuswalkietalkie_OpusEncoder
 * Method:    nativeInitEncoder
 * Signature: (I;I;I)Z
 */
JNIEXPORT jboolean JNICALL Java_de_stuttgart_hdm_opuswalkietalkie_OpusEncoder_nativeInitEncoder (JNIEnv *env, jobject obj, jint samplingRate, jint numberOfChannels, jint frameSize)
{
	FRAME_SIZE = frameSize;
	SAMPLING_RATE = samplingRate;
	CHANNELS = numberOfChannels;

	int error;
	int size;

	size = opus_encoder_get_size(1);
	enc = malloc(size);
	error = opus_encoder_init(enc, SAMPLING_RATE, CHANNELS, APPLICATION_TYPE);

	sprintf(logMsg, "Initialized Encoder with ErrorCode: %d", error);
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);

	return error;
}

/*
 * Class:     de_stuttgart_hdm_opuswalkietalkie_OpusEncoder
 * Method:    nativeEncodeBytes
 * Signature: ([S;[B)I
 */
JNIEXPORT jint JNICALL Java_de_stuttgart_hdm_opuswalkietalkie_OpusEncoder_nativeEncodeBytes (JNIEnv *env, jobject obj, jshortArray in, jbyteArray out)
{
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", "Opus Encoding");

	sprintf(logMsg, "FrameSize: %d - SamplingRate: %d - Channels: %d", FRAME_SIZE, SAMPLING_RATE, CHANNELS);
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);

	jint inputArraySize = (*env)->GetArrayLength(env, in);
	jint outputArraySize = (*env)->GetArrayLength(env, out);

	jshort* audioSignal = (*env)->GetShortArrayElements(env, in, 0);

	sprintf(logMsg, "Length of Input Data: %d", inputArraySize);
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);

	unsigned char *data = (unsigned char*)calloc(MAX_PAYLOAD_BYTES,sizeof(unsigned char));
	int dataArraySize = opus_encode(enc, audioSignal, FRAME_SIZE, data, MAX_PAYLOAD_BYTES);

	sprintf(logMsg, "Length of Encoded Data: %d", dataArraySize);
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);

	if (dataArraySize >=0)
	{
		if (dataArraySize <= outputArraySize)
		{
			(*env)->SetByteArrayRegion(env,out,0,dataArraySize,data);
		}
		else
		{
			sprintf(logMsg, "Output array of size: %d to small for storing encoded data.", outputArraySize);
			__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);

			return -1;
		}
	}

	(*env)->ReleaseShortArrayElements(env,in,audioSignal,JNI_ABORT);

	return dataArraySize;
}

/*
 * Class:     de_stuttgart_hdm_opuswalkietalkie_OpusEncoder
 * Method:    nativeReleaseEncoder
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_de_stuttgart_hdm_opuswalkietalkie_OpusEncoder_nativeReleaseEncoder (JNIEnv *env, jobject obj)
{
	/*
	 * 	opus_encoder_destroy(enc);
	 *
	 *	free(enc);
	 *
	 * If the encoder was created with opus_encoder_init() rather than opus_encoder_create(),
	 * then no action is required aside from potentially freeing the memory that was manually allocated for it
	 * (calling free(enc) for the example above)
	 */


	return 1;
}

void main(){}
