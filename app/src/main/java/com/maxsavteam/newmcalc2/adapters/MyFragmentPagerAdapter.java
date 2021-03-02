package com.maxsavteam.newmcalc2.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.maxsavteam.newmcalc2.fragments.fragment1.Fragment1;
import com.maxsavteam.newmcalc2.fragments.fragment2.Fragment2;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
	private final Fragment1.InitializationObject mInitializationObjectF1;
	private final Fragment2.InitializationObject mInitializationObjectF2;

	public MyFragmentPagerAdapter(InitializationObject initializationObject){
		super(initializationObject.getFragmentManager());
		this.mInitializationObjectF1 = initializationObject.getInitializationObjectF1();
		this.mInitializationObjectF2 = initializationObject.getInitializationObjectF2();
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		if (position == 1) {
			return new Fragment2( mInitializationObjectF2 );
		}
		return new Fragment1( mInitializationObjectF1 );
	}

	@Override
	public int getCount() {
		return 2;
	}

	public static class InitializationObject {
		private FragmentManager mFragmentManager;
		private Context mContext;
		private Fragment1.InitializationObject mInitializationObjectF1;
		private Fragment2.InitializationObject mInitializationObjectF2;

		FragmentManager getFragmentManager() {
			return mFragmentManager;
		}

		public InitializationObject setFragmentManager(FragmentManager fragmentManager) {
			mFragmentManager = fragmentManager;
			return this;
		}

		public Context getContext() {
			return mContext;
		}

		public InitializationObject setContext(Context context) {
			mContext = context;
			return this;
		}

		Fragment1.InitializationObject getInitializationObjectF1() {
			return mInitializationObjectF1;
		}

		public InitializationObject setInitializationObject(Fragment1.InitializationObject initializationObject) {
			mInitializationObjectF1 = initializationObject;
			return this;
		}

		Fragment2.InitializationObject getInitializationObjectF2() {
			return mInitializationObjectF2;
		}

		public InitializationObject setInitializationObject(Fragment2.InitializationObject initializationObject) {
			mInitializationObjectF2 = initializationObject;
			return this;
		}
	}
}
