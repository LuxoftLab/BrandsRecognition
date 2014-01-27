package com.luxsoft.brandsrecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.luxsoft.recognition.R;

import android.content.Context;
import android.util.Log;

public class Algorithm implements Runnable {
	
	private CascadeClassifier detector;
	private boolean hasFrame;
	private Mat frame;
	private boolean isRunning;
	
	private Context context;
	private Controller controller;
	
	public Algorithm(Context context, Controller controller) {
		this.context = context;
		this.controller = controller;
	}
	
	private void initCascades() {
		frame = new Mat();
		try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = context.getResources().openRawResource(R.raw.cascade);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "cascade.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
 
 
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
 
 
            // Load the cascade classifier
            detector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
        	detector = null;
            Log.e("luxsoft", "Error loading cascade", e);
        }
	}
	
	public void putFrame(Mat currentFrame, Rect area) {
		if(hasFrame) {
			return;
		}
		Mat part = new Mat(currentFrame, area);
		Imgproc.cvtColor(part, frame, Imgproc.COLOR_RGB2GRAY);
		hasFrame = true;
	}
	
	public void disable() {
		isRunning = false;
	}
	
	@Override
	public void run() {
		isRunning = true;
		hasFrame = false;
		
		initCascades();
		
		while(isRunning) {
			if(!hasFrame) {
				continue;
			}
			MatOfRect rects = new MatOfRect();
			
			if(detector != null) {
				detector.detectMultiScale(frame, rects, 1.1, 2, 2, new Size(30, 30), new Size());
			}
			//activity.onResult(rects.toList().size() != 0);
			if(rects.toList().size() != 0) {
				Log.d("luxsoft", "has");
			} else {
				Log.d("luxsoft", "none");
			}
			hasFrame = false;
		}
		
	}

}
