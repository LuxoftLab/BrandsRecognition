package com.luxsoft.brandsrecognition;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Controller extends BaseLoaderCallback implements SurfaceHolder.Callback {
	
	private CameraView cameraView;
	private CameraCapture camera;
	private Algorithm algo;
	private Cache cache;
	private ProccesingArea processingArea;
	private Handler handler;
	private boolean opencvLoaded;
	
	private Size cameraSize;
	private Size surfaceSize;
	private org.opencv.core.Rect area;
	
	private boolean showCanny;
	private boolean blur;
	private int thresholdMin = 100;
	private int thresholdMax = 150;
	
	public Controller(Activity activity, SurfaceView surface) {
		super(activity);
		SurfaceHolder holder = surface.getHolder();
		holder.addCallback(this);
		
		cameraView = new CameraView(holder, this);
		camera = new CameraCapture(this);
		cache = new Cache(activity);
		algo = new Algorithm(this, cache);
		processingArea = new ProccesingArea(this);
		
		handler = new Handler(activity.getMainLooper(), cameraView);
		surface.setOnTouchListener(processingArea);
		
		opencvLoaded = false;
		cameraSize = null;
		surfaceSize = null;
		area = null;
	}
	
	public void pause() {
		camera.disable();
		algo.disable();
	}
	
	public void resume() {
		showCanny = false;
		blur = false;
		startThreads();
	}
	
	private void startThreads() {
		if(opencvLoaded) {
			new Thread(algo).start();
			new Thread(camera).start();
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
	
	public void onFrame(Mat frame) {
		Mat frame_gray = new Mat();
		Imgproc.cvtColor(frame, frame_gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.equalizeHist(frame_gray, frame_gray);
		if(blur) {
			Imgproc.blur(frame_gray, frame_gray, new Size(3, 3));
		}
		Imgproc.Canny(frame_gray, frame_gray, thresholdMin, thresholdMax, 3, true);
		if(showCanny) {
			handler.obtainMessage(CameraView.UPDATE_FRAME, frame_gray).sendToTarget();
		} else {
			handler.obtainMessage(CameraView.UPDATE_FRAME, frame).sendToTarget();
		}

		if(area != null) {
			algo.putFrame(frame_gray, area);
		}
	}
	
	public void onAlgorithmResult(String msg) {
		handler.obtainMessage(CameraView.UPDATE_MSG, msg).sendToTarget();
	}
	
	public void onProccesingAreaChanged(Rect area) {
		recalculateArea();
		handler.obtainMessage(CameraView.UPDATE_AREA, area).sendToTarget();
	}
	
	public void cameraChanged(int width, int height) {
		cameraSize = new Size(width, height);
		recalculateArea();
		cameraView.setCameraSize(width, height);
		Log.d("lifecycle", "camera changed: "+width+"x"+height);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("lifecycle", "surface created");
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder h, int f, int width, int height) {
		//TODO: delete this "bad" code
		if(width < height) {
			return;
		}
		surfaceSize = new Size(width, height);
		processingArea.setSurfaceSize(width, height);
		cameraView.setSurfaceSize(width, height);
		cameraView.setReady(true);	
		Log.d("lifecycle", "surface changed: "+width+"x"+height);
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		cameraView.setReady(false);
		Log.d("lifecycle", "surface destroyed");
	}
	
	public Float getScale() {
		if(surfaceSize == null || cameraSize == null) {
			return null;
		}
		float scale = (float) Math.max(
				surfaceSize.width/cameraSize.width, 
				surfaceSize.height/cameraSize.height
				);
		return scale;
	}
	
	public Size getCameraSize() {
		return cameraSize;
	}
	
	private void recalculateArea() {
		Float scale = getScale();
		Rect area = processingArea.getArea();
		if(scale == null || area == null) {
			return;
		}
		int areaWidth = (int) (area.width()/scale);
		int areaHeight = (int) (area.height()/scale);
		this.area = new org.opencv.core.Rect(
				(int)(cameraSize.width-areaWidth)/2,
				(int)(cameraSize.height-areaHeight)/2,
				areaWidth,
				areaHeight
				);
	}
}
