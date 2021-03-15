package com.maxsavteam.newmcalc2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
	private final ArrayList<Fragment> mFragments;

	public MyFragmentPagerAdapter(@NonNull FragmentManager fm, ArrayList<Fragment> fragments) {
		super( fm );
		mFragments = fragments;
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		return mFragments.get( position );
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

}
