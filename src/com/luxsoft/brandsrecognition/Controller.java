package com.luxsoft.brandsrecognition;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Controller extends BaseLoaderCallback implements SurfaceHolder.Callback {
	
	private CameraView cameraView;
	private CameraCapture camera;
	private Algorithm algo;
	private ProccesingArea processingArea;
	private Handler handler;
	private boolean opencvLoaded;
	
	private Size cameraSize;
	private Size surfaceSize;
	private org.opencv.core.Rect area;
	
	public Controller(Context context, SurfaceView surface) {
		super(context);
		SurfaceHolder holder = surface.getHolder();
		holder.addCallback(this);
		
		cameraView = new CameraView(holder, this);
		camera = new CameraCapture(this);
		algo = new Algorithm(context, this);
		processingArea = new ProccesingArea(this);
		
		handler = new Handler(context.getMainLooper(), cameraView);
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
	
	public void onFrame(Mat frame) {
		handler.obtainMessage(CameraView.UPDATE_FRAME, frame).sendToTarget();
		if(area != null) {
			algo.putFrame(frame, area);
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
