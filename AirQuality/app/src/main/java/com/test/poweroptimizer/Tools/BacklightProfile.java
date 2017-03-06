package com.test.poweroptimizer.Tools;


public class BacklightProfile {
    public String profileName;
    public String packageName;
    public String applicationName;
    public int timeout;
    public int level;
    public boolean status;

    public BacklightProfile() {
        profileName = null;
        packageName = null;
        applicationName = null;
        timeout = 30;
        level = 50;
        status = true;
    }
}
