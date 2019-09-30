package com.maxsavteam.newmcalc.viewpagerfragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.Image;
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
	private View.OnLongClickListener[] longClickListeners;
	public Fragment2(Context context, View.OnLongClickListener[] longClick){
		c = context;
		this.longClickListeners = longClick;
	}
	private Point display_size = new Point();

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
		WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		d.getSize(display_size);
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
				R.id.btnImgPassgen,
				R.id.imgBtnAboutAG,
				R.id.imgBtnBin
		};
		if(DarkMode) {
			Drawable[] drawables;
			drawables = new Drawable[]{
					c.getResources().getDrawable(R.drawable.settings_dark),
					c.getResources().getDrawable(R.drawable.history_dark),
					c.getResources().getDrawable(R.drawable.dice_dark),
					c.getResources().getDrawable(R.drawable.passgen_dark),
					c.getResources().getDrawable(R.drawable.help_dark),
					c.getResources().getDrawable(R.drawable.binary_dark)
			};
			for (int i = 0; i < imgbtnids.length; i++) {
				img = view.findViewById(imgbtnids[i]);
				img.setImageDrawable(drawables[i]);
				ViewGroup.LayoutParams par = img.getLayoutParams();
				int width = display_size.x;
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
					c.getResources().getDrawable(R.drawable.passgen),
					c.getResources().getDrawable(R.drawable.help),
					c.getResources().getDrawable(R.drawable.binary)
			};
			for (int i = 0; i < imgbtnids.length; i++) {
				img = view.findViewById(imgbtnids[i]);
				img.setImageDrawable(drawables[i]);
				ViewGroup.LayoutParams par = img.getLayoutParams();
				int width = display_size.x;
				par.height = width / 5;
				par.width = width / 5;
				img.setLayoutParams(par);
			}
		}
		ImageButton btn = view.findViewById(imgbtnids[4]);
		ViewGroup.LayoutParams par = btn.getLayoutParams();
		int width = display_size.x;
		par.width = width / 7;
		par.height = width / 7;
		btn.setLayoutParams(par);

		return view;
	}
}
