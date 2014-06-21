package com.google.maps.android.utils.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.integer;
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
import android.widget.Toast;

public class CameraActivity extends Activity{

	public Uri fileUri = null;

	public LocationListener locationListener;
	public LocationManager lm;
	public static String currentPosition;
	
	public static Intent i;
	
	public String getCurrentPosition() {
		return currentPosition;
	}



	public void setCurrentPosition(String currentPosition) {
		this.currentPosition = currentPosition;
	}

	PrintWriter writer;
	String lol;

	private String ebos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fileUri = Uri.fromFile(getOutputPhotoFile());
		
		// Acquire a reference to the system Location Manager
		lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		// Define a listener that responds to location updates
		locationListener = new LocationListener() {

			public void onLocationChanged(Location location) {
				String laPosition = Double.toString(location.getLatitude()) + ":"
						+ Double.toString(location.getLongitude());
				Log.e("EBOS", "onLocationChanged : " + laPosition);
				updatePos(laPosition);
				setCurrentPosition(laPosition);
				startActivityForResult(i, 0);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.e("EBOS", "onStatusChanged");
			}

			public void onProviderEnabled(String provider) {
				Log.e("EBOS", "onProviderEnabled !");
			}

			public void onProviderDisabled(String provider) {
				Log.e("EBOS", "onProviderDisabled !");
			}
		};

		
		Log.e("EBOS", "demande MAJ");
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		
		i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		
		Toast.makeText(this, "Localisation en cours", Toast.LENGTH_LONG).show();
	}
	


	private void updatePos(String currentPos) {
		setCurrentPosition(currentPos);
		currentPosition=currentPos;
		this.lol=currentPos;
		ebos = currentPos;
		Toast.makeText(this, "up : "+ebos, Toast.LENGTH_SHORT).show();
		
	}
	
	private void test(){

		String pos = getCurrentPosition();
		Toast.makeText(this, "test : "+pos, Toast.LENGTH_SHORT).show();
		
		if(pos != ""){
		
			File data = new File(
					Environment
							.getExternalStoragePublicDirectory("map_my_strolls"),
					"data.txt");
			
			FileWriter fileWritter;
			try {
				fileWritter = new FileWriter(data,true);
		        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		        bufferWritter.newLine();
		        bufferWritter.write(fileUri.getPath() + ";" + pos);
		        bufferWritter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void onActivityResult(int  requestId, int resultCode, Intent data) {
		   if (resultCode == Activity.RESULT_CANCELED){
				Log.e("ALERT","RESULT_CANCELED");
		       
		   } else if (resultCode == Activity.RESULT_OK) {
			   test();
				Log.e("ALERT","RESULT_OK");
		      
		   }
		}
	
	@Override
	protected void onResume() {
		super.onResume();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,locationListener);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
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
