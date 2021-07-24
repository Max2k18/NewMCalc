package com.maxsavteam.newmcalc2.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.adapters.ViewPagerAdapter;

public class MathOperationsFragmentFactory implements ViewPagerAdapter.ViewPagerFragmentFactory {

	public static final int TYPE = 2;

	@Override
	public View justCreateView(ViewGroup parent) {
		return LayoutInflater.from( parent.getContext() ).inflate( R.layout.math_operations_fragment, parent, false );
	}

	@Override
	public void bindView(View view) {

	}

	@Override
	public int getType() {
		return TYPE;
	}
}
