package com.maxsavteam.newmcalc2.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.adapters.ViewPagerAdapter;
import com.maxsavteam.newmcalc2.variables.Variable;
import com.maxsavteam.newmcalc2.variables.VariableUtils;

import java.util.ArrayList;

public class VariablesFragmentFactory implements ViewPagerAdapter.ViewPagerFragmentFactory {
	public static final int TYPE = 3;

	private final View.OnLongClickListener mMemoryActionsLongClickListener;
	private final View.OnLongClickListener mVariableButtonsLongClickListener;
	private final Context mContext;

	public VariablesFragmentFactory(Context context, View.OnLongClickListener memoryActionsLongClickListener, View.OnLongClickListener variableButtonsLongClickListener) {
		mMemoryActionsLongClickListener = memoryActionsLongClickListener;
		mVariableButtonsLongClickListener = variableButtonsLongClickListener;
		mContext = context;
	}

	@Override
	public View justCreateView(ViewGroup parent) {
		return LayoutInflater.from( mContext ).inflate( R.layout.variables_fragment_layout, parent, false );
	}

	@Override
	public void bindView(View view) {
		Button b = view.findViewById( R.id.btnMR );
		b.setOnLongClickListener( mMemoryActionsLongClickListener );

		b = view.findViewById( R.id.btnMS );
		b.setOnLongClickListener( mMemoryActionsLongClickListener );

		setVariableButtons(view);
	}

	@Override
	public int getType() {
		return TYPE;
	}

	private int findButtonByTag(int tag) {
		switch ( tag ) {
			case 1:
				return R.id.btnVar1;
			case 2:
				return R.id.btnVar2;
			case 3:
				return R.id.btnVar3;
			case 4:
				return R.id.btnVar4;
			case 5:
				return R.id.btnVar5;
			case 6:
				return R.id.btnVar6;
			case 7:
				return R.id.btnVar7;
			case 8:
				return R.id.btnVar8;
			default:
				return 0;
		}
	}

	private void setVariableButtons(View view) {
		setDefaultButtons(view);
		ArrayList<Variable> a = VariableUtils.readVariables();
		for (int i = 0; i < a.size(); i++) {
			Button b = view.findViewById( findButtonByTag( a.get( i ).getTag() ) );
			b.setText( a.get( i ).getName() );
			b.setTextSize( TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize( R.dimen.variables_buttons_text_size ) );
			b.setContentDescription( a.get( i ).getValue() );
			b.setOnLongClickListener( mVariableButtonsLongClickListener );
			b.setTransformationMethod( null );
		}
	}

	private void setDefaultButtons(View view) {
		for (int i = 1; i <= 8; i++) {
			Button button = view.findViewById( findButtonByTag( i ) );
			button.setText( "+" );
			button.setTextSize( TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize( R.dimen.variables_buttons_text_size_default ) );
		}
	}

}
