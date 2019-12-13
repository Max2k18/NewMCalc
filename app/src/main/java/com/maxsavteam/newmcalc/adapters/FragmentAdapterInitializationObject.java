package com.maxsavteam.newmcalc.adapters;

import android.content.Context;

import androidx.fragment.app.FragmentManager;

import com.maxsavteam.newmcalc.fragments.fragment1.FragmentOneInitializationObject;
import com.maxsavteam.newmcalc.fragments.fragment2.FragmentTwoInitializationObject;

public class FragmentAdapterInitializationObject {
	private FragmentManager mFragmentManager;
	private Context mContext;
	private FragmentOneInitializationObject mFragmentOneInitializationObject;
	private FragmentTwoInitializationObject mFragmentTwoInitializationObject;

	FragmentManager getFragmentManager() {
		return mFragmentManager;
	}

	public FragmentAdapterInitializationObject setFragmentManager(FragmentManager fragmentManager) {
		mFragmentManager = fragmentManager;
		return this;
	}

	public Context getContext() {
		return mContext;
	}

	public FragmentAdapterInitializationObject setContext(Context context) {
		mContext = context;
		return this;
	}

	FragmentOneInitializationObject getFragmentOneInitializationObject() {
		return mFragmentOneInitializationObject;
	}

	public FragmentAdapterInitializationObject setFragmentOneInitializationObject(FragmentOneInitializationObject fragmentOneInitializationObject) {
		mFragmentOneInitializationObject = fragmentOneInitializationObject;
		return this;
	}

	FragmentTwoInitializationObject getFragmentTwoInitializationObject() {
		return mFragmentTwoInitializationObject;
	}

	public FragmentAdapterInitializationObject setFragmentTwoInitializationObject(FragmentTwoInitializationObject fragmentTwoInitializationObject) {
		mFragmentTwoInitializationObject = fragmentTwoInitializationObject;
		return this;
	}
}
