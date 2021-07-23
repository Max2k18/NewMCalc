package com.maxsavteam.newmcalc2.fragments;

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

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumPadFragment extends Fragment {

	private View view;

	private View.OnLongClickListener mCalculateButtonLongClickListener;

	public NumPadFragment() {
	}

	public NumPadFragment(View.OnLongClickListener calculateButtonLongClickListener) {
		mCalculateButtonLongClickListener = calculateButtonLongClickListener;
	}

	@Override
	public View getView() {
		return view;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		//view = inflater.inflate( R.layout.fragment_1, container, false );
		view = LayoutInflater.from( getContext() ).inflate( R.layout.numpad_fragment_layout, container, false );

		Button b = view.findViewById( R.id.btnCalc );
		b.setOnLongClickListener( mCalculateButtonLongClickListener );

		Locale locale;
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
			locale = getContext().getResources().getConfiguration().getLocales().get( 0 );
		}else{
			locale = getContext().getResources().getConfiguration().locale;
		}
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
		b = view.findViewById( R.id.btnDot );
		b.setText( String.valueOf( symbols.getDecimalSeparator() ) );

		return view;
	}
}
