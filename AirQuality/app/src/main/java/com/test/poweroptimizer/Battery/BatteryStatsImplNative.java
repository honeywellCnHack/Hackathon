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
//  @ File Name : BatteryStatsImpl.java
//  @ Date : 29/08/2016
//  @ Author(s) : Sand(E547883)
//
//
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- */

package com.test.poweroptimizer.Battery;

import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BatteryStatsImplNative {
    private static final String TAG = "BatteryStatsProxy";
    /**
     * Type to be passed to getNetworkActivityCount for different stats.
     */
    private static final int NETWORK_MOBILE_RX_BYTES = 0; // received bytes
    private static final int NETWORK_MOBILE_TX_BYTES = 1; // transmitted bytes
    private static final int NETWORK_WIFI_RX_BYTES = 2; // received bytes using
    private static final int NETWORK_WIFI_TX_BYTES = 3; // transmitted bytes
    // using mobile data
    /**
     * An instance to the UidNameResolver
     */
    private static BatteryStatsImplNative m_proxy = null;
    // using mobile data
    /*
     * Instance of the BatteryStatsImpl
     */
    private Object m_Instance = null;
    // wifi
    @SuppressWarnings("rawtypes")
    private Class m_ClassDefinition = null;
    // using wifi
    private boolean LOG = false;
    /*
     * The UID stats are kept here as their methods / data can not be accessed
     * outside of this class due to non-public types (Uid, Proc, etc.)
     */
    private SparseArray<? extends Object> m_uidStats = null;

    /**
     * Default cctor
     */
    private BatteryStatsImplNative(Context context) {
        /*
         * As BatteryStats is a service we need to get a binding using the
         * IBatteryStats.Stub.getStatistics() method (using reflection). If we
         * would be using a public API the code would look like:
         *
         * @see com.android.settings.fuelgauge.PowerUsageSummary.java protected
         * void onCreate(Bundle icicle) { super.onCreate(icicle);
         *
         * mStats = (BatteryStatsImpl)getLastNonConfigurationInstance();
         *
         * addPreferencesFromResource(R.xml.power_usage_summary); mBatteryInfo =
         * IBatteryStats.Stub.asInterface(
         * ServiceManager.getService("batteryinfo")); mAppListGroup =
         * (PreferenceGroup) findPreference("app_list"); mPowerProfile = new
         * PowerProfile(this); }
         *
         * followed by private void load() { try { byte[] data =
         * mBatteryInfo.getStatistics(); Parcel parcel = Parcel.obtain();
         * parcel.unmarshall(data, 0, data.length); parcel.setDataPosition(0);
         * mStats = com.android.internal.os.BatteryStatsImpl.CREATOR
         * .createFromParcel(parcel);
         * mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED); }
         * catch (RemoteException e) { Log.e(TAG, "RemoteException:", e); } }
         */

        try {
            ClassLoader cl = context.getClassLoader();

            m_ClassDefinition = cl
                    .loadClass("com.android.internal.os.BatteryStatsImpl");

            // get the IBinder to the "batteryinfo" service
            @SuppressWarnings("rawtypes")
            Class serviceManagerClass = cl
                    .loadClass("android.os.ServiceManager");

            // parameter types
            @SuppressWarnings("rawtypes")
            Class[] paramTypesGetService = new Class[1];
            paramTypesGetService[0] = String.class;

            @SuppressWarnings("unchecked")
            Method methodGetService = serviceManagerClass.getMethod(
                    "getService", paramTypesGetService);

            String service = "";
            if (Build.VERSION.SDK_INT >= 19) {
                // kitkat and following
                service = "batterystats";
            } else {
                service = "batteryinfo";
            }
            // parameters
            Object[] paramsGetService = new Object[1];
            paramsGetService[0] = service;

            if (LOG) {
                Log.i(TAG, "invoking android.os.ServiceManager.getService: "
                        + service);
            }
            IBinder serviceBinder = (IBinder) methodGetService.invoke(
                    serviceManagerClass, paramsGetService);

            if (LOG) {
                Log.i(TAG,
                        "android.os.ServiceManager.getService(\"batteryinfo\") returned a service binder");
            }

            // now we have a binder. Let's us that on
            // IBatteryStats.Stub.asInterface
            // to get an IBatteryStats
            // Note the $-syntax here as Stub is a nested class
            @SuppressWarnings("rawtypes")
            Class iBatteryStatsStub = cl
                    .loadClass("com.android.internal.app.IBatteryStats$Stub");

            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypesAsInterface = new Class[1];
            paramTypesAsInterface[0] = IBinder.class;

            @SuppressWarnings("unchecked")
            Method methodAsInterface = iBatteryStatsStub.getMethod(
                    "asInterface", paramTypesAsInterface);

            // Parameters
            Object[] paramsAsInterface = new Object[1];
            paramsAsInterface[0] = serviceBinder;

            if (LOG) {
                Log.i(TAG,
                        "invoking com.android.internal.app.IBatteryStats$Stub.asInterface");
            }
            Object mBatteryInfo = methodAsInterface.invoke(iBatteryStatsStub,
                    paramsAsInterface);

            // and finally we call getStatistics from that IBatteryStats to
            // obtain a Parcel
            @SuppressWarnings("rawtypes")
            Class iBatteryStats = cl
                    .loadClass("com.android.internal.app.IBatteryStats");

            @SuppressWarnings("unchecked")
            Method methodGetStatistics = iBatteryStats
                    .getMethod("getStatistics");

            if (LOG) {
                Log.i(TAG, "invoking getStatistics");
            }
            byte[] data = (byte[]) methodGetStatistics.invoke(mBatteryInfo);

            if (LOG) {
                Log.i(TAG, "retrieving parcel");
            }

            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);

            @SuppressWarnings("rawtypes")
            Class batteryStatsImpl = cl
                    .loadClass("com.android.internal.os.BatteryStatsImpl");

            if (LOG) {
                Log.i(TAG, "reading CREATOR field");
            }
            Field creatorField = batteryStatsImpl.getField("CREATOR");

            // From here on we don't need reflection anymore
            @SuppressWarnings("rawtypes")
            Parcelable.Creator batteryStatsImpl_CREATOR = (Parcelable.Creator) creatorField
                    .get(batteryStatsImpl);

            m_Instance = batteryStatsImpl_CREATOR.createFromParcel(parcel);

            Class[] paramTypes = new Class[1];
            paramTypes[0] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod("distributeWorkLocked",
                    paramTypes);

            // Parameters
            Object[] params = new Object[1];
            params[0] = BatteryStatsTypes.STATS_SINCE_CHARGED;
            method.invoke(m_Instance, params);

        } catch (Exception e) {
            if (e instanceof InvocationTargetException && e.getCause() != null) {
                Log.e(TAG,
                        "An exception occured in BatteryStatsProxy(). Message: "
                                + e.getCause().getMessage());
            } else {
                Log.e(TAG,
                        "An exception occured in BatteryStatsProxy(). Message: "
                                + e.getMessage());
            }
            m_Instance = null;

        }
    }

    synchronized public static BatteryStatsImplNative getInstance(Context ctx) {
        if (m_proxy == null) {
            m_proxy = new BatteryStatsImplNative(ctx);
        }

        return m_proxy;
    }

    public static void invalidate() {
        m_proxy = null;
    }

    /**
     * Returns true if the proxy could not be initialized properly
     *
     * @return true if the proxy wasn't initialized
     */
    public boolean initFailed() {
        return m_Instance == null;
    }

    /**
     * Returns the total, last, or current battery realtime in microseconds.
     *
     * @param curTime    the current elapsed realtime in microseconds.
     * @param iStatsType one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long computeBatteryRealtime(long curTime, int iStatsType) {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = long.class;
            paramTypes[1] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod(
                    "computeBatteryRealtime", paramTypes);

            // Parameters
            Object[] params = new Object[2];
            params[0] = new Long(curTime);
            params[1] = new Integer(iStatsType);

            ret = (Long) method.invoke(m_Instance, params);

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            ret = new Long(0);

        }

        return ret;

    }

    /**
     * Returns the total, last, or current battery realtime in microseconds.
     *
     * @param curTime    the current elapsed realtime in microseconds.
     * @param iStatsType one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getBatteryRealtime(long curTime)
            throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[1];
            paramTypes[0] = long.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod("getBatteryRealtime",
                    paramTypes);

            // Parameters
            Object[] params = new Object[1];
            params[0] = new Long(curTime);

            ret = (Long) method.invoke(m_Instance, params);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            ret = new Long(0);
            throw new BatteryInfoUnavailableException();
        }

        return ret;

    }

    /**
     * Returns the total, last, or current battery uptime in microseconds.
     *
     * @param curTime    the current elapsed realtime in microseconds.
     * @param iStatsType one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long computeBatteryUptime(long curTime, int iStatsType)
            throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = long.class;
            paramTypes[1] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod("computeBatteryUptime",
                    paramTypes);

            // Parameters
            Object[] params = new Object[2];
            params[0] = new Long(curTime);
            params[1] = new Integer(iStatsType);

            ret = (Long) method.invoke(m_Instance, params);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            ret = new Long(0);
            throw new BatteryInfoUnavailableException();
        }

        return ret;

    }

    /**
     * Returns the total, last, or current screen on time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getScreenOnTime(long batteryRealtime, int iStatsType) {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = long.class;
            paramTypes[1] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod("getScreenOnTime",
                    paramTypes);

            // Parameters
            Object[] params = new Object[2];
            params[0] = new Long(batteryRealtime);
            params[1] = new Integer(iStatsType);

            ret = (Long) method.invoke(m_Instance, params);

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            ret = new Long(0);

        }

        return ret;

    }

    /**
     * Returns if phone is on battery.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public boolean getIsOnBattery() throws BatteryInfoUnavailableException {
        boolean ret = true;

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = long.class;
            paramTypes[1] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod("getIsOnBattery",
                    paramTypes);

            ret = (Boolean) method.invoke(m_Instance);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            ret = true;
            throw new BatteryInfoUnavailableException();
        }

        return ret;

    }

    /**
     * Returns the total, last, or current phone on time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getPhoneOnTime(long batteryRealtime, int iStatsType) {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = long.class;
            paramTypes[1] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod("getPhoneOnTime",
                    paramTypes);

            // Parameters
            Object[] params = new Object[2];
            params[0] = new Long(batteryRealtime);
            params[1] = new Integer(iStatsType);

            ret = (Long) method.invoke(m_Instance, params);

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            ret = new Long(0);

        }

        return ret;

    }

    /**
     * Returns the total, last, or current wifi on time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getWifiOnTime(long batteryRealtime, int iStatsType) {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = long.class;
            paramTypes[1] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod("getWifiOnTime",
                    paramTypes);

            // Parameters
            Object[] params = new Object[2];
            params[0] = new Long(batteryRealtime);
            params[1] = new Integer(iStatsType);

            ret = (Long) method.invoke(m_Instance, params);

            if (LOG) {
                Log.i(TAG, "getWifiOnTime with params " + params[0] + " and "
                        + params[1] + " returned " + ret);
            }

        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            ret = new Long(0);
        }

        return ret;

    }

    /**
     * Returns the total, last, or current wifi on time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getGlobalWifiRunningTime(long batteryRealtime, int iStatsType) {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = long.class;
            paramTypes[1] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod(
                    "getGlobalWifiRunningTime", paramTypes);

            // Parameters
            Object[] params = new Object[2];
            params[0] = new Long(batteryRealtime);
            params[1] = new Integer(iStatsType);

            ret = (Long) method.invoke(m_Instance, params);

            if (LOG) {
                Log.i(TAG, "getGlobalWifiRunningTime with params " + params[0]
                        + " and " + params[1] + " returned " + ret);
            }

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            ret = new Long(0);
        }

        return ret;

    }

    public long getWifiBatchedScanTime(Context context, Object myUid,
                                       int csphBin, long batteryRealtime, int iStatsType)
            throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        this.collectUidStats();
        if (m_uidStats != null) {
            try {

                ClassLoader cl = context.getClassLoader();
                @SuppressWarnings("rawtypes")
                Class iBatteryStatsUid = cl
                        .loadClass("com.android.internal.os.BatteryStatsImpl$Uid");

                int NU = m_uidStats.size();

                // Object is an instance of BatteryStats.Uid

                @SuppressWarnings("rawtypes")
                Class[] paramTypes = new Class[3];
                paramTypes[0] = int.class;
                paramTypes[1] = long.class;
                paramTypes[2] = int.class;

                @SuppressWarnings("unchecked")
                Method method = iBatteryStatsUid.getMethod(
                        "getWifiBatchedScanTime", paramTypes);

                // Parameters
                Object[] params = new Object[3];
                params[0] = new Integer(csphBin);
                params[1] = new Long(batteryRealtime);
                params[2] = new Integer(iStatsType);

                ret = (Long) method.invoke(myUid, params);

                if (LOG) {
                    Log.i(TAG, "getWifiRunningTime with params " + params[0]
                            + " and " + params[1] + " returned " + ret);
                }

            } catch (IllegalArgumentException e) {
                Log.e(TAG,
                        "getWifiBatchedScanTime threw an IllegalArgumentException: "
                                + e.getMessage());
                throw e;
            } catch (Exception e) {
                Log.e(TAG,
                        "getWifiBatchedScanTime threw an Exception: "
                                + e.getMessage());
                ret = new Long(0);
                throw new BatteryInfoUnavailableException();
            }
        }
        return ret;
    }

    public long getWifiScanTime(Context context, Object myUid,
                                long batteryRealtime, int iStatsType)
            throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        this.collectUidStats();
        if (m_uidStats != null) {
            try {

                ClassLoader cl = context.getClassLoader();
                @SuppressWarnings("rawtypes")
                Class iBatteryStatsUid = cl
                        .loadClass("com.android.internal.os.BatteryStatsImpl$Uid");

                int NU = m_uidStats.size();

                // Object is an instance of BatteryStats.Uid

                @SuppressWarnings("rawtypes")
                Class[] paramTypes = new Class[2];
                paramTypes[0] = long.class;
                paramTypes[1] = int.class;

                @SuppressWarnings("unchecked")
                Method method = iBatteryStatsUid.getMethod("getWifiScanTime",
                        paramTypes);

                // Parameters
                Object[] params = new Object[2];
                params[0] = new Long(batteryRealtime);
                params[1] = new Integer(iStatsType);

                ret = (Long) method.invoke(myUid, params);

                if (LOG) {
                    Log.i(TAG, "getWifiRunningTime with params " + params[0]
                            + " and " + params[1] + " returned " + ret);
                }

            } catch (IllegalArgumentException e) {
                Log.e(TAG,
                        "getWifiScanTime threw an IllegalArgumentException: "
                                + e.getMessage());
                throw e;
            } catch (Exception e) {
                Log.e(TAG,
                        "getWifiScanTime threw an Exception: " + e.getMessage());
                ret = new Long(0);
                throw new BatteryInfoUnavailableException();
            }
        }
        return ret;
    }

    /**
     * Returns the total, last, or current wifi running time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getWifiRunningTime(Context context, Object myUid,
                                   long batteryRealtime, int iStatsType)
            throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        this.collectUidStats();
        if (m_uidStats != null) {
            try {

                ClassLoader cl = context.getClassLoader();
                @SuppressWarnings("rawtypes")
                Class iBatteryStatsUid = cl
                        .loadClass("com.android.internal.os.BatteryStatsImpl$Uid");

                int NU = m_uidStats.size();

                // Object is an instance of BatteryStats.Uid

                @SuppressWarnings("rawtypes")
                Class[] paramTypes = new Class[2];
                paramTypes[0] = long.class;
                paramTypes[1] = int.class;

                @SuppressWarnings("unchecked")
                Method method = iBatteryStatsUid.getMethod(
                        "getWifiRunningTime", paramTypes);

                // Parameters
                Object[] params = new Object[2];
                params[0] = new Long(batteryRealtime);
                params[1] = new Integer(iStatsType);

                ret = (Long) method.invoke(myUid, params);

                if (LOG) {
                    Log.i(TAG, "getWifiRunningTime with params " + params[0]
                            + " and " + params[1] + " returned " + ret);
                }

            } catch (IllegalArgumentException e) {
                Log.e(TAG, "getWifiRunning threw an IllegalArgumentException: "
                        + e.getMessage());
                throw e;
            } catch (Exception e) {
                Log.e(TAG,
                        "getWifiRunning threw an Exception: " + e.getMessage());
                ret = new Long(0);
                throw new BatteryInfoUnavailableException();
            }
        }
        return ret;
    }

    /**
     * Returns the total, last, or current wifi lock time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getFullWifiLockTime(Context context, long batteryRealtime,
                                    int iStatsType) throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        this.collectUidStats();
        if (m_uidStats != null) {
            try {

                ClassLoader cl = context.getClassLoader();
                @SuppressWarnings("rawtypes")
                Class iBatteryStatsUid = cl
                        .loadClass("com.android.internal.os.BatteryStatsImpl$Uid");

                int NU = m_uidStats.size();
                for (int iu = 0; iu < NU; iu++) {
                    // Object is an instance of BatteryStats.Uid
                    Object myUid = m_uidStats.valueAt(iu);

                    @SuppressWarnings("rawtypes")
                    Class[] paramTypes = new Class[2];
                    paramTypes[0] = long.class;
                    paramTypes[1] = int.class;

                    @SuppressWarnings("unchecked")
                    Method method = iBatteryStatsUid.getMethod(
                            "getFullWifiLockTime", paramTypes);

                    // Parameters
                    Object[] params = new Object[2];
                    params[0] = new Long(batteryRealtime);
                    params[1] = new Integer(iStatsType);

                    ret += (Long) method.invoke(myUid, params);

                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                ret = new Long(0);
                throw new BatteryInfoUnavailableException();
            }
        }
        return ret;
    }

    /**
     * Returns the total, last, or current wifi scanning time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getScanWifiLockTime(Context context, long batteryRealtime,
                                    int iStatsType) throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        this.collectUidStats();
        if (m_uidStats != null) {
            try {

                ClassLoader cl = context.getClassLoader();
                @SuppressWarnings("rawtypes")
                Class iBatteryStatsUid = cl
                        .loadClass("com.android.internal.os.BatteryStatsImpl$Uid");

                int NU = m_uidStats.size();
                for (int iu = 0; iu < NU; iu++) {
                    // Object is an instance of BatteryStats.Uid
                    Object myUid = m_uidStats.valueAt(iu);

                    @SuppressWarnings("rawtypes")
                    Class[] paramTypes = new Class[2];
                    paramTypes[0] = long.class;
                    paramTypes[1] = int.class;

                    @SuppressWarnings("unchecked")
                    Method method = iBatteryStatsUid.getMethod(
                            "getScanWifiLockTime", paramTypes);

                    // Parameters
                    Object[] params = new Object[2];
                    params[0] = new Long(batteryRealtime);
                    params[1] = new Integer(iStatsType);

                    ret += (Long) method.invoke(myUid, params);

                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                ret = new Long(0);
                throw new BatteryInfoUnavailableException();
            }
        }
        return ret;
    }

    /**
     * Returns the total, last, or current wifi multicast time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getWifiMulticastTime(Context context, long batteryRealtime,
                                     int iStatsType) throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        this.collectUidStats();
        if (m_uidStats != null) {
            try {

                ClassLoader cl = context.getClassLoader();
                @SuppressWarnings("rawtypes")
                Class iBatteryStatsUid = cl
                        .loadClass("com.android.internal.os.BatteryStatsImpl$Uid");

                int NU = m_uidStats.size();
                for (int iu = 0; iu < NU; iu++) {
                    // Object is an instance of BatteryStats.Uid
                    Object myUid = m_uidStats.valueAt(iu);

                    @SuppressWarnings("rawtypes")
                    Class[] paramTypes = new Class[2];
                    paramTypes[0] = long.class;
                    paramTypes[1] = int.class;

                    @SuppressWarnings("unchecked")
                    Method method = iBatteryStatsUid.getMethod(
                            "getWifiMulticastTime", paramTypes);

                    // Parameters
                    Object[] params = new Object[2];
                    params[0] = new Long(batteryRealtime);
                    params[1] = new Integer(iStatsType);

                    ret += (Long) method.invoke(myUid, params);

                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                ret = new Long(0);
                throw new BatteryInfoUnavailableException();
            }
        }
        return ret;
    }

    /**
     * Returns the time in microseconds the phone has been running with the
     * given data connection type.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     * @params dataType the given data connection type (@see
     * http://www.netmite.com
     * /android/mydroid/donut/frameworks/base/core/
     * java/android/os/BatteryStats.java)
     */
    public Long getPhoneDataConnectionTime(int dataType, long batteryRealtime,
                                           int iStatsType) throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[3];
            paramTypes[0] = int.class;
            paramTypes[1] = long.class;
            paramTypes[2] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod(
                    "getPhoneDataConnectionTime", paramTypes);

            // Parameters
            Object[] params = new Object[3];
            params[0] = new Integer(dataType);
            params[1] = new Long(batteryRealtime);
            params[2] = new Integer(iStatsType);

            ret = (Long) method.invoke(m_Instance, params);
            if (LOG) {
                Log.i(TAG, "getPhoneDataConnectionTime with params "
                        + params[0] + ", " + params[1] + "and " + params[2]
                        + " returned " + ret);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            ret = new Long(0);
            throw new BatteryInfoUnavailableException();
        }

        return ret;

    }

    /**
     * Returns the time in microseconds the phone has been running with the
     * given signal strength.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     * @params signalStrength the given data connection type (@see
     * http://www.netmite
     * .com/android/mydroid/donut/frameworks/base/core/
     * java/android/os/BatteryStats.java)
     */
    public Long getPhoneSignalStrengthTime(int signalStrength,
                                           long batteryRealtime, int iStatsType) {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[3];
            paramTypes[0] = int.class;
            paramTypes[1] = long.class;
            paramTypes[2] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod(
                    "getPhoneDataConnectionTime", paramTypes);

            // Parameters
            Object[] params = new Object[3];
            params[0] = new Integer(signalStrength);
            params[1] = new Long(batteryRealtime);
            params[2] = new Integer(iStatsType);

            ret = (Long) method.invoke(m_Instance, params);
            if (LOG) {
                Log.i(TAG, "getPhoneSignalStrengthTime with params "
                        + params[0] + ", " + params[1] + "and " + params[2]
                        + " returned " + ret);
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            ret = new Long(0);
        }

        return ret;

    }

    /**
     * Returns the time in microseconds the screen has been running with the
     * given brightness
     */
    public Long getScreenBrightnessTime(int brightness, long batteryRealtime,
                                        int iStatsType) {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[3];
            paramTypes[0] = int.class;
            paramTypes[1] = long.class;
            paramTypes[2] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod(
                    "getScreenBrightnessTime", paramTypes);

            // Parameters
            Object[] params = new Object[3];
            params[0] = new Integer(brightness);
            params[1] = new Long(batteryRealtime);
            params[2] = new Integer(iStatsType);

            ret = (Long) method.invoke(m_Instance, params);
            if (LOG) {
                Log.i(TAG, "getScreenBrightnessTime with params " + params[0]
                        + ", " + params[1] + "and " + params[2] + " returned "
                        + ret);
            }

        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            ret = new Long(0);

        }

        return ret;

    }

    /**
     * Returns the total, last, or current audio on time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getAudioTurnedOnTime(Context context, long batteryRealtime,
                                     int iStatsType) throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        this.collectUidStats();
        if (m_uidStats != null) {
            try {

                ClassLoader cl = context.getClassLoader();
                @SuppressWarnings("rawtypes")
                Class iBatteryStatsUid = cl
                        .loadClass("com.android.internal.os.BatteryStatsImpl$Uid");

                int NU = m_uidStats.size();
                for (int iu = 0; iu < NU; iu++) {
                    // Object is an instance of BatteryStats.Uid
                    Object myUid = m_uidStats.valueAt(iu);

                    @SuppressWarnings("rawtypes")
                    Class[] paramTypes = new Class[2];
                    paramTypes[0] = long.class;
                    paramTypes[1] = int.class;

                    @SuppressWarnings("unchecked")
                    Method method = iBatteryStatsUid.getMethod(
                            "getAudioTurnedOnTime", paramTypes);

                    // Parameters
                    Object[] params = new Object[2];
                    params[0] = new Long(batteryRealtime);
                    params[1] = new Integer(iStatsType);

                    ret += (Long) method.invoke(myUid, params);

                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                ret = new Long(0);
                throw new BatteryInfoUnavailableException();
            }
        }
        return ret;
    }

    /**
     * Returns the total, last, or current video on time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getVideoTurnedOnTime(Context context, long batteryRealtime,
                                     int iStatsType) throws BatteryInfoUnavailableException {
        Long ret = new Long(0);

        this.collectUidStats();
        if (m_uidStats != null) {
            try {

                ClassLoader cl = context.getClassLoader();
                @SuppressWarnings("rawtypes")
                Class iBatteryStatsUid = cl
                        .loadClass("com.android.internal.os.BatteryStatsImpl$Uid");

                int NU = m_uidStats.size();
                for (int iu = 0; iu < NU; iu++) {
                    // Object is an instance of BatteryStats.Uid
                    Object myUid = m_uidStats.valueAt(iu);

                    @SuppressWarnings("rawtypes")
                    Class[] paramTypes = new Class[2];
                    paramTypes[0] = long.class;
                    paramTypes[1] = int.class;

                    @SuppressWarnings("unchecked")
                    Method method = iBatteryStatsUid.getMethod(
                            "getVideoTurnedTime", paramTypes);

                    // Parameters
                    Object[] params = new Object[2];
                    params[0] = new Long(batteryRealtime);
                    params[1] = new Integer(iStatsType);

                    ret += (Long) method.invoke(myUid, params);

                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                ret = new Long(0);
                throw new BatteryInfoUnavailableException();
            }
        }
        return ret;
    }

    /**
     * Returns the total, last, or current bluetooth on time in microseconds.
     *
     * @param batteryRealtime the battery realtime in microseconds (@see
     *                        computeBatteryRealtime).
     * @param iStatsType      one of STATS_TOTAL, STATS_LAST, or STATS_CURRENT.
     */
    public Long getBluetoothOnTime(long batteryRealtime, int iStatsType) {
        Long ret = new Long(0);

        try {
            // Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = long.class;
            paramTypes[1] = int.class;

            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod("getBluetoothOnTime",
                    paramTypes);

            // Parameters
            Object[] params = new Object[2];
            params[0] = new Long(batteryRealtime);
            params[1] = new Integer(iStatsType);

            ret = (Long) method.invoke(m_Instance, params);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            ret = new Long(0);

        }

        return ret;

    }

    public long getMobileTcpBytesReceived(int which) {
        long ret = 0;
        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod(
                    "getMobileTcpBytesReceived",
                    new Class<?>[]{Integer.class});

            ret = (Integer) method.invoke(m_Instance, new Object[]{which});

        } catch (IllegalArgumentException e) {
            Log.e(TAG,
                    "An exception occured in getMobileTcpBytesReceived(). Message: "
                            + e.getMessage() + ", cause: "
                            + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            ret = 0;
        }
        return ret;
    }

    public long getNetworkActivityCount(int type, int which) {
        long ret = 0;
        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod(
                    "getNetworkActivityCount", new Class<?>[]{int.class,
                            int.class});

            ret = (Integer) method.invoke(m_Instance, new Object[]{type,
                    which});

        } catch (IllegalArgumentException e) {
            Log.e(TAG,
                    "An exception occured in getMobileTcpBytesSent(). Message: "
                            + e.getMessage() + ", cause: "
                            + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            ret = 0;
        }
        return ret;
    }

    public long getMobileTcpBytesSent(int which) {
        long ret = 0;
        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod(
                    "getMobileTcpBytesSent", new Class<?>[]{int.class});

            ret = (Integer) method.invoke(m_Instance, new Object[]{which});

        } catch (IllegalArgumentException e) {
            Log.e(TAG,
                    "An exception occured in getMobileTcpBytesSent(). Message: "
                            + e.getMessage() + ", cause: "
                            + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            ret = 0;
        }
        return ret;
    }

    public long getTotalTcpBytesReceived(int which) {
        long ret = 0;
        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod(
                    "getTotalTcpBytesReceived", new Class<?>[]{int.class});

            ret = (Integer) method.invoke(m_Instance, new Object[]{which});

        } catch (IllegalArgumentException e) {
            Log.e(TAG,
                    "An exception occured in getTotalTcpBytesReceived(). Message: "
                            + e.getMessage() + ", cause: "
                            + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            ret = 0;
        }
        return ret;
    }

    public long getTotalTcpBytesSent(int which) {
        long ret = 0;
        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod("getTotalTcpBytesSent",
                    new Class<?>[]{int.class});

            ret = (Integer) method.invoke(m_Instance, new Object[]{which});

        } catch (IllegalArgumentException e) {
            Log.e(TAG,
                    "An exception occured in getTotalTcpBytesSent(). Message: "
                            + e.getMessage() + ", cause: "
                            + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            ret = 0;
        }
        return ret;
    }

    public long getRadioDataUptime() {
        long ret = 0;
        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod("getRadioDataUptime");

            ret = (Integer) method.invoke(m_Instance);

        } catch (IllegalArgumentException e) {
            Log.e(TAG,
                    "An exception occured in getRadioDataUptime(). Message: "
                            + e.getMessage() + ", cause: "
                            + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            ret = 0;

        }
        return ret;
    }

    // /**
    // * Return whether we are currently running on battery.
    // */
    // public boolean getIsOnBattery(Context context) throws
    // BatteryInfoUnavailableException
    // {
    // boolean ret = false;
    //
    // try
    // {
    // @SuppressWarnings("unchecked")
    // Method method = m_ClassDefinition.getMethod("getIsOnBattery");
    //
    // Boolean oRet = (Boolean) method.invoke(m_Instance);
    // ret = oRet.booleanValue();
    //
    // }
    // catch( IllegalArgumentException e )
    // {
    // throw e;
    // }
    // catch( Exception e )
    // {
    // ret = false;
    // throw new BatteryInfoUnavailableException();
    // }
    //
    // return ret;
    // }

    /**
     * Returns the current battery percentage level if we are in a discharge
     * cycle, otherwise returns the level at the last plug event.
     */
    public int getDischargeCurrentLevel()
            throws BatteryInfoUnavailableException {
        int ret = 0;

        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition
                    .getMethod("getDischargeCurrentLevel");

            Integer oRet = (Integer) method.invoke(m_Instance);
            ret = oRet.intValue();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            ret = 0;
            throw new BatteryInfoUnavailableException();
        }

        return ret;
    }

    /**
     * Initalizes the collection of history items
     */
    public boolean startIteratingHistoryLocked()
            throws BatteryInfoUnavailableException {
        Boolean ret = false;

        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition
                    .getMethod("startIteratingHistoryLocked");

            ret = (Boolean) method.invoke(m_Instance);

        } catch (IllegalArgumentException e) {
            Log.e(TAG,
                    "An exception occured in startIteratingHistoryLocked(). Message: "
                            + e.getMessage() + ", cause: "
                            + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            ret = false;
            throw new BatteryInfoUnavailableException();
        }

        return ret;

    }

    /**
     * Initalizes the collection of history items
     */
    public boolean finishIteratingHistoryLocked()
            throws BatteryInfoUnavailableException {
        Boolean ret = false;

        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition
                    .getMethod("finishIteratingHistoryLocked");

            ret = (Boolean) method.invoke(m_Instance);

        } catch (IllegalArgumentException e) {
            Log.e(TAG,
                    "An exception occured in finishIteratingHistoryLocked(). Message: "
                            + e.getMessage() + ", cause: "
                            + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            ret = false;
            throw new BatteryInfoUnavailableException();
        }

        return ret;

    }

    public int getBluetoothPingCount() {
        int ret = 0;
        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition
                    .getMethod("getBluetoothPingCount");

            ret = (Integer) method.invoke(m_Instance);

        } catch (IllegalArgumentException e) {
            Log.e(TAG,
                    "An exception occured in finishIteratingHistoryLocked(). Message: "
                            + e.getMessage() + ", cause: "
                            + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            ret = 0;
        }
        return ret;
    }

    public long getPhoneSignalScanningTime(long batteryRealtime, int which) {
        long ret = 0;
        try {
            @SuppressWarnings("unchecked")
            Method method = m_ClassDefinition.getMethod(
                    "getPhoneSignalScanningTime", new Class<?>[]{long.class,
                            int.class});

            ret = (Integer) method.invoke(m_Instance, new Object[]{
                    batteryRealtime, which});

        } catch (IllegalArgumentException e) {
            Log.e(TAG,
                    "An exception occured in getPhoneSignalScanningTime(). Message: "
                            + e.getMessage() + ", cause: "
                            + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            ret = 0;
        }
        return ret;
    }

    /**
     * Collect the UidStats using reflection and store them
     */
    @SuppressWarnings("unchecked")
    private void collectUidStats() {
        try {
            Method method = m_ClassDefinition.getMethod("getUidStats");

            m_uidStats = (SparseArray<? extends Object>) method
                    .invoke(m_Instance);

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "An exception occured in collectUidStats(). Message: "
                    + e.getMessage() + ", cause: " + e.getCause().getMessage());
            throw e;
        } catch (Exception e) {
            m_uidStats = null;
        }

    }

    public SparseArray<? extends Object> getUidStats() {
        collectUidStats();
        return m_uidStats;
    }

}
