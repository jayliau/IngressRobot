package com.autohack.ingressrobot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class AutoHack extends BaseAuto {

	
	Context mContext;
	HashMap<String, Point> mMap;
	
	PortalDatabase mPortalDB;
	
	public AutoHack(Context context, HashMap<String, Point> map, PortalDatabase portalDB) {
		super(context);
		mContext = context;
		mMap = map;
		mPortalDB = portalDB;
	}



	public void worker(/*Context context, int portalNo, HashMap<String, Point> map*/) {
		Instrumentation inst = new Instrumentation();
		//final HashMap<String, Point> Map = mMap;

		while (!isCancelled()) {
			
			StringBuilder dump = new StringBuilder();
			
			long reload = needReload(dump);
			/*
			 * reload = 0, inital, or all hack 4 times and over 4 hrs reload =
			 * -1 no need to reload reload > 0 all hack 4 times, need reload,
			 * but not reach 4 hr, return the rest time.
			 */
			
			if (reload >= 0) {
				Log.e("FakGPS",
						"table is full or empty download portal file from network");
				downloadFileAndUpdateDB(dump);
				if (reload > 0) {
					publishProgress(dump.toString());
					SLEEP(reload);
					continue;
				}
			}

			float next_run = setPortalPosAndGetNextRun(dump);
			
			next_run -= SLEEP(2);

			Intent activity = new Intent(mContext, MainActivity.class).
					addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT|Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(activity);

			next_run -= SLEEP(2);

			Intent ingress = mContext.getPackageManager().getLaunchIntentForPackage("com.nianticproject.ingress");
			mContext.startActivity(ingress);
			
			next_run -= SLEEP(20);
			
			Point center = mMap.get("Center");
			doClick(center, inst);
			
			next_run -= SLEEP(2);

			Point hack = mMap.get("Hack");
			doClick(hack, inst);
			
			next_run -= SLEEP(5);
			
			Point OK = mMap.get("OK");
			doClick(OK, inst);
			
			next_run -= SLEEP(7);

			// Toast.makeText(mContext, "send point ",
			// Toast.LENGTH_LONG).show();

			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
			
			next_run -= SLEEP(2);
			
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
			
			publishProgress(dump.toString()+"next run start after "+next_run+" sec\n");
			SLEEP(next_run);
		}
	}

	private float SLEEP(float sec) {
		if(sec < 0  || isCancelled()) 
			return 0;
		try {
			Thread.sleep((int)(sec*1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return sec;
	}

	private void doClick(Point center, Instrumentation inst) {
		if(center == null || inst == null || isCancelled()) 
			return ;
		
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



	private long needReload(StringBuilder dump) {
		SQLiteDatabase db = mPortalDB.getReadableDatabase();

		Cursor cursor = db.query(PortalDatabase.TABLE_NAME, null, null, null, null, null, null, null);
		
		//no data need to reload
		if (cursor.getCount() == 0 ) {
			Log.e("FakeGPS", " get count 0, do reload!!!!!!!");
			dump.append("get count 0, do reload!!!!!!!\n");
			return 0;
		}
		cursor.moveToLast();
		int index = cursor.getColumnIndex(PortalDatabase.COUNT);
		
		//all data run once
		if( cursor.getInt(index) >= 4) {
			Log.e("FakeGPS", " get last portal count 4, do reload!!!!!!!");
			
			cursor.moveToFirst();
			// get first hack time, need to hack after 4hrs
			long firstTime = cursor.getLong(cursor.getColumnIndex(PortalDatabase.FIRST));
			long now = System.currentTimeMillis();
			
			long between = now - firstTime;
			// over 4hr reload and hack
			if( between > 1000*60*60*4 ) {
				dump.append("get last portal count 4, do reload!!!!!!! hack right now\n");
				return 0;
			} 
			else {
				long wait = (1000*60*60*4 - between)/1000;
				dump.append("get last portal count 4, do reload!!!!!!! wait "+wait+"for next run\n");
				return wait;
			}
		}
		
		return -1;
	}
	public static double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
		Log.e("FakeGPS",String.format("lat1 = %f lon1 = %f lat2 = %f lon2 = %f", lat_a, lng_a, lat_b, lng_b));
		final double EARTH_RADIUS = 6378137.0;
		double radLat1 = (lat_a * Math.PI / 180.0);
		double radLat2 = (lat_b * Math.PI / 180.0);
		double a = radLat1 - radLat2;
		double b = (lng_a - lng_b) * Math.PI / 180.0;
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
		+ Math.cos(radLat1) * Math.cos(radLat2)
		* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		//s = Math.round(s * 10000) / 10000;
		return s;
	}

	/*private void setNextRun(long second) {
		
	     Calendar calendar=Calendar.getInstance();
	     calendar.setTimeInMillis(System.currentTimeMillis());
	     calendar.add(Calendar.SECOND, (int)second);
	     
	     
	     AlarmManager alarm=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
	     alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), mPendingIntent);
	}*/

	private float setPortalPosAndGetNextRun(StringBuilder dump) {

		SQLiteDatabase db = mPortalDB.getWritableDatabase();
		
		//get current and total
		Cursor info = db.query(PortalDatabase.INFO_TABLE_NAME, null, null,	null, null, null, null);
		info.moveToFirst();
		final int Total = info.getInt(info.getColumnIndex(PortalDatabase.TOTAL));
		final int Now = info.getInt(info.getColumnIndex(PortalDatabase.NEXT));
		final int Next = Now + 1 <= Total ? Now + 1 : 1;

		

		// Get Now portal data
		Cursor nowPortal = db.query(PortalDatabase.TABLE_NAME, null, PortalDatabase.NO+"="+Now, null, null, null,null);
		if (nowPortal.moveToFirst() ) {

			String lat = nowPortal.getString(nowPortal.getColumnIndex(PortalDatabase.LAT));
			String lon = nowPortal.getString(nowPortal.getColumnIndex(PortalDatabase.LON));
			String result = lat+","+lon+",100000000";
			
			Settings.System.putString(mContext.getContentResolver(),	"FakeGPS", result);
			Log.e("FakGPS", "set FakeGPS:"+result);
			
			logToFile(Now,lat,lon);
			
			int count = nowPortal.getInt(nowPortal.getColumnIndex(PortalDatabase.COUNT));
			ContentValues NowPortalUpdate = new ContentValues();
			if( count == 0 ) {
				NowPortalUpdate.put(PortalDatabase.COUNT, 1);
				NowPortalUpdate.put(PortalDatabase.FIRST, System.currentTimeMillis());
			}
			else if ( count > 0 && count < 4){
				NowPortalUpdate.put(PortalDatabase.COUNT, count+1);
				NowPortalUpdate.put(PortalDatabase.LAST, System.currentTimeMillis());
			}
			int re = db.update(PortalDatabase.TABLE_NAME, NowPortalUpdate, PortalDatabase.NO+"="+Now, null);
			Log.w("Timmy", "update portal " + Now+" get "+re);
			Log.e("FakGPS", "updata current time to No."+Now+" portal");
			

			// get Next Portal Info
			Cursor NextPortal = db.query(PortalDatabase.TABLE_NAME, null, PortalDatabase.NO+"="+Next, null, null, null, null);
			NextPortal.moveToFirst();
			String lat_n = NextPortal.getString(NextPortal.getColumnIndex(PortalDatabase.LAT));
			String lon_n = NextPortal.getString(NextPortal.getColumnIndex(PortalDatabase.LON));
			
			int next_count = NextPortal.getInt(NextPortal.getColumnIndex(PortalDatabase.COUNT));
			long lastTime = 0;
			if( next_count == 1) {
				lastTime = NextPortal.getLong(NextPortal.getColumnIndex(PortalDatabase.FIRST));
			} else if( next_count > 1) {
				lastTime = NextPortal.getLong(NextPortal.getColumnIndex(PortalDatabase.LAST));
			}
			
			long next_in_second = calculateDistanceSecond(lat,lon,lat_n,lon_n, lastTime);
			// update next
			ContentValues values = new ContentValues();
			values.put(PortalDatabase.NEXT, Next);
			values.put(PortalDatabase.WAIT, next_in_second);
			int re1 =db.update(PortalDatabase.INFO_TABLE_NAME, values, PortalDatabase.TOTAL+ "=" + Total, null);
			Log.w("Timmy", "update Info get "+re1);
			Log.e("FakeGPS", "Next portal NO,"+Next+" wait for "+next_in_second+" second");
			return next_in_second;
		}
		else {
			Log.e("FakeGPS", "no cursor");
		}
		return 60;
	}

	private void logToFile(int now, String lat, String lon) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dt=new Date();
			OutputStream out = mContext.openFileOutput("Log.txt", Context.MODE_APPEND);
			out.write(String.format("No.%d (%s,%s) is hack at %s\n",now,lat,lon,sdf.format(dt)).getBytes()); 
			out.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}



	private long calculateDistanceSecond(String lat, String lon,
			String lat_n, String lon_n, long lastTime) {
		
		
		double Lat = Double.parseDouble(lat);
		double Lon = Double.parseDouble(lon);
		double Lat_n = Double.parseDouble(lat_n);
		double Lon_n = Double.parseDouble(lon_n);
		
		double distance = gps2m(Lat, Lon, Lat_n, Lon_n);
		
		double second = distance / 10.0f;
		
		long now = System.currentTimeMillis();
		long between = (now - lastTime)/1000;
		
		Log.e("FakeGPS", "distance = "+distance +" sencond = "+second + " last = "+lastTime + " between = "+between);
		
		if( lastTime == 0 ) {
			//publishProgress("last time = 0, 60s start next run");
			return 60;
		} else {
			if(between < 360) {
				long max = Math.max((360-between), Math.round(second));
				long more1min = Math.max(max, 60);
				//publishProgress("between="+between+", second="+second+", return "+more1min);
				return more1min;
			} else {
				long more1min = Math.max(Math.round(second), 60);
				//publishProgress("between>360, second="+second+", return "+more1min);
				return more1min;
			}
		}
	}

	private void downloadFileAndUpdateDB(StringBuilder dump) {

		
		Log.w("Timmy", "do reload!!!!!!!!!!!");
		

		SQLiteDatabase db = mPortalDB.getWritableDatabase();
		db.delete(PortalDatabase.TABLE_NAME, null, null);
		db.delete(PortalDatabase.INFO_TABLE_NAME, null, null);

		try {
			
			File f = new File("/data/data/com.flyer.airplanemode/files/default.portal");
			BufferedReader in;
			if( f.exists() && f.canRead()) {
				in = new BufferedReader(new FileReader(f));
				dump.append("read from default.portal, ");
			}
			else {
				URL url = new URL(
						"https://drive.google.com/uc?id=0B97Dxl2k2ZA-Z0dfTkN4YWVoMzg&export=download");
				URLConnection conection = url.openConnection();
				conection.connect();
				in = new BufferedReader(new InputStreamReader(url.openStream()));
				dump.append("read from google doc, ");
			}
			
			String str = null;
			int count = 1;
			while ((str = in.readLine()) != null) {						
				if (str.startsWith("#")) {
					continue;
				}
				if (str.lastIndexOf("pll=") < 0) {
					continue;
				}
				String sub = str.substring(str.lastIndexOf("pll="));
				Log.e("FakeGPS", sub);
				String pll = sub.substring(sub.lastIndexOf("=") + 1);
				String ll[] = pll.split(",");
				if (ll.length != 2) {
					continue;
				}
				
				try{
					Float.parseFloat(ll[0]);
					Float.parseFloat(ll[1]);
				}catch(NumberFormatException e){
					continue;
				}
					
				double lat = Double.parseDouble(ll[0]);
				double lon = Double.parseDouble(ll[1]);
				ContentValues values = new ContentValues();
				values.put(PortalDatabase.NO, count);
				values.put(PortalDatabase.LAT, lat);
				values.put(PortalDatabase.LON, lon);
				values.put(PortalDatabase.FIRST, 0);
				values.put(PortalDatabase.LAST, 0);
				values.put(PortalDatabase.COUNT, 0);
				db.insert(PortalDatabase.TABLE_NAME, null, values);

				count++;

			}
			ContentValues values = new ContentValues();
			values.put(PortalDatabase.TOTAL, count - 1);
			values.put(PortalDatabase.NEXT, 1);
			values.put(PortalDatabase.WAIT, 0);
			db.insert(PortalDatabase.INFO_TABLE_NAME, null, values);
			dump.append("total portal "+(count -1)+"\n");
			in.close();
			//publishProgress(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		worker();
		return null;
	}

}
