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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private static String	TAG						= "Main Activity";

	// View bindings
	private TextView			recordStatus;
	private Button				recordButton;
	private Button				playButton;

	private Playback		playback	= new Playback();
	private Recording		recording	= new Recording();

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		this.recordStatus = ( TextView ) this.findViewById( R.id.tv_record_status );
		this.recordButton = ( Button ) this.findViewById( R.id.b_record_button );
		this.playButton = ( Button ) this.findViewById( R.id.b_play_button );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		getMenuInflater().inflate( R.menu.activity_main, menu );
		return true;
	}

	/**
	 *	Button listener for record button
	 */
	public void onRecord( View view )
	{
		if ( this.recording.isRecording )
		{
			this.stopRecording();
		}
		else
		{
			this.startRecording();
		}
	}

	/**
	 *	Button listener for play button
	 */
	public void onPlay( View view )
	{
		if ( !this.recording.isRecordFinished )
		{
			Log.d( TAG, "No record found" );
			return;
		}

		Log.d( TAG, "Play button pressed" );
		if ( this.playback.isPlaying )
		{
			this.stopPlaying();
		}
		else
		{

			if ( this.recording.isRecording )
			{
				this.stopRecording();
			}

			this.startPlaying();
		}

	}

	/**
	 *	Starts the thread to play a recorded file.
	 */
	private void startPlaying()
	{

		if ( this.recording.isRecordFinished && this.recording.recordedFile != null )
		{
			new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					playback.playFile( recording.recordedFile );
					Log.d( TAG, "Play thread end" );
				}
			} ).start();

		}

	}

	/**
	 *	Stops the playback thread.
	 */
	private void stopPlaying()
	{
		this.playback.stopPlaying();

	}

	/**
	 *	Starts recording and ends the current playback thread if necessary.
	 */
	private void startRecording()
	{
		if ( this.playback.isPlaying )
		{
			this.playback.stopPlaying();
		}

		this.recordButton.setText( "Stop" );
		this.recordStatus.setText( "Recording..." );
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				recording.recordToFile();
				
				Log.d( TAG, "Record thread end" );
			}
		} ).start();
	}

	/**
	 *	Stops recording.
	 */
	private void stopRecording()
	{
		this.recordButton.setText( "Record" );
		this.recordStatus.setText( "Press 'Record' to start recording" );

		this.recording.stopRecording();
	}
}
