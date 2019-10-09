package com.maxsavteam.newmcalc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Updater extends AppCompatActivity {

	private SharedPreferences sp;
	private AlertDialog dl;

	private void backPressed() {
		finish();
		overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
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

	boolean DarkMode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		DarkMode = sp.getBoolean("dark_mode", false);
		if(DarkMode){
			setTheme(android.R.style.Theme_Material_NoActionBar);
		}else{
			setTheme(R.style.AppTheme);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_updater_main);

		getSupportActionBar().setTitle(getResources().getString(R.string.settings));
		Trace tr = FirebasePerformance.getInstance().newTrace("UpdaterStart");
		tr.start();

		apply_theme();

		if(sp.getBoolean("storage_denied", false)){
			findViewById(R.id.import_export).setVisibility(View.GONE);
		}

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
		tr.stop();
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
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
		/*sw = findViewById(R.id.show_all_errors);
		sw.setChecked(sp.getBoolean("show_calc_errors", true));*/
		Button b = findViewById(R.id.btnImport);
		b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
		b = findViewById(R.id.btnExport);
		b.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
		sw = findViewById(R.id.switchDarkMode);
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

	public void restart(){
		Intent intent = new Intent(this, MainActivity.class);
		this.startActivity(intent);
		this.finishAffinity();
		overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
	}

	public void apply_theme(){
		ActionBar appActionBar = getSupportActionBar();
		try {
			appActionBar.setDisplayHomeAsUpEnabled(true);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

		}
		Button btn;
		if(DarkMode) {
			getWindow().setBackgroundDrawableResource(R.drawable.black);
			TextView t = findViewById(R.id.txtxCopyRight);
			t.setTextColor(getResources().getColor(R.color.white));
			appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_32dp);
			appActionBar.setBackgroundDrawable(getDrawable(R.drawable.black));
			t = findViewById(R.id.txtExIm);
			t.setTextColor(getResources().getColor(R.color.white));

			btn = findViewById(R.id.btnExport);
			btn.setTextColor(getResources().getColor(R.color.white));
			btn = findViewById(R.id.btnImport);
			btn.setTextColor(getResources().getColor(R.color.white));
		}else{
			getWindow().setBackgroundDrawableResource(R.drawable.white);
			appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_32dp);
			appActionBar.setBackgroundDrawable(getDrawable(R.drawable.white));
		}
		appActionBar.setElevation(0);
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
