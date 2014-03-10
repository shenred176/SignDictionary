package com.byucs.signdictionary;

import java.io.File;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.ImageView;

import com.google.android.glass.media.CameraManager;

public class ProcessImageActivity extends ARActivity
{
	private static final int TAKE_PICTURE_REQUEST = 77;
	private boolean saveTimerStarted;
	private long saveStart;
	private String picturePath;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		saveTimerStarted = false;
		setContentView(R.layout.activity_sign_look_up);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PICTURE_REQUEST);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_look_up, menu);
		return true;
	}



	@Override
	public boolean onKeyDown(int keycode, KeyEvent event)
	{
		if (keycode == KeyEvent.KEYCODE_BACK) 
		{
			finish();
		}
		return false;
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK)
		{
			String picturePath = data.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
			Log.d("PictureSaved", picturePath);
			setContentView(R.layout.loading);
			waitForPicture(picturePath);
		}

		super.onActivityResult(requestCode, resultCode, data);

	}


	
	
	private void processPicture(String picturePath)
	{
		Log.d("Timer", "Save Time: " + (SystemClock.elapsedRealtime() - saveStart) );
		this.picturePath = picturePath;
		new OCRTask().execute(this);
	}
	
	
//	private void withoutAsync()
//	{
//		Mat image = Highgui.imread(picturePath);
//		String word = ObjectFinder.getWord(image);
//
//		Intent intent = new Intent(this, DisplayDefinitionActivity.class);
//		intent.putExtra("Word", word);
//		startActivity(intent);
//		finish();
//	}
	
	
	private void debugImage(Mat image, String picturePath)
	{
//		int	extensionIndex  = picturePath.indexOf(".jpg");
//		
//		String savePath = picturePath.substring(0, extensionIndex) + "DEBUG" + picturePath.substring(extensionIndex, picturePath.length()) ;				
//		boolean saveSuccesful = Highgui.imwrite(savePath, image);
//		Log.d("PictureSaved", savePath + " - " + saveSuccesful );
//		Log.d("PictureSaved", Environment.getExternalStorageState() );
		
		Mat debugImage = new Mat();
		Imgproc.resize(image, debugImage, new Size(640, 360));
		Imgproc.cvtColor(debugImage, debugImage, Imgproc.COLOR_BGR2RGB);
		setContentView(R.layout.activity_sign_look_up);
		ImageView view = (ImageView) findViewById(R.id.debugImage);
		
		Bitmap bmp = ObjectFinder.getBitmap(debugImage);
		if(bmp != null) view.setImageBitmap(bmp);
	}
	
	


	private void waitForPicture(final String picturePath)
	{
		final File pictureFile = new File(picturePath);
		
		if(!saveTimerStarted)
		{
			saveTimerStarted = true;
			saveStart = SystemClock.elapsedRealtime();
		}
		
		if (pictureFile.exists())
		{
			processPicture(picturePath);
		}
		else
		{
			final File parentDirectory = pictureFile.getParentFile();
			FileObserver observer = new FileObserver(parentDirectory.getPath())
			{
				private boolean isFileWritten;



				@Override
				public void onEvent(int event, String path)
				{
					if (!isFileWritten)
					{
						File affectedFile = new File(parentDirectory, path);
						isFileWritten = (event == FileObserver.CLOSE_WRITE && affectedFile.equals(pictureFile));

						if (isFileWritten)
						{
							stopWatching();
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									waitForPicture(picturePath);
								}
							});
						}
					}
				}
			};
			observer.startWatching();
		}
	}

	
	private void ocrFinished(String word)
	{
		Intent intent = new Intent(this, DisplayDefinitionActivity.class);
		intent.putExtra("Word", word);
		startActivity(intent);
		finish();
	}
	
	
	
	private class OCRTask extends AsyncTask<ProcessImageActivity, Void, String>
	{
		ProcessImageActivity caller;
		
		@Override
		protected String doInBackground(ProcessImageActivity... callers)
		{
			caller = callers[0];
			String picturePath = caller.picturePath;
			Mat image = Highgui.imread(picturePath);
			String word = ObjectFinder.getWord(image);
			return word;
		}

		
		@Override
		protected void onPostExecute(String word) 
		{
			caller.ocrFinished(word);
		}
	}

}
