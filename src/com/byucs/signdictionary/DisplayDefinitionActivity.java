package com.byucs.signdictionary;


import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class DisplayDefinitionActivity extends Activity implements Callback, OnCompletionListener
{
	MediaPlayer mediaPlayer;
	SurfaceHolder holder;
	LayoutInflater wordInflater; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		String word = getIntent().getStringExtra("Word");
		String videoPath = "/mnt/sdcard/Movies/SignDefinitions/" + word + ".mp4";
		Uri videoUri = Uri.parse(videoPath);
		
		mediaPlayer = MediaPlayer.create(this, videoUri);
		
		if(mediaPlayer != null)
		{
			setContentView(R.layout.video_layout);
			SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
			holder = mSurfaceView.getHolder();
			holder.addCallback(this);
			mediaPlayer.setOnCompletionListener(this);
			
			wordInflater = LayoutInflater.from(getBaseContext());
			View viewWord = wordInflater.inflate(R.layout.word_display, null);
			LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			this.addContentView(viewWord, layoutParamsControl);	
			TextView wordDisplay = (TextView) findViewById(R.id.word_display_text);
			wordDisplay.setText(" \"" + word + "\" ");
		}
		else
		{
			setContentView(R.layout.definition_not_found);
			TextView errorMessage = (TextView) findViewById(R.id.definition_not_found_text);
			errorMessage.setText("A defintion for " + word + " was not found");
		}
		

	}

	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
	{
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0)
	{
		mediaPlayer.setDisplay(holder);
		mediaPlayer.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0)
	{
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onCompletion(MediaPlayer arg0)
	{
		mediaPlayer.release();
		mediaPlayer = null;
		finish();
	}

}
