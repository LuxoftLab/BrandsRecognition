package com.luxsoft.brandsrecognition;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ProccesingArea implements OnTouchListener {
	
	private static final int DEFAULT_MIN_WIDTH = 200;
	private static final int DEFAULT_MIN_HEIGHT = 150;
	private static final int DEFAULT_MAX_WIDTH = 600;
	private static final int DEFAULT_MAX_HEIGHT = 450;
	private static final int DEFAULT_CORNER_REDIUS = 50;
	private static final int DEFAULT_BORDER_SIZE = 25;
	
											//ltrb
	private static final int LEFT   = 0x08; //1000b
	private static final int TOP    = 0x04; //0100b
	private static final int RIGHT  = 0x02; //0010b
	private static final int BOTTOM = 0x01; //0001b
	private static final int NONE   = 0x00; //0000b
	
	private int minWidth, minHeight;
	private int maxWidth, maxHeight;
	private int cornerRadius, borderSize;
	
	private Controller controller;
	private Rect area;
	private Rect surface;
	
	private Point lastTouch;
	private int action;
	
	public ProccesingArea(Controller controller) {
		this.controller = controller;
		area = null;
		surface = null;
	}
	
	public void setSurfaceSize(int width, int height) {
		float scale = ((float)width*height)/(CameraView.DEFAULT_SCREEN_WIDTH*CameraView.DEFAULT_SCREEN_HEIGHT);
		float widthScale = (float)width/CameraView.DEFAULT_SCREEN_WIDTH;
		float heightScale = (float)height/CameraView.DEFAULT_SCREEN_HEIGHT;
		minWidth = (int)(DEFAULT_MIN_WIDTH*widthScale);
		minHeight = (int)(DEFAULT_MIN_HEIGHT*heightScale);
		maxWidth = (int)(DEFAULT_MAX_WIDTH*widthScale);
		maxHeight = (int)(DEFAULT_MAX_HEIGHT*heightScale);
		cornerRadius = (int)(DEFAULT_CORNER_REDIUS*scale);
		borderSize = (int)(DEFAULT_BORDER_SIZE*scale);
		
		surface = new Rect(0, 0, width, height);
		
		if(area == null) {
			area = new Rect(
					(width - (maxWidth-minWidth))/2,
					(height - (maxHeight-minHeight))/2,
					(width + (maxWidth-minWidth))/2,
					(height + (maxHeight-minHeight))/2
			);
		} else {
			int areaWidth = area.width();
			int areaHeight = area.height();
			area.left = (width-areaWidth)/2;
			area.top = (height-areaHeight)/2;
			area.right = (width+areaWidth)/2;
			area.bottom = (height+areaHeight)/2;
			checkAreaSize();
		}
		controller.onProccesingAreaChanged(area);
	}
	
	private void onDown(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		lastTouch = new Point(x, y);
		int sqr = cornerRadius*cornerRadius;
		int length = (area.left-x)*(area.left-x)+(area.top-y)*(area.top-y);
		if(length <= sqr) {
			action = LEFT | TOP;
			return;
		}
		length = (area.left-x)*(area.left-x)+(area.bottom-y)*(area.bottom-y);
		if(length <= sqr) {
			action = LEFT | BOTTOM;
			return;
		}
		length = (area.right-x)*(area.right-x)+(area.bottom-y)*(area.bottom-y);
		if(length <= sqr) {
			action = RIGHT | BOTTOM;
			return;
		}
		length = (area.right-x)*(area.right-x)+(area.top-y)*(area.top-y);
		if(length <= sqr) {
			action = RIGHT | TOP;
			return;
		}
		Rect bigArea = new Rect(area.left-borderSize, area.top-borderSize,
								area.right+borderSize, area.bottom+borderSize);
		Rect smallArea = new Rect(area.left+borderSize, area.top+borderSize,
								area.right-borderSize, area.bottom-borderSize);
		if(y >= area.top && y <= area.bottom && x >= bigArea.left && x <= smallArea.left) {
			action = LEFT;
			return;
		}
		if(y >= area.top && y <= area.bottom && x >= smallArea.right && x <= bigArea.right) {
			action = RIGHT;
			return;
		}
		if(x >= area.left && x <= area.right && y >= bigArea.top && y <= smallArea.top) {
			action = TOP;
			return;
		}
		if(x >= area.left && x <= area.right && y >= smallArea.bottom && y <= bigArea.bottom) {
			action = BOTTOM;
			return;
		}
		action = NONE;
	}
	
	private void onMove(MotionEvent event) {
		if(action == NONE) {
			return;
		}
		if(lastTouch == null) {
			onDown(event);
			return;
		}
		
		int x = (int) event.getX();
		int y = (int) event.getY();
		int dx = x - lastTouch.x;
		int dy = y - lastTouch.y;
		lastTouch.x = x;
		lastTouch.y = y;
		
		if(test(LEFT)) {
			area.left += dx;
			area.right -= dx;
		}
		if(test(RIGHT)) {
			area.right += dx;
			area.left -= dx;
		}
		if(test(TOP)) {
			area.top += dy;
			area.bottom -= dy;
		}
		if(test(BOTTOM)) {
			area.bottom += dy;
			area.top -= dy;
		}
		checkAreaSize();
		controller.onProccesingAreaChanged(area);
	}
	
	private void onUp(MotionEvent event) {
		lastTouch = null;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			onDown(event);
			break;
		case MotionEvent.ACTION_MOVE:
			onMove(event);
			break;
		case MotionEvent.ACTION_UP:
			onUp(event);
			break;
		}
		return true;
	}
	
	private void checkAreaSize() {
		if(area.width() < minWidth) {
			area.left = (surface.width() - minWidth)/2;
			area.right = (surface.width() + minWidth)/2;
		} else if(area.width() > maxWidth) {
			area.left = (surface.width() - maxWidth)/2;
			area.right = (surface.width() + maxWidth)/2;
		}
		if(area.height() < minHeight) {
			area.top = (surface.height() - minHeight)/2;
			area.bottom = (surface.height() + minHeight)/2;
		} else if(area.height() > maxHeight) {
			area.top = (surface.height() - maxHeight)/2;
			area.bottom = (surface.height() + maxHeight)/2;
		}
	}
	
	private boolean test(int mask) {
		return (action & mask) == mask;
	}
	
	public Rect getArea() {
		return area;
	}

}
