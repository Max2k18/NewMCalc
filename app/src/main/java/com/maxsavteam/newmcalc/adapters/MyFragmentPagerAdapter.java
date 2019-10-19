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
import com.maxsavteam.newmcalc.viewpagerfragment.Fragment3;

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
			View.OnLongClickListener[] viewLongClicksFragment1,
			View.OnLongClickListener[] viewLongClicksFragment2){
		this.viewLongClicksFragment1 = viewLongClicksFragment1;
		this.viewLongClicksFragment2 = viewLongClicksFragment2;
	}

	public View getFragmentView(int which){
		switch (which){
			case 1:
				return fragment1.getView();
			case 2:
				return fragment2.getView();
			case 3:
				return fragment3.getView();
			default:
				return null;
		}
	}

	private Fragment2 fragment2;
	private Fragment1 fragment1;
	private Fragment3 fragment3;

	@NonNull
	@Override
	public Fragment getItem(int position) {
		fragment1 = new Fragment1(context, viewLongClicksFragment1);
		fragment2 = new Fragment2(context, viewLongClicksFragment2);
		fragment3 = new Fragment3(context);
		switch (position) {
			case 1:
				return fragment2;
			case 2:
				return fragment3;

			default:
				return fragment1;
		}
	}

	@Override
	public int getCount() {
		return 3;
	}


}
