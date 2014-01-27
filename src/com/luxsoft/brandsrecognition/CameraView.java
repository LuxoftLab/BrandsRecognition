package com.luxsoft.brandsrecognition;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

public class CameraView implements Handler.Callback {

	public static final int DEFAULT_SCREEN_WIDTH = 1200;
	public static final int DEFAULT_SCREEN_HEIGHT = 720;
	
	private static final int DEFAULT_CORNER_REDIUS = 25;
	private static final int DEFAULT_BORDER_SIZE = 10;
	
	public static final int UPDATE_FRAME = 10;
	public static final int UPDATE_AREA = 11;
	
	private float cornerRadius, borderSize;
	
	private boolean isReady;
	private SurfaceHolder holder;
	private Controller controller;
	private int width, height;
	private int cameraWidth, cameraHeight;
	private Bitmap frame;
	private Paint paint;
	private Matrix matrix;
	private Rect processingArea;
	
	public CameraView(SurfaceHolder holder, Controller controller) {
		this.holder = holder;
		this.controller = controller;
		paint = new Paint();
		isReady = false;
		frame = null;
		width = height = 0;
		cameraWidth = cameraHeight = 0;
	}
	
	public void setReady(boolean ready) {
		isReady = ready;
	}
	
	public void setSurfaceSize(int width, int height) {
		this.width = width;
		this.height = height;
		float scale = ((float)width*height)/(DEFAULT_SCREEN_WIDTH*DEFAULT_SCREEN_HEIGHT);
		borderSize = DEFAULT_BORDER_SIZE*scale;
		cornerRadius = DEFAULT_CORNER_REDIUS*scale;
		paint.setStrokeWidth(borderSize);
		calculateMatrix();
	}
	
	public void setCameraSize(int cameraWidth, int cameraHeight) {
		this.cameraHeight = cameraHeight;
		this.cameraWidth = cameraWidth;
		frame = Bitmap.createBitmap(cameraWidth, cameraHeight, Bitmap.Config.ARGB_8888);
		calculateMatrix();
	}
	
	public void calculateMatrix() {
		Float scale = controller.getScale();
		Size cameraSize = controller.getCameraSize();
		Size surfaceSize = controller.getSurfaceSize();
		if(scale == null) {
			return;
		}
		matrix = new Matrix();
		matrix.postScale(scale, scale);
		matrix.postTranslate(
				(float)(surfaceSize.width - cameraSize.width*scale)/2, 
				(float)(surfaceSize.height - cameraSize.height*scale)/2
				);
		/*if(width == 0 || cameraWidth == 0) {
			return;
		}
		matrix = new Matrix();
		float scale = Math.max((float)width/cameraWidth, (float)height/cameraHeight);
		matrix.postScale(scale, scale);
		matrix.postTranslate((width - cameraWidth*scale)/2, (height - cameraHeight*scale)/2);*/
	}
	
	private void draw() {
		if(!isReady) {
			return;
		}
		Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            canvas.drawBitmap(frame, matrix, null);
            
    		paint.setColor(Color.WHITE);
    		paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(processingArea, paint);
    		paint.setStyle(Paint.Style.FILL);
    		canvas.drawCircle(processingArea.left, processingArea.top, cornerRadius, paint);
    		canvas.drawCircle(processingArea.left, processingArea.bottom, cornerRadius, paint);
    		canvas.drawCircle(processingArea.right, processingArea.bottom, cornerRadius, paint);
    		canvas.drawCircle(processingArea.right, processingArea.top, cornerRadius, paint);
            /*if(detected) {
            	paint.setColor(Color.GREEN);
            	paint.setTextSize(72);
            	canvas.drawText("YES", size.width()/2 - 20, size.height()/2, paint);
            } else {
            	paint.setColor(Color.RED);
            	paint.setTextSize(72);
            	canvas.drawText("NONE", size.width()/2 - 30, size.height()/2, paint);
            }*/
        } catch(Exception e) {
        	//Log.e("luxsoft", "view exception", e);
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
		case UPDATE_FRAME:
			if(frame == null) {
				break;
			}
			Utils.matToBitmap((Mat)msg.obj, frame);
			break;
		case UPDATE_AREA:
			processingArea = (Rect)msg.obj;
			break;
		default: 
			return false;
		}
		draw();
		return true;
	}

}
