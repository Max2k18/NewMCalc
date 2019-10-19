package com.maxsavteam.newmcalc.viewpagerfragment;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxsavteam.newmcalc.R;

import java.net.ContentHandler;

public class Fragment3 extends Fragment {

	private Context c;

	public Fragment3(Context context) {
		this.c = context;
	}
	private Point display_size = new Point();

	@Nullable
	@Override
	public View getView() {
		return view;
	}

	private View view;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_3, container, false);
		ImageButton img;
		boolean DarkMode = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext()).getBoolean("dark_mode", false);
		WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		d.getSize(display_size);
		int[] imgbtnids = new int[]{
				R.id.imgBtnSettings,
				R.id.btnImgHistory,
				R.id.btnImgNumGen,
				R.id.btnImgPassgen,
				R.id.imgBtnBin
		};
		Drawable[] drawables;
		if(DarkMode) {
			drawables = new Drawable[]{
					c.getResources().getDrawable(R.drawable.settings_dark),
					c.getResources().getDrawable(R.drawable.history_dark),
					c.getResources().getDrawable(R.drawable.dice_dark),
					c.getResources().getDrawable(R.drawable.passgen_dark),
					c.getResources().getDrawable(R.drawable.binary_dark)
			};
		}else{
			drawables = new Drawable[]{
					c.getResources().getDrawable(R.drawable.settings),
					c.getResources().getDrawable(R.drawable.history),
					c.getResources().getDrawable(R.drawable.dice),
					c.getResources().getDrawable(R.drawable.passgen),
					c.getResources().getDrawable(R.drawable.binary)
			};
		}
		final int BUTTON_WIDTH = 4;
		for (int i = 0; i < imgbtnids.length; i++) {
			img = view.findViewById(imgbtnids[i]);
			img.setImageDrawable(drawables[i]);
			ViewGroup.LayoutParams par = img.getLayoutParams();
			int width = display_size.x;
			par.height = width / BUTTON_WIDTH;
			par.width = width / BUTTON_WIDTH;
			img.setLayoutParams(par);
		}
		return view;
	}
}
