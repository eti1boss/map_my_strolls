package com.google.maps.android.utils.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.utils.demo.model.Person;

/**
 * Demonstrates heavy customisation of the look of rendered clusters.
 */
public class CustomMarkerClusteringDemoActivity extends BaseDemoActivity implements ClusterManager.OnClusterClickListener<Person>, ClusterManager.OnClusterInfoWindowClickListener<Person>, ClusterManager.OnClusterItemClickListener<Person>, ClusterManager.OnClusterItemInfoWindowClickListener<Person> {
    private ClusterManager<Person> mClusterManager;
    private Random mRandom = new Random(1984);

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class PersonRenderer extends DefaultClusterRenderer<Person> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;
        
        public PersonRenderer() {
      	
            super(getApplicationContext(), getMap(), mClusterManager);
            
            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            //mIconGenerator.setContentView(mImageView);
            
//            String pathName = "/storage/emulated/0/map_my_strolls/pictures/test.jpg"; 
//            Drawable d = Drawable.createFromPath(pathName);
//            mIconGenerator.setBackground(d);
            mIconGenerator.setContentView(mImageView);
//EBOS
        }

		@Override
        protected void onBeforeClusterItemRendered(Person person, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.

//        	String pathName = "/storage/emulated/0/map_my_strolls/pictures/test.jpg"; 
        	String pathName = person.mImageURL; 
        	
        	BitmapFactory bmF = new BitmapFactory();
        	Bitmap bmOLD = bmF.decodeFile(pathName);
        	Bitmap bm = Bitmap.createScaledBitmap(bmOLD, 150, 150, true);
        	
        	Drawable drawable = new BitmapDrawable(bm);  

        	
            Drawable d = Drawable.createFromPath(pathName);
            //mIconGenerator.setBackground(drawable);
            
            mImageView.setImageBitmap(bm);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Person p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
//                Drawable drawable = getResources().getDrawable(p.profilePhoto);
                Drawable drawable = Drawable.createFromPath(p.mImageURL);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);

            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Person> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().name;
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Person> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Person item) {
    	Toast.makeText(this, item.mImageURL, Toast.LENGTH_SHORT).show();
    	// Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person item) {
    	File file = new File(item.mImageURL);
    	Uri path = Uri.fromFile(file);
        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfOpenintent.setDataAndType(path, "image/*");
     
            startActivity(pdfOpenintent);
        // Does nothing, but you could go into the user's profile page, for example.
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.8529143319951,-0.60242645385582), 11f));

        mClusterManager = new ClusterManager<Person>(this, getMap());
        mClusterManager.setRenderer(new PersonRenderer());
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        addItems();
        mClusterManager.cluster();
    }

    @SuppressLint("NewApi")
	private void addItems() {
        // http://www.flickr.com/photos/sdasmarchives/5036248203/
    	//mClusterManager.addItem(new Person(position(), "Walter", R.drawable.walter));
    	
    	
    	BufferedReader reader;
    	
    	File data = new File(
				Environment
						.getExternalStoragePublicDirectory("map_my_strolls"),
				"data.txt");

		try {
			reader = new BufferedReader(new FileReader("/storage/emulated/0/map_my_strolls/data.txt"));
	    	String line = null;
	    	while ((line = reader.readLine()) != null) {
	    		if(line != ""){
		    		String[] parts = line.split(";");
		    		if(parts.length != 0){
		    			String[] pos = parts[1].split(":");
		    			if(pos.length != 0){
		    				File imgFile = new  File(parts[0]);
		    				if(imgFile.exists()){
		    					mClusterManager.addItem(new Person(customPosition(Double.parseDouble(pos[0]),Double.parseDouble(pos[1])), "Ouvrir"/*parts[0]*/, R.drawable.teacher,imgFile.getPath()));
			    		    }
		    			}
		    		}
	    		}	
	    	}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//mClusterManager.addItem(new Person(customPosition(44.8310992826452,-0.56041431503595), "Terrasse des Quinconces", R.drawable.teacher,"ok"));
    	
    }

    private LatLng position() {
        return new LatLng(random(51.6723432, 51.38494009999999), random(0.148271, -0.3514683));
    }
    
    private LatLng customPosition(Double lat, Double lng) {
        return new LatLng(lat,lng);
    }

    private double random(double min, double max) {
        return mRandom.nextDouble() * (max - min) + min;
    }
}
