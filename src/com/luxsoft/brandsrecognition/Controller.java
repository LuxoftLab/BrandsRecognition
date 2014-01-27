package com.luxsoft.brandsrecognition;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Controller extends BaseLoaderCallback implements SurfaceHolder.Callback {
	
	private CameraView cameraView;
	private CameraCapture camera;
	private ProccesingArea processingArea;
	private Handler handler;
	private boolean opencvLoaded;
	
	public Controller(Context context, SurfaceView surface) {
		super(context);
		SurfaceHolder holder = surface.getHolder();
		holder.addCallback(this);
		
		cameraView = new CameraView(holder);
		camera = new CameraCapture(this);
		processingArea = new ProccesingArea(this);
		
		handler = new Handler(context.getMainLooper(), cameraView);
		surface.setOnTouchListener(processingArea);
		
		opencvLoaded = false;
	}
	
	public void pause() {
		camera.disable();
	}
	
	public void resume() {
		startThreads();
	}
	
	private void startThreads() {
		if(opencvLoaded) {
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
	}
	
	public void onProccesingAreaChanged(Rect area) {
		handler.obtainMessage(CameraView.UPDATE_AREA, area).sendToTarget();
	}
	
	public void cameraChanged(int width, int height) {
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
}
