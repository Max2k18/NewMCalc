package com.maxsavteam.newmcalc;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Updater extends AppCompatActivity {

	public Drawable btndr, btnbl;
	public Boolean downloading = false;
	public FirebaseDatabase db = FirebaseDatabase.getInstance();
	public DatabaseReference ref = db.getReference("versionCode");
	public DatabaseReference refCount = db.getReference("version");
	public File localFile;
	public String file_url_path = "http://maxsavteam.tk/apk/NewMCalc.apk";
	public SharedPreferences sp;
	public AlertDialog deval;
	public AlertDialog about_dev;
	public String newversion = "";
	public File outputFile = null;
	public String vk = "maksin.colf", insta = "maksin.colf/", facebook = "profile.php?id=100022307565005", tw = "maks_savitsky", site = "maxsavteam.tk";
	public int layUpVis;

	ValueEventListener list = new ValueEventListener() {
		@Override
		public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
			switch (Objects.requireNonNull(dataSnapshot.getKey())) {
				case "vk":
					vk = dataSnapshot.getValue(String.class);
					break;
				case "insta":
					insta = dataSnapshot.getValue(String.class);
					break;
				case "twitter":
					tw = dataSnapshot.getValue(String.class);
					break;
				case "facebook":
					facebook = dataSnapshot.getValue(String.class);
					break;
				case "site":
					site = dataSnapshot.getValue(String.class);
					break;
			}

		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError) {
			Toast.makeText(getApplicationContext(), "cancelled", Toast.LENGTH_SHORT).show();
		}
	};
	AlertDialog dl;
	View.OnClickListener notJoin = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			//findViewById(R.id.layDev).setVisibility(View.GONE);
			sp.edit().putBoolean("show_laydev", false).apply();
			deval.cancel();
		}
	};
	View.OnClickListener social = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent in = new Intent(Intent.ACTION_VIEW);
			if (v.getId() == R.id.imgBtnVk) {
				in.setData(Uri.parse("https://vk.com/" + vk));
			} else if (v.getId() == R.id.imgBtnInsta) {
				in.setData(Uri.parse("https://instagram.com/" + insta));
			} else if (v.getId() == R.id.imgBtnTw) {
				in.setData(Uri.parse("https://twitter.com/" + tw));
			} else if (v.getId() == R.id.imgBtnWeb) {
				in.setData(Uri.parse("https://" + site));
			} else if (v.getId() == R.id.btnImgMore) {
				in.setData(Uri.parse("https://" + site + "/Apps.m/"));
			}
			startActivity(in);
		}
	};
	View.OnLongClickListener show_join = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			sp.edit().remove("stop_receive_all").apply();
			sp.edit().remove("show_laydev").apply();
			Toast.makeText(getApplicationContext(), "You can join to testers community", Toast.LENGTH_SHORT).show();
			return true;
		}
	};
	/*View.OnClickListener join = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			sp.edit().putBoolean("isdev", true).apply();
			setContentView(R.layout.updater_main);
			//findViewById(R.id.layDev).setVisibility(View.GONE);
			sp.edit().putBoolean("show_laydev", false).apply();
			findViewById(R.id.btnStopReceive).setVisibility(View.VISIBLE);
			deval.cancel();
			Toast.makeText(getApplicationContext(), "Now you are a tester!\nTo apply the settings, restart the application.", Toast.LENGTH_SHORT).show();
		}
	};*/
	AlertDialog report_al;
	Intent send = new Intent(Intent.ACTION_SEND);
	Intent action;
	View mv;
	String up_path = "", up_ver = "";
	String up_type = "simple", newDevVer = "";
	int newCodeDev = 0, newCodeSimple = 0;
	View.OnLongClickListener defLang = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			sp.edit().remove("lang").apply();
			return true;
		}
	};
	boolean isotherset = false;
	ProgressDialog pd;

	protected void backPressed() {
		if (isotherset) {
			/*isotherset = false;
			setContentView(R.layout.updater_main);
			LinearLayout ll = findViewById(R.id.layoutUpdate);
			ll.setVisibility(layUpVis);
			apply_dark_mode();
			postcreate();
			overridePendingTransition(R.anim.abc_popup_enter, R.anim.alpha_hide);*/
			change_views(null);
		} else {
			finish();
			overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
		}
	}

	@Override
	public void onBackPressed() {
		backPressed();
		//super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		//Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
		if (id == android.R.id.home) {
			backPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	public void clear_history(View v) {
		dl.show();
	}

	public void report(View v) {
		send.putExtra(Intent.EXTRA_EMAIL, new String[]{"maxsavsu@gmail.com"});
		send.putExtra(Intent.EXTRA_SUBJECT, "Problem in New MCalc");
		send.putExtra(Intent.EXTRA_TEXT, "[" + BuildConfig.VERSION_NAME + "," + BuildConfig.VERSION_CODE + "]\nProblem:\n");
		send.setType("message/rfc822");
		if(DarkMode)
			report_al.getWindow().setBackgroundDrawableResource(R.drawable.grey);
		report_al.show();
		//Toast.makeText(getApplicationContext(), R.string.donot_clear_info_email, Toast.LENGTH_LONG).show();
	}

	public void switchSave(View v) {
		Switch sw = findViewById(v.getId());
		if(v.getId() == R.id.switchSaveOnExit){
			sp.edit().putBoolean("saveResult", sw.isChecked()).apply();
			if (sw.isChecked()) {
				sw.setText(R.string.switchSaveOn);
			} else {
				sw.setText(R.string.switchSaveOff);
				sp.edit().remove("saveResult").apply();
			}
		}else if(v.getId() == R.id.switchDarkMode){
			sp.edit().putBoolean("dark_mode", sw.isChecked()).apply();
			restart();
		}

	}

	public void sh_about_dev(View v) {
		about_dev.getWindow().setBackgroundDrawableResource(R.drawable.grey);
		about_dev.show();
	}

	boolean DarkMode = false;

	protected void onCreate(Bundle savedInstanceState) {
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		DarkMode = sp.getBoolean("dark_mode", false);
		if(DarkMode){
			setTheme(android.R.style.Theme_Material_NoActionBar);
		}else{
			setTheme(R.style.AppTheme);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.updater_main);

        /*Slide slide = new Slide();
        slide.setDuration(100);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);*/
		getSupportActionBar().setTitle(getResources().getString(R.string.settings));
		Trace tr = FirebasePerformance.getInstance().newTrace("UpdaterStart");
		tr.start();
		//findViewById(R.id.imgBtnWeb).setOnLongClickListener(show_join);
		mv = getLayoutInflater().inflate(R.layout.about_developer, null);
		mv.findViewById(R.id.imgBtnWeb).setOnLongClickListener(show_join);
		mv.findViewById(R.id.imgBtnInsta).setOnClickListener(social);
		mv.findViewById(R.id.imgBtnTw).setOnClickListener(social);
		mv.findViewById(R.id.imgBtnVk).setOnClickListener(social);
		mv.findViewById(R.id.btnImgMore).setOnClickListener(social);

		apply_theme();

		if(sp.getBoolean("storage_denied", false)){
			findViewById(R.id.import_export).setVisibility(View.GONE);
		}
		//Toast.makeText(this, Boolean.toString(sp.getBoolean("storage_denied", false)), Toast.LENGTH_LONG).show();
		about_dev = new AlertDialog.Builder(this).setCancelable(true).setTitle(R.string.about_dev).setView(mv).create();
		postcreate();

		AlertDialog.Builder build = new AlertDialog.Builder(this);
		build.setTitle(R.string.confirm)
				.setMessage(R.string.confirm_cls_history)
				.setCancelable(false)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						sp.edit().remove("history").apply();
						findViewById(R.id.btnClsHistory).setVisibility(View.INVISIBLE);
					}
				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		dl = build.create();
		if(DarkMode)
			dl.getWindow().setBackgroundDrawableResource(R.drawable.grey);
		build = new AlertDialog.Builder(this);
		build.setCancelable(true).setMessage(R.string.donot_clear_info_email).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				startActivity(Intent.createChooser(send, "Choose email client"));
			}
		});
		report_al = build.create();
		if(DarkMode)
			report_al.getWindow().setBackgroundDrawableResource(R.drawable.grey);
		action = getIntent();
		up_path = action.getStringExtra("update_path");
		up_ver = action.getStringExtra("upVerName");
		if (action.getStringExtra("action").equals("update")) {
			setContentView(R.layout.layout_updater);
			downloading = true;
			//return;
		} else {
			DatabaseReference dbm = db.getReference("links/vk");
			dbm.addValueEventListener(list);
			dbm = db.getReference("links/insta");
			dbm.addValueEventListener(list);
			dbm = db.getReference("links/facebook");
			dbm.addValueEventListener(list);
			dbm = db.getReference("links/twitter");
			dbm.addValueEventListener(list);
			dbm = db.getReference("links/site");
			dbm.addValueEventListener(list);
		}
		tr.stop();
	}

	public void restart(){
		Intent intent = new Intent(this, MainActivity.class);
		this.startActivity(intent);
		this.finishAffinity();
		overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
	}

	public void postcreate() {
		other_settings();
		Button b = findViewById(R.id.btnReport);
		b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
		b.setTextColor(getResources().getColor(R.color.white));
		if (!sp.getString("history", "not").equals("not")) {
			findViewById(R.id.btnClsHistory).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.btnClsHistory).setVisibility(View.GONE);
		}
		Switch sw = findViewById(R.id.switchSaveOnExit);
		sw.setChecked(sp.getBoolean("saveResult", false));
		if (sw.isChecked()) {
			sw.setText(R.string.switchSaveOn);
			sw.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
		} else {
			sw.setText(R.string.switchSaveOff);
		}
	}

	public void apply_theme(){
		ActionBar appActionBar = getSupportActionBar();
		try {
			appActionBar.setDisplayHomeAsUpEnabled(true);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

		}
		if(DarkMode) {
			getWindow().setBackgroundDrawableResource(R.drawable.black);
			TextView t = findViewById(R.id.txtxCopyRight);
			t.setTextColor(getResources().getColor(R.color.white));
			appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_32dp);
			appActionBar.setBackgroundDrawable(getDrawable(R.drawable.black));
		}else{
			getWindow().setBackgroundDrawableResource(R.drawable.white);
			appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_32dp);
			appActionBar.setBackgroundDrawable(getDrawable(R.drawable.white));
		}
		appActionBar.setElevation(0);
	}

	public void apply_dark_at_othersettings(){
		if(!DarkMode)
			return;
		TextView t;
		t = findViewById(R.id.txtExIm);
		t.setTextColor(getResources().getColor(R.color.white));
		t = findViewById(R.id.txtChooseBtnLoc);
		t.setTextColor(getResources().getColor(R.color.white));
		Button lef = findViewById(R.id.btnLeft), rig = findViewById(R.id.btnRight);
		if(lef.getCurrentTextColor() == getResources().getColor(R.color.black)){
			lef.setTextColor(getResources().getColor(R.color.white));
		}
		if(rig.getCurrentTextColor() == getResources().getColor(R.color.black)){
			rig.setTextColor(getResources().getColor(R.color.white));
		}
		lef = findViewById(R.id.btnExport);
		lef.setTextColor(getResources().getColor(R.color.white));
		lef = findViewById(R.id.btnImport);
		lef.setTextColor(getResources().getColor(R.color.white));
	}

	public void choose_btn(View v) {
		Button btn = findViewById(v.getId());
		Button btn2;
		if (v.getId() == R.id.btnRight) {
			btn2 = findViewById(R.id.btnLeft);
		} else {
			btn2 = findViewById(R.id.btnRight);
		}
        /*ColorDrawable cd = new ColorDrawable();
        try{
            cd = (ColorDrawable) btn.getBackground();
        }catch (Exception e){
            e.getCause().printStackTrace();
        }*/
        Drawable textColor, bgColor;
        /*if(DarkMode){
        	textColor =
        }
		if (btn.getBackground() != btnbl) {
			btn2.setBackground(btn.getBackground());
			if(DarkMode)
				btn2.setTextColor(getResources().getColor(R.color.white));
			else
				btn2.setTextColor(getResources().getColor(R.color.black));
			btn.setBackgroundColor(getResources().getColor(R.color.black));
			btn.setTextColor(getResources().getColor(R.color.white));
			if (btn.getId() == R.id.btnRight) {
				sp.edit().putInt("btn_add_align", 1).apply();
			} else
				sp.edit().putInt("btn_add_align", 0).apply();
			Intent btnal = new Intent(BuildConfig.APPLICATION_ID + ".ON_BTN_ALIGN_CHANGE");
			sendBroadcast(btnal);
		}*/
		TypedArray array = getTheme().obtainStyledAttributes(new int[]{
				android.R.attr.windowBackground});
		int backgroundColor = array.getColor(0, 0xFF00FF);
		array.recycle();
		ColorDrawable colorDrawable = (ColorDrawable) btn.getBackground();
		int colorId = colorDrawable.getColor();
        if(colorId == R.color.white){
        	if(!DarkMode) {
		        btn.setBackgroundColor(getResources().getColor(R.color.black));
		        btn.setTextColor(getResources().getColor(R.color.white));
		        btn2.setBackgroundColor(backgroundColor);
		        btn2.setTextColor(getResources().getColor(R.color.black));
		        if (btn.getId() == R.id.btnRight) {
			        sp.edit().putInt("btn_add_align", 1).apply();
		        } else
			        sp.edit().putInt("btn_add_align", 0).apply();
		        Intent btnal = new Intent(BuildConfig.APPLICATION_ID + ".ON_BTN_ALIGN_CHANGE");
		        sendBroadcast(btnal);
	        }
        }else{
            if(DarkMode){
                btn.setBackgroundColor(getResources().getColor(R.color.white));
                btn.setTextColor(getResources().getColor(R.color.black));
                btn2.setBackgroundColor(getResources().getColor(R.color.black));
                btn2.setTextColor(getResources().getColor(R.color.white));
	            if (btn.getId() == R.id.btnRight) {
		            sp.edit().putInt("btn_add_align", 1).apply();
	            } else
		            sp.edit().putInt("btn_add_align", 0).apply();
	            Intent btnal = new Intent(BuildConfig.APPLICATION_ID + ".ON_BTN_ALIGN_CHANGE");
	            sendBroadcast(btnal);
	        }
        }
	}

	public void change_views(View v){
		LinearLayout other = findViewById(R.id.other_settings), main = findViewById(R.id.main_updater);
		/*if(isotherset){
			Animation hide = AnimationUtils.loadAnimation(this, R.anim.updater_hideothersettings);
			Animation show = AnimationUtils.loadAnimation(this, R.anim.updater_showmain);
			hide.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					other.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			show.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					main.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			//if(other.getAnimation() != null)
				other.clearAnimation();
			//if(main.getAnimation() != null)
				main.clearAnimation();
			other.setAnimation(hide);
			main.setAnimation(show);

			other.animate();
			hide.start();
			main.animate();
			show.start();
		}else{
			Animation hide = AnimationUtils.loadAnimation(this, R.anim.updater_hidemain);
			Animation show = AnimationUtils.loadAnimation(this, R.anim.updater_showothersettings);
			hide.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					main.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					main.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			show.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					other.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					other.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			//if(other.getAnimation() != null)
				other.clearAnimation();
			//if(main.getAnimation() != null)
				main.clearAnimation();

			other.setAnimation(show);
			main.setAnimation(hide);

			main.animate();
			hide.start();
			other.animate();
			//other.setVisibility(View.VISIBLE);
			show.start();
		}
		isotherset = !isotherset;*/
		if(isotherset){
			Animation showmain = AnimationUtils.loadAnimation(this, R.anim.updater_showmain),
					hide_other = AnimationUtils.loadAnimation(this, R.anim.updater_hideothersettings);
			hide_other.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					other.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			other.clearAnimation();
			main.clearAnimation();
			showmain.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					main.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			other.setAnimation(hide_other);
			main.setAnimation(showmain);
			other.animate();
			main.animate();
			showmain.start();
			hide_other.start();
			isotherset = false;
		}else{
			Animation hidemain = AnimationUtils.loadAnimation(this, R.anim.updater_hidemain),
					showother = AnimationUtils.loadAnimation(this, R.anim.updater_showothersettings);
			hidemain.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					main.setVisibility(View.GONE);
					other.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			showother.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					other.setVisibility(View.VISIBLE);
					main.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			main.clearAnimation();
			other.clearAnimation();
			other.setAnimation(showother);
			main.setAnimation(hidemain);
			other.animate();
			main.animate();
			showother.start();
			hidemain.start();
			isotherset = true;
		}
	}

	public void other_settings() {
		apply_dark_at_othersettings();
		Button b = findViewById(R.id.btnImport);
		b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
		b = findViewById(R.id.btnExport);
		b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
		Switch sw = findViewById(R.id.switchDarkMode);
		sw.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
		sw.setChecked(sp.getBoolean("dark_mode", false));
		findViewById(R.id.btnExport).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				sp.edit().clear().apply();
				restart();
				return true;
			}
		});
	}

	public void import_(View v) {
		File f = new File(Environment.getExternalStorageDirectory() + "/MST files/NewMCalc.imc");
		if (!f.exists()) {
			Toast.makeText(getApplicationContext(), R.string.export_file_not_found, Toast.LENGTH_LONG).show();
			return;
		}
		try {
			FileReader fr = new FileReader(f);
			//tring ans = "";
			sp.edit().clear().apply();
			while (fr.ready()) {
				char t = (char) fr.read();
				String tag = "";
				char read = (char) fr.read();
				while (read != '=') {
					tag += read;
					read = (char) fr.read();
				}
				String value = "";
				read = (char) fr.read();
				while (read != '©') {
					value += read;
					read = (char) fr.read();
				}
				//ans += t + " " + tag + " = " + value  + "\n";
				if (t == 'S') {
					sp.edit().putString(tag, value).apply();
				} else if (t == 'I') {
					sp.edit().putInt(tag, Integer.valueOf(value)).apply();
				} else if (t == 'B') {
					sp.edit().putBoolean(tag, Boolean.parseBoolean(value)).apply();
				}
			}
			Toast.makeText(getApplicationContext(), R.string.on_import, Toast.LENGTH_LONG).show();
			fr.close();
			//postcreate();
			restart();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	public void create_import(View v) {
		sp.edit().remove("simple_upd_exist").apply();
		sp.edit().remove("dev_upd_exist").apply();
		ProgressDialog pd;
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setCancelable(false);
		File f = new File(Environment.getExternalStorageDirectory() + "/MST files");
		if (!f.isDirectory()) {
			f.mkdir();
		}
		f = new File(Environment.getExternalStorageDirectory() + "/MST files/NewMCalc.imc");
		pd.show();
		try {
			FileWriter fw = new FileWriter(f, false);
			String s = sp.getAll().toString();
			Map<String, ?> m = sp.getAll();
			fw.write("");
			//fw.append(s);
			Set<String> se = m.keySet();
			List<String> l = new ArrayList<String>(se);
			for (int i = 0; i < l.size(); i++) {
				String ty = m.get(l.get(i)).getClass().getName();
				ty = ty.replace("java.lang.", "");
				ty = Character.toString(ty.charAt(0));
				fw.append(ty).append(l.get(i)).append("=").append(String.valueOf(m.get(l.get(i)))).append("©");
			}
			pd.dismiss();
			fw.flush();
			Toast.makeText(getApplicationContext(), R.string.exported, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			pd.dismiss();
			pd.setMessage(e.toString());
			pd.setCancelable(true);
			pd.show();
			try {
				Thread.sleep(2000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
}
