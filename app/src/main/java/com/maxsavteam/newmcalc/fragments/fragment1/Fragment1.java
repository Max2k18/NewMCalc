package com.maxsavteam.newmcalc.fragments.fragment1;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxsavteam.newmcalc.R;

public class Fragment1 extends Fragment {
	private Context c;
	private View.OnLongClickListener[] longClickListeners;
	public Fragment1(FragmentOneInitializationObject fragmentOneInitializationObject){
		c = fragmentOneInitializationObject.getContext();
		this.longClickListeners = fragmentOneInitializationObject.getLongClickListeners();
	}

	@Override
	public View getView() {
		return view;
	}

	private View view;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_1, container, false);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
		boolean darkMode = sp.getBoolean("dark_mode", false);
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
				R.id.btnDot,
				R.id.btnMinus
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
		if(darkMode) {
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
		WindowManager windowManager = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
		if(c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && windowManager != null) {
			TableLayout tbl = view.findViewById(R.id.tbl);
			ViewGroup.LayoutParams tblLayoutParams = tbl.getLayoutParams();
			Display display = windowManager.getDefaultDisplay();
			Point displaySize = new Point();
			display.getSize(displaySize);
			/*tblLayoutParams.height = displaySize.y;
			tbl.setLayoutParams(tblLayoutParams);*/
			try {
				tblLayoutParams = view.getLayoutParams();
				tblLayoutParams.height = displaySize.y;
				view.setLayoutParams(tblLayoutParams);
			}catch (Exception e){
				Toast.makeText(c, e.toString(), Toast.LENGTH_LONG).show();
			}
		}
		return view;
	}

}
