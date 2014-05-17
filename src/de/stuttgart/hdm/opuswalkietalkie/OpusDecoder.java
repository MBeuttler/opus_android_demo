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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

/**
 * @author Manuel Beuttler
 *
 *
 *	OpusDecoder class extending FilterOutputStream
 *
 *	Uses native c code via JNI to read and decode bytes from an input stream and write them into an array.
 *
 *	Native resource: de_stuttgart_hdm_opuswalkietalkie_OpusDecoder.h, e_stuttgart_hdm_opuswalkietalkie_OpusDecoder.c
 */
public class OpusDecoder extends FilterInputStream
{
	private static String	TAG	= "OpusDecoder";

	//Native methods
	private native boolean nativeInitDecoder( int samplingRate, int numberOfChannels, int frameSize );
	private native int nativeDecodeBytes( byte[] in , short[] out);
	private native boolean nativeReleaseDecoder();

	//Fields
	private InputStream	in;

	static
	{
		try
		{
			System.loadLibrary( "OpusDecoder" );
		}
		catch ( Exception e )
		{
			Log.e( TAG, "Could not load Systemlibrary 'OpusDecoder'" );
		}
	}

	/**
	 * Default constructor with 8khz sampling rate, mono channel and 20ms frames
	 * 
	 * @param out InputStream to write to
	 */
	protected OpusDecoder( InputStream in )
	{
		this(in, 8000, 1, 20);
	}

	/**
	 * The passed frame_size must an opus frame size for the encoder's sampling rate.
	 * For example, at 48kHz the permitted values are 120, 240, 480, 960, 1920, and 2880.
	 * 
	 * @param in InputStream to write to
	 * @param frequency Configured sampling rate or frequency
	 * @param numberOfChannels Number of channels in the audio signal ( 1 = mono)
	 * @param frameSize Number of samples per frame of input signal
	 */
	public OpusDecoder( InputStream in, int frequency, int numberOfChannels, int frameSize )
	{
		super( in );
		this.in = in;

		this.nativeInitDecoder(frequency, numberOfChannels, frameSize);
	}
	@Override
	public int read() throws IOException
	{
		throw new UnsupportedOperationException( "Method not implemented in inheriting class" );
	}

	@Override
	public int read( byte[] buffer ) throws IOException
	{
		throw new UnsupportedOperationException( "Method not implemented in inheriting class for byte[]. Please use short[]." );
	}

	/**
	 * Reads bytes from the InputStream, decodes them an writes them into the given buffer.
	 * 
	 * @param buffer The buffer to write the decoded data to.
	 * @return Amount of bytes read.
	 * @throws IOException
	 */
	public int read( short[] buffer ) throws IOException
	{
		Log.d( TAG, "Buffer Size: " + buffer.length );
		
		byte[] encodedBuffer = new byte[buffer.length / 5];

		int bytesRead = this.in.read( encodedBuffer );
		Log.d( TAG, bytesRead + " bytes read from input stream" );
		if ( bytesRead >= 0 )
		{
			int bytesEncoded = nativeDecodeBytes( encodedBuffer , buffer);
			Log.d( TAG, bytesEncoded + " bytes encoded" );
		}

		return bytesRead;
	}

	@Override
	public int read( byte[] buffer, int offset, int count ) throws IOException
	{
		throw new UnsupportedOperationException( "Method not implemented in inheriting class" );
	}

	@Override
	public void close() throws IOException
	{
		this.in.close();
		this.nativeReleaseDecoder();
	}

}
