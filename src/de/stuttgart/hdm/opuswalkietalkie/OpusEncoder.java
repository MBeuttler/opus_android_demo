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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.R.bool;
import android.util.Log;

/**
 * @author Manuel Beuttler
 *
 *
 *	OpusEncoder class extending FilterOutputStream
 *
 *	Uses native c code via JNI to encode bytes and write them to an output stream.
 *
 *	Native resource: de_stuttgart_hdm_opuswalkietalkie_OpusEncoder.h, e_stuttgart_hdm_opuswalkietalkie_OpusEncoder.c
 */
public class OpusEncoder extends FilterOutputStream
{
	
	private static String	TAG	= "OpusEncoder";

	//Native methods
	private native bool nativeInitEncoder( int samplingRate, int numberOfChannels, int frameSize);
	private native int nativeEncodeBytes( short[] in, byte[] out );
	private native boolean nativeReleaseEncoder();

	//Fields
	private OutputStream	out;

	static
	{
		try
		{
			System.loadLibrary( "OpusEncoder" );
		}
		catch ( Exception e )
		{
			Log.e( TAG, "Could not load Systemlibrary 'OpusEncoder'" );
		}
	}

	/**
	 * Default constructor with 8khz sampling rate, mono channel and 20ms frames
	 * 
	 * @param out OutputStream to write to
	 */
	public OpusEncoder( OutputStream out )
	{
		this( out, 8000, 1, 20 );
	}

	
	/**
	 * The passed frame_size must an opus frame size for the encoder's sampling rate.
	 * For example, at 48kHz the permitted values are 120, 240, 480, 960, 1920, and 2880.
	 * 
	 * @param out OutputStream to write to
	 * @param frequency Configured sampling rate or frequency
	 * @param numberOfChannels Number of channels in the audio signal ( 1 = mono)
	 * @param frameSize Number of samples per frame of input signal
	 */
	public OpusEncoder( OutputStream out, int samplingRate, int numberOfChannels, int frameSize)
	{
		super( out );
		this.out = out;
		
		this.nativeInitEncoder( samplingRate, numberOfChannels, frameSize );
	}
	
	@Override
	public void write( byte[] buffer ) throws IOException
	{
		throw new UnsupportedOperationException( "Method not implemented for byte[]. Please use short[]" );
	}
	
	/**
	 * Encodes a buffered input signal and writes it to the output stream.
	 * 
	 * @param buffer Input signal (interleaved if 2 channels). Length needs to be frame_size*channels*sizeof(opus_int16)
	 * @throws IOException
	 */
	public void write( short[] buffer ) throws IOException
	{
		byte[] encodedBuffer = new byte[buffer.length];
		int lenEncodedBytes = this.nativeEncodeBytes( buffer , encodedBuffer);
		if (lenEncodedBytes > 0)
		{
			this.out.write( encodedBuffer, 0, lenEncodedBytes );
		}
		else
		{
			Log.e( TAG, "Error during Encoding. Error Code: " +  lenEncodedBytes);
			
			throw new IOException( "Error during Encoding. Error Code: " +  lenEncodedBytes );
		}
	}

	@Override
	public void write( byte[] buffer, int offset, int length ) throws IOException
	{
		throw new UnsupportedOperationException( "Method not implemented in inheriting class" );
	}

	@Override
	public void write( int oneByte ) throws IOException
	{
		throw new UnsupportedOperationException( "Method not implemented in inheriting class" );
	}

	@Override
	public void close() throws IOException
	{
		this.out.close();
		this.nativeReleaseEncoder();
	}

}
