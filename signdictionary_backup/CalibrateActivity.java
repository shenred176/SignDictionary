package com.byucs.signdictionary;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class CalibrateActivity extends ARActivity
{
	
	private double minH;
	private double maxH;
	private double minS;
	private double maxS;
	private double minV;
	private double maxV;
	private boolean recordingSkin;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		minS = 255;
		maxS = 0;
		minH = 255;
		maxH = 0;
		minV = 255;
		maxV = 0;
		recordingSkin = false;
	}


	
	@Override
	public boolean onKeyDown(int keycode, KeyEvent event)
	{
		if (keycode == KeyEvent.KEYCODE_DPAD_CENTER)
		{
			recordingSkin = !recordingSkin;
			return true;
		}
		else if(keycode == KeyEvent.KEYCODE_BACK)
		{
			Intent intent = new Intent();
			double[] minHSV = {minH, minS, minV};
			double[] maxHSV = {maxH, maxS, maxV};
			intent.putExtra("minHSV", minHSV);
			intent.putExtra("maxHSV", maxHSV);
			setResult(RESULT_OK, intent);
			finish();
			return true;
		}
		return false;
	}
	
	
	@Override
	protected Mat augment(Mat image)
	{
		Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV);
		Scalar aimBoxColor;
		
		//define a skin range
		double[] hsv = image.get(180, 320);
		if(recordingSkin)
		{
			aimBoxColor = RGB_RED;
			minH = Math.min(minH, hsv[0]);
			maxH = Math.max(maxH, hsv[0]);
			minS = Math.min(minS, hsv[1]);
			maxS = Math.max(maxS, hsv[1]);
			minV = Math.min(minV, hsv[2]);
			maxV = Math.max(maxV, hsv[2]);
			Log.d("NewCalibration", "min(" + minH + ", "+ minS + ", "+ minV + ");  max("+ maxH + ", "+ maxS + ", " + maxV + ")");
		}
		else
		{
			aimBoxColor = RGB_GREEN;
		}


		Imgproc.cvtColor(image, image, Imgproc.COLOR_HSV2BGR);
		Core.rectangle(image, new Point(310, 170), new Point(330, 190), aimBoxColor); //draw aimBox
		return image;
	}	

}
