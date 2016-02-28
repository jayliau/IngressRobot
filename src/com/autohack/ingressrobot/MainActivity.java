package com.autohack.ingressrobot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private static final String AUTO_HACK_STRING = "IngressRobot_AutoHack";
	
	private final int SELECT_PHOTO = 1246;
	private DrawImageView mImage;
	private PopupWindow mPopupWindow;
	private Bitmap mSelectedImage = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenshot_main);
		
		// screen on and unlock screen
		final Window win = getWindow();
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		  try {
		        ViewConfiguration config = ViewConfiguration.get(this);
		        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
		        if(menuKeyField != null) {
		            menuKeyField.setAccessible(true);
		            menuKeyField.setBoolean(config, false);
		        }
		    } catch (Exception ex) {
		        // Ignore
		    }
		
		mImage = (DrawImageView) this.findViewById(R.id.imageView);
		mImage.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				return true;
			}
		});
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) { 
	    case SELECT_PHOTO:
	        if(resultCode == RESULT_OK){  
	            Uri selectedImage = data.getData();
	            InputStream imageStream = null;
				try {
					imageStream = getContentResolver().openInputStream(selectedImage);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            mSelectedImage = BitmapFactory.decodeStream(imageStream);
	            mImage.setImageBitmap(mSelectedImage);
	        }
	    }
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		
		super.onResume();
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId() == R.id.action_image_pic) {
			Log.e("FakeGPS", "select menu");
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, SELECT_PHOTO );    
		}
		else if(item.getItemId() == R.id.action_start_service) {
			startService(new Intent(this, RobotService.class)); 
		}
		else if(item.getItemId() == R.id.action_debug) {
			HashMap<String, Point> map = new HashMap<String, Point>();//mImage.getMap();
			 try {
				BufferedReader in = new BufferedReader(new InputStreamReader(openFileInput("Config.txt")));
				String line = null;
				while( (line = in.readLine()) != null) {
					String arr[] = line.split("=");
					String point[] = arr[1].split(",");
					map.put(arr[0], new Point(Integer.parseInt(point[0]),Integer.parseInt(point[1])));
				}
				in.close();
			} catch (FileNotFoundException e) {
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 mImage.setMap(map);
		}
		else if(item.getItemId() == R.id.action_save_config) {
			HashMap<String, Point> map = new HashMap<String, Point>();//mImage.getMap();
			 try {
				BufferedReader in = new BufferedReader(new InputStreamReader(openFileInput("Config.txt")));
				String line = null;
				while( (line = in.readLine()) != null) {
					String arr[] = line.split("=");
					String point[] = arr[1].split(",");
					map.put(arr[0], new Point(Integer.parseInt(point[0]),Integer.parseInt(point[1])));
				}
				in.close();
			} catch (FileNotFoundException e) {
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 map.putAll(mImage.getMap());
			 
			try {
				OutputStream out= this.openFileOutput("Config.txt",  MODE_PRIVATE);
				for(Entry<String, Point> e : map.entrySet()) {
					String line = e.getKey()+"="+e.getValue().x+","+e.getValue().y+"\n"; 
					out.write(line.getBytes());
				}
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return super.onMenuItemSelected(featureId, item);
	}
/*
	@Override
	protected void onNewIntent(Intent intent) {
		// screen on and unlock screen
		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		super.onNewIntent(intent);
	}
*/
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}
}
