package com.maxsavteam.newmcalc.fragments.fragment1;

import android.content.Context;
import android.view.View;

public class FragmentOneInitializationObject {
	private Context mContext;
	private View.OnLongClickListener[] mLongClickListeners;
	private int mActionBarHeight;

	/*int getActionBarHeight() {
		return mActionBarHeight;
	}

	public FragmentOneInitializationObject setActionBarHeight(int actionBarHeight) {
		mActionBarHeight = actionBarHeight;
		return this;
	}*/

	public Context getContext() {
		return mContext;
	}

	public FragmentOneInitializationObject setContext(Context context) {
		mContext = context;
		return this;
	}

	View.OnLongClickListener[] getLongClickListeners() {
		return mLongClickListeners;
	}

	public FragmentOneInitializationObject setLongClickListeners(View.OnLongClickListener[] longClickListeners) {
		mLongClickListeners = longClickListeners;
		return this;
	}
}
