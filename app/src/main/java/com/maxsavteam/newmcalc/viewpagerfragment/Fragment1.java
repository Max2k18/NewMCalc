package com.maxsavteam.newmcalc.viewpagerfragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.maxsavteam.newmcalc.R;

public class Fragment1 extends Fragment {
	private boolean DarkMode = false;
	private Context c;
	private View.OnLongClickListener[] longClickListeners;
	public Fragment1(Context context, View.OnLongClickListener[] longClick){
		c = context;
		this.longClickListeners = longClick;
	}

	@Nullable
	@Override
	public View getView() {
		return view;
	}

	private View view;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_1, container, false);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
		DarkMode = sp.getBoolean("dark_mode", false);
		String[] arr = c.getResources().getStringArray(R.array.additional_chars);
		int[] btnIds = {
				R.id.btn7,
				R.id.btn8,
				R.id.btn4,
				R.id.btn5,
				R.id.btn1,
				R.id.btn2,

				R.id.btn9,
				R.id.btnMult,
				R.id.btn6,
				R.id.btnDiv,
				R.id.btn3,
				R.id.btnPlus,

				R.id.btnZero,
				R.id.btnDot
		};
		for(int ii = 0; ii < btnIds.length; ii++){
			Button btn = view.findViewById(btnIds[ii]);
			btn.setTransformationMethod(null);
			btn.setOnLongClickListener(longClickListeners[0]);
			String num = btn.getText().toString().substring(0, 1);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				btn.setText(Html.fromHtml(num + "<sup><small><small><small>" + arr[ii] + "</small></small></small></sup>", Html.FROM_HTML_MODE_COMPACT));
			}else{
				btn.setText(Html.fromHtml(num + "<sup><small><small><small>" + arr[ii] + "</small></small></small></sup>"));
			}
		}
		Button b = view.findViewById(R.id.btnCalc);
		b.setOnLongClickListener(longClickListeners[1]);
		if(DarkMode) {
			b.setTextColor(getResources().getColor(R.color.white));
			b = view.findViewById(R.id.btnDelAll);
			b.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.white)));
			b.setTextColor(c.getResources().getColor(R.color.black));
		} else {
			b.setTextColor(getResources().getColor(R.color.black));
			b = view.findViewById(R.id.btnDelAll);
			b.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.black)));
			b.setTextColor(c.getResources().getColor(R.color.white));
		}
		b = view.findViewById(R.id.btnDelete);
		b.setOnLongClickListener(longClickListeners[2]);

		return view;
	}
}
