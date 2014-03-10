package com.byucs.signdictionary;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.byucs.signdictionary.R;

public class ARActivity extends Activity implements CvCameraViewListener2
{
	private CameraBridgeViewBase mOpenCvCameraView;
	private static String TAG = "ARActivity";
	protected static final Scalar RGB_BLUE = new Scalar(0, 0, 255);
	protected static final Scalar RGB_RED = new Scalar(255, 0, 0);
	protected static final Scalar RGB_GREEN = new Scalar (0, 255, 0);
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_hello_open_cvactivty);
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ar, menu);
		return true;
	}

	
	
	@Override
	public void onPause()
	{
		super.onPause();
		releaseCamera();
	}



	public void onDestroy()
	{
		super.onDestroy();
		releaseCamera();
	}

	
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}

	
	public void onCameraViewStarted(int width, int height)
	{
	}



	public void onCameraViewStopped()
	{
	}



	public Mat onCameraFrame(CvCameraViewFrame inputFrame)
	{
		Mat thumbnail = inputFrame.rgba();
		return augment(thumbnail);
	}

	
	
	protected Mat augment(Mat image)
	{
		//overrider this method to do something cooler
		return image;
	}
	
	
	
	public String arrToString(double[] arr)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++)
		{
			sb.append(arr[i]);
			if (i != arr.length - 1)
				sb.append(", ");
		}
		return sb.toString();
	}
	
	
	protected void releaseCamera()
	{
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}
	
	
	protected BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
	{
		@Override
		public void onManagerConnected(int status)
		{
			switch (status)
			{
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default:
			{
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
	
}
