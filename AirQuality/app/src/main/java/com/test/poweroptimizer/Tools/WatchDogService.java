package com.test.poweroptimizer.Tools;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class WatchDogService extends Service {
    private boolean flag;
    private String packName;
    private ActivityManager am;
    private LockScreenReceiver lockScreenReceiver;
    private UnLockScreenReceiver unLockScreenReceiver;
    private String className;
    private String tempPackName;
    private Intent intent;
    private List<String> packNames;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        lockScreenReceiver = new LockScreenReceiver();
        unLockScreenReceiver = new UnLockScreenReceiver();
        //intent = new Intent(WatchDogService.this, LockScreenActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        registerReceiver(lockScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(unLockScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));

        //packNames = dao.findAll();
        super.onCreate();
        flag = true;
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        monitorTopTaskSwitch();
    }

    private void monitorTopTaskSwitch() {
        new Thread() {
            public void run() {
                while (flag) {
                    ComponentName runningActivity = am.getRunningTasks(1)
                            .get(0).topActivity;
                    packName = runningActivity.getPackageName();
                    Log.e("=====", "=========" + packName);
                    // className=runningActivity.getClassName();
                    // System.out.println(className);
/*					if (packNames.contains(packName)) {
                        if (packName.equals(tempPackName)) {

						} else {
							tempPackName = null;
							intent.putExtra("packName", packName);
							startActivity(intent);
						}
					} else if (!packName.equals(getPackageName())) {
						tempPackName = null;
					}*/

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        flag = false;
        unregisterReceiver(lockScreenReceiver);
        unregisterReceiver(unLockScreenReceiver);

        unLockScreenReceiver = null;
        lockScreenReceiver = null;
    }


    private class LockScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            tempPackName = null;
            flag = false;
            // System.out.println(tempPackName);
        }

    }

    private class UnLockScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            flag = true;
            monitorTopTaskSwitch();
            // System.out.println(tempPackName);
        }
    }
}
