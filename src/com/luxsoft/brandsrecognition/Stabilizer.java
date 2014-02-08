package com.luxsoft.brandsrecognition;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class Stabilizer {
	
	private int processedFrames;
	private int neededFrames;
	
	private LinkedList<Cascade> queue;
	private HashMap<Cascade, Integer> amountOfRepeats;
	
	public Stabilizer(XmlPullParser parser) throws XmlPullParserException, IOException {
		processedFrames = Integer.valueOf(
				parser.getAttributeValue(null, "processedFrames"));
		neededFrames = Integer.valueOf(parser.getAttributeValue(null, "neededFrames"));
		parser.nextTag();
		
		queue = new LinkedList<Cascade>();
		amountOfRepeats = new HashMap<Cascade, Integer>();
	}
	
	public void registerResult(Cascade cascade) {
		queue.offer(cascade);
		Integer lastResult = amountOfRepeats.get(cascade);
		if(cascade == null) {
			//There is no need to count null values
		} else if(lastResult == null) {
			amountOfRepeats.put(cascade, 1);
		} else {
			amountOfRepeats.put(cascade, amountOfRepeats.get(cascade) + 1);
		}
		if(queue.size() != processedFrames) {
			return;
		}
		Cascade deleted = queue.poll();
		if(deleted != null) {
			lastResult = amountOfRepeats.get(deleted);
			amountOfRepeats.put(deleted, amountOfRepeats.get(deleted) - 1);
		}
	}
	
	public Cascade getMostProbable() {
		Entry<Cascade, Integer> max = null;
		for(Entry<Cascade, Integer> entry : amountOfRepeats.entrySet()) {
			Log.d("luxsoft", entry.getKey().getName()+" "+entry.getValue());
			if(max == null || max.getValue() < entry.getValue()) {
				max = entry;
			}
		}
		if(max != null && max.getValue() >= neededFrames) {
			return max.getKey();
		}
		return null;
	}

}
