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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
            
            String pathName = "/storage/emulated/0/map_my_strolls/pictures/test.jpg"; 
            Drawable d = Drawable.createFromPath(pathName);
            mIconGenerator.setBackground(d);
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
        	Bitmap bm = Bitmap.createScaledBitmap(bmOLD, 150, 150, false);
        	
        	Drawable drawable = new BitmapDrawable(bm);  

        	
            Drawable d = Drawable.createFromPath(pathName);
            mIconGenerator.setBackground(drawable);
            
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
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person item) {
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
        Toast.makeText(getApplicationContext(), "qsfeqsdfqs", Toast.LENGTH_LONG);
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
		    					mClusterManager.addItem(new Person(customPosition(Double.parseDouble(pos[0]),Double.parseDouble(pos[1])), parts[0], R.drawable.teacher,imgFile.getPath()));
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
    	
    	/*mClusterManager.addItem(new Person(customPosition(44.8155134613628,-0.55440958300831), "Jardin Brascassat", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.846346095224,-0.57027209701738), "Terrasse des Quinconces", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8427104724202,-0.58533428258489), "Square des Martyrs de la Résistance", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8463501049228,-0.5649715593407), "Parc des Berges de Queyries", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8634384524561,-0.56352782729974), "Parc Chantecrit", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8508400589585,-0.54551958250028), "Square Souriaux", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8131424881763,-0.56631019968645), "Square Liotard", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8601641564201,-0.6371486173325), "Square Les Jasmins", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8741199689853,-0.54402749865433), "Parc de Bacalan", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8529143319951,-0.60242645385582), "Parc Bordelais", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8322299683336,-0.57535658264159), "Place Francis de Pressensé", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8883743911242,-0.57635112519142), "Berges du lac", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8772540959615,-0.54152067753823), "Parc du Port de la Lune", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8483120393279,-0.59507875635849), "Jardin de la Visitation", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8411412725735,-0.58058202793965), "Place Gambetta", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8337029237602,-0.56091738189362), "Parc des Sports Saint Michel", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8512589464279,-0.61461002009681), "Square de l'église Saint Amand", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8511076592916,-0.5604283051404), "Parc aux Angéliques séquence 2", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8541666320585,-0.63152446326111), "Parc Monséjour", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8228499690165,-0.59492906425748), "Square Valmy", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8591673209751,-0.58198719193787), "Square de l'Europe", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8827414268288,-0.57978118078902), "Berges du lac", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8291975217503,-0.56241149721864), "Jardin André Meunier", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8330696740192,-0.61122843553524), "Jardin de Lili", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8592991849519,-0.56077049435909), "Jardin de ta soeur", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8303537416189,-0.60335594695551), "La Maison aux personnages des Kabakov", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8247482807509,-0.57713029127432), "Square Argonne", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8371307857454,-0.5817728525313), "Square Saint John Perse", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8606333884683,-0.58170859217271), "Parc de la Cité du Grand Parc", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8642805685378,-0.570369978126), "Square Haussmann", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.830691565307,-0.56394836801076), "Jardin des Cèdres", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8306854976094,-0.58881728045945), "Aire de jeux place d'Arlac", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8457883297168,-0.56416506274888), "Place des Droits de l'Enfant", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8551224268659,-0.61485387468071), "Square Honoré d'Estienne d'Orves", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8490860371795,-0.56363500914887), "Parc aux Angéliques séquence 1", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8365622040709,-0.60800164142515), "Square Emile Combes", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8264628940148,-0.59848036133239), "Parc de la Béchade", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8194923120669,-0.57024382926165), "Square Paul Antin", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8263184637852,-0.59238201224352), "Square des Tilleuls", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8236473227632,-0.60859796110473), "Parc de la Cité Carreire", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8185477209283,-0.57613254506874), "Eglise Sainte Geneviève", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8198199113962,-0.59906513926498), "Square Saint Julien", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8769884542364,-0.54285074157771), "Aire de jeux du Parc du Port de la Lune", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8378156427562,-0.5804615766991), "Jardin de la Mairie", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8394332588418,-0.57144978236588), "Square Vinet", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.825017299018,-0.58203835007396), "Jardin des Dames de la foi", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8142929529499,-0.57244915044476), "Jardin d'Ars", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8416916800611,-0.56897029888861), "Jardin des Lumières", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8611222721839,-0.56025882919705), "Square Joséphine", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8643386874518,-0.57666592556124), "Square Charazac", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8489591240912,-0.57836299172279), "Jardin Public", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8366388717985,-0.56961128095368), "Square Jean Bureau", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8474737240455,-0.56080700911914), "Square Reignier", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8419990651074,-0.59328791291832), "Jardin Georges Mandel", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8856333789349,-0.53980293808487), "Parc des Berges de Garonne", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8192322450814,-0.5775805296348), "Square Bertrand de Goth", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8551293940604,-0.58682066323661), "Parc Rivière", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8915971007383,-0.58735051211613), "Berges du lac_Nord", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8378291814857,-0.58206181056096), "Square André Lhôte", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8575876871705,-0.62224804013049), "Parc de la piscine Stéhélin", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8567844247201,-0.6236382808561), "Parc Stéhélin", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.9034039913513,-0.5731224718106), "Bois de Bordeaux", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8793053251661,-0.5441542793271), "Square docteur Roger Hypoustéguy", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.860619757365,-0.6170311381988), "Square Hortense Schneider", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8246499654276,-0.61259594932301), "Square Alfred Smith", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8307363466072,-0.56523831759999), "Jardin des Remparts", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.872849410881,-0.57124378012408), "Parc de la Cité des Aubiers", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8178483205415,-0.57389228082191), "Square Jean Mermoz", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8331451702635,-0.59364380670929), "Square Gaviniès", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8310992826452,-0.56041431503595), "Square Dom Bedos", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8567844247201,-0.6236382808561), "Parc Stéhélin", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.9034039913513,-0.5731224718106), "Bois de Bordeaux", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8793053251661,-0.5441542793271), "Square docteur Roger Hypoustéguy", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.860619757365,-0.6170311381988), "Square Hortense Schneider", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8246499654276,-0.61259594932301), "Square Alfred Smith", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8307363466072,-0.56523831759999), "Jardin des Remparts", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.872849410881,-0.57124378012408), "Parc de la Cité des Aubiers", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8178483205415,-0.57389228082191), "Square Jean Mermoz", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8331451702635,-0.59364380670929), "Square Gaviniès", R.drawable.teacher));
    	mClusterManager.addItem(new Person(customPosition(44.8310992826452,-0.56041431503595), "Square Dom Bedos", R.drawable.teacher));*/
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
