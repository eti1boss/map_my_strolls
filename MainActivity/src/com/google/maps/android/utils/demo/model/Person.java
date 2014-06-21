package com.google.maps.android.utils.demo.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Person implements ClusterItem {
    public final String name;
    public final int profilePhoto;
    private final LatLng mPosition;
    public final String mImageURL;

    public Person(LatLng position, String name, int pictureResource,String imageURL) {
        this.name = name;
        profilePhoto = pictureResource;
        mPosition = position;
        mImageURL = imageURL;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
