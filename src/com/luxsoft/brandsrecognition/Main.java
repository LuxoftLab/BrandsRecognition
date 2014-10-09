package com.luxsoft.brandsrecognition;

import java.io.File;

import org.opencv.android.OpenCVLoader;

import com.luxsoft.recognition.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.EditText;

public class Main extends Activity implements DialogInterface.OnClickListener {

	public static File CACHE_DIR, DIR;
	private static Context context;
	
	private Controller controller;
	private Menu menu;
	private AlertDialog dialog;
	Size previewSize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//final Size previewSize;
		context = getApplicationContext();
		openCacheDir();
		
		Log.d("lifecycle", "created");
		setContentView(R.layout.activity_main);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage("Threshold values");
		builder.setView(getLayoutInflater().inflate(R.layout.dialog, null));
		builder.setPositiveButton("Apply", this);
		
		dialog = builder.create();

		CameraView camera = (CameraView)findViewById(R.id.java_surface_view);
		controller = new Controller(this, camera);
		
		camera.setVisibility(SurfaceView.VISIBLE);
		camera.setListener(controller);
		
		camera.enableFpsMeter();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, controller);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id) {
		case R.id.toogleCannyOn:
			controller.toogleShowCanny();
			item.setVisible(false);
			menu.findItem(R.id.toogleCannyOff).setVisible(true);
			break;
		case R.id.toogleCannyOff:
			controller.toogleShowCanny();
			item.setVisible(false);
			menu.findItem(R.id.toogleCannyOn).setVisible(true);
			break;
		case R.id.blurOn:
			controller.setBlur(false);
			item.setVisible(false);
			menu.findItem(R.id.blurOff).setVisible(true);
			break;
		case R.id.blurOff:
			controller.setBlur(true);
			item.setVisible(false);
			menu.findItem(R.id.blurOn).setVisible(true);
			break;
		case R.id.threshold:
			dialog.show();
			break;
		case R.id.saveImage:
			controller.saveImage();
			break;
		case R.id.captureSetOn:
			Log.d("capture", "Main disable");
			controller.captureSet();
			item.setVisible(false);
			menu.findItem(R.id.captureSetOff).setVisible(true);
			break;
		case R.id.captureSetOff:
			Log.d("capture", "Main enable");
			controller.captureSet();
			item.setVisible(false);
			menu.findItem(R.id.captureSetOn).setVisible(true);
			break;
		}
		return super.onOptionsItemSelected(item);
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
		DIR = new File(Environment.getExternalStorageDirectory(), "cascades");
		if(!DIR.exists()) {
			DIR.mkdir();
		}
	}

	@Override
	public void onClick(DialogInterface d, int which) {
		int min = Integer.parseInt(((EditText)dialog.findViewById(R.id.min)).getText().toString());
		int max = Integer.parseInt(((EditText)dialog.findViewById(R.id.max)).getText().toString());
		controller.setThreshold(min, max);
	}

}
