package com.maxsavteam.newmcalc2.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.adapters.ViewPagerAdapter;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumPadFragment implements ViewPagerAdapter.ViewPagerFragmentFactory {

	public static final int TYPE = 1;

	private final View.OnLongClickListener mCalculateButtonLongClickListener;
	private final Context mContext;

	public NumPadFragment(Context context, View.OnLongClickListener calculateButtonLongClickListener) {
		mCalculateButtonLongClickListener = calculateButtonLongClickListener;
		mContext = context;
	}

	@Override
	public View justCreateView(ViewGroup parent) {
		return LayoutInflater.from( mContext ).inflate( R.layout.numpad_fragment_layout, parent, false );
	}

	@Override
	public void bindView(View view) {
		Button b = view.findViewById( R.id.btnCalc );
		b.setOnLongClickListener( mCalculateButtonLongClickListener );

		Locale locale;
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
			locale = mContext.getResources().getConfiguration().getLocales().get( 0 );
		}else{
			locale = mContext.getResources().getConfiguration().locale;
		}
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
		b = view.findViewById( R.id.btnDot );
		b.setText( String.valueOf( symbols.getDecimalSeparator() ) );
	}

	@Override
	public int getType() {
		return TYPE;
	}
}
