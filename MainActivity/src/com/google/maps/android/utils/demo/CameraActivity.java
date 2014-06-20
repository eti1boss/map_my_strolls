package com.google.maps.android.utils.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class CameraActivity extends Activity {

	Uri fileUri = null;

	protected LocationListener locationListener;
	protected LocationManager lm;
	String currentPosition;
	PrintWriter writer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fileUri = Uri.fromFile(getOutputPhotoFile());
		
		try {
			File data = new File(
					Environment
							.getExternalStoragePublicDirectory("map_my_strolls"),
					"data.txt");

			writer = new PrintWriter(data, "UTF-8");

			writer.println(fileUri.getPath() + "|" + currentPosition);
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Acquire a reference to the system Location Manager
		lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				currentPosition = Double.toString(location.getLatitude()) + ":"
						+ Double.toString(location.getLongitude());
				Log.e("EBOS", "onLocationChanged : " + currentPosition);
				currentPosition="ok";
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.e("EBOS", "onStatusChanged");
			}

			public void onProviderEnabled(String provider) {
				Log.e("EBOS", "onProviderEnabled");
			}

			public void onProviderDisabled(String provider) {
				Log.e("EBOS", "onProviderDisabled");
			}
		};
		
		Log.e("EBOS", "demande MAJ");
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
		
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		
		

		Log.e("EBOS", "startActivityForResult");
		startActivityForResult(i, 0);
	}

	@Override
	protected void onResume() {
		Log.e("EBOS", "onResume");
		Log.e("EBOS", "fileUri : "+fileUri.getPath());
		Log.e("EBOS", "currentPosition : "+this.currentPosition);
		super.onResume();
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,locationListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		lm.removeUpdates(locationListener);
	}

	private File getOutputPhotoFile() {
		File directory = new File(
				Environment.getExternalStoragePublicDirectory("map_my_strolls"),
				"pictures");
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				return null;
			}
		}
		String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss")
				.format(new Date());
		String filePath = directory.getPath() + File.separator + "IMG_"
				+ timeStamp + ".jpg";

		return new File(filePath);
	}

}
