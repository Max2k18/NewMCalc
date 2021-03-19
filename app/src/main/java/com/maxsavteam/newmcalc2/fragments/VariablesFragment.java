package com.maxsavteam.newmcalc2.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.variables.Variable;
import com.maxsavteam.newmcalc2.variables.VariableUtils;

import java.util.ArrayList;

public class VariablesFragment extends Fragment {
	private final Context mContext;
	private View view;

	private final View.OnLongClickListener mMemoryActionsLongClickListener;
	private final View.OnLongClickListener mVariableButtonsLongClickListener;

	public VariablesFragment(Context context, View.OnLongClickListener memoryActionsLongClickListener, View.OnLongClickListener variableButtonsLongClickListener) {
		mContext = context;
		mMemoryActionsLongClickListener = memoryActionsLongClickListener;
		mVariableButtonsLongClickListener = variableButtonsLongClickListener;
	}

	@Nullable
	@Override
	public View getView() {
		return view;
	}

	private int findButtonByTag(int tag){
		switch (tag){
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

	private void setVariableButtons() {
		setDefaultButtons();
		ArrayList<Variable> a = VariableUtils.readVariables();
		for (int i = 0; i < a.size(); i++) {
			Button b = view.findViewById( findButtonByTag( a.get( i ).getTag() ) );
			b.setText( a.get( i ).getName() );
			b.setTextSize( TypedValue.COMPLEX_UNIT_SP, 18 );
			b.setContentDescription( a.get( i ).getValue() );
			b.setOnLongClickListener( mVariableButtonsLongClickListener );
			b.setTransformationMethod( null );
		}
	}

	private void setDefaultButtons(){
		for(int i = 1; i <= 8; i++){
			Button button = view.findViewById(findButtonByTag(i));
			button.setText("+");
			button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate( R.layout.variables_fragment_layout, container, false );

		Button b = view.findViewById(R.id.btnMR);
		b.setOnLongClickListener( mMemoryActionsLongClickListener );

		b = view.findViewById(R.id.btnMS);
		b.setOnLongClickListener( mMemoryActionsLongClickListener );

		int[] ids = new int[]{
				R.id.btnMR,
				R.id.btnMS,
				R.id.btnMemMinus,
				R.id.btnMemPlus
		};
		for(int id : ids) {
			b = view.findViewById(id);
			b.setTextColor(ColorStateList.valueOf(mContext.getResources().getColor(R.color.colorAccent)));
		}
		setVariableButtons();
		return view;
	}

}
