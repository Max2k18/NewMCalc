package com.maxsavteam.newmcalc.viewpagerfragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxsavteam.newmcalc.R;

public class Fragment2 extends Fragment {
	private Context c;
	private View.OnClickListener[] clicks;
	private View.OnLongClickListener[] longClickListeners;
	public Fragment2(Context context, View.OnClickListener[] v, View.OnLongClickListener[] longClick){
		c = context;
		this.clicks = v;
		this.longClickListeners = longClick;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_2, container, false);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
		boolean DarkMode = sp.getBoolean("dark_mode", false);
		Button b = view.findViewById(R.id.btnMR);
		b.setOnLongClickListener(longClickListeners[0]);
		b = view.findViewById(R.id.btnMS);
		b.setOnLongClickListener(longClickListeners[0]);
		b = view.findViewById(R.id.btnMemPlus);
		b.setOnClickListener(clicks[0]);
		b = view.findViewById(R.id.btnMemMinus);
		b.setOnClickListener(clicks[1]);
		int[] ids = new int[]{
				R.id.btnMR,
				R.id.btnMS,
				R.id.btnMemMinus,
				R.id.btnMemPlus
		};
		for(int id : ids){
			b = view.findViewById(id);
			b.setTextColor(ColorStateList.valueOf(c.getResources().getColor(R.color.colorAccent)));
		}
		ImageButton img;
		int[] imgbtnids = new int[]{
				R.id.imgBtnSettings,
				R.id.btnImgHistory,
				R.id.btnImgNumGen,
				R.id.btnImgPassgen
		};
		if(DarkMode) {
			Drawable[] drawables;
			drawables = new Drawable[]{
					c.getResources().getDrawable(R.drawable.settings_dark),
					c.getResources().getDrawable(R.drawable.history_dark),
					c.getResources().getDrawable(R.drawable.dice_dark),
					c.getResources().getDrawable(R.drawable.passgen_dark)
			};
			for (int i = 0; i < imgbtnids.length; i++) {
				img = view.findViewById(imgbtnids[i]);
				img.setImageDrawable(drawables[i]);
				ViewGroup.LayoutParams par = img.getLayoutParams();
				WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
				Display d = wm.getDefaultDisplay();
				Point p = new Point();
				d.getSize(p);
				int width = p.x;
				par.height = width / 5;
				par.width = width / 5;
				img.setLayoutParams(par);
			}
		}else{
			Drawable[] drawables;
			drawables = new Drawable[]{
					c.getResources().getDrawable(R.drawable.settings),
					c.getResources().getDrawable(R.drawable.history),
					c.getResources().getDrawable(R.drawable.dice),
					c.getResources().getDrawable(R.drawable.passgen)
			};
			for (int i = 0; i < imgbtnids.length; i++) {
				img = view.findViewById(imgbtnids[i]);
				img.setImageDrawable(drawables[i]);
				ViewGroup.LayoutParams par = img.getLayoutParams();
				WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
				Display d = wm.getDefaultDisplay();
				Point p = new Point();
				d.getSize(p);
				int width = p.x;
				par.height = width / 5;
				par.width = width / 5;
				img.setLayoutParams(par);
			}
		}

		return view;
	}
}
