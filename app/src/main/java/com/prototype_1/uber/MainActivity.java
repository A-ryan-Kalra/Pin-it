package com.prototype_1.uber;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity {

    public  void redirectActivity() {
        if(ParseUser.getCurrentUser().get("riderOrDriver").equals("Rider")) {
            Intent intent = new Intent(this, RiderActivity.class);
            startActivity(intent);
        }else
        {
            Intent intent=new Intent(this,ViewRequestActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        ParseInstallation.getCurrentInstallation().saveInBackground();

        if(ParseUser.getCurrentUser()==null){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e==null){
                        Log.i("Info","Anonymous Login successful");
                     }else{
                        Log.i("Info","Anonymous Login Failed");
                     }
                }
            });
        }
        else{
            if(ParseUser.getCurrentUser().get("riderOrDriver") !=null) {
                Log.i("Info","Redirecting as "+ParseUser.getCurrentUser().get("riderOrDriver"));
                redirectActivity();
            }
        }
        
    }

    public void getStarted(View view) {
        Switch userTypeSwitch =findViewById(R.id.switch1);
        String usertype="Driver";

        if(userTypeSwitch.isChecked()){
            usertype="Rider";
        }
        ParseUser.getCurrentUser().put("riderOrDriver",usertype);
        Log.i("Check",usertype);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null)
                    redirectActivity();
            }
        });

    }
}