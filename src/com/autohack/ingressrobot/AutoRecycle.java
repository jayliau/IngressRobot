package com.autohack.ingressrobot;

import java.util.HashMap;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.graphics.Point;
import android.os.Vibrator;
import android.view.MotionEvent;


public class AutoRecycle extends BaseAuto {

	
	Context mContext;
	HashMap<String, Point> mMap;
	private int mRuns;
	
	public AutoRecycle(Context context, HashMap<String, Point> map, int runs) {
		super(context);
		mContext = context;
		mMap = map;
		mRuns = runs;
	}



	public void worker(/*Context context, int portalNo, HashMap<String, Point> map*/) {
		Instrumentation inst = new Instrumentation();
		//final HashMap<String, Point> Map = mMap;
		
		((Vibrator)mContext.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(100);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {

		}
		
				
		for( int a = mRuns ; a > 0 ; a --) {
			if( isCancelled()) {
				return;
			}
			
			if (a % 10 == 0  && a != 0) {
				publishProgress("remaining "+a+" times");
			}
						
			Point item = mMap.get("Item");
			doClick(item, inst);
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {

			}
		
			Point recycle = mMap.get("Recycle");
			doClick(recycle, inst);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
	}

	

	private static void doClick(Point center, Instrumentation inst) {

		long downtime = System.currentTimeMillis();
		
		MotionEvent event = MotionEvent.obtain(downtime, downtime, MotionEvent.ACTION_DOWN, center.x, center.y, 0);
		inst.sendPointerSync(event);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		event.recycle();
		long eventtime = System.currentTimeMillis();
		event = MotionEvent.obtain(downtime, eventtime, MotionEvent.ACTION_UP, center.x	, center.y, 0);
		inst.sendPointerSync(event);
		event.recycle();
		
	}

	@Override
	protected Void doInBackground(Void... params) {
		worker();
		return null;
	}

}
