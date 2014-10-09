package com.luxsoft.brandsrecognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


import android.graphics.Bitmap;
import android.util.Log;
import android.util.Xml;

public class Algorithm implements Runnable {
	
	public static final String INIT = "Initialization, please wait...";
	public static final String WAIT = "Capture logo and wait...";
	public static final String ERROR = "Error occurred";
	public static final String LOAD = "Loading cache, please wait...";
	public static final String CAPTURE = "Capturing...";
	
	private boolean hasFrame;
	private Mat frame;
	private boolean isRunning;
	private boolean isLoading;
	
	private Controller controller;
	private Cache cache;
	
	private Cascade detector;
	private Stabilizer stabilizer;
	
	private boolean save = false;
	protected static boolean capture = false;
	
	protected static int fcount = 0;
	
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

		controller.onAlgorithmResult(INIT);
		
		detector = new Cascade(Main.DIR);
		detector.load();
		
		stabilizer = new Stabilizer(null);
		
		isLoading = true;
	}
	
	public void putFrame(Mat currentFrame, Rect area) throws FileNotFoundException {
		Log.d("algo", hasFrame +" "+ isRunning);
		if(hasFrame || !isRunning) {
			return;
		}
		Log.d("algo", "put frame");
		frame = new Mat(currentFrame, area);
		Mat part = frame;
		
		if(save) {
			Bitmap frame = Bitmap.createBitmap(part.cols(), part.rows(), Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(part, frame);
			File file = new File(Main.CACHE_DIR, "img.png");
			try {
				FileOutputStream out = new FileOutputStream(file);
				frame.compress(Bitmap.CompressFormat.PNG, 50, out);
				out.flush();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			save = false;
		}
		
		if(capture) {
			Log.d("capture", "Algo enable");
			String filename = "Recognition_";
		    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		    Date date = new Date(System.currentTimeMillis());
		    String dateString = fmt.format(date);
		    filename += dateString;
		    filename += ".png";   
		    File file = new File(Main.CACHE_DIR, filename);    
		    filename = file.toString();
			Highgui.imwrite(filename, part);
			fcount++;
		}
		
		if(capture){
			hasFrame = false;
		} else hasFrame = true;
	}
	
	public void disable() {
		Log.d("algorun", "disable");
		isRunning = false;
	}
	
	@Override
	public void run() {
		hasFrame = false;
		Log.d("algorun", "run");
		try {
			initCascades();
			Log.d("algorun", "init done");
			isRunning = true;
		} catch(Exception e) {
			//TODO: 
			controller.onAlgorithmResult(ERROR);
			Log.e("luxsoft", "loading cascades:" + e.getMessage(), e);
		}
		
		LinkedList<Cascade> results;
		LinkedList<Rect> rects = new LinkedList <Rect>();
		while(isRunning) {
			//Log.d("algorun", "while");
			if(!hasFrame) {
				continue;
			}
			results = new LinkedList<Cascade>();
			rects = new LinkedList<Rect>();
			Log.d("algorun", "before detect");
			detector.detectFromChildren(frame, results, rects);
			Log.d("algorun", "after end");
			Log.d("algo", "rects: "+rects.size());
			controller.setRects(rects);
			Log.d("algorun", "before register");
			stabilizer.registerResult(results);
			Log.d("algorun", "after register");
			Cascade stibilized = stabilizer.getMostProbable();
			if(stibilized != null) {
				controller.onAlgorithmResult(stibilized.getName());
			} else {
				controller.onAlgorithmResult(WAIT);
			}
			
			hasFrame = false;
		}
		
	}
	
	public void saveImage() {
		save = true;
	}
	
	public void captureSet() {
		if(capture == true) {
			Log.d("capture", "capture set false");
			controller.onAlgorithmResult(WAIT);
			capture = false;
		} else {
			Log.d("capture", "capture true");
			controller.onAlgorithmResult(CAPTURE);
			capture = true;
		}
	}

}
