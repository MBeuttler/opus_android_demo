/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2012 Manuel Beuttler
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.os.Environment;
import android.test.AndroidTestCase;

/**
 * @author manuelbeuttler
 *
 *
 */
public class OpusTest extends AndroidTestCase
{
	//Audio config
	private int					frequency				= 8000; // Capture mono data at 8kHz
	private int					channelConfiguration	= AudioFormat.CHANNEL_CONFIGURATION_MONO; // The AudioRecord constructor only allows the deprecated configuration value.
	private int					audioEncoding			= AudioFormat.ENCODING_PCM_16BIT;
	private int					numberOfChannels		= 1;	// Mono
	private int 				frameSize				= 20;	// The frame_size must an opus frame size for the encoder's sampling rate.
																	// For example, at 48kHz the permitted values are 120, 240, 480, 960, 1920, and 2880.
	private final int			bufferSize				= frameSize * numberOfChannels * (Short.SIZE / Byte.SIZE);	// Defines the length of the byte array that is passed to the encoder.
																														// (Short.SIZE / Byte.SIZE) equals sizeof(opus_int16) 
	
	public void testEncode() throws Exception
	{
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File( sdCard.getAbsolutePath() + "/audioTest" );
		dir.mkdirs();
		File audioFile = new File( dir, "test1.raw" );
		final File outputFile = new File (dir, "test1.opus");
		
		
		try
		{
			FileInputStream in = new FileInputStream( audioFile );
			FileOutputStream out = new FileOutputStream( outputFile );
			
			BufferedInputStream bIn = new BufferedInputStream( in );			
			DataInputStream dIn = new DataInputStream( bIn );
			
			final OpusEncoder encoder = new OpusEncoder( out, frequency, numberOfChannels, frameSize ); 
			
			short[] buffer = new short[bufferSize];
			
			while (dIn.available() > 1)
			{
				for ( int i = 0; i < buffer.length; i++ )
				{
					if (dIn.available() > 1)
					{
						buffer[i] = dIn.readShort();	
					}
					else
					{
						break;
					}
				}
				encoder.write( buffer );
			}
			
			encoder.flush();
			encoder.close();
			
			final Playback playback = new Playback();
			new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					playback.playFile( outputFile );
				}
			} ).start();
		}
		catch ( FileNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
