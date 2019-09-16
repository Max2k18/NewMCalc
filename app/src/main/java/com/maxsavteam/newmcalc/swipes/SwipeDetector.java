package com.maxsavteam.newmcalc.swipes;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import static java.lang.Math.abs;

public class SwipeDetector implements View.OnTouchListener {
	private float downY;
	private float downX;

	private touch t;

	public void setTouch(touch T){
		this.t = T;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {

		switch (motionEvent.getAction()){
			case MotionEvent.ACTION_DOWN:{
				view.setOnLongClickListener(null);
				downX = motionEvent.getX();
				downY = motionEvent.getY();
				return false;
			}
			case MotionEvent.ACTION_UP:{
				view.setOnLongClickListener(null);
				float upX = motionEvent.getX();
				float upY = motionEvent.getY();
				float deltaX = downX - upX, deltaY = downY - upY;
				int MIN_DISTANCE = 5;
				if(abs(deltaX) > abs(deltaY)){
					if(abs(deltaX) > MIN_DISTANCE){
						if(deltaX > 0){
							t.onRightSwipe(view, abs(deltaX));
							return true;
						}
						if(deltaX < 0){
							t.onLeftSwipe(view, abs(deltaX));
							return true;
						}
					}else
						return false;
				}else{
					if(abs(deltaY) > MIN_DISTANCE){
						if(deltaY < 0){
							t.onDownSwipe(view, abs(deltaY));
							return true;
						}
						if(deltaY > 0){
							t.onUpSwipe(view, abs(deltaY));
							return true;
						}
					}else
						return false;
				}
			}
		}
		return false;
	}

	public interface touch{
		void onUpSwipe(View v, float distance);
		void onRightSwipe(View v, float distance);
		void onDownSwipe(View v, float distance);
		void onLeftSwipe(View v, float distance);
	}
}
