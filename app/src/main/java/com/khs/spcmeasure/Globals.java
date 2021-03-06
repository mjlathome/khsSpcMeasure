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
    private int latestCode = -1;
    private String latestName = "";
    private boolean loggedIn = false;   // assume not logged in
    private boolean canMeasure = false; // assume cannot measure
    private String username = "";   // assume blank


    // restrict constructor from being instantiated
    public Globals(){}

    // single instance logic
    public static synchronized Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
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

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isCanMeasure() {
        return canMeasure;
    }

    public void setCanMeasure(boolean canMeasure) {
        this.canMeasure = canMeasure;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
