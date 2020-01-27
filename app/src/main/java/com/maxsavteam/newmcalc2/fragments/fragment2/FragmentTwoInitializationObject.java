package com.maxsavteam.newmcalc2.fragments.fragment2;

import android.content.Context;
import android.view.View;

public class FragmentTwoInitializationObject {
	private Context mContext;
	private View.OnLongClickListener[] mLongClickListeners;

	public Context getContext() {
		return mContext;
	}

	public FragmentTwoInitializationObject setContext(Context context) {
		mContext = context;
		return this;
	}

	public View.OnLongClickListener[] getLongClickListeners() {
		return mLongClickListeners;
	}

	public FragmentTwoInitializationObject setLongClickListeners(View.OnLongClickListener[] longClickListeners) {
		mLongClickListeners = longClickListeners;
		return this;
	}
}
