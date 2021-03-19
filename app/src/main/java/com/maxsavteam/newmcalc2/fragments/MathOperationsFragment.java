package com.maxsavteam.newmcalc2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxsavteam.newmcalc2.R;

public class MathOperationsFragment extends Fragment {

	private View mView;

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View getView() {
		return mView;
	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		mView = inflater.inflate( R.layout.math_operations_fragment, container, false );
		return mView;
	}
}
