package com.autohack.ingressrobot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.graphics.Point;

public class RobotService extends Service {

	private WindowManager mWindowManager;
	private View mLayout;
	private ToggleButton mMenuButton;
	
	private Button mHackButton;
	private Button mDropButton;
	private Button mRecycleButton;
	private Button mAquireButton;
	private ToggleButton mMoveButton;
	
	private Button mUpButton;
	private Button mDownButton;
	private Button mLeftButton;
	private Button mRightButton;
	
	
	
	
	private HashMap<String, Point> mMap;
	
	private PendingIntent mPendingIntent;
	
	private PortalDatabase mPortalDB; 
	
	private boolean mRunning = false;
	
	private AsyncTask<Void, String, Void> mRunningJob;

	private MoveListener mMoveListener = new MoveListener();
	private class MoveListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			((Vibrator)RobotService.this.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(100);
			
			String res = Settings.System.getString(RobotService.this.getContentResolver(),
		                "FakeGPS");
				
			String ll[] = res.split(",");
			double _0 = Double.parseDouble(ll[0]);
			double _1 = Double.parseDouble(ll[1]);
			switch(v.getId()) {
			case R.id.button_up:
				_0 += 0.0003;
				break;
			case R.id.button_down:
				_0 -= 0.0003;
				break;
			case R.id.button_left:
				_1 -= 0.0003;
				break;
			case R.id.button_right:
				_1 += 0.0003;
				break;
			}
			Settings.System.putString(RobotService.this.getContentResolver(),
	                "FakeGPS",_0+","+_1+","+ll[2]);
		}
		

	};
	@Override
	public void onCreate() {
		super.onCreate();

		mMap = loadConfig();
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		mPortalDB = new PortalDatabase(this);
		
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayout = (View) inflater.inflate(R.layout.menu_view, null);
		mMenuButton = (ToggleButton) mLayout.findViewById(R.id.button_menu);
		
		mHackButton = (Button) mLayout.findViewById(R.id.button_hack);
		mHackButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((Vibrator)RobotService.this.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(100);
				if( mRunningJob != null) {
					if(mRunningJob.getStatus() == AsyncTask.Status.RUNNING) {
						mRunningJob.cancel(true);
						mRunningJob = null;
						Toast.makeText(RobotService.this, "Hack stop", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				mRunningJob = new AutoHack(RobotService.this, mMap, mPortalDB);
				mRunningJob.execute();
			}
		});
		mHackButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				SQLiteDatabase db = mPortalDB.getWritableDatabase();
				db.delete(PortalDatabase.TABLE_NAME, null, null);
				db.delete(PortalDatabase.INFO_TABLE_NAME, null, null);
				RobotService.this.deleteFile("Log.txt");
				Toast.makeText(RobotService.this, "reset database and log", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		mDropButton = (Button) mLayout.findViewById(R.id.button_drop);
		mDropButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((Vibrator)RobotService.this.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(100);
				if( mRunningJob != null) {
					if(mRunningJob.getStatus() == AsyncTask.Status.RUNNING) {
						mRunningJob.cancel(true);
						mRunningJob = null;
						Toast.makeText(RobotService.this, "Drop stop", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RobotService.this);

				// set prompts.xml to alertdialog builder
				LayoutInflater inflater = (LayoutInflater) RobotService.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = (View) inflater.inflate(R.layout.input_dialog, null);
				alertDialogBuilder.setView(layout);

				final EditText userInput = (EditText) layout.findViewById(R.id.editTextDialogUserInput);

				// set dialog message
				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										int run = Integer.parseInt(userInput.getText().toString());
										mRunningJob = new AutoDrop(RobotService.this, mMap, run);
										Toast.makeText(RobotService.this, "Drop start!!!", Toast.LENGTH_LONG).show();
										mRunningJob.execute();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

				// show it
				alertDialog.show();
			}
		});
		mRecycleButton = (Button) mLayout.findViewById(R.id.button_recycle);
		mRecycleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((Vibrator)RobotService.this.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(100);
				if( mRunningJob != null) {
					if(mRunningJob.getStatus() == AsyncTask.Status.RUNNING) {
						mRunningJob.cancel(true);
						mRunningJob = null;
						Toast.makeText(RobotService.this, "Recycle stop", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RobotService.this);

				// set prompts.xml to alertdialog builder
				LayoutInflater inflater = (LayoutInflater) RobotService.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = (View) inflater.inflate(R.layout.input_dialog, null);
				alertDialogBuilder.setView(layout);

				final EditText userInput = (EditText) layout.findViewById(R.id.editTextDialogUserInput);

				// set dialog message
				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										int run = Integer.parseInt(userInput.getText().toString());
										mRunningJob = new AutoRecycle(RobotService.this, mMap, run);
										Toast.makeText(RobotService.this, "Recycle start!!!", Toast.LENGTH_LONG).show();
										mRunningJob.execute();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

				// show it
				alertDialog.show();
			}
		});
		mAquireButton = (Button) mLayout.findViewById(R.id.button_aquire);
		mAquireButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((Vibrator)RobotService.this.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(100);
				if( mRunningJob != null) {
					if(mRunningJob.getStatus() == AsyncTask.Status.RUNNING) {
						mRunningJob.cancel(true);
						mRunningJob = null;
						Toast.makeText(RobotService.this, "aquire stop", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RobotService.this);

				// set prompts.xml to alertdialog builder
				LayoutInflater inflater = (LayoutInflater) RobotService.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = (View) inflater.inflate(R.layout.input_dialog, null);
				alertDialogBuilder.setView(layout);

				final EditText userInput = (EditText) layout.findViewById(R.id.editTextDialogUserInput);

				// set dialog message
				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										int run = Integer.parseInt(userInput.getText().toString());
										mRunningJob = new AutoAquire(RobotService.this, mMap, run);
										Toast.makeText(RobotService.this, "Acquire start!!!", Toast.LENGTH_LONG).show();
										mRunningJob.execute();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

				// show it
				alertDialog.show();
			}
		});
		mUpButton = (Button) mLayout.findViewById(R.id.button_up);
		mUpButton.setOnClickListener(mMoveListener);
		mDownButton = (Button) mLayout.findViewById(R.id.button_down);
		mDownButton.setOnClickListener(mMoveListener);
		mLeftButton = (Button) mLayout.findViewById(R.id.button_left);
		mLeftButton.setOnClickListener(mMoveListener);
		mRightButton = (Button) mLayout.findViewById(R.id.button_right);
		mRightButton.setOnClickListener(mMoveListener);
		mMoveButton = (ToggleButton) mLayout.findViewById(R.id.button_move);
		mMoveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mMoveButton.isChecked()) {
					mUpButton.setVisibility(View.VISIBLE);
					mDownButton.setVisibility(View.VISIBLE);
					mLeftButton.setVisibility(View.VISIBLE);
					mRightButton.setVisibility(View.VISIBLE);
					mHackButton.setVisibility(View.INVISIBLE);
					mDropButton.setVisibility(View.INVISIBLE);
					mRecycleButton.setVisibility(View.INVISIBLE);
					mAquireButton.setVisibility(View.INVISIBLE);
				} else {
					mUpButton.setVisibility(View.INVISIBLE);
					mDownButton.setVisibility(View.INVISIBLE);
					mLeftButton.setVisibility(View.INVISIBLE);
					mRightButton.setVisibility(View.INVISIBLE);
					mHackButton.setVisibility(View.VISIBLE);
					mDropButton.setVisibility(View.VISIBLE);
					mRecycleButton.setVisibility(View.VISIBLE);
					mAquireButton.setVisibility(View.VISIBLE);
				}
			}
			
		});
		mMenuButton.setChecked(false);
		mMenuButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mMenuButton.isChecked()) {
					mHackButton.setVisibility(View.VISIBLE);
					mDropButton.setVisibility(View.VISIBLE);
					mRecycleButton.setVisibility(View.VISIBLE);
					mAquireButton.setVisibility(View.VISIBLE);
					mMoveButton.setVisibility(View.VISIBLE);
				} else {
					mHackButton.setVisibility(View.INVISIBLE);
					mDropButton.setVisibility(View.INVISIBLE);
					mRecycleButton.setVisibility(View.INVISIBLE);
					mAquireButton.setVisibility(View.INVISIBLE);
					mMoveButton.setVisibility(View.INVISIBLE);
				}
			}
		});
		mMenuButton.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				RobotService.this.stopSelf();
				return true;
			}
		});

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 0;
		mWindowManager.addView(mLayout, params);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Toast.makeText(this.getApplicationContext(),
		// "start command "+intent.toString(), Toast.LENGTH_LONG).show();
		return super.onStartCommand(intent, flags, startId);
	}



	private HashMap<String, Point> loadConfig() {
		HashMap<String, Point> map = new HashMap<String, Point>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					openFileInput("Config.txt")));
			String line = null;
			while ((line = in.readLine()) != null) {
				String arr[] = line.split("=");
				String point[] = arr[1].split(",");
				map.put(arr[0],
						new Point(Integer.parseInt(point[0]), Integer
								.parseInt(point[1])));
				Log.e("FakeGPS", line);
			}
			in.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mLayout != null) {
			mWindowManager.removeView(mLayout);
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}