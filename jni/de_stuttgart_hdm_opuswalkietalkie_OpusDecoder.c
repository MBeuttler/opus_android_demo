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


#include <de_stuttgart_hdm_opuswalkietalkie_OpusDecoder.h>
#include <string.h>
#include <android/log.h>
#include <opus/opus.h>
#include <stdio.h>

//Fields
char logMsg[255];
OpusDecoder *dec;

//Config
opus_int32 SAMPLING_RATE;
int CHANNELS;
int APPLICATION_TYPE = OPUS_APPLICATION_VOIP;
int FRAME_SIZE;
//--

/*
 * Class:     de_stuttgart_hdm_opuswalkietalkie_OpusDecoder
 * Method:    nativeInitDecoder
 * Signature: (I;I;I)Z
 */
JNIEXPORT jboolean JNICALL Java_de_stuttgart_hdm_opuswalkietalkie_OpusDecoder_nativeInitDecoder (JNIEnv *env, jobject obj, jint samplingRate, jint numberOfChannels, jint frameSize )
{
	FRAME_SIZE = frameSize;
	SAMPLING_RATE = samplingRate;
	CHANNELS = numberOfChannels;

	int size;
	int error;

	size = opus_decoder_get_size(CHANNELS);
	dec = malloc(size);
	error = opus_decoder_init(dec, SAMPLING_RATE, CHANNELS);

	sprintf(logMsg, "Initialized Decoder with ErrorCode: %d", error);
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);

	return error;
}

/*
 * Class:     de_stuttgart_hdm_opuswalkietalkie_OpusDecoder
 * Method:    nativeDecodeBytes
 * Signature: ([B;[S)I
 */
JNIEXPORT jint JNICALL Java_de_stuttgart_hdm_opuswalkietalkie_OpusDecoder_nativeDecodeBytes (JNIEnv *env, jobject obj, jbyteArray in, jshortArray out)
{
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", "Opus decoding");

	sprintf(logMsg, "FrameSize: %d - SamplingRate: %d - Channels: %d", FRAME_SIZE, SAMPLING_RATE, CHANNELS);
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);

	jint inputArraySize = (*env)->GetArrayLength(env, in);
	jint outputArraySize = (*env)->GetArrayLength(env, out);

	sprintf(logMsg, "Length of Input Array: %d", inputArraySize);
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);
	sprintf(logMsg, "Length of Output Array: %d", outputArraySize);
		__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);

	jbyte* encodedData = (*env)->GetByteArrayElements(env, in, 0);
	opus_int16 *data = (opus_int16*)calloc(outputArraySize,sizeof(opus_int16));
	int decodedDataArraySize = opus_decode(dec, encodedData, inputArraySize, data, FRAME_SIZE, 0);

	sprintf(logMsg, "Length of Decoded Data: %d", decodedDataArraySize);
	__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);

	if (decodedDataArraySize >=0)
	{
		if (decodedDataArraySize <= outputArraySize)
		{
			(*env)->SetShortArrayRegion(env,out,0,decodedDataArraySize,data);
		}
		else
		{
			sprintf(logMsg, "Output array of size: %d to small for storing encoded data.", outputArraySize);
			__android_log_write(ANDROID_LOG_DEBUG, "Native Code:", logMsg);

			return -1;
		}
	}

	(*env)->ReleaseByteArrayElements(env,in,encodedData,JNI_ABORT);

	return decodedDataArraySize;
}

/*
 * Class:     de_stuttgart_hdm_opuswalkietalkie_OpusDecoder
 * Method:    nativeReleaseDecoder
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_de_stuttgart_hdm_opuswalkietalkie_OpusDecoder_nativeReleaseDecoder (JNIEnv *env, jobject obj)
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
