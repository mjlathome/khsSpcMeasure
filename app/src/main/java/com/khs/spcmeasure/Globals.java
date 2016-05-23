package com.khs.spcmeasure;

/**
 * Created by Mark on 3/27/2016.
 * see: https://androidresearch.wordpress.com/2012/03/22/defining-global-variables-in-android/
 * created as a singleton as extended Application approach is restricted to Activities and Services with getApplication() call
 */
public class Globals{
    private static Globals instance;

    // global variables
    private boolean doVersionCheck = true;
    private boolean versionOk = true;  // assume version is ok
    private int latestCode = -1;
    private String latestName = "";

    // restrict constructor from being instantiated
    public Globals(){}

    // single instance logic
    public static synchronized Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }

    public boolean getDoVersionCheck() {
        return doVersionCheck;
    }

    public void setDoVersionCheck(boolean doVersionCheck) {
        this.doVersionCheck = doVersionCheck;
    }

    public boolean isVersionOk() {
        return versionOk;
    }

    public void setVersionOk(boolean versionOk) {
        this.versionOk = versionOk;
    }

    public int getLatestCode() {
        return latestCode;
    }

    public void setLatestCode(int latestCode) {
        this.latestCode = latestCode;
    }

    public String getLatestName() {
        return latestName;
    }

    public void setLatestName(String latestName) {
        this.latestName = latestName;
    }
}
