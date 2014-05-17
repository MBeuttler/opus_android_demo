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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

/**
 * @author Manuel Beuttler
 *
 *	Class implements methods to play a audio file.
 */
public class Playback
{
	private static String	TAG						= "Playback";
	
	// status flags
	public boolean				isPlaying				= false;
	public boolean				isStopped				= false;
	private boolean				shoudStopPlaying		= false;
	
	//Audio config
	private boolean			isOpusEncoded			= true; //Toggles opus decoding
	private int				frequency				= 8000;	// Capture mono data at 8kHz
	private int				channelConfiguration	= AudioFormat.CHANNEL_CONFIGURATION_MONO; // The AudioRecord constructor only allows the deprecated configuration value.
	private int				numberOfChannels		= 1;	// Mono
	private int				audioEncoding			= AudioFormat.ENCODING_PCM_16BIT; // raw encoding
	private int 			frameSize				= 20;	// The frame_size must an opus frame size for the encoder's sampling rate.
															// For example, at 48kHz the permitted values are 120, 240, 480, 960, 1920, and 2880.
	private final int		audioRecBufferSize				= 2 * AudioRecord.getMinBufferSize( frequency, channelConfiguration, audioEncoding ); // Buffer for audio output.

	

	/**
	 * Plays a given file.
	 * 
	 * @param file The file to play
	 */
	public void playFile( File file)
	{	
		this.shoudStopPlaying = false;
		this.isPlaying = true;

		FileInputStream	inputStream = this.initInputStreamForFile( file );

		AudioTrack atrack = new AudioTrack( AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, audioRecBufferSize, AudioTrack.MODE_STREAM );
		atrack.play();

		int bytesRead = 0;
		if (this.isOpusEncoded)
		{
			short[] buffer = new short[audioRecBufferSize];

			OpusDecoder decoder = new OpusDecoder( inputStream, frequency, numberOfChannels, frameSize);

			while ( !shoudStopPlaying && bytesRead < file.length() )
			{
				try
				{
					// Read from the InputStream                
					bytesRead += decoder.read( buffer );
					atrack.write( buffer, 0, buffer.length );
					atrack.flush();
				}
				catch ( IOException e )
				{
					Log.e( TAG, e.getLocalizedMessage(),e );
					break;
				}
			}
		}
		else //no encoding
		{
			byte[] directBuffer = new byte[audioRecBufferSize];
			while ( !shoudStopPlaying && bytesRead < file.length() )
			{
				try
				{
					// Read from the InputStream                
					bytesRead += inputStream.read( directBuffer );
					atrack.write( directBuffer, 0, directBuffer.length );
					atrack.flush();
				}
				catch ( IOException e )
				{
					Log.e( TAG, e.getLocalizedMessage(),e );
					break;
				}
			}
		}
		atrack.stop();

		this.isPlaying = false;
	}

	/**
	 * Opens a FileInputStream for a file
	 * 
	 * @param file The file to open
	 * @return InputStream for file
	 */
	private FileInputStream initInputStreamForFile( File file )
	{
		try
		{
			return new FileInputStream( file );
		}
		catch ( FileNotFoundException e )
		{
			Log.e( TAG,e.getLocalizedMessage(), e );
			
			return null;
		}
	}

	/**
	 * Sets the flag to stop the playing thread.
	 */
	public void stopPlaying()
	{
		this.shoudStopPlaying = true;
	}
}
