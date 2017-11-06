package edu.uw.yw239.yama;

/**
 * Created by yunwu on 11/4/17.
 */

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MyApplication extends Application {

    private static Application sApplication;

    private static final String LOG_TAG = "EOMeApp";
    public static Application getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG,"On create of application");
        sApplication = this;
    }

}