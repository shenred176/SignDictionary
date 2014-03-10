package com.byucs.signdictionary;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class ObjectFinder extends ARActivity
{
	public static Scalar minHSV;
	public static Scalar maxHSV;
	private static final int MIN_HAND_SIZE = 200;
	private static Mat imageBin;
	
	
	public static Point findFingerTip(Mat image)
	{
		return findFingerTip(image, true);
		
	}
	
	
	public static Point findFingerTip(Mat image, boolean thumbnail)
	{
		long fingerTipStart  = SystemClock.elapsedRealtime();
		Point fingerTip;
		Mat imageHSV = new Mat();
		if(thumbnail)
		{
			Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2HSV);
		}
		else
		{
			Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_RGB2HSV);	
		}
		

		imageBin = new Mat();
		Core.inRange(imageHSV, minHSV, maxHSV, imageBin);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		
		Imgproc.findContours(imageBin, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
		
		//find the biggest contour 
		int max = 0;
		int maxInd = 0;
		for (int i = 0; i < contours.size(); i++)
		{
			int newLength = contours.get(i).toArray().length;
			if (max < newLength)
			{
				max = newLength;
				maxInd = i;
			}
		}
		Log.d("SizeOfHand", "" + max);
		
		
		//if the biggest contour is large enough to be a hand/finger, find the top of it and return it
		if(max > MIN_HAND_SIZE)
		{
			Point[] handCoords = contours.get(maxInd).toArray(); 
			
			int topIndex = 0;
			double top = handCoords[topIndex].y;
			
			for(int i=0; i < handCoords.length; i++ )
			{
				if(handCoords[i].y < top)
				{
					topIndex = i;
					top = handCoords[topIndex].y;
				}
			}
			fingerTip = handCoords[topIndex];
		}
		else
		{
			fingerTip = new Point(-1,-1);
		}
		
		if(!thumbnail)
		{
			Log.d("FingerTip", fingerTip.toString());
			Log.d("Timer", "fingerTip: " + (SystemClock.elapsedRealtime() - fingerTipStart) );
		}
		return fingerTip;
	}//end findFingerTip
	
	
	
	
	private static Mat cropToWord(Mat image, Point fingerTip)
	{
//		Mat skins = imageBin;
//		Imgproc.blur(skins, skins, new Size(21,21));
//		Imgproc.threshold(skins, skins, 1, 1, Imgproc.THRESH_BINARY_INV);

//		Mat edges = new Mat();		
//		Imgproc.Canny(image, edges, 60, 180);
//		Imgproc.cvtColor(edges, edges, Imgproc.COLOR_GRAY2BGR);
		
		
//		Imgproc.cvtColor(skins, skins, Imgproc.COLOR_GRAY2BGR);
//		Core.circle(skins, fingerTip, 20, RGB_BLUE, -1);
		
		
		
		//default implementation; replace this with something more sophisticated
		int wordWidth = 800;
		int wordHeight = 400;				
		Rect roi = new Rect((int) fingerTip.x - wordWidth/2, (int) fingerTip.y - wordHeight, wordWidth, wordHeight);
		Mat cropped = new Mat(image, roi);
		
		return cropped;
	}
	
	
	//input: cropped image showing a single word
	private static String ocrWord(Mat image)
	{
		
		long ocrStart = SystemClock.elapsedRealtime();
		TessBaseAPI ocr = new TessBaseAPI();
		ocr.init("/mnt/sdcard/DCIM/Camera", "eng");
		ocr.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmopqrstuvwxyz,. ");
		ocr.setImage(getBitmap(image));
		
		String recognizedText = ocr.getUTF8Text();
		ocr.end();
		
		Log.d("Timer", "ocr: " + (SystemClock.elapsedRealtime() - ocrStart) );
		return recognizedText;
	}
	
	
	
	public static String getWord(Mat image)
	{
		Point fingerTip = findFingerTip(image, false);
		Mat croppedWord = cropToWord(image, fingerTip);
		String word = ocrWord(croppedWord);
		Log.d("RecognizedText", word);	
		return word;
	}
	
	
	
	public static Bitmap getBitmap(Mat image)
	{
		try
		{
			Bitmap bmp = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(image, bmp);
			return bmp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	

	
}
