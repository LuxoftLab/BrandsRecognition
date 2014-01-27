package com.luxsoft.brandsrecognition;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import android.util.Log;

public class CameraCapture implements Runnable {
	
	private Controller controller;
	private boolean isRunning;
	
	private Mat frame;
	private VideoCapture camera;

	public CameraCapture(Controller controller) {
		this.controller = controller;
		camera = null;
	}
	
	private void initCamera() {
		releaseCamera();
		
		frame = new Mat();
		camera = new VideoCapture(0); 
		
		List<Size> sizes = camera.getSupportedPreviewSizes();
		for(Size size : sizes) {
			Log.d("luxsoft", size.width+"x"+size.height);
		}
    	Size size = sizes.get(0);
    	camera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, size.width);
    	camera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, size.height);
    	
    	controller.cameraChanged((int)size.width, (int)size.height);
	}
	
	private void releaseCamera() {
		if (camera != null) {
			VideoCapture temp = camera;
			camera = null;
			temp.release();
		}
	}
	
	@Override
	public void run() {
		isRunning = true;
		initCamera();
		while(isRunning && camera != null) {
			if(camera.grab()) {
				camera.retrieve(frame, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
				controller.onFrame(frame);
			}
		}
		releaseCamera();
	}
	
	public void disable() {
		isRunning = false;
	}
	
}
