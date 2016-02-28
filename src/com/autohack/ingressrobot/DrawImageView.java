package com.autohack.ingressrobot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class DrawImageView extends ImageView {

	
	
	private HashMap<String,Point> mClickPoints = new HashMap<String,Point>();
	private Paint mPaint = new Paint();
	private Context mContext;
	private GestureDetector mGestureDetector;
	private LongPressListener mListener = new LongPressListener();
	
	class MyDialog extends Dialog implements View.OnClickListener {

		Point mPoint;
		
		public MyDialog(Context context, int theme, Point p) {
			super(context, theme);
			setContentView(R.layout.point_button);
			findViewById(R.id.hack).setOnClickListener(this);
			findViewById(R.id.ok).setOnClickListener(this);
			findViewById(R.id.center).setOnClickListener(this);
			findViewById(R.id.item).setOnClickListener(this);
			findViewById(R.id.drop).setOnClickListener(this);
			findViewById(R.id.recycle).setOnClickListener(this);
			findViewById(R.id.ops).setOnClickListener(this);
			findViewById(R.id.aquire).setOnClickListener(this);
			mPoint = p;
		}

		public void onClick(View v) {
			String text = ((Button) v).getText().toString();
			mClickPoints.put(text,mPoint);
			this.dismiss();
			DrawImageView.this.invalidate();
		}
	}

	public HashMap<String, Point> getMap() {
		return mClickPoints;
	}
	public void setMap(HashMap<String, Point> map) {
		mClickPoints.clear();
		mClickPoints.putAll(map);
		this.invalidate();
	}
	public class LongPressListener implements OnGestureListener {

		@Override
		public void onLongPress(MotionEvent arg0) {
			Log.e("FakeGPS", arg0.toString());
			MyDialog dialog = new MyDialog(mContext,R.style.PointDialog, new Point((int)arg0.getX(),(int)arg0.getY()));//��w�۩w�q�˦�
			dialog.show();
			

		}

		@Override
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onShowPress(MotionEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	public DrawImageView(Context context) {
		super(context);
		mContext = context;
		initialView();
	}
	public DrawImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initialView();
    }

	public DrawImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initialView();
    }
	private void initialView() {
		mGestureDetector = new GestureDetector(mContext,mListener);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mClickPoints.size() > 0) {
			for( Entry<String,Point> p : mClickPoints.entrySet()) {
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(Color.WHITE);
			mPaint.setStrokeWidth(5.0f);
			mPaint.setTextSize(50);
			canvas.drawCircle(p.getValue().x, p.getValue().y, 10, mPaint);
			canvas.drawText(p.getKey(), p.getValue().x, p.getValue().y+30, mPaint);
			
			Log.e("FakeGPS", "draw point "+p.toString());
			}
		}
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.e("FakeGPS", event.toString());
		mGestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
}
