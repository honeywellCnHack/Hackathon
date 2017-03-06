package com.test.poweroptimizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.Request;
import com.test.poweroptimizer.Battery.BatteryInfo;
import com.test.poweroptimizer.Battery.BatteryStatsUtils;
import com.test.poweroptimizer.Tools.WatchDogService;

public class MonitorFragment extends Fragment {

    private static final String[] sBattery_Health = new String[]{"Unknown",
            "Good", "Overheat", "Dead", "Voltage", "Unspecified failure"};
    private View mRootView;
    private BroadcastReceiver mBatteryReceiver;
    private String[] mBatt = new String[3];
    private ImageView mBattery;
    private TextView mBattTitle, mBattHealth;
    private TextView midleOnTime, mScreenOnTime, mPhoneOnTime, mWifiOnTime;
    private TextView mTemperatureTextView, mVoltageTextView, mInternalTemperatureTextView, mCycleCountTextView, mStateOfHealthTextView, mSerialNumberTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                    int status = intent.getIntExtra("status", 0);
                    switch (status) {
                        case BatteryManager.BATTERY_STATUS_UNKNOWN:
                            mBatt[0] = "unknown";
                            break;
                        case BatteryManager.BATTERY_STATUS_CHARGING:
                            mBatt[0] = "charging";
                            break;
                        case BatteryManager.BATTERY_STATUS_DISCHARGING:
                            mBatt[0] = "discharging";
                            break;
                        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                            mBatt[0] = "not charging";
                            break;
                        case BatteryManager.BATTERY_STATUS_FULL:
                            mBatt[0] = "full";
                            break;
                    }

                    int health = intent.getIntExtra(
                            BatteryManager.EXTRA_HEALTH, -1);
                    switch (health) {
                        case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                            mBatt[2] = sBattery_Health[0];
                            break;
                        case BatteryManager.BATTERY_HEALTH_GOOD:
                            mBatt[2] = sBattery_Health[1];
                            break;
                        case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                            mBatt[2] = sBattery_Health[2];
                            break;
                        case BatteryManager.BATTERY_HEALTH_DEAD:
                            mBatt[2] = sBattery_Health[3];
                            break;
                        case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                            mBatt[2] = sBattery_Health[4];
                            break;
                        case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                            mBatt[2] = sBattery_Health[5];
                            break;
                        default:
                            mBatt[2] = "";
                            break;
                    }
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,
                            0);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,
                            100);

                    level = level * 100 / scale;
                    updateStatus(level);
                }
            }
        };

        this.getContext().startService(new Intent(this.getContext(), WatchDogService.class));

        return;
    }

    public void onResume() {
        super.onResume();
        // Register the receiver;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        this.getContext().registerReceiver(mBatteryReceiver, filter);
    }

    public void onPause() {
        super.onPause();
        this.getContext().unregisterReceiver(mBatteryReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.monitor_fragment, container, false);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }

        mBattery = (ImageView) mRootView.findViewById(R.id.icon_battery);
        mBattTitle = (TextView) mRootView.findViewById(R.id.battery_level);
        mBattHealth = (TextView) mRootView.findViewById(R.id.battery_health);
        mBattHealth.setVisibility(View.INVISIBLE);

        midleOnTime = (TextView) mRootView.findViewById(R.id.idle_on_time_value);
        mScreenOnTime = (TextView) mRootView.findViewById(R.id.screen_on_time_value);
        mPhoneOnTime = (TextView) mRootView.findViewById(R.id.power_on_time_value);
        mWifiOnTime = (TextView) mRootView.findViewById(R.id.wifi_on_time_value);

        midleOnTime.setVisibility(View.INVISIBLE);
        mScreenOnTime.setVisibility(View.INVISIBLE);
        mPhoneOnTime.setVisibility(View.INVISIBLE);
        mWifiOnTime.setVisibility(View.INVISIBLE);


        mSerialNumberTextView = (TextView) mRootView.findViewById(R.id.battery_info_sn_value);
        mTemperatureTextView = (TextView) mRootView.findViewById(R.id.battery_info_temperture);
        mVoltageTextView = (TextView) mRootView.findViewById(R.id.battery_info_voltage);
        mInternalTemperatureTextView = (TextView) mRootView.findViewById(R.id.battery_info_internal_temperature);
        mCycleCountTextView = (TextView) mRootView.findViewById(R.id.battery_info_charging_cycles);
        mStateOfHealthTextView = (TextView) mRootView.findViewById(R.id.battery_info_health);

        //mSerialNumberTextView.setVisibility(View.INVISIBLE);
/*        mTemperatureTextView.setVisibility(View.INVISIBLE);
        mVoltageTextView.setVisibility(View.INVISIBLE);
        mInternalTemperatureTextView.setVisibility(View.INVISIBLE);
        mCycleCountTextView.setVisibility(View.INVISIBLE);
        mStateOfHealthTextView.setVisibility(View.INVISIBLE);*/

        mSerialNumberTextView.setText("14");
        mTemperatureTextView.setText("35");
        mVoltageTextView.setText("24");
        mInternalTemperatureTextView.setText("15");
        mCycleCountTextView.setText("1");
        mStateOfHealthTextView.setText("86");

        return mRootView;
    }

    // Update the information displayed on UI
    private void updateStatus(int level) {
        mBatt[1] = String.valueOf(level);
        mBattTitle.setText(mBatt[1]);

/*        if (mBatt[0].equals("charging") || mBatt[0].equals("full")) {
            if (level == 100) {
                mBattery.setImageResource(R.drawable.charging_icon_battery_100);
            } else if (80 <= level && level < 100) {
                mBattery.setImageResource(R.drawable.charging_icon_battery_80);
            } else if (60 <= level && level < 80) {
                mBattery.setImageResource(R.drawable.charging_icon_battery_60);
            } else if (40 <= level && level < 60) {
                mBattery.setImageResource(R.drawable.charging_icon_battery_40);
            } else if (20 <= level && level < 40) {
                mBattery.setImageResource(R.drawable.charging_icon_battery_20);
            } else {
                mBattery.setImageResource(R.drawable.charging_icon_battery_0);
            }
        } else if (mBatt[0].equals("discharging")
                || mBatt[0].equals("not charging")) {
            if (level == 100) {
                mBattery.setImageResource(R.drawable.icon_battery_100);
            } else if (80 <= level && level < 100) {
                mBattery.setImageResource(R.drawable.icon_battery_80);
            } else if (60 <= level && level < 80) {
                mBattery.setImageResource(R.drawable.icon_battery_60);
            } else if (40 <= level && level < 60) {
                mBattery.setImageResource(R.drawable.icon_battery_40);
            } else if (20 <= level && level < 40) {
                mBattery.setImageResource(R.drawable.icon_battery_20);
            } else if (10 <= level && level < 20) {
                mBattery.setImageResource(R.drawable.icon_battery_0);
            } else {
                mBattery.setImageResource(R.drawable.connecticon_battery_0);
            }
        }*/

        mBattery.setImageResource(R.drawable.air_green);
        mBattHealth.setText(String.format(getResources().getString(R.string.battery_health), mBatt[2]));

        BatteryInfo batteryInfo = new BatteryInfo(this.getContext());

        double screenOn = batteryInfo.getAveragePower(BatteryInfo.POWER_SCREEN_ON);

        double radioOn = batteryInfo.getAveragePower(BatteryInfo.POWER_RADIO_ACTIVE);
        double screenFull = batteryInfo.getAveragePower(BatteryInfo.POWER_SCREEN_FULL);
        double wifiOn = batteryInfo.getAveragePower(BatteryInfo.POWER_WIFI_SCAN);
        double cpuActive = batteryInfo.getAveragePower(BatteryInfo.POWER_CPU_ACTIVE);
        double idel = batteryInfo.getAveragePower(BatteryInfo.POWER_CPU_IDLE);

        double currentCapacity = getCurrentCapacity();

        midleOnTime.setText(BatteryStatsUtils.formatElapsedTime(this.getContext(), currentCapacity * 3600 * 1000 / ((screenOn + cpuActive + 120))));
        mScreenOnTime.setText(BatteryStatsUtils.formatElapsedTime(this.getContext(), currentCapacity * 3600 * 1000 / (screenOn + cpuActive)));
        mPhoneOnTime.setText(BatteryStatsUtils.formatElapsedTime(this.getContext(), currentCapacity * 3600 * 1000 / (screenOn + radioOn + cpuActive)));
        mWifiOnTime.setText(BatteryStatsUtils.formatElapsedTime(this.getContext(), currentCapacity * 3600 * 1000 / (wifiOn + screenOn + cpuActive)));

        midleOnTime.setVisibility(View.INVISIBLE);
        mScreenOnTime.setVisibility(View.INVISIBLE);
        mPhoneOnTime.setVisibility(View.INVISIBLE);
        mWifiOnTime.setVisibility(View.INVISIBLE);
    }

    public int getCurrentCapacity() {
        BatteryManager mBatteryManager =
                (BatteryManager) this.getContext().getSystemService(Context.BATTERY_SERVICE);
        int energy =
                mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        return energy;
    }

    public void getJsonData () {
        OkHttpClientManager.getAsyn("https://www.baidu.com", new OkHttpClientManager.ResultCallback<String>()
        {
            @Override
            public void onError(Request request, Exception e)
            {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String u)
            {
                Log.d("ARQ", u);
            }
        });
    }
}
