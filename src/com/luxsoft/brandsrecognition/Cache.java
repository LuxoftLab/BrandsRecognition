package com.luxsoft.brandsrecognition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Cache {
	
	private final static String REMOUT_FILE = "cache/cache.zip";
	private final static String FILE_URL = 
			"https://github.com/gorz/BrandsRecognition/raw/master/"+REMOUT_FILE;
	private final static String QUERY = 
			"https://api.github.com/repos/gorz/BrandsRecognition/commits?path="+
			REMOUT_FILE+"&page=1&per_page=1";
	
	private SharedPreferences preferences;
	
	public Cache(Activity activity) {
		this.preferences = activity.getPreferences(Context.MODE_PRIVATE);
	}

	public void update() {
		String lastCommit = getLastCommit();
		String currentCommit = getCurrentCommit();
		Log.d("cache", lastCommit + " " + currentCommit);
		if(currentCommit == null || currentCommit.equals(lastCommit)) {
			return;
		}
		if(download()) {
			updateLastCommit(currentCommit);
		}
	}
	
	private boolean download() {
		Log.d("cache", "download start");
		try {
			URL url = new URL(FILE_URL);
			Log.d("cache", "open connection");
			unzip(url.openStream());
			Log.d("cache", "close connection");
		} catch(IOException e) {
			Log.e("errors", "download", e);
			return false;
		}
		Log.d("cache", "download end");
		return true;
	}
	
	private void unzip(InputStream in) throws IOException {
		Log.d("cache", "delete cache zip");
		deleteDirRecursive(Main.CACHE_DIR);
		Log.d("cache", "reopen cache dir");
		Main.openCacheDir();
		Log.d("cache", "zip open");
		ZipInputStream zip = new ZipInputStream(in);
		byte data[] = new byte[1024];
		Log.d("cache", "zip entry loop");
		for(ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			Log.d("cache", entry.getName());
			String fileName = Main.CACHE_DIR + File.separator + entry.getName();
			if(entry.isDirectory()) {
				mkdir(fileName);
			} else {
				FileOutputStream fout = new FileOutputStream(fileName); 
				int count;
				while((count = zip.read(data, 0, data.length)) > 0) {
					fout.write(data, 0, count);
				}
		 
		        zip.closeEntry(); 
		        fout.close(); 
			}
		}
		zip.close();
		Log.d("cache", "zip close");
	}
	
	private void deleteDirRecursive(File dir) {
		if(dir == null) {
			Log.d("cache", "dir null");
		}
		File list[] = dir.listFiles();
		for(File file : list) {
			if(file == null) {
				Log.d("cache", "file null");
			}
			Log.d("cache", file.getName() + " " + file.isDirectory());
			if(file.isDirectory()) {
				deleteDirRecursive(file);
			} else {
				file.delete();
			}
		}
		dir.delete();
	}
	
	private void mkdir(String name) {
		File dir = new File(name);
		if(!dir.isDirectory()) {
			dir.mkdir();
		}
	}
	
	private String getCurrentCommit() {
		String commit;
		try {
			URL url = new URL(QUERY);
			BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream())
					);
			StringBuilder jsonString = new StringBuilder();
			String tempString;
			while((tempString = reader.readLine()) != null) {
				jsonString.append(tempString);
			}
			JSONArray json = new JSONArray(jsonString.toString());
			commit = json.getJSONObject(0).getString("sha");
		} catch(IOException e) {
			commit = null;
			Log.e("errors", "commit", e);
		} catch (JSONException e) {
			commit = null;
			Log.e("errors", "json", e);
		}
		return commit;
	}
	
	private String getLastCommit() {
		return preferences.getString("commit", null);
	}
	
	private void updateLastCommit(String commit) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("commit", commit);
		editor.commit();
	}
	
}
