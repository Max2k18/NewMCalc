package com.maxsavteam.newmcalc2.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.adapters.ViewPagerAdapter;

public class MathOperationsFragmentFactory implements ViewPagerAdapter.ViewPagerFragmentFactory {

	public static final int TYPE = 2;

	@Override
	public View justCreateView(ViewGroup parent) {
		return LayoutInflater.from( parent.getContext() ).inflate( R.layout.fragment_math_operations, parent, false );
	}

	@Override
	public void bindView(View view, int parentHeight) {

	}

	@Override
	public int getType() {
		return TYPE;
	}
}
