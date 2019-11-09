package com.maxsavteam.newmcalc.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.maxsavteam.newmcalc.viewpagerfragment.fragment1.Fragment1;
import com.maxsavteam.newmcalc.viewpagerfragment.fragment1.FragmentOneInitializationObject;
import com.maxsavteam.newmcalc.viewpagerfragment.fragment2.Fragment2;
import com.maxsavteam.newmcalc.viewpagerfragment.fragment2.FragmentTwoInitializationObject;
import com.maxsavteam.newmcalc.viewpagerfragment.fragment3.Fragment3;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
	private Context context;
	private FragmentOneInitializationObject mFragmentOneInitializationObject;
	private FragmentTwoInitializationObject mFragmentTwoInitializationObject;

	public MyFragmentPagerAdapter(FragmentAdapterInitializationObject initializationObject){
		super(initializationObject.getFragmentManager());
		this.context = initializationObject.getContext();
		this.mFragmentOneInitializationObject = initializationObject.getFragmentOneInitializationObject();
		this.mFragmentTwoInitializationObject = initializationObject.getFragmentTwoInitializationObject();
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 1:
				return new Fragment2(mFragmentTwoInitializationObject);
			case 2:
				return new Fragment3(context);

			default:
				return new Fragment1(mFragmentOneInitializationObject);
		}
	}

	@Override
	public int getCount() {
		return 2;
	}
}
