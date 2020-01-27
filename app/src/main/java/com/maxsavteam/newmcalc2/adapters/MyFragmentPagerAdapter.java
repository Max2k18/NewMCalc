package com.maxsavteam.newmcalc2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.maxsavteam.newmcalc2.fragments.fragment1.Fragment1;
import com.maxsavteam.newmcalc2.fragments.fragment1.FragmentOneInitializationObject;
import com.maxsavteam.newmcalc2.fragments.fragment2.Fragment2;
import com.maxsavteam.newmcalc2.fragments.fragment2.FragmentTwoInitializationObject;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
	private FragmentOneInitializationObject mFragmentOneInitializationObject;
	private FragmentTwoInitializationObject mFragmentTwoInitializationObject;

	public MyFragmentPagerAdapter(FragmentAdapterInitializationObject initializationObject){
		super(initializationObject.getFragmentManager());
		this.mFragmentOneInitializationObject = initializationObject.getFragmentOneInitializationObject();
		this.mFragmentTwoInitializationObject = initializationObject.getFragmentTwoInitializationObject();
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		if (position == 1) {
			return new Fragment2(mFragmentTwoInitializationObject);
		}
		return new Fragment1(mFragmentOneInitializationObject);
	}

	@Override
	public int getCount() {
		return 2;
	}
}
