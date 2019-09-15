package com.maxsavteam.newmcalc;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.maxsavteam.newmcalc.R.color.*;

public class catch_service extends AppCompatActivity {

	update_service ups;
	public int [] ar;

	TextView pr;
	TextView all;
	ProgressBar pb;
	SharedPreferences sp;
	String default_vk = "maksin.colf", default_inst = "maksin.colf";

	@Override
	public void onBackPressed(){
		/*ups.set_send_progress(false);
		ups.set_sh_alert(true);
		ups.kill();*/
		sp.edit().remove("count_catchservice").apply();
		finish();
		overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
	}

	public void hide(View v){
		ups.set_send_progress(false);
		ups.set_sh_alert(true);
		onBackPressed();
	}

	public void stop(View v){
		ups.set_send_progress(false);
		ups.set_sh_alert(true);
		ups.kill();

		onBackPressed();
	}

	public void install(View v){
		ups.kill();
		ups.install();
	}

	public void social(View v){
		Intent in = new Intent(Intent.ACTION_VIEW);
		if(v.getId() == R.id.vkBtn)
			in.setData(Uri.parse("https://vk.com/" + default_vk));
		else if(v.getId() == R.id.instBtn)
			in.setData(Uri.parse("https://instagram.com/" + default_inst));
		else if(v.getId() == R.id.siteBtn)
			in.setData(Uri.parse("https://maxsavteam.tk/Mobile/"));
		startActivity(in);
	}

	public void puase_start(View v){
		Button btn = findViewById(v.getId());
		if(btn.getText().toString().equals("pause")){
			pr.setText("Pause...");
			ups.set_pause(true);
			ups.set_send_progress(false);
			pb.setIndeterminate(true);
			btn.setText("start");
		}else{
			ups.set_pause(false);
			btn.setText("pause");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		//Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
		if(id == android.R.id.home){
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	boolean DarkMode;

	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		DarkMode = sp.getBoolean("dark_mode", false);
		if(DarkMode)
			setTheme(android.R.style.Theme_Material_NoActionBar);
		else
			setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		//sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Intent in = getIntent();
		String action = in.getStringExtra("action");
		Intent ser;
		ups = new update_service(this);
		try{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("");
			//getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}catch(Exception e){
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

		}
		/*BroadcastReceiver on_shortcut_numgen = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Intent resultIntent = new Intent(getApplicationContext(), numgen.class);
				resultIntent.putExtra("type", "number");
				startActivity(resultIntent);
				overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha_hide);
			}
		};
		registerReceiver(on_shortcut_numgen, new IntentFilter("com.maxsavteam.newmcalc.ShortCutNumGen"));*/
		assert action != null;
		if ("about_app".equals(action)) {/*if(DarkMode)
					setTheme(android.R.style.Theme_Material_NoActionBar);
				else
					setTheme(R.style.AppTheme);*/

			setContentView(R.layout.about_activity);
			((TextView) findViewById(R.id.version)).setText(BuildConfig.VERSION_NAME);
			((TextView) findViewById(R.id.compiledate)).setText(Integer.toString(BuildConfig.COMPILE_DATE));
			((TextView) findViewById(R.id.build)).setText(Integer.toString(BuildConfig.VERSION_CODE));
			if (DarkMode) {
				getWindow().setBackgroundDrawableResource(R.drawable.black);
				int[] ids = new int[]{
						R.id.label_version,
						R.id.label_note,
						R.id.appname,
						R.id.label_compiledate,
						R.id.label_build,
						R.id.build,
						R.id.version,
						R.id.compiledate
				};
				for (int id : ids) {
					((TextView) findViewById(id)).setTextColor(getResources().getColor(white));
				}
			}
		} else if ("NOT_BTN_PRESSED".equals(action)) {
			ser = new Intent(BuildConfig.APPLICATION_ID + ".NOT_BTN_PRESSED");
			sendBroadcast(ser);
			finish();
		} else if ("on_prepare_pressed".equals(action)) {
			ser = new Intent(BuildConfig.APPLICATION_ID + ".DELETE_NOT");
			sendBroadcast(ser);
			finish();
		} else if ("sh_progress".equals(action)) {
			setContentView(R.layout.activity_sh_progress);
			int count = sp.getInt("count_catchservice", 0);
			if (count > 0) {
				finish();
			} else {
				count++;
				sp.edit().putInt("count_catchservice", count).apply();
			}
			pr = findViewById(R.id.txtProgress);
			all = findViewById(R.id.txtAll);
			if (DarkMode) {
				pr.setTextColor(getResources().getColor(white));
				all.setTextColor(getResources().getColor(white));
				getWindow().setBackgroundDrawableResource(R.drawable.black);
				Button b = findViewById(R.id.btnStop);
				b.setTextColor(getResources().getColor(white));
				b = findViewById(R.id.btnHide);
				b.setTextColor(getResources().getColor(white));
				b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorAccent)));
				b = findViewById(R.id.btnInstall);
				b.setTextColor(getResources().getColor(white));
				b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorAccent)));
			} else {
				Button b = findViewById(R.id.btnHide);
				b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorAccent)));
				b = findViewById(R.id.btnStop);
				b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorAccent)));
			}
			pb = findViewById(R.id.pbOnUpdate);
			ups.set_send_progress(true);
			ups.set_sh_alert(false);
			ar = ups.get_ints();
			BroadcastReceiver br = new BroadcastReceiver() {
				@SuppressLint("SetTextI18n")
				@Override
				public void onReceive(Context context, Intent intent) {
					//int [] ar = intent.getIntArrayExtra("values");
					ar = ups.get_ints();
					pr.setText(ar[0] + "%");
					int bytes = ar[2], total = ar[1];
					double bytesmb = bytes / 1000000.0, totalmb = total / 1000000.0;
					all.setText(Double.toString(totalmb).substring(0, 3) + "MB of " + Double.toString(bytesmb).substring(0, 3) + "MB");
					//all.setText(ar[1] + " of " + ar[2]);
					if (ar[3] == 1)
						pb.setIndeterminate(true);
					else
						pb.setIndeterminate(false);
					pb.setProgress(ar[0]);
				}
			};
			registerReceiver(br, new IntentFilter(BuildConfig.APPLICATION_ID + ".ON_UPDATE"));

			BroadcastReceiver on_suc = new BroadcastReceiver() {
				@SuppressLint("SetTextI18n")
				@Override
				public void onReceive(Context context, Intent intent) {
					int bytes = ar[2], total = ar[1];
					//all.setText(total + " of " + bytes);
					double bytesmb = bytes / 1000000.0, totalmb = total / 1000000.0;
					all.setText(Double.toString(totalmb).substring(0, 3) + "MB of " + Double.toString(bytesmb).substring(0, 3) + "MB");
					pr.setText("100%");
					ups.set_send_progress(false);
					ups.set_sh_alert(true);
					pb.setVisibility(View.GONE);
					findViewById(R.id.btnInstall).setVisibility(View.VISIBLE);
					findViewById(R.id.btnStop).setVisibility(View.GONE);
					findViewById(R.id.btnHide).setVisibility(View.GONE);
					ups.kill();
				}
			};
			registerReceiver(on_suc, new IntentFilter(BuildConfig.APPLICATION_ID + ".NEWMCALC_UPDATE_SUC"));
		}
	}
}
