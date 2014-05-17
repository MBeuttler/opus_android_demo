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


package de.stuttgart.hdm.opuswalkietalkie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

/**
 * @author Manuel Beuttler
 * 
 *	Class implements methods to record audio and write them to a file.
 *
 */
public class Recording
{
	private static String		TAG						= "Recording";
	
	//Config
	public boolean isOpusEncodingEnabled 				= true; 	// Toggle opus encoding.
	private String audioFolder					=  "/audioTest";	// Folder in which the audio file is written.
	private String audioFile					= "testAudio.opus";	// Name of the audio file.
	
	//Status flags
	public boolean				isRecording				= false;
	public boolean				isRecordFinished		= false;
	
	//Fields
	public File					recordedFile			= null;

	//Audio config
	private int					frequency				= 8000; // Capture mono data at 8kHz
	private int					channelConfiguration	= AudioFormat.CHANNEL_CONFIGURATION_MONO; // The AudioRecord constructor only allows the deprecated configuration value.
	private int					audioEncoding			= AudioFormat.ENCODING_PCM_16BIT;
	private int					numberOfChannels		= 1;	// Mono
	private int 				frameSize				= 20;	// The frame_size must an opus frame size for the encoder's sampling rate.
																// For example, at 48kHz the permitted values are 120, 240, 480, 960, 1920, and 2880.
	private final int			bufferSize				= frameSize * numberOfChannels * (Short.SIZE / Byte.SIZE);	// Defines the length of the byte array that is passed to the encoder.
																													// (Short.SIZE / Byte.SIZE) equals sizeof(opus_int16) 
	private final int			audioRecBufferSize		= 2 * AudioRecord.getMinBufferSize( frequency, channelConfiguration, audioEncoding ); // Buffer for audio output.
	
	private boolean				shouldStopRecording		= false;

	/**
	 *	Starts audio recording and writes the recorded data into a file.
	 */
	public void recordToFile()
	{
		this.shouldStopRecording = false;
		this.isRecording = true;
		AudioRecord arec = new AudioRecord( MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, audioRecBufferSize );
		arec.startRecording();
		Log.d( TAG, "Start Recording" );
		Log.d( TAG, "Buffer Configuration - Buffersize: " + bufferSize + " Channelconfiguration: " + channelConfiguration );
		
		FileOutputStream	outputStream = this.initOutputStream();
		final OpusEncoder encoder = new OpusEncoder( outputStream , frequency, numberOfChannels, frameSize);
		

		if (this.isOpusEncodingEnabled)
		{
			short[] buffer 		= new short[bufferSize];
			
			while ( !this.shouldStopRecording )
			{
				arec.read( buffer, 0, bufferSize );

				Log.d( TAG, "Calling native code" );
				try
				{
					encoder.write( buffer );
				}
				catch ( IOException e )
				{
					Log.e( TAG, e.getLocalizedMessage(), e );
				}
			}
		}
		else // Record raw audio
		{
			while (!this.shouldStopRecording)
			{
				byte[] directBuffer = new byte[bufferSize];
				
				arec.read( directBuffer, 0, bufferSize );
				try
				{
					Log.d( TAG, "Writing to stream" );
					outputStream.write( directBuffer );
				}
				catch ( IOException e )
				{
					Log.e( TAG, e.getLocalizedMessage(), e );
				}
			}
		}

		arec.stop();

		try
		{
			Log.d( TAG, "flush encoder" );
			encoder.flush();
			Log.d( TAG, "close encoder" );
			encoder.close();
			Log.d(TAG, "flushed and closed");
		}
		catch ( IOException e )
		{
			Log.e( TAG,"Could not close or flush stream", e );
		}
		this.isRecording = false;
		this.isRecordFinished = true;
	}

	/**
	 * Creates the output file and folder.
	 * 
	 * @return The output stream for the created file.
	 */
	private FileOutputStream initOutputStream()
	{
		FileOutputStream fileOutputStream = null;
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File( sdCard.getAbsolutePath() + this.audioFolder );
		dir.mkdirs();
		File audioFile = new File( dir, this.audioFile );

		if ( audioFile.length() > 0 )
		{
			audioFile.delete();
			audioFile = new File( dir, this.audioFile );
		}

		try
		{
			fileOutputStream = new FileOutputStream( audioFile, true );
		}
		catch ( FileNotFoundException e )
		{
			Log.e( TAG, e.getLocalizedMessage(), e );
		}

		this.recordedFile = audioFile;

		return fileOutputStream;
	}

	
	/**
	 * Sets the flag for the recording thread to stop.
	 */
	public void stopRecording()
	{
		this.shouldStopRecording = true;
	}
}
