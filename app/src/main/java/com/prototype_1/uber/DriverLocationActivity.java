package com.prototype_1.uber;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.prototype_1.uber.databinding.ActivityDriverLocationBinding;

import java.util.ArrayList;
import java.util.List;

public class DriverLocationActivity extends FragmentActivity implements OnMapReadyCallback {

        private GoogleMap mMap;
        private ActivityDriverLocationBinding binding;
        Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        intent=getIntent();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        ConstraintLayout constraintLayout=findViewById(R.id.mapConstraintLayout);
        constraintLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                ArrayList<Marker> markers=new ArrayList<>();

                // Add a marker in Sydney and move the camera
                LatLng driverLocation = new LatLng(intent.getDoubleExtra("driverLatitude",0),intent.getDoubleExtra("driverLongitude",0));
                LatLng requestLocation=new LatLng(intent.getDoubleExtra("requestLatitude",0),intent.getDoubleExtra("requestLongitude",0));

                markers.add(mMap.addMarker(new MarkerOptions().position(driverLocation).title("Your location")));
                markers.add(mMap.addMarker(new MarkerOptions().position(requestLocation).title("Request location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));

                LatLngBounds.Builder builder=new LatLngBounds.Builder();
                for(Marker marker:markers){
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds=builder.build();

                int padding=0;
                CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds,padding);

                mMap.animateCamera(cu);
            }
        });


    }

    public void acceptRequest(View view) {
        ParseQuery<ParseObject> query=ParseQuery.getQuery("Request");
        query.whereEqualTo("username",intent.getStringExtra("username"));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    for(ParseObject object:objects){
                        object.put("driverUsername", ParseUser.getCurrentUser().getUsername());
                        object.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e==null){
                                    Intent directionsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                                            Uri.parse("http://maps.google.com/maps?saddr="+intent.getDoubleExtra("driverLatitude",0)+ "," +intent.getDoubleExtra("driverLongitude",0) +"&daddr="+intent.getDoubleExtra("requestLatitude",0)+","+intent.getDoubleExtra("requestLongitude",0)));
                                    startActivity(directionsIntent);
                                        }
                            }
                        });
                    }
                }
            }
        });
    }
}