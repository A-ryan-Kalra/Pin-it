package com.prototype_1.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestActivity extends AppCompatActivity {

    ArrayList<Double>requestLatitude=new ArrayList<>();
    ArrayList<Double>requestLongitude=new ArrayList<>();
    ArrayList<String>names=new ArrayList<>();

    ListView requestListView;
    ArrayList<String> requests=new ArrayList<>();
    LocationManager locationManager;
    LocationListener locationListener;
    ArrayAdapter arrayAdapter;
    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                @SuppressLint("MissingPermission") Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateListView(lastKnownLocation);
            }
        }
    }

    public void updateListView(Location location){
        if(location!=null) {
            requests.clear();
            requestLatitude.clear();
            requestLongitude.clear();
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");

            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
           // query.whereDoesNotExist("driverUsername");
            query.whereNear("location", parseGeoPoint);
            query.setLimit(10);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        for (ParseObject object : objects) {
                            ParseGeoPoint requestLocation=(ParseGeoPoint) object.get("location");
                            Double distanceInKilometer = parseGeoPoint.distanceInMilesTo(requestLocation);
                            Double distanceInOneDp = Double.valueOf(Math.round(distanceInKilometer * 10) / 10);
                            requests.add(distanceInOneDp.toString() + " miles " + object.getString("username"));
                            requestLatitude.add(requestLocation.getLatitude());
                            requestLongitude.add(requestLocation.getLongitude());
                            names.add( object.getString("username"));
                        }
                    } else {
                        requests.add("No active requests nearby");
                    }
                    arrayAdapter.notifyDataSetChanged();
                    Log.i("Latitude", String.valueOf(requestLatitude.size()));
                    Log.i("Longitude", String.valueOf(requestLongitude.size()));
                }
            });
        }else
            Toast.makeText(ViewRequestActivity.this, "Location can't be null", Toast.LENGTH_SHORT).show();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);

        setTitle("Nearby Requests");
        requestListView=findViewById(R.id.requestListview);

        arrayAdapter=new ArrayAdapter(this, android.R.layout.simple_list_item_1,requests);
        requests.clear();
        requests.add("Getting nearby Requests");

        requestListView.setAdapter(arrayAdapter);
        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(ViewRequestActivity.this,android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {

                    Location lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (requestLatitude.size() > i && requestLongitude.size() > i && lastKnownLocation!=null && names.size()>i) {
                        Intent intent =new Intent(getApplicationContext(),DriverLocationActivity.class);
                        intent.putExtra("requestLatitude",requestLatitude.get(i));
                        intent.putExtra("requestLongitude",requestLongitude.get(i));
                        intent.putExtra("driverLatitude",lastKnownLocation.getLatitude());
                        intent.putExtra("driverLongitude",lastKnownLocation.getLongitude());
                        intent.putExtra("username",names.get(i));

                        startActivity(intent);
                    }
                }
            }
        });
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateListView(location);
            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }
        }else//will check whether the permission is give to the user in order to access the app
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                Location lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnownLocation!=null){
                    updateListView(lastKnownLocation);
                }
            }
        }
    }
}