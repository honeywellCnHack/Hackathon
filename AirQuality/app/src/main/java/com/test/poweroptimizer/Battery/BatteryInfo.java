/*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 COPYRIGHT (c) 2016   HONEYWELL INC.,  ALL RIGHTS RESERVED
 
 This software is a copyrighted work and/or information protected as a trade secret. 
 Legal rights of Honeywell Inc. in this software  is distinct from ownership of any 
 medium in which the software is embodied. Copyright or trade secret notices included 
 must be reproduced in any copies authorized by Honeywell Inc. 
 The information in this software is subject to change without notice and should not 
 be considered as a commitment by Honeywell Inc.

//  @ Project : DAC
//  @ Module  : Device Engine 
//  @ Component : Battery 
//  @ File Name : BatteryInfo.java
//  @ Date : 29/08/2016
//  @ Author(s) : Sand(E547883)
//
//
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- */

package com.test.poweroptimizer.Battery;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.os.UserManager;
import android.util.Log;
import android.util.SparseArray;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatteryInfo {
    /**
     * Power consumption when CPU is awake (when a wake lock is held). This
     * should be 0 on devices that can go into full CPU power collapse even when
     * a wake lock is held. Otherwise, this is the power consumption in addition
     * to POWERR_CPU_IDLE due to a wake lock being held but with no CPU
     * activity.
     */
    public static final String POWER_CPU_AWAKE = "cpu.awake";

    /**
     * Power consumption when CPU is in power collapse mode.
     */
    public static final String POWER_CPU_ACTIVE = "cpu.active";

    /**
     * Power consumption when WiFi driver is scanning for networks.
     */
    public static final String POWER_WIFI_SCAN = "wifi.scan";

    /**
     * Power consumption when WiFi driver is on.
     */
    public static final String POWER_WIFI_ON = "wifi.on";

    /**
     * Power consumed by wif batched scaning. Broken down into bins by Channels
     * Scanned per Hour. May do 1-720 scans per hour of 1-100 channels for a
     * range of 1-72,000. Going logrithmic (1-8, 9-64, 65-512, 513-4096, 4097-)!
     */
    public static final String POWER_WIFI_BATCHED_SCAN = "wifi.batchedscan";

    /**
     * Power consumption when WiFi driver is transmitting/receiving.
     */
    public static final String POWER_WIFI_ACTIVE = "wifi.active";

    /**
     * Power consumption when GPS is on.
     */
    public static final String POWER_GPS_ON = "gps.on";

    /**
     * Power consumption when Bluetooth driver is on.
     */
    public static final String POWER_BLUETOOTH_ON = "bluetooth.on";

    /**
     * Power consumption when Bluetooth driver is transmitting/receiving.
     */
    public static final String POWER_BLUETOOTH_ACTIVE = "bluetooth.active";

    /**
     * Power consumption when Bluetooth driver gets an AT command.
     */
    public static final String POWER_BLUETOOTH_AT_CMD = "bluetooth.at";

    /**
     * Power consumption when screen is on, not including the backlight power.
     */
    public static final String POWER_SCREEN_ON = "screen.on";

    /**
     * Power consumption when cell radio is on but not on a call.
     */
    public static final String POWER_RADIO_ON = "radio.on";

    /**
     * Power consumption when cell radio is hunting for a signal.
     */
    public static final String POWER_RADIO_SCANNING = "radio.scanning";

    /**
     * Power consumption when talking on the phone.
     */
    public static final String POWER_RADIO_ACTIVE = "radio.active";

    /**
     * Power consumption at full backlight brightness. If the backlight is at
     * 50% brightness, then this should be multiplied by 0.5
     */
    public static final String POWER_SCREEN_FULL = "screen.full";

    /**
     * Power consumed by the audio hardware when playing back audio content.
     * This is in addition to the CPU power, probably due to a DSP and / or
     * amplifier.
     */
    public static final String POWER_AUDIO = "dsp.audio";

    /**
     * Power consumed by any media hardware when playing back video content.
     * This is in addition to the CPU power, probably due to a DSP.
     */
    public static final String POWER_VIDEO = "dsp.video";
    public static final String POWER_CPU_IDLE = "cpu.idle";
    public static final String POWER_CPU_SPEEDS = "cpu.speeds";

    public static final String POWER_BATTERY_CAPACITY = "battery.capacity";

    /**
     * A constant indicating a partial wake lock timer.
     */
    public static final int WAKE_TYPE_PARTIAL = 0;

    /**
     * A constant indicating a full wake lock timer.
     */
    public static final int WAKE_TYPE_FULL = 1;

    /**
     * A constant indicating a window wake lock timer.
     */
    public static final int WAKE_TYPE_WINDOW = 2;

    /**
     * A constant indicating a sensor timer.
     */
    public static final int SENSOR = 3;

    /**
     * A constant indicating a a wifi running timer
     */
    public static final int WIFI_RUNNING = 4;

    /**
     * A constant indicating a full wifi lock timer
     */
    public static final int FULL_WIFI_LOCK = 5;

    /**
     * A constant indicating a scan wifi lock timer
     */
    public static final int SCAN_WIFI_LOCK = 6;

    /**
     * A constant indicating a wifi multicast timer
     */
    public static final int WIFI_MULTICAST_ENABLED = 7;

    /**
     * A constant indicating an audio turn on timer
     */
    public static final int AUDIO_TURNED_ON = 7;

    /**
     * A constant indicating a video turn on timer
     */
    public static final int VIDEO_TURNED_ON = 8;

    /**
     * Include all of the data in the stats, including previously saved data.
     */
    public static final int STATS_SINCE_CHARGED = 0;

    /**
     * Include only the last run in the stats.
     */
    public static final int STATS_LAST = 1;

    /**
     * Include only the current run in the stats.
     */
    public static final int STATS_CURRENT = 2;

    /**
     * Include only the run since the last time the device was unplugged in the
     * stats.
     */
    public static final int STATS_SINCE_UNPLUGGED = 3;

    /**
     * Defines the UID/GID for the WIFI supplicant process.
     *
     * @hide
     */
    public static final int WIFI_UID = 1010;

    /**
     * Defines the UID/GID for the Bluetooth service process.
     *
     * @hide
     */
    public static final int BLUETOOTH_UID = 1002;
    public static final int MSG_UPDATE_NAME_ICON = 1;
    private static final String TAG = "BatteryInfo";
    private static final boolean DEBUG = false;
    private static final int MIN_POWER_THRESHOLD = 5;
    private static final int NETWORK_MOBILE_RX_BYTES = 0; // received bytes
    // using mobile data

    private static final int NETWORK_MOBILE_TX_BYTES = 1; // transmitted bytes
    // using mobile data

    private static final int NETWORK_WIFI_RX_BYTES = 2; // received bytes using
    // wifi

    private static final int NETWORK_WIFI_TX_BYTES = 3; // transmitted bytes
    // using wifi
    final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
    private final List<BatterySipper> mUsageList = new ArrayList<BatterySipper>();
    private final List<BatterySipper> mWifiSippers = new ArrayList<BatterySipper>();
    private final List<BatterySipper> mBluetoothSippers = new ArrayList<BatterySipper>();
    private final SparseArray<List<BatterySipper>> mUserSippers = new SparseArray<List<BatterySipper>>();
    private final SparseArray<Double> mUserPower = new SparseArray<Double>();
    public double mMaxPower = 1;
    public int testType;
    private int mStatsType = 0;
    private Object mPowerProfile;
    private BatteryStatsImplNative mStats;
    private double mMinPercentOfTotal = 0.1d;
    private long mStatsPeriod = 0;
    private double mTotalPower;
    private double mWifiPower;
    private double mBluetoothPower;
    private long mRawRealtime;
    private long mRawUptime;
    private long mAppWifiRunning;
    private Context mContext;
    private long uSecTime;

    private UserManager mUm;

    public BatteryInfo(Context context) {
        testType = 1;
        mContext = context;

        mStatsType = BatteryStatsTypes.STATS_SINCE_CHARGED;
        BatteryStatsImplNative.invalidate();
        mUm = (UserManager) mContext.getSystemService(Context.USER_SERVICE);

    }

    public long getCalculatedTime() {
        uSecTime = SystemClock.elapsedRealtime() * 1000;
        if (mStats == null) {

            mStats = BatteryStatsImplNative.getInstance(mContext);
        }
        return mStats.computeBatteryRealtime(uSecTime, mStatsType);
    }

    public void setMinPercentOfTotal(double minPercentOfTotal) {
        this.mMinPercentOfTotal = minPercentOfTotal;
    }

    public double getTotalPower() {
        return mTotalPower;
    }

    public String getStatsPeriod() {
        return BatteryStatsUtils.formatElapsedTime(mContext, mStatsPeriod);
    }

    public List<BatterySipper> getBatteryStats() {
        mStats = null;
        if (mStats == null) {

            mStats = BatteryStatsImplNative.getInstance(mContext);
        }

        if (mStats == null) {
            return getAppListCpuTime();
        }

        mMaxPower = 0;
        mTotalPower = 0;
        mWifiPower = 0;
        mBluetoothPower = 0;
        mAppWifiRunning = 0;

        mUsageList.clear();
        mWifiSippers.clear();
        mBluetoothSippers.clear();
        mUserSippers.clear();
        mUserPower.clear();

        mRawUptime = SystemClock.uptimeMillis() * 1000;
        mRawRealtime = SystemClock.elapsedRealtime() * 1000;

        processAppUsage();
        processMiscUsage();

        final List<BatterySipper> list = new ArrayList<BatterySipper>();

        Collections.sort(mUsageList);
        for (BatterySipper sipper : mUsageList) {
            if (sipper.getValue() < MIN_POWER_THRESHOLD)
                continue;
            double percentOfTotal = ((sipper.getValue() / mTotalPower) * 100);
            if (percentOfTotal > 1)
                sipper.setPercent((int) (Math.ceil(percentOfTotal)));
            else
                sipper.setPercent(percentOfTotal);
            if (percentOfTotal < mMinPercentOfTotal)
                continue;
            list.add(sipper);
        }

        if (list.size() <= 1) {
            return getAppListCpuTime();
        }

        return list;
    }

    private long getAppProcessTime(int pid) {
        FileInputStream in = null;
        String ret = null;
        try {
            in = new FileInputStream("/proc/" + pid + "/stat");
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            ret = os.toString();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (ret == null) {
            return 0;
        }

        String[] s = ret.split(" ");
        if (s == null || s.length < 17) {
            return 0;
        }

        final long utime = string2Long(s[13]);
        final long stime = string2Long(s[14]);
        final long cutime = string2Long(s[15]);
        final long cstime = string2Long(s[16]);

        return utime + stime + cutime + cstime;
    }

    private long string2Long(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
        }
        return 0;
    }

    private List<BatterySipper> getAppListCpuTime() {
        testType = 2;
        final List<BatterySipper> list = new ArrayList<BatterySipper>();

        long totalTime = 0;
        ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();

        HashMap<Integer, BatterySipper> templist = new HashMap<Integer, BatterySipper>();
        for (RunningAppProcessInfo info : runningApps) {
            final long time = getAppProcessTime(info.pid);
            String[] pkgNames = info.pkgList;
            int pid = info.pid;
            if (pkgNames == null) {
                if (templist.containsKey(pid)) {
                    BatterySipper sipper = templist.get(pid);

                    sipper.setValue(sipper.getValue() + time);
                } else {
                    templist.put(pid, new BatterySipper(mContext,
                            info.processName, time));
                }
                totalTime += time;
            } else {
                for (String pkgName : pkgNames) {
                    if (templist.containsKey(pid)) {
                        BatterySipper sipper = templist.get(pid);
                        sipper.setValue(sipper.getValue() + time);
                    } else {
                        templist.put(pid, new BatterySipper(mContext, pkgName,
                                time));
                    }
                    totalTime += time;
                }
            }
        }

        if (totalTime == 0)
            totalTime = 1;

        list.addAll(templist.values());
        for (int i = list.size() - 1; i >= 0; i--) {
            BatterySipper sipper = list.get(i);
            double percentOfTotal = sipper.getValue() * 100 / totalTime;
            if (percentOfTotal < mMinPercentOfTotal) {
                list.remove(i);
            } else {
                sipper.setPercent(percentOfTotal);
            }
        }

        Collections.sort(list, new Comparator<BatterySipper>() {
            @Override
            public int compare(BatterySipper object1, BatterySipper object2) {
                double d1 = object1.getPercentOfTotal();
                double d2 = object2.getPercentOfTotal();
                if (d1 - d2 < 0) {
                    return 1;
                } else if (d1 - d2 > 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        return list;
    }

    boolean isWifiOnly(Context context) {
        boolean supportraido = false;
        ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] infos = connectivity.getAllNetworkInfo();
            if (infos != null && infos.length > 0) {
                for (NetworkInfo info : infos) {
                    if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        supportraido = true;
                        break;
                    }
                }
            }

        }

        return (supportraido == false);
    }

    private void processMiscUsage() {
        final int which = mStatsType;
        long uSecTime = SystemClock.elapsedRealtime() * 1000;
        final long uSecNow = mStats.computeBatteryRealtime(uSecTime, which);
        final long timeSinceUnplugged = uSecNow;
        if (DEBUG) {
            Log.i(TAG, "Uptime since last unplugged = "
                    + (timeSinceUnplugged / 1000));
            Log.i(TAG, "uSecNow = " + uSecNow);
        }
        addUserUsage();
        addPhoneUsage(uSecNow);
        addScreenUsage(uSecNow);
        addWiFiUsage(uSecNow);
        addBluetoothUsage(uSecNow);
        addIdleUsage(uSecNow); // Not including cellular idle power
        if (!isWifiOnly(mContext))
            addRadioUsage(uSecNow);
    }

    private void addUserUsage() {
        for (int i = 0; i < mUserSippers.size(); i++) {
            final int userId = mUserSippers.keyAt(i);
            final List<BatterySipper> sippers = mUserSippers.valueAt(i);
            Double userPower = mUserPower.get(userId);
            double power = (userPower != null) ? userPower : 0.0;
            BatterySipper bs = addEntry(DrainType.USER, 0, power);
            // bs.icon = null;
            aggregateSippers(bs, sippers, "User:" + String.valueOf(userId));
        }
    }

    private int getNumCpuClusters() {
        int speedsteps = 0;
        double batteryCapacity;
        if (mPowerProfile == null) {
            try {
                mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                        .getConstructor(Context.class).newInstance(mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mPowerProfile != null) {
            try {
                speedsteps = (Integer) Class.forName(POWER_PROFILE_CLASS)
                        .getMethod("getNumCpuClusters").invoke(mPowerProfile);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (DEBUG)
            Log.i(TAG, "speedsteps = " + speedsteps);
        return speedsteps;

    }

    private int getNumSpeedSteps() {
        int speedsteps = 0;
        double batteryCapacity;
        if (mPowerProfile == null) {
            try {
                mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                        .getConstructor(Context.class).newInstance(mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mPowerProfile != null) {
            try {
                speedsteps = (Integer) Class.forName(POWER_PROFILE_CLASS)
                        .getMethod("getNumSpeedSteps").invoke(mPowerProfile);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (DEBUG)
            Log.i(TAG, "speedsteps = " + speedsteps);
        return speedsteps;
    }

    public double getAveragePower(String type) {

        double batteryCapacity = 0;
        if (mPowerProfile == null) {
            try {
                mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                        .getConstructor(Context.class).newInstance(mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mPowerProfile != null) {
            try {
                batteryCapacity = (Double) Class.forName(POWER_PROFILE_CLASS)
                        .getMethod("getAveragePower", String.class)
                        .invoke(mPowerProfile, type);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return batteryCapacity;
    }

    private double getAveragePower(String type, int level) {
        if (DEBUG)
            Log.i(TAG, "begin getAveragePower, type = " + type + " , level = "
                    + level);
        double batteryCapacity = 0;
        if (mPowerProfile == null) {
            try {
                mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                        .getConstructor(Context.class).newInstance(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mPowerProfile != null) {
            try {
                batteryCapacity = (Double) Class
                        .forName(POWER_PROFILE_CLASS)
                        .getMethod("getAveragePower",
                                new Class<?>[]{String.class, int.class})
                        .invoke(mPowerProfile, new Object[]{type, level});

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (DEBUG)
            Log.i(TAG, "end getAveragePower batteryCapacity = "
                    + batteryCapacity);
        return batteryCapacity;
    }

    private void addPhoneUsage(long uSecNow) {
        long phoneOnTimeMs = mStats.getPhoneOnTime(uSecNow, mStatsType) / 1000;
        double phoneOnPower = getAveragePower(POWER_RADIO_ACTIVE)
                * phoneOnTimeMs / 1000;
        if (DEBUG)
            Log.i(TAG, "Phone power = " + phoneOnPower);
        addEntry(DrainType.PHONE, phoneOnTimeMs, phoneOnPower);
    }

    private void addScreenUsage(long uSecNow) {
        double power = 0;
        long screenOnTimeMs = mStats.getScreenOnTime(uSecNow, mStatsType) / 1000;
        power += screenOnTimeMs * getAveragePower(POWER_SCREEN_ON);
        final double screenFullPower = getAveragePower(POWER_SCREEN_FULL);
        for (int i = 0; i < 5; i++) {
            double screenBinPower = screenFullPower * (i + 0.5f) / 5;
            long brightnessTime = mStats.getScreenBrightnessTime(i, uSecNow,
                    mStatsType) / 1000;
            power += screenBinPower * brightnessTime;
            if (DEBUG) {
                Log.i(TAG, "Screen bin power = " + (int) screenBinPower
                        + ", time = " + brightnessTime);
            }
        }
        power /= 1000; // To seconds
        if (DEBUG)
            Log.i(TAG, "Screen power = " + power);
        addEntry(DrainType.SCREEN, screenOnTimeMs, power);
    }

    private void addWiFiUsage(long uSecNow) {
        if (!versionValid()) {// less than 2.3.3
            return;
        }

        long onTimeMs = mStats.getWifiOnTime(uSecNow, mStatsType) / 1000;
        long runningTimeMs = mStats.getGlobalWifiRunningTime(uSecNow,
                mStatsType) / 1000;
        if (DEBUG)
            Log.i(TAG, "WIFI runningTime=" + runningTimeMs
                    + " app runningTime=" + mAppWifiRunning);
        runningTimeMs -= mAppWifiRunning;
        if (runningTimeMs < 0)
            runningTimeMs = 0;
        double wifiPower = (onTimeMs * 0 * getAveragePower(POWER_WIFI_ON) + runningTimeMs
                * getAveragePower(POWER_WIFI_ON)) / 1000;
        if (DEBUG)
            Log.i(TAG, "WIFI power=" + wifiPower + " from procs=" + mWifiPower);
        BatterySipper bs = addEntry(DrainType.WIFI, runningTimeMs, wifiPower
                + mWifiPower);
        if (DEBUG)
            Log.i(TAG, "Wifi power = " + (wifiPower + mWifiPower));
        aggregateSippers(bs, mWifiSippers, "WIFI");
    }

    private void addBluetoothUsage(long uSecNow) {
        long btOnTimeMs = mStats.getBluetoothOnTime(uSecNow, mStatsType) / 1000;
        double btPower = btOnTimeMs * getAveragePower(POWER_BLUETOOTH_ON)
                / 1000;
        int btPingCount = mStats.getBluetoothPingCount();
        btPower += (btPingCount * getAveragePower(POWER_BLUETOOTH_AT_CMD)) / 1000;
        BatterySipper bs = addEntry(DrainType.BLUETOOTH, btOnTimeMs, btPower
                + mBluetoothPower);
        if (DEBUG)
            Log.i(TAG, "BT power = " + (btPower + mBluetoothPower));
        aggregateSippers(bs, mBluetoothSippers, "Bluetooth");
    }

    private void addIdleUsage(long uSecNow) {
        long idleTimeMs = (uSecNow - mStats
                .getScreenOnTime(uSecNow, mStatsType)) / 1000;
        double idlePower = (idleTimeMs * getAveragePower(POWER_CPU_IDLE)) / 1000;
        if (DEBUG)
            Log.i(TAG, "Idle power = " + (idlePower));
        addEntry(DrainType.IDLE, idleTimeMs, idlePower);
    }

    private void addRadioUsage(long uSecNow) {
        double power = 0;
        final int BINS = 5;
        long signalTimeMs = 0;
        for (int i = 0; i < BINS; i++) {
            long strengthTimeMs = mStats.getPhoneSignalStrengthTime(i, uSecNow,
                    mStatsType) / 1000;
            power += strengthTimeMs / 1000
                    * getAveragePower(POWER_RADIO_ON, i);
            signalTimeMs += strengthTimeMs;
        }
        long scanningTimeMs = mStats.getPhoneSignalScanningTime(uSecNow,
                mStatsType) / 1000;
        power += scanningTimeMs / 1000
                * getAveragePower(POWER_RADIO_SCANNING);
        BatterySipper bs = addEntry(DrainType.CELL, signalTimeMs, power);
        if (DEBUG)
            Log.i(TAG, "Radio power = " + (power));
        if (signalTimeMs != 0) {
            bs.noCoveragePercent = mStats.getPhoneSignalStrengthTime(0,
                    uSecNow, mStatsType) / 1000 * 100.0 / signalTimeMs;
        }
    }

    private void aggregateSippers(BatterySipper bs, List<BatterySipper> from,
                                  String tag) {
        for (int i = 0; i < from.size(); i++) {
            BatterySipper wbs = from.get(i);
            if (DEBUG)
                Log.i(TAG, tag + " adding sipper " + wbs + ": cpu="
                        + wbs.cpuTime);
            bs.cpuTime += wbs.cpuTime;
            bs.gpsTime += wbs.gpsTime;
            bs.wifiRunningTime += wbs.wifiRunningTime;
            bs.cpuFgTime += wbs.cpuFgTime;
            bs.wakeLockTime += wbs.wakeLockTime;
            // bs.tcpBytesReceived += wbs.tcpBytesReceived;
            // bs.tcpBytesSent += wbs.tcpBytesSent;
            bs.mobileRxBytes += wbs.mobileRxBytes;
            bs.mobileTxBytes += wbs.mobileTxBytes;
            bs.wifiRxBytes += wbs.wifiRxBytes;
            bs.wifiTxBytes += wbs.wifiTxBytes;
        }
    }

    private BatterySipper addEntry(int drainType, long time, double power) {
        if (power > mMaxPower)
            mMaxPower = power;
        mTotalPower += power;
        BatterySipper bs = new BatterySipper(mContext, drainType, null,
                new double[]{power});
        bs.usageTime = time;
        mUsageList.add(bs);
        return bs;
    }

    private boolean versionValid() {
        return Build.VERSION.SDK_INT >= 10;// less than 2.3.3
    }

    private void processAppUsageM() {
        final int which = mStatsType;

    }

    private void processAppUsage() {
        SensorManager sensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        long appWakelockTime = 0;
        BatterySipper osApp = null;

        final int which = mStatsType;
        final int speedSteps = getNumSpeedSteps();
        final double mobilePowerPerByte = getMobilePowerPerByte();
        final double wifiPowerPerByte = getWifiPowerPerByte();
        final double[] powerCpuNormal = new double[speedSteps];
        final long[] cpuSpeedStepTimes = new long[speedSteps];
        for (int p = 0; p < speedSteps; p++) {
            powerCpuNormal[p] = getAveragePower(POWER_CPU_ACTIVE, p);
        }

        final double averageCostPerByte = getAverageDataCost();
        if (DEBUG)
            Log.i(TAG, "averageCostPerByte = " + averageCostPerByte);
        uSecTime = mStats.computeBatteryRealtime(
                SystemClock.elapsedRealtime() * 1000, which);

        mStatsPeriod = uSecTime;

        SparseArray<? extends Object> uidStats = mStats.getUidStats();
        if (uidStats == null) {
            return;
        }
        String processname = null;
        final int NU = uidStats.size();
        try {
            ClassLoader cl = mContext.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class iBatteryStatsUid = cl
                    .loadClass("com.android.internal.os.BatteryStatsImpl$Uid");
            for (int iu = 0; iu < NU; iu++) {
                Object u = uidStats.valueAt(iu);
                double power = 0;
                double highestDrain = 0;
                String packageWithHighestDrain = null;
                Method methodGetProcessStats = iBatteryStatsUid
                        .getMethod("getProcessStats");

                Method methodGetUid = iBatteryStatsUid.getMethod("getUid");
                int uid = (Integer) methodGetUid.invoke(u);

                Map<String, ? extends Object> processStats = (Map<String, ? extends Object>) methodGetProcessStats
                        .invoke(u);// u.getProcessStats();
                long cpuTime = 0;
                long cpuFgTime = 0;
                long wakelockTime = 0;
                long gpsTime = 0;

                if (processStats != null && processStats.size() > 0) {
                    // Log.i(TAG, "processStats.size() = " +
                    // processStats.size());
                    // 1, Process CPU time
                    for (Map.Entry<String, ? extends Object> ent : processStats
                            .entrySet()) {
                        if (DEBUG)
                            Log.i(TAG, "Process name = " + ent.getKey());
                        processname = ent.getKey();
                        if (processname.equals("com.honeywell.deviceacuity"))
                            Log.i(TAG, "test");
                        Object ps = ent.getValue();
                        Class batteryStatsUidProc = cl
                                .loadClass("com.android.internal.os.BatteryStatsImpl$Uid$Proc");

                        // Parameters Types
                        @SuppressWarnings("rawtypes")
                        Class[] paramTypesGetXxxTime = new Class[1];
                        paramTypesGetXxxTime[0] = int.class;

                        Class[] ParamTypesGetCPUSpeed = new Class[2];
                        ParamTypesGetCPUSpeed[0] = int.class;
                        ParamTypesGetCPUSpeed[1] = int.class;

                        Method methodGetUserTime = batteryStatsUidProc
                                .getMethod("getUserTime", paramTypesGetXxxTime);
                        Method methodGetSystemTime = batteryStatsUidProc
                                .getMethod("getSystemTime",
                                        paramTypesGetXxxTime);
                        Method methodGetForegroundTime = batteryStatsUidProc
                                .getMethod("getForegroundTime",
                                        paramTypesGetXxxTime);
                        Method methodGetTimeAtCpuSpeedStep = batteryStatsUidProc
                                .getMethod("getTimeAtCpuSpeedStep",
                                        ParamTypesGetCPUSpeed);

                        Object[] paramsGetXxxTime = new Object[1];
                        paramsGetXxxTime[0] = new Integer(which);

                        final long userTime = (Long) methodGetUserTime.invoke(
                                ps, paramsGetXxxTime);// ps.getUserTime(which);
                        final long systemTime = (Long) methodGetSystemTime
                                .invoke(ps, paramsGetXxxTime);// ps.getSystemTime(which);
                        final long foregroundTime = (Long) methodGetForegroundTime
                                .invoke(ps, paramsGetXxxTime);// ps.getForegroundTime(which);
                        cpuFgTime += foregroundTime * 10; // convert to millis
                        final long tmpCpuTime = (userTime + systemTime) * 10; // convert
                        // to
                        // millis
                        int totalTimeAtSpeeds = 0;
                        // Get the total first

                        for (int step = 0; step < speedSteps; step++) {
                            Object[] paraCUPSpped = new Object[2];
                            paraCUPSpped[0] = new Integer(step);
                            paraCUPSpped[1] = new Integer(which);
                            cpuSpeedStepTimes[step] = (Long) methodGetTimeAtCpuSpeedStep
                                    .invoke(ps, paraCUPSpped);// ps.getTimeAtCpuSpeedStep(step,
                            // which);
                            totalTimeAtSpeeds += cpuSpeedStepTimes[step];
                        }
                        if (totalTimeAtSpeeds == 0)
                            totalTimeAtSpeeds = 1;
                        // Then compute the ratio of time spent at each speed
                        double processPower = 0;
                        for (int step = 0; step < speedSteps; step++) {
                            double ratio = (double) cpuSpeedStepTimes[step]
                                    / totalTimeAtSpeeds;
                            processPower += ratio * tmpCpuTime
                                    * powerCpuNormal[step];
                        }
                        cpuTime += tmpCpuTime;
                        power += processPower;
                        if (packageWithHighestDrain == null
                                || packageWithHighestDrain.startsWith("*")) {
                            highestDrain = processPower;
                            packageWithHighestDrain = ent.getKey();
                        } else if (highestDrain < processPower
                                && !ent.getKey().startsWith("*")) {
                            highestDrain = processPower;
                            packageWithHighestDrain = ent.getKey();
                        }
                    }
                }
                if (cpuFgTime > cpuTime) {
                    if (DEBUG && cpuFgTime > cpuTime + 10000) {
                        Log.i(TAG,
                                "WARNING! Cputime is more than 10 seconds behind Foreground time");
                    }
                    cpuTime = cpuFgTime; // Statistics may not have been
                    // gathered yet.
                }
                power /= 1000;
                if (DEBUG)
                    Log.i(TAG, "app cup power = " + power + " , cup time = "
                            + cpuTime);

                // 2, Process wake lock usage
                Method methodGetWakelockStats = iBatteryStatsUid
                        .getMethod("getWakelockStats");
                Map<String, ? extends Object> wakelockStats = (Map<String, ? extends Object>) methodGetWakelockStats
                        .invoke(u);// u.getWakelockStats();
                for (Map.Entry<String, ? extends Object> wakelockEntry : wakelockStats
                        .entrySet()) {
                    Object wakelock = wakelockEntry.getValue();
                    // Only care about partial wake locks since full wake locks
                    // are canceled when the user turns the screen off.
                    @SuppressWarnings("rawtypes")
                    Class batteryStatsUidWakelock = cl
                            .loadClass("com.android.internal.os.BatteryStatsImpl$Uid$Wakelock");

                    // Parameters Types
                    @SuppressWarnings("rawtypes")
                    Class[] paramTypesGetWakeTime = new Class[1];
                    paramTypesGetWakeTime[0] = int.class;

                    Method methodGetWakeTime = batteryStatsUidWakelock
                            .getMethod("getWakeTime", paramTypesGetWakeTime);

                    // Parameters
                    Object[] paramsGetWakeTime = new Object[1];

                    // Partial wake locks BatteryStatsTypes.WAKE_TYPE_PARTIAL
                    // are the ones that should normally be of interest but
                    // WAKE_TYPE_PARTIAL, WAKE_TYPE_FULL, WAKE_TYPE_WINDOW
                    // are possible
                    paramsGetWakeTime[0] = Integer
                            .valueOf(WAKE_TYPE_PARTIAL);

                    Object timer = methodGetWakeTime.invoke(wakelock,
                            paramsGetWakeTime);// wakelock.getWakeTime(Constants.WAKE_TYPE_PARTIAL);
                    if (timer != null) {
                        @SuppressWarnings("rawtypes")
                        Class iBatteryStatsTimer = cl
                                .loadClass("com.android.internal.os.BatteryStatsImpl$Timer");

                        // Parameters Types
                        @SuppressWarnings("rawtypes")
                        Class[] paramTypesGetTotalTimeLocked = new Class[2];
                        paramTypesGetTotalTimeLocked[0] = long.class;
                        paramTypesGetTotalTimeLocked[1] = int.class;

                        Method methodGetTotalTimeLocked = iBatteryStatsTimer
                                .getMethod("getTotalTimeLocked",
                                        paramTypesGetTotalTimeLocked);

                        // Parameters
                        Object[] paramsGetTotalTimeLocked = new Object[2];
                        paramsGetTotalTimeLocked[0] = new Long(uSecTime);
                        paramsGetTotalTimeLocked[1] = Integer.valueOf(which);
                        Long wake = (Long) methodGetTotalTimeLocked.invoke(
                                timer, paramsGetTotalTimeLocked);
                        wakelockTime += wake;// timer.getTotalTimeLocked(uSecTime,
                        // which);
                    }
                }
                wakelockTime /= 1000; // convert to millis
                appWakelockTime += wakelockTime;
                // Add cost of holding a wake lock
                double wakepower = (wakelockTime * getAveragePower(POWER_CPU_AWAKE)) / 1000;
                power += wakepower;
                if (DEBUG)
                    Log.i(TAG, "app wakelockTime power = " + wakepower
                            + " , wakelockTime time = " + wakelockTime);
                // 3, Add cost of data traffic

                @SuppressWarnings("rawtypes")
                Class[] paramTypesGetNetworkActivity = new Class[]{int.class,
                        int.class};
                Method methodGetNetworkActivity = iBatteryStatsUid
                        .getMethod("getNetworkActivityCount",
                                paramTypesGetNetworkActivity);
                // Parameters for getting received bytes from mobile
                Object paramGetNetworkActivityCount[] = {
                        NETWORK_MOBILE_RX_BYTES, mStatsType};
                final long mobileRx = (Long) methodGetNetworkActivity.invoke(u,
                        paramGetNetworkActivityCount);

                paramGetNetworkActivityCount[0] = NETWORK_MOBILE_TX_BYTES;
                final long mobileTx = (Long) methodGetNetworkActivity.invoke(u,
                        paramGetNetworkActivityCount);
                power += (mobileRx + mobileTx) * mobilePowerPerByte;

                // change parameter to get received bytes from wifi
                paramGetNetworkActivityCount[0] = NETWORK_WIFI_RX_BYTES;
                // Add wifi
                final long wifiRx = (Long) methodGetNetworkActivity.invoke(u,
                        paramGetNetworkActivityCount);
                paramGetNetworkActivityCount[0] = NETWORK_WIFI_TX_BYTES;
                final long wifiTx = (Long) methodGetNetworkActivity.invoke(u,
                        paramGetNetworkActivityCount);

                power += (wifiRx + wifiTx) * wifiPowerPerByte;

                // 4.1 Add cost of keeping WIFI running.
                if (versionValid()) {
                    long wifiRunningTimeMs = mStats.getWifiRunningTime(
                            mContext, u, uSecTime, which) / 1000;// u.getWifiRunningTime(uSecTime,
                    // which) / 1000;
                    mAppWifiRunning += wifiRunningTimeMs;
                    power += (wifiRunningTimeMs * getAveragePower(POWER_WIFI_ON)) / 1000;
                }
                // 4.2 Add cost of WIFI scans
                long wifiScanTimeMs = mStats.getWifiScanTime(mContext, u,
                        uSecTime, which) / 1000;
                power += (wifiScanTimeMs * getAveragePower(POWER_WIFI_SCAN)) / 1000;
                final int NUM_WIFI_BATCHED_SCAN_BINS = 5;
                for (int bin = 0; bin < NUM_WIFI_BATCHED_SCAN_BINS; bin++) {
                    long batchScanTimeMs = mStats.getWifiBatchedScanTime(
                            mContext, u, bin, uSecTime, which) / 1000;
                    power += (batchScanTimeMs * getAveragePower(
                            POWER_WIFI_BATCHED_SCAN, bin));
                }

                // 5, Process Sensor usage
                Method methodGetSensroStats = iBatteryStatsUid
                        .getMethod("getSensorStats");
                Map<Integer, ? extends Object> sensorStats = (Map<Integer, ? extends Object>) methodGetSensroStats
                        .invoke(u);// u.getSensorStats();
                for (Map.Entry<Integer, ? extends Object> sensorEntry : sensorStats
                        .entrySet()) {
                    Object sensor = sensorEntry.getValue();
                    @SuppressWarnings("rawtypes")
                    Class iBatteryStatsSensor = cl
                            .loadClass("com.android.internal.os.BatteryStatsImpl$Uid$Sensor");
                    Method mSensorGetSensorHandle = iBatteryStatsSensor
                            .getMethod("getHandle");
                    Method mSensorGetSensorTime = iBatteryStatsSensor
                            .getMethod("getSensorTime");
                    int sensorHandle = (Integer) mSensorGetSensorHandle
                            .invoke(sensor);// sensor.getHandle();
                    Object timer = mSensorGetSensorTime.invoke(sensor);// sensor.getSensorTime();
                    @SuppressWarnings("rawtypes")
                    Class iBatteryStatsTimer = cl
                            .loadClass("com.android.internal.os.BatteryStatsImpl$Timer");

                    // Parameters Types
                    @SuppressWarnings("rawtypes")
                    Class[] paramTypesGetTotalTimeLocked = new Class[2];
                    paramTypesGetTotalTimeLocked[0] = long.class;
                    paramTypesGetTotalTimeLocked[1] = int.class;

                    Method methodGetTotalTimeLocked = iBatteryStatsTimer
                            .getMethod("getTotalTimeLocked",
                                    paramTypesGetTotalTimeLocked);

                    // Parameters
                    Object[] paramsGetTotalTimeLocked = new Object[2];
                    paramsGetTotalTimeLocked[0] = new Long(uSecTime);
                    paramsGetTotalTimeLocked[1] = Integer.valueOf(which);
                    Long sensorTime = (Long) methodGetTotalTimeLocked.invoke(
                            timer, paramsGetTotalTimeLocked) / 1000;
                    // long sensorTime = timer.getTotalTimeLocked(uSecTime,
                    // which) / 1000;
                    double multiplier = 0;
                    switch (sensorHandle) {
                        case -10000: // gps
                            multiplier = getAveragePower(POWER_GPS_ON);
                            gpsTime = sensorTime;
                            break;
                        default:
                            Class Sensorclass = cl
                                    .loadClass("android.hardware.Sensor");
                            Method SensorHandle = Sensorclass
                                    .getMethod("getHandle");
                            List<Sensor> sensorList = sensorManager
                                    .getSensorList(Sensor.TYPE_ALL);
                            for (Sensor s : sensorList) {

                                int shandler = (Integer) SensorHandle.invoke(s);// sensor.getHandle();
                                if (shandler == sensorHandle) {
                                    multiplier = s.getPower();
                                    break;
                                }
                            }
                    }
                    power += (multiplier * sensorTime) / 1000;
                    if (DEBUG)
                        Log.i(TAG, processname + "  power = " + power);
                }

                // Add the app to the list if it is consuming power
                boolean isOtherUser = false;
                Class<?> UserHandle = Class.forName("android.os.UserHandle");
                Method getUserId = UserHandle.getMethod("getUserId", int.class);
                final int userId = (Integer) getUserId.invoke(null, uid);
                Method myUserIdmthod = UserHandle.getMethod("myUserId");
                int myUserId = (Integer) myUserIdmthod.invoke(null);
                Method getAppId = UserHandle.getMethod("getAppId", int.class);
                int appId = (Integer) getAppId.invoke(null, uid);
                if (power != 0 || uid == 0) {
                    if (DEBUG)
                        Log.i(TAG, processname + " TOTAL power = " + power
                                + " , mStatsPeriod = " + mStatsPeriod);

                    BatterySipper app = new BatterySipper(mContext,
                            DrainType.APP, u, new double[]{power});
                    app.cpuTime = cpuTime;
                    app.gpsTime = gpsTime;
                    // app.wifiRunningTime = wifiRunningTimeMs;
                    app.cpuFgTime = cpuFgTime;
                    app.wakeLockTime = wakelockTime;
                    app.wakeLockTime = wakelockTime;
                    app.mobileRxBytes = mobileRx;
                    app.mobileTxBytes = mobileTx;
                    app.wifiRxBytes = wifiRx;
                    app.wifiTxBytes = wifiTx;
                    if (app.getName() == null) {
                        if (processname != null)

                            app.setName(processname);
                        else
                            app.setName(String.valueOf(uid));
                    }
                    // app.tcpBytesReceived = tcpBytesReceived;
                    // app.tcpBytesSent = tcpBytesSent;
                    // wifi
                    if (uid == WIFI_UID) {
                        mWifiSippers.add(app);
                    } else if (uid == BLUETOOTH_UID) {
                        mBluetoothSippers.add(app);
                    } else if (userId != myUserId
                            && appId >= android.os.Process.FIRST_APPLICATION_UID) {
                        isOtherUser = true;
                        List<BatterySipper> list = mUserSippers.get(userId);
                        if (list == null) {
                            list = new ArrayList<BatterySipper>();
                            mUserSippers.put(userId, list);
                        }
                        list.add(app);
                    } else {
                        mUsageList.add(app);
                    }
                    if (uid == 0) {
                        osApp = app;
                    }
                }
                if (power != 0) {
                    if (uid == 1010) {
                        mWifiPower += power;
                    } else if (uid == 1002) {
                        mBluetoothPower += power;
                    } else if (isOtherUser) {
                        Double userPower = mUserPower.get(userId);
                        if (userPower == null) {
                            userPower = power;
                        } else {
                            userPower += power;
                        }
                        mUserPower.put(userId, userPower);
                    } else {
                        if (power > mMaxPower)
                            mMaxPower = power;
                        mTotalPower += power;
                    }
                }

                if (DEBUG)
                    Log.i(TAG, "Added power = " + power);
            }

            if (osApp != null) {
                long wakeTimeMillis = mStats.computeBatteryUptime(
                        SystemClock.uptimeMillis() * 1000, which) / 1000;
                wakeTimeMillis -= appWakelockTime
                        + (mStats.getScreenOnTime(
                        SystemClock.elapsedRealtime(), which) / 1000);
                if (wakeTimeMillis > 0) {
                    double power = (wakeTimeMillis * getAveragePower(POWER_CPU_AWAKE)) / 1000;
                    Log.i(TAG, "OS wakeLockTime " + wakeTimeMillis + " power "
                            + power);
                    osApp.wakeLockTime += wakeTimeMillis;
                    osApp.value += power;
                    osApp.values[0] += power;
                    if (osApp.value > mMaxPower)
                        mMaxPower = osApp.value;
                    mTotalPower += power;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    public double getLeftpower() {
        return getAveragePower(POWER_BATTERY_CAPACITY);
    }

    /**
     * Return estimated power (in mAs) of sending a byte with the mobile radio.
     */
    private double getMobilePowerPerByte() {
        final long MOBILE_BPS = 200000;
        final double MOBILE_POWER = getAveragePower(POWER_RADIO_ACTIVE) / 3600;
        final long mobileRx = mStats.getNetworkActivityCount(
                NETWORK_MOBILE_RX_BYTES, mStatsType);
        final long mobileTx = mStats.getNetworkActivityCount(
                NETWORK_MOBILE_TX_BYTES, mStatsType);
        final long mobileData = mobileRx + mobileTx;
        final long radioDataUptimeMs = mStats.getRadioDataUptime() / 1000;
        final long mobileBps = radioDataUptimeMs != 0 ? mobileData * 8 * 1000
                / radioDataUptimeMs : MOBILE_BPS;
        return MOBILE_POWER / (mobileBps / 8);
    }

    /**
     * Return estimated power (in mAs) of sending a byte with the Wi-Fi radio.
     */
    private double getWifiPowerPerByte() {
        final long WIFI_BPS = 1000000; // TODO: Extract average bit rates from
        // system
        final double WIFI_POWER = getAveragePower(POWER_WIFI_ACTIVE) / 3600;
        return WIFI_POWER / (WIFI_BPS / 8);
    }

    private double getAverageDataCost() {
        final long WIFI_BPS = 1000000; // TODO: Extract average bit rates from
        // system
        final long MOBILE_BPS = 200000; // TODO: Extract average bit rates from
        // system
        final double WIFI_POWER = getAveragePower(POWER_WIFI_ACTIVE) / 3600;
        final double MOBILE_POWER = getAveragePower(POWER_RADIO_ACTIVE) / 3600;

        long mobileData = 0;
        long wifiData = 0;
        if (Build.VERSION.SDK_INT < 19) {
            mobileData = mStats.getMobileTcpBytesReceived(mStatsType)
                    + mStats.getMobileTcpBytesSent(mStatsType);
            wifiData = mStats.getTotalTcpBytesReceived(mStatsType)
                    + mStats.getTotalTcpBytesSent(mStatsType) - mobileData;
        } else {
            mobileData = mStats.getNetworkActivityCount(
                    NETWORK_MOBILE_RX_BYTES, mStatsType)
                    + mStats.getNetworkActivityCount(NETWORK_MOBILE_TX_BYTES,
                    mStatsType);
            wifiData = mStats.getNetworkActivityCount(NETWORK_WIFI_RX_BYTES,
                    mStatsType)
                    + mStats.getNetworkActivityCount(NETWORK_WIFI_TX_BYTES,
                    mStatsType);
        }

        final long radioDataUptimeMs = mStats.getRadioDataUptime() / 1000;
        final long mobileBps = radioDataUptimeMs != 0 ? mobileData * 8 * 1000
                / radioDataUptimeMs : MOBILE_BPS;

        double mobileCostPerByte = MOBILE_POWER / (mobileBps / 8);
        double wifiCostPerByte = WIFI_POWER / (WIFI_BPS / 8);

        if (wifiData + mobileData != 0) {
            return (mobileCostPerByte * mobileData + wifiCostPerByte * wifiData)
                    / (mobileData + wifiData);
        } else {
            return 0;
        }
    }

}
