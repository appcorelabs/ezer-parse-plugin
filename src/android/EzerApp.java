package com.ezerapp;
import android.app.Application;
import com.parse.Parse;
import com.parse.ParseInstallation;

public class EzerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // NOTE- EZER: Replace app ids if parse app changes - init parse only here
        // live app
        //Parse.initialize(this, "cJVMkfGazwUV5ENhcO3gc54Zfhv4Q5eVxvCPICVf", "aeMqWvvVhdMFD4OMD5UtkSjgCOapBG7999Dm4s3w");
        // test app
        Parse.initialize(this, "3QzEKMneMtpDfpytxS5XmbKHNZDigobnVhSS49q5", "0FqsUaGygcFqxelnJYFEasNhwW4kIEoxdKQ0Madv");
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
