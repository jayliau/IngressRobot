package com.autohack.ingressrobot;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public abstract class BaseAuto extends AsyncTask<Void, String, Void> {

	private Context mContext;
	public BaseAuto(Context context) {
		mContext = context;
	}
	@Override
	protected void onPreExecute() {
		Toast.makeText(mContext, this.getClass().getName()+" started !!!", Toast.LENGTH_LONG).show();
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Void result) {
		Toast.makeText(mContext, this.getClass().getName()+" done !!!", Toast.LENGTH_LONG).show();
		
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled() {
		Toast.makeText(mContext, this.getClass().getName()+" canceled !!!", Toast.LENGTH_LONG).show();
		
		super.onCancelled();
	}
	
	@Override
	protected void onProgressUpdate(String... values) {
		if(values.length > 0 && values[0].length() > 0) {
			Toast.makeText(mContext, values[0], Toast.LENGTH_LONG).show();
		}
		super.onProgressUpdate(values);
	}

}
