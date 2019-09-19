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
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.maxsavteam.newmcalc.R.color.*;

public class catch_service extends AppCompatActivity {

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
	String APP_TYPE = BuildConfig.APPTYPE;

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
			if(!APP_TYPE.equals("stable"))
				((TextView) findViewById(R.id.appname)).setText(Html.fromHtml(getResources().getString(R.string.app_name) + "<sup>" + BuildConfig.APPTYPE + "</sup>"));
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
		}
	}
}
