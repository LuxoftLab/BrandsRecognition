package com.luxsoft.brandsrecognition;

import java.util.List;

import org.opencv.android.JavaCameraView;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

public class CameraView extends JavaCameraView implements Handler.Callback, CvCameraViewListener {

	public static final String TAG = "CameraView";
	
	public static final int DEFAULT_SCREEN_WIDTH = 1200;
	public static final int DEFAULT_SCREEN_HEIGHT = 720;
	
	private static final int DEFAULT_CORNER_REDIUS = 25;
	private static final int DEFAULT_BORDER_SIZE = 10;
	private static final int DEFAULT_TEXT_SIZE = 72;
	
	public static final int UPDATE_FRAME = 10;
	public static final int UPDATE_AREA = 11;
	public static final int UPDATE_MSG = 12;
	
	private float cornerRadius, borderSize, textSize;
	
	private CameraListener listener;
	private int width, height;
	private Bitmap frame;
	private Paint paint;
	private Matrix matrix;
	private SelectedArea area;
	private String message;
	
	public interface CameraListener {
		public void onCameraViewStarted(int width, int height);
		public Mat onFrame(Mat input);
		public void onCameraViewStopped();
		public void onAreaChanged(Rect area);
	}
	
	public CameraView(Context con, AttributeSet a) {
		super(con, a);
		setCvCameraViewListener(this);
		Log.d("lifecycle", "create cameraview");
		paint = new Paint();
	}
	
	public void setListener(CameraListener l) {
		listener = l;
	}
	
	@Override
	protected boolean connectCamera(int width, int height) {
		Log.d(TAG, "connectCamera "+width+"x"+height);
		boolean result = super.connectCamera(width, height);
		if(result) {
			this.width = width;
			this.height = height;
			float scale = Math.min(
					(float)width/DEFAULT_SCREEN_WIDTH, 
					(float)height/DEFAULT_SCREEN_HEIGHT
					);
			borderSize = DEFAULT_BORDER_SIZE*scale;
			textSize = DEFAULT_TEXT_SIZE*scale;
			cornerRadius = DEFAULT_CORNER_REDIUS*scale;
			paint.setStrokeWidth(borderSize);
			paint.setTextSize(textSize);
			calculateMatrix();
			area = new SelectedArea(this, width, height);
			this.setOnTouchListener(area);
		}
		return result;
	}
	
	@Override
	protected void AllocateCache() {
		frame = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
		Log.d(TAG, "AllocateCache "+mFrameWidth+"x"+mFrameHeight);
	}
	
	public void calculateMatrix() {
		Log.d(TAG, "CalcMatrix "+width+"x"+height+" "+mFrameWidth+"x"+mFrameHeight);
		float scale = getScale();
		Log.d(TAG, "CalcMatrix "+scale);
		matrix = new Matrix();
		matrix.postScale(scale, scale);
		matrix.postTranslate(
				(float)(width - mFrameWidth*scale)/2, 
				(float)(height - mFrameHeight*scale)/2
				);
	}
	
	@Override
	protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
		Mat img = frame.rgba();
		if(listener != null) {
			img = listener.onFrame(img);
		}
        Utils.matToBitmap(img, this.frame);
        draw();
	}
	
	private void draw() {
		Canvas canvas = null;
        try {
            canvas = getHolder().lockCanvas();
            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(this.frame, matrix, null);
            
            Rect rect = area.getArea();
    		paint.setColor(Color.WHITE);
    		paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rect, paint);
    		paint.setStyle(Paint.Style.FILL);
    		canvas.drawCircle(rect.left, rect.top, cornerRadius, paint);
    		canvas.drawCircle(rect.left, rect.bottom, cornerRadius, paint);
    		canvas.drawCircle(rect.right, rect.bottom, cornerRadius, paint);
    		canvas.drawCircle(rect.right, rect.top, cornerRadius, paint);
    		if(message != null) {
    			float textWidth = paint.measureText(message);
    			float x = (width-textWidth)/2;
    			float y = rect.bottom + textSize;
    			canvas.drawText(message, x, y, paint);
    		}

        } catch(Exception e) {
        	Log.e(TAG, "Exception", e);
        } finally {
            if (canvas != null) {
            	getHolder().unlockCanvasAndPost(canvas);
            }
        }
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
		case UPDATE_MSG:
			message = (String)msg.obj;
			break;
		default: 
			return false;
		}
		draw();
		return true;
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		if(listener != null) {
			listener.onCameraViewStarted(width, height);
			listener.onAreaChanged(area.getArea());
		}
	}

	@Override
	public void onCameraViewStopped() {
		if(listener != null) {
			listener.onCameraViewStopped();
		}
	}

	@Override
	public Mat onCameraFrame(Mat inputFrame) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onAreaChanged(boolean f) {
		if(f && listener != null) {
			listener.onAreaChanged(area.getArea());
		}
		draw();
	}

	@Override
	protected Size calculateCameraFrameSize(List<?> sizes,
			ListItemAccessor accessor, int surfaceWidth, int surfaceHeight) {
		Log.d(TAG, "calculateCameraFrameSize");
		List<android.hardware.Camera.Size> s = (List<android.hardware.Camera.Size>) sizes;
		int max = 0, i=0, imax=-1;
		for(android.hardware.Camera.Size _s : s) {
			if(_s.width > max) {
				max = _s.width;
				imax = i;
			}
			i++;
		}
		Log.d(TAG, "calculateCameraFrameSize: "+imax + " " + max);
        return new Size(s.get(imax).width, s.get(imax).height);
	}
	
	public float getScale() {
		return (float) Math.max(
					(float)width/mFrameWidth, 
					(float)height/mFrameHeight
				);
	}
}
