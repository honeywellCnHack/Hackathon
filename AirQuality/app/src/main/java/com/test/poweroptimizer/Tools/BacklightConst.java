package com.test.poweroptimizer.Tools;

import android.net.Uri;

/**
 * Created by E560866 on 3/1/2017.
 */

public class BacklightConst {
    public static final String LOG_TAG = "PowerOptimizer";
    static public final String AUTHORITY = "com.honeywell.poweroptimizer.Tools.SmartBacklightPorvider";
    static public final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/SmartBacklight");
    static final public String STRING_PROFILE_NAME = "PROFILE_NAME";
    static final public String STRING_PN = "PACKAGE_NAME";
    static final public String STRING_STATUS = "PROFILE_STATUS";
    static final public String STRING_BACKLIGHT_LEVEL = "BACKLIGHT_LEVEL";
    static final public String STRING_BACKLIGHT_TIME = "BACKLIGHT_TIME";

    static final public int INDEX_PROFILE_NAME = 0;
    static final public int INDEX_PN = 1;
    static final public int INDEX_STATUS = 2;
    static final public int INDEX_BACKLIGHT_LEVEL = 3;
    static final public int INDEX_BACKLIGHT_TIME = 4;

    static final public String[] PROJECTION = new String[]{STRING_PROFILE_NAME, STRING_PN, STRING_STATUS, STRING_BACKLIGHT_LEVEL, STRING_BACKLIGHT_TIME};
}
