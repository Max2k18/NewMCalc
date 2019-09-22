package com.maxsavteam.newmcalc.adapters;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.maxsavteam.newmcalc.viewpagerfragment.Fragment1;
import com.maxsavteam.newmcalc.viewpagerfragment.Fragment2;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
	private Context context;
	private View.OnClickListener[] viewClicksFragment1;
	private View.OnLongClickListener[] viewLongClicksFragment1;
	private View.OnClickListener[] viewClicksFragment2;
	private View.OnLongClickListener[] viewLongClicksFragment2;

	public MyFragmentPagerAdapter(FragmentManager fm, Context c){
		super(fm);
		this.context = c;
	}

	public void prepare_to_initialize(
			View.OnClickListener[] viewClicksFragment1, View.OnLongClickListener[] viewLongClicksFragment1,
			View.OnClickListener[] viewClicksFragment2, View.OnLongClickListener[] viewLongClicksFragment2){
		this.viewClicksFragment1 = viewClicksFragment1;
		this.viewLongClicksFragment1 = viewLongClicksFragment1;
		this.viewClicksFragment2 = viewClicksFragment2;
		this.viewLongClicksFragment2 = viewLongClicksFragment2;
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		Fragment1 fragment1 = new Fragment1(context, viewClicksFragment1, viewLongClicksFragment1);
		Fragment2 fragment2 =  new Fragment2(context, viewClicksFragment2, viewLongClicksFragment2);
		switch (position) {
			case 1:
			case 2:
				return fragment2;

			default:
				return fragment1;
		}
		/*if (position == 2) {
			return;
		}
		return ;*/
	}

	@Override
	public int getCount() {
		return 2;
	}
}
