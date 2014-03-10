package com.byucs.signdictionary;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

public class TrackFingerActivity extends ARActivity
{


	private static final int SKIN_TONE_CALIBRATION = 14;
	private static final double MOVE_TOLERANCE = 18;
	private static final double STILL_TIME_TOLERANCE = 1500; //milliseconds
	private double stillTimer;
	private Point lastFingerTip;
	private long lastFingerTipTime;

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ObjectFinder.minHSV = new Scalar(105, 83, 0);// 154);
		ObjectFinder.maxHSV = new Scalar(118, 148, 255);// 218);
		lastFingerTip = new Point(0, 0);
		lastFingerTipTime = SystemClock.elapsedRealtime();
		stillTimer = 0; 
	}



	@Override
	public boolean onKeyDown(int keycode, KeyEvent event)
	{
		if (keycode == KeyEvent.KEYCODE_DPAD_CENTER)
		{
			Intent intent = new Intent(this, CalibrateActivity.class);
			startActivityForResult(intent, SKIN_TONE_CALIBRATION);
			return true;
		}
		else if(keycode == KeyEvent.KEYCODE_BACK)
		{
			releaseCamera();
			finish();
		}
		return false;
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case SKIN_TONE_CALIBRATION:
			double[] min = data.getDoubleArrayExtra("minHSV");
			ObjectFinder.minHSV = new Scalar(min[0], min[1], 0);
			double[] max = data.getDoubleArrayExtra("maxHSV");
			ObjectFinder.maxHSV = new Scalar(max[0], max[1], 255);
			break;
		}

	}



	@Override
	protected Mat augment(Mat image)
	{
		Point fingerTip = ObjectFinder.findFingerTip(image);
			
		if(fingerTip.x >= 0 & fingerTip.y >= 0)
		{
//			Core.circle(image, fingerTip, 20, RGB_RED);
			Core.line(image, new Point(fingerTip.x - 20, fingerTip.y), new Point(fingerTip.x + 20, fingerTip.y), RGB_RED);
			
			double dist = distance(lastFingerTip, fingerTip);
			Log.d("MoveDistance", "" + dist);
			
			
			if(dist < MOVE_TOLERANCE)
			{
				stillTimer += (SystemClock.elapsedRealtime() - lastFingerTipTime); 
			}
			else
			{
				stillTimer = 0;
			}
		
			Log.d("StillTimer", "" + stillTimer);
			
			lastFingerTip = fingerTip;
			lastFingerTipTime = SystemClock.elapsedRealtime();
			
			if(stillTimer > STILL_TIME_TOLERANCE)
			{
				signLookUp();
			}
		}
		return image;
	}

	
	
	private void signLookUp()
	{
		Intent intent = new Intent(this, ProcessImageActivity.class);
		startActivity(intent);
	}
	
	
	private double distance(Point point1, Point point2)
	{
		double arg1 = Math.pow((point1.x - point2.x), 2);
		double arg2 = Math.pow((point1.y - point2.y), 2);
		return Math.sqrt(arg1 + arg2);
	}
	
}
