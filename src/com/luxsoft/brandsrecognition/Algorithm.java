package com.luxsoft.brandsrecognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class Algorithm implements Runnable {
	
	public static final String INIT = "Initialization, please wait...";
	public static final String WAIT = "Capture logo and wait...";
	public static final String ERROR = "Error occurred";
	public static final String LOAD = "Loading cache, please wait...";
	
	private boolean hasFrame;
	private Mat frame;
	private boolean isRunning;
	private boolean isLoading;
	
	private Controller controller;
	private Cache cache;
	
	private Cascade detector;
	private Stabilizer stabilizer;
	
	public Algorithm(Controller controller, Cache cache) {
		this.controller = controller;
		this.cache = cache;
		isRunning = false;
		isLoading = false;
	}
	
	private void initCascades() throws IOException, XmlPullParserException {
		if(isLoading) {
			return;
		}
		controller.onAlgorithmResult(LOAD);
		frame = new Mat();
		
		cache.update();
		
		controller.onAlgorithmResult(INIT);
		InputStream is = new FileInputStream(new File(Main.CACHE_DIR, "cascade_tree.xml"));
		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(is, null);
		parser.nextTag();
		parser.nextTag();
		
		detector = new Cascade(parser);
		detector.load();
		
		parser.nextTag();
		stabilizer = new Stabilizer(parser);
		
		is.close();
		isLoading = true;
	}
	
	public void putFrame(Mat currentFrame, Rect area) {
		if(hasFrame || !isRunning) {
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
			Log.e("luxsoft", "loading cascades:" + e.getMessage(), e);
		}
		
		LinkedList<Cascade> results;
		while(isRunning) {
			if(!hasFrame) {
				continue;
			}
			results = new LinkedList<Cascade>();
			detector.detectFromChildren(frame, results);
			stabilizer.registerResult(results);
			Cascade stibilized = stabilizer.getMostProbable();
			if(stibilized != null) {
				controller.onAlgorithmResult(stibilized.getName());
			} else {
				controller.onAlgorithmResult(WAIT);
			}
			
			hasFrame = false;
		}
		
	}

}
