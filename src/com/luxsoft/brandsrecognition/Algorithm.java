package com.luxsoft.brandsrecognition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.util.Xml;

public class Algorithm implements Runnable {
	
	public static final String INIT = "Initialization, please wait...";
	public static final String WAIT = "Capture logo and wait...";
	public static final String ERROR = "Error occurred";
	/*public static final String ACURA = "ACURA";
	public static final String BMW = "BMW";
	
	private CascadeClassifier detector;
	private CascadeClassifier bmw;*/
	
	private boolean hasFrame;
	private Mat frame;
	private boolean isRunning;
	
	private Context context;
	private Controller controller;
	
	private Cascade detector;
	
	public Algorithm(Context context, Controller controller) {
		this.context = context;
		this.controller = controller;
		
	}
	
	private void initCascades() throws IOException, XmlPullParserException {
		controller.onAlgorithmResult(INIT);
		frame = new Mat();
		
		File dir = context.getDir("cascade", Context.MODE_PRIVATE);
		Log.d("luxsoft", dir.getAbsolutePath());
		AssetManager assets = context.getAssets();
		InputStream is = assets.open("cascade_tree.xml");
		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(is, null);
		parser.nextTag();
		parser.nextTag();
		detector = new Cascade(parser);
		detector.load(assets, dir);
		is.close();
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
		hasFrame = false;
		
		try {
			initCascades();
			isRunning = true;
		} catch(Exception e) {
			//TODO: 
			controller.onAlgorithmResult(ERROR);
			Log.e("luxsoft", "loading cascades", e);
		}
		
		while(isRunning) {
			if(!hasFrame) {
				continue;
			}
			
			Cascade result = detector.detectFromChildren(frame);
			
			if(result != null) {
				controller.onAlgorithmResult(result.getName());
			} else {
				controller.onAlgorithmResult(WAIT);
			}
			
			hasFrame = false;
		}
		
	}

}
