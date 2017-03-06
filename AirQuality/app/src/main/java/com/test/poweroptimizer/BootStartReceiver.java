package com.test.poweroptimizer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.test.poweroptimizer.Tools.WatchDogService;

public class BootStartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			context.startService(new Intent(context, WatchDogService.class));
		} 
	}

}
