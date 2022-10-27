package com.prototype_1.uber;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseUser;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());

        ParseUser.enableAutomaticUser();


    }
}

