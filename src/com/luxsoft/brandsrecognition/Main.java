package com.luxsoft.brandsrecognition;

import java.io.File;

import org.opencv.android.OpenCVLoader;

import com.luxsoft.recognition.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

public class Main extends Activity {

	public static File CACHE_DIR;
	private static Context context;
	
	private Controller controller;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context = getApplicationContext();
		openCacheDir();
		
		Log.d("lifecycle", "created");
		setContentView(R.layout.activity_main);
		
		SurfaceView surface = (SurfaceView) findViewById(R.id.surface);
		
		controller = new Controller(this, surface);
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, controller);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		controller.pause();
		Log.d("lifecycle", "paused");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		controller.resume();
		Log.d("lifecycle", "resumed");
	}
	
	public static void openCacheDir() {
		CACHE_DIR = context.getExternalCacheDir();
	}

}
