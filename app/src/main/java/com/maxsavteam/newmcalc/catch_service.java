package com.maxsavteam.newmcalc;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.maxsavteam.newmcalc.R.color.*;

public class catch_service extends AppCompatActivity {

	SharedPreferences sp;
	String default_vk = "maksin.colf", default_inst = "maksin.colf";
	String dynamic_vk = "https://maxsavteam.page.link/VK", dynamic_inst = "https://maxsavteam.page.link/Instagram";

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
			in.setData(Uri.parse(dynamic_vk));
		else if(v.getId() == R.id.instBtn)
			in.setData(Uri.parse(dynamic_inst));
		else if(v.getId() == R.id.siteBtn)
			in.setData(Uri.parse("https://maxsavteam.tk/Mobile/"));
		else if(v.getId() == R.id.playMarketBtn)
			in.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
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
	private void apply_actionbar(){
		ActionBar appActionBar = getSupportActionBar();
		if(DarkMode){
			appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_32dp);
			appActionBar.setBackgroundDrawable(getDrawable(R.drawable.black));
		}else{
			appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_32dp);
			appActionBar.setBackgroundDrawable(getDrawable(R.drawable.white));
		}
	}

	final String debugInfoStr = "Android Version: " + Build.VERSION.RELEASE + "\n" +
			"Android SDK: " + Build.VERSION.SDK_INT + "\n\n" +
			" - App type: " + BuildConfig.APPTYPE + "\n" +
			" - Build type: " + BuildConfig.BUILD_TYPE + "\n\n" +
			" - Core version: " + BuildConfig.CoreVersion + "\n\n" +
			" - UpdateChecker Module activated: " + BuildConfig.UCModuleActivated + "\n" +
			" - UpdateChecker version: " + BuildConfig.UpdateCheckerVersion + "\n" +
			" - UpdateDownloader version: " + BuildConfig.UpdateDowVersion;

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
			getSupportActionBar().setElevation(0);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}catch(Exception e){
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

		}
		apply_actionbar();
		assert action != null;
		if ("about_app".equals(action)) {
			setContentView(R.layout.about_activity);
			/*if(!APP_TYPE.equals("stable"))
				((TextView) findViewById(R.id.appname)).setText(Html.fromHtml(getResources().getString(R.string.app_name) + "<sup>" + BuildConfig.APPTYPE + "</sup>"));*/
			((TextView) findViewById(R.id.version)).setText(BuildConfig.VERSION_NAME);
			((TextView) findViewById(R.id.compiledate)).setText(Integer.toString(BuildConfig.COMPILE_DATE));
			((TextView) findViewById(R.id.build)).setText(Integer.toString(BuildConfig.VERSION_CODE));

			ImageView img = findViewById(R.id.appIcon);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				img.setImageIcon(Icon.createWithResource(this, R.drawable.newmcalc));
			}
			img.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					AlertDialog debug_info;
					AlertDialog.Builder builder = new AlertDialog.Builder(catch_service.this);
					builder.setTitle("Debug info")
							.setMessage(debugInfoStr)
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							})
							.setCancelable(false);
					debug_info = builder.create();
					if(DarkMode)
						debug_info.getWindow().setBackgroundDrawableResource(R.drawable.grey);
					debug_info.show();
					return true;
				}
			});
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
			}else{
				getWindow().setBackgroundDrawableResource(R.drawable.white);
			}
		}else if(action.equals("aboutAG")){
			setContentView(R.layout.about_average_geometric);
			int[] ids = new int[]{
					R.id.lblAboutAG,
					R.id.lblA,
					R.id.lblG,
					R.id.lblAboutA,
					R.id.lblAboutG
			};
			if(DarkMode) {
				getWindow().setBackgroundDrawableResource(R.drawable.black);
				for (int id : ids) {
					((TextView) findViewById(id)).setTextColor(Color.WHITE);
				}
			}else{
				getWindow().setBackgroundDrawableResource(R.drawable.white);
			}
		}
	}
}
