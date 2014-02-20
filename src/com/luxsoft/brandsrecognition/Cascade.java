package com.luxsoft.brandsrecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.AssetManager;

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
	
	public void load(AssetManager assets, File dir) throws IOException {
		if(name != null) {
			InputStream is = assets.open(cascade);
            File mCascadeFile = new File(dir, cascade.replace('/', '_'));
            FileOutputStream os = new FileOutputStream(mCascadeFile);
 
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
 
            detector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
		}
		for(Cascade child : children) {
			child.load(assets, dir);
		} 
	}
	
	public void detect(Mat frame, LinkedList<Cascade> result) {
		MatOfRect rects = new MatOfRect();
		if(detector != null) {
			detector.detectMultiScale(frame, rects, 1.1, 2, 2, new Size(30, 30), new Size());
		}
		if(rects.toList().size() != 0) {
			if(children.size() == 0) {
				result.add(this);
			} else {
				detectFromChildren(frame, result);
			}
		}
	}
	
	public void detectFromChildren(Mat frame, LinkedList<Cascade> result) {
		for(Cascade child : children) {
			child.detect(frame, result);
		} 
	}
	
	public String getName() {
		return name;
	}

}
