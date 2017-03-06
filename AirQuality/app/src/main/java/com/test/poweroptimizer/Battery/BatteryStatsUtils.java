package com.test.poweroptimizer.Battery;

import android.content.Context;
import android.content.pm.PackageManager;


public class BatteryStatsUtils {

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 60 * 60;
    private static final int SECONDS_PER_DAY = 24 * 60 * 60;

    /**
     * Checks if we have BATTERY_STATS permission
     *
     * @param context
     * @return true if the permission was granted
     */
    public static boolean hasBatteryStatsPermission(Context context) {
        return wasPermissionGranted(context,
                android.Manifest.permission.BATTERY_STATS);
    }

    /**
     * Checks if we have DUMP permission
     *
     * @param context
     * @return true if the permission was granted
     */

    public static boolean hasDumpsysPermission(Context context) {
        return wasPermissionGranted(context, android.Manifest.permission.DUMP);
    }

    private static boolean wasPermissionGranted(Context context,
                                                String permission) {
        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(permission, context.getPackageName());
        return (hasPerm == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Returns elapsed time for the given millis, in the following format: 2d 5h
     * 40m 29s
     *
     * @param context the application context
     * @param millis  the elapsed time in milli seconds
     * @return the formatted elapsed time
     */
    public static String formatElapsedTime(Context context, double millis) {
        StringBuilder sb = new StringBuilder();
        int seconds = (int) Math.floor(millis / 1000);

        int days = 0, hours = 0, minutes = 0;
        if (seconds > SECONDS_PER_DAY) {
            days = seconds / SECONDS_PER_DAY;
            seconds -= days * SECONDS_PER_DAY;
        }
        if (seconds > SECONDS_PER_HOUR) {
            hours = seconds / SECONDS_PER_HOUR;
            seconds -= hours * SECONDS_PER_HOUR;
        }
        if (seconds > SECONDS_PER_MINUTE) {
            minutes = seconds / SECONDS_PER_MINUTE;
            seconds -= minutes * SECONDS_PER_MINUTE;
        }
        if (days > 0) {
            sb.append(" ");
        } else if (hours > 0) {
            sb.append("");
        } else if (minutes > 0) {
            sb.append("");
        } else {
            sb.append("");
        }

        return sb.toString();
    }

}
