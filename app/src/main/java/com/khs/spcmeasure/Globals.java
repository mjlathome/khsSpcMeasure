package com.khs.spcmeasure;

/**
 * Created by Mark on 3/27/2016.
 * see: https://androidresearch.wordpress.com/2012/03/22/defining-global-variables-in-android/
 * created as a singleton as extended Application approach is restricted to Activities and Services with getApplication() call
 */
public class Globals{
    private static Globals instance;

    // global variables
    private boolean versionOk = false;  // assume version is not ok

    // restrict constructor from being instantiated
    public Globals(){}

    // single instance logic
    public static synchronized Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }

    // get version ok
    public boolean getVerionOk() {
        return this.versionOk;
    }

    // set verion ok
    public void setVersionOk(boolean ok) {
        this.versionOk = ok;
    }
}
