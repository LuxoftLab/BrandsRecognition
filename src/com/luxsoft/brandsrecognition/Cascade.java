package com.luxsoft.brandsrecognition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class Cascade {
	
	private ArrayList<Cascade> children;
	private String name, cascade;
	private CascadeClassifier detector;
	
	public Cascade(XmlPullParser parser) throws XmlPullParserException, IOException {
		children = new ArrayList<Cascade>();
		if(!Boolean.valueOf(parser.getAttributeValue(null, "isRoot"))) {
			name = parser.getAttributeValue(null, "name");
			cascade = parser.getAttributeValue(null, "cascade");
		}
		parser.nextTag();
		while(parser.getEventType() != XmlPullParser.END_TAG) {
			children.add(new Cascade(parser));
			parser.nextTag();
		}
	}
	
	public void load() throws IOException {
		if(name != null) {
			File f = new File(Main.CACHE_DIR, cascade);
			if(!f.exists()) {
				throw new IOException(name+" not exsits");
			}
			detector = new CascadeClassifier(f.getAbsolutePath());
		}
		for(Cascade child : children) {
			child.load();
		} 
	}
	
	public void detect(Mat frame, LinkedList<Cascade> result, LinkedList<Rect> rects) {
		MatOfRect _rects = new MatOfRect();
		if(detector != null) {
			detector.detectMultiScale(frame, _rects, 1.1, 2, 2, new Size(30, 30), new Size());
		}
		if(_rects.toList().size() != 0) {
			if(children.size() == 0) {
				for(Rect r : _rects.toList()) {
					rects.add(r);
				}
				result.add(this);
			} else {
				detectFromChildren(frame, result, rects);
			}
		}
	}
	
	public void detectFromChildren(Mat frame, LinkedList<Cascade> result, LinkedList<Rect> rects) {
		for(Cascade child : children) {
			child.detect(frame, result, rects);
		} 
	}
	
	public String getName() {
		return name;
	}

}
