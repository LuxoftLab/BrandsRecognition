package com.luxsoft.brandsrecognition;

import java.io.FileNotFoundException;
import java.util.LinkedList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvParamGrid;

import com.luxsoft.brandsrecognition.CameraView.CameraListener;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;

public class Controller extends BaseLoaderCallback implements CameraListener {
	
	private static int _rectsSize = 0;
	
	private CameraView cameraView;
	private Algorithm algo;
	private Cache cache;
	private Handler handler;
	private boolean opencvLoaded;
	
	private Size cameraSize;
	private org.opencv.core.Rect area;
	
	private boolean showCanny;
	private boolean blur;
	private int thresholdMin = 100;
	private int thresholdMax = 150;
	
	
	public Controller(Activity activity, CameraView view) {
		super(activity);
		cameraView = view;
		cache = new Cache(activity);
		algo = new Algorithm(this, cache);
		
		handler = new Handler(activity.getMainLooper(), cameraView);
		
		opencvLoaded = false;
	}
	
	public void pause() {
		algo.disable();
		cameraView.disableView();
	}
	
	public void resume() {
		showCanny = false;
		blur = false;
		startThreads();
	}
	
	private void startThreads() {
		if(opencvLoaded) {
			cameraView.enableView();
			new Thread(algo).start();
		}
	}
	
	@Override
	public void onManagerConnected(int status) {
		if(status != LoaderCallbackInterface.SUCCESS) {
			super.onManagerConnected(status);
			return;
		}
		Log.d("lifecycle", "opencv loaded");
		opencvLoaded = true;
		startThreads();
	}
	
	public void toogleShowCanny() {
		showCanny = !showCanny;
	}
	
	public void setBlur(boolean f) {
		blur = f;
	}
	
	public void setThreshold(int min, int max) {
		thresholdMin = min;
		thresholdMax = max;
	}
	
	public void saveImage() {
		algo.saveImage();
	}
	
	public void captureSet() {
		Log.d("capture", "Controller enable");
		algo.captureSet();
	}
	
	public void onAlgorithmResult(String msg) {
		Log.d("algo", msg);
		handler.obtainMessage(CameraView.UPDATE_MSG, msg).sendToTarget();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		Log.d("activity", "start:"+width+"x"+height);
		cameraSize = new Size(width, height);
	}

	@Override
	public void onCameraViewStopped() {
		Log.d("activity", "stop");
	}

	@Override
	public void onAreaChanged(Rect area) {
		float scale = cameraView.getScale();
		int areaWidth = (int) (area.width()/scale);
		int areaHeight = (int) (area.height()/scale);
		this.area = new org.opencv.core.Rect(
				(int)(cameraSize.width-areaWidth)/2,
				(int)(cameraSize.height-areaHeight)/2,
				areaWidth,
				areaHeight
				);
	}
	
	@Override
	public Mat onFrame(Mat input) {
		Mat result = input;
		Mat frame_gray = new Mat();
		Imgproc.cvtColor(input, frame_gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.equalizeHist(frame_gray, frame_gray);
		if(blur) {
			Imgproc.blur(frame_gray, frame_gray, new Size(3, 3));
		}
		
		Imgproc.Canny(frame_gray, frame_gray, thresholdMin, thresholdMax, 3, true);
		
		//Imgproc.adaptiveThreshold(frame_gray, frame_gray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
		
		//Imgproc.adaptiveThreshold(frame_gray, frame_gray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);
		
		if(showCanny) {
			result = frame_gray;
		}
		if(area != null) {
			Log.d("algo", "try put area");
			try {
				algo.putFrame(frame_gray, area);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(rects != null) {
			Log.d("algo", "rects in controller: "+rects.size());
			_rectsSize = rects.size();
			for(org.opencv.core.Rect r : rects) {
				Log.d("luxsoft", "rects: "+r.x+" "+r.y+" "+r.width+" "+r.height);
				Core.rectangle(result, new Point(input.cols()/2-r.x/2, input.rows()/2-r.y/2), new Point(input.cols()/2+(r.x+r.width)/2, input.rows()/2+(r.y+r.height)/2), new Scalar(255, 0, 0));
			}
		}
		return result;
	}
	LinkedList<org.opencv.core.Rect> rects;
	public void setRects(LinkedList<org.opencv.core.Rect> r) {
		rects = r;
	}
	
	public static int getRectsSize () {
		return _rectsSize;
	}
}
