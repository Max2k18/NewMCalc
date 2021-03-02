package com.maxsavteam.newmcalc2.fragments.fragment2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.variables.Variable;
import com.maxsavteam.newmcalc2.variables.VariableUtils;

import java.util.ArrayList;

public class Fragment2 extends Fragment {
	private final Context mContext;
	private final View view;
	private final View.OnLongClickListener[] longClickListeners;
	private final Point mDisplaySize = new Point();

	/*
	 *  0 - memory
	 *  1 - variable buttons
	 */
	public Fragment2(InitializationObject initializationObject){
		mContext = initializationObject.getContext();
		this.longClickListeners = initializationObject.getLongClickListeners();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.fragment_2, null, false);
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
		if ( a == null ) {
			return;
		}
		for (int i = 0; i < a.size(); i++) {
			Button b = view.findViewById( findButtonByTag( a.get( i ).getTag() ) );
			b.setText( a.get( i ).getName() );
			b.setTextSize( TypedValue.COMPLEX_UNIT_SP, 18 );
			b.setContentDescription( a.get( i ).getValue() );
			b.setOnLongClickListener( longClickListeners[ 1 ] );
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
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
		boolean DarkMode = sp.getBoolean("dark_mode", false);
		Button b = view.findViewById(R.id.btnMR);
		b.setOnLongClickListener(longClickListeners[0]);
		b = view.findViewById(R.id.btnMS);
		b.setOnLongClickListener(longClickListeners[0]);
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		d.getSize(mDisplaySize);
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
		TextView var = view.findViewById(R.id.variables);
		if(DarkMode){
			var.setTextColor(Color.WHITE);
		}else {
			var.setTextColor(Color.BLACK);
		}
		setVariableButtons();
		return view;
	}

	public static class InitializationObject {
		private Context mContext;
		private View.OnLongClickListener[] mLongClickListeners;

		public Context getContext() {
			return mContext;
		}

		public InitializationObject setContext(Context context) {
			mContext = context;
			return this;
		}

		public View.OnLongClickListener[] getLongClickListeners() {
			return mLongClickListeners;
		}

		public InitializationObject setLongClickListeners(View.OnLongClickListener[] longClickListeners) {
			mLongClickListeners = longClickListeners;
			return this;
		}
	}
}
