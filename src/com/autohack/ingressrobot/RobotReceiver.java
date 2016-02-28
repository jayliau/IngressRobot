package com.autohack.ingressrobot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

public class RobotReceiver extends BroadcastReceiver {

	private static final String AUTO_HACK = "auto_hack";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("FakeGPS","get intent"+intent.toString());
		if(intent.getAction().contentEquals(AUTO_HACK)) {
			
			context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			
			Intent service = new Intent(context, RobotService.class);
			service.setAction("AUTO_HACK");
			
			context.startService(service);
			
		}

	}

}
