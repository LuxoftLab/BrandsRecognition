package com.luxsoft.brandsrecognition;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.luxsoft.brandsrecognition.CameraView.CameraListener;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Controller extends BaseLoaderCallback implements CameraListener {
	
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
	private Main m;
	
	public Controller(Activity activity, CameraView view) {
		super(activity);
		m = (Main)activity;
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
	
	public void onAlgorithmResult(String msg) {
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
		if(showCanny) {
			result = frame_gray;
		}
		if(area != null) {
			algo.putFrame(frame_gray, area);
		}
		return result;
	}
}
