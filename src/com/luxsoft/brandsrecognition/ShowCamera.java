package com.luxsoft.brandsrecognition;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder holdMe;
	private Camera theCamera;

	public ShowCamera(Context context, AttributeSet arg1) {
		super(context, arg1);
		holdMe = getHolder();
		holdMe.addCallback(this);
	}
	
	public void setCamera(Camera cam) {
		Log.d("activity", "set");
		theCamera = cam;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.d("activity", "change");
		try {
			theCamera.setPreviewDisplay(holdMe);
			theCamera.startPreview();
		} catch (IOException e) {
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
	}

}
