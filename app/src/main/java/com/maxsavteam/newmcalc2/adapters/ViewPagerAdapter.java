package com.maxsavteam.newmcalc2.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStateAdapter {

	private final ArrayList<ViewPagerFragmentFactory> mFactories;

	public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<ViewPagerFragmentFactory> f) {
		super( fragmentActivity );
		mFactories = f;
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
		return mFactories.get( position ).createFragment();
	}

	@Override
	public int getItemCount() {
		return mFactories.size();
	}

	public interface ViewPagerFragmentFactory {

		Fragment createFragment();

	}
}
