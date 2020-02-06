package com.maxsavteam.newmcalc2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.maxsavteam.newmcalc2.types.Tuple;
import com.maxsavteam.newmcalc2.utils.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
//import java.lang.;

public class Settings extends AppCompatActivity {

	private SharedPreferences sp;

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

	public void switchSave(View v) {
		Switch sw = findViewById(v.getId());
		if(v.getId() == R.id.switchSaveOnExit){
			if (sw.isChecked()) {
				sw.setText(R.string.switchSaveOn);
				sp.edit().putBoolean("saveResult", true).apply();
			} else {
				sw.setText(R.string.switchSaveOff);
				sp.edit().remove("saveResult").apply();
			}
		}else if(v.getId() == R.id.switchDarkMode){
			sp.edit().putBoolean("dark_mode", sw.isChecked()).apply();
			if(sw.isChecked()) {
				sp.edit().putBoolean("force_enable_light", true).remove("force_enable_dark").apply();
			}else{
				sp.edit().putBoolean("force_enable_dark", true).remove("force_enable_light").apply();
			}
			restartApp();
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
		setContentView(R.layout.activity_settings);
		try {
			getSupportActionBar().setTitle(getResources().getString(R.string.settings));
		}catch (NullPointerException e){
			e.printStackTrace();
		}
		applyTheme();

		if(sp.getBoolean("storage_denied", false)){
			findViewById(R.id.import_export).setVisibility(View.GONE);
		}
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		Switch sw = findViewById(R.id.switchSaveOnExit);
		sw.setChecked(sp.getBoolean("saveResult", false));
		if (sw.isChecked()) {
			sw.setText(R.string.switchSaveOn);
		} else {
			sw.setText(R.string.switchSaveOff);
		}
		sw = findViewById(R.id.switchDarkMode);
		sw.setChecked(sp.getBoolean("dark_mode", false));
		findViewById(R.id.btnExport).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				sp.edit().clear().apply();
				restartApp();
				return true;
			}
		});

		TextView scale = findViewById( R.id.textViewScale );
		scale.setText( String.format( Locale.ROOT, "%d", sp.getInt( "rounding_scale", 8 ) ) );
	}

	private void restartApp(){
		Intent intent = new Intent(this, Main2Activity.class);
		this.startActivity(intent);
		this.finishAffinity();
		overridePendingTransition(R.anim.activity_in1, R.anim.activity_out1);
	}

	public void changeScaleClickListener(View v){
		TextView scalet = findViewById( R.id.textViewScale );
		int scale = Integer.parseInt( scalet.getText().toString() );
		if(v.getId() == R.id.btnMinusScale){
			if(scale > 2)
				scale--;
		}else if(v.getId() == R.id.btnPlusScale){
			if(scale < 15)
				scale++;
		}
		scalet.setText( String.format( Locale.ROOT, "%d", scale ) );
		sp.edit().putInt( "rounding_scale", scale ).apply();
	}

	@SuppressLint("SourceLockedOrientationActivity")
	private void applyTheme(){
		ActionBar appActionBar = getSupportActionBar();
		if(appActionBar != null){
			appActionBar.setDisplayHomeAsUpEnabled(true);
			appActionBar.setElevation(0);
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		if(DarkMode) {
			getWindow().setBackgroundDrawableResource(R.drawable.black);
			getWindow().setNavigationBarColor(Color.BLACK);
			if(appActionBar != null) {
				appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_32dp);
				appActionBar.setBackgroundDrawable(getDrawable(R.drawable.black));
			}

			int[] textViewIds = new int[]{R.id.txtExIm, R.id.txtAccurancyOfCalculations, R.id.textViewScale};
			for(int id : textViewIds){
				TextView t = findViewById( id );
				t.setTextColor(getResources().getColor(R.color.white));
			}

			int[] buttonsIds = new int[]{R.id.btnImport, R.id.btnExport};
			for(int id : buttonsIds){
				Button button = findViewById( id );
				button.setTextColor(getResources().getColor(R.color.white));
				button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
			}
		}else{
			getWindow().setBackgroundDrawableResource(R.drawable.white);
			getWindow().setNavigationBarColor(Color.WHITE);
			if(appActionBar != null){
				appActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_32dp);
				appActionBar.setBackgroundDrawable(getDrawable(R.drawable.white));
			}
		}
		Switch sw = findViewById(R.id.switchSaveOnExit);
		sw.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
		sw = findViewById(R.id.switchDarkMode);
		sw.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
	}

	public void initializeImport(View v){
		String fileName = "NewMCalc" + (APPTYPE.equals("dev") ? "Dev" : "")  + ".imc";
		File f = new File(Environment.getExternalStorageDirectory() + "/MST files/" + fileName);
		if (!f.exists()) {
			Toast.makeText(getApplicationContext(), R.string.export_file_not_found, Toast.LENGTH_LONG).show();
			return;
		}
		AlertDialog warning;
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(
				getResources().getString(R.string.to_continue_need_to_restart_app)
						+ "\n"
						+ getResources().getString(R.string.want_to_continue_question)
				)
				.setCancelable(false).setPositiveButton(R.string.restart, (dialog, which) -> {
					import_settings();
					dialog.cancel();
				})
				.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
		warning = builder.create();
		Window alertDialogWindow = warning.getWindow();
		if (alertDialogWindow != null) {
			alertDialogWindow.requestFeature(Window.FEATURE_NO_TITLE);
			if(DarkMode)
				alertDialogWindow.setBackgroundDrawableResource(R.drawable.grey);
		}
		Utils.recolorAlertDialogButtons(warning, this);
		warning.show();
	}

	private void import_settings() {
		String fileName = "NewMCalc" + (APPTYPE.equals("dev") ? "Dev" : "")  + ".imc";
		File f = new File(Environment.getExternalStorageDirectory() + "/MST files/" + fileName);
		try {
			FileReader fr = new FileReader( f );
			Map<String, ?> m = sp.getAll();
			Set<String> se = m.keySet();
			List<String> l = new ArrayList<>( se );
			ArrayList<Tuple<String, String, String>> a = new ArrayList<>();
			for (int i = 0; i < l.size(); i++) {
				String type = m.get( l.get( i ) ).getClass().getName();
				if ( type.contains( "java.lang." ) ) {
					type = type.replaceAll( "java.lang.", "" );
					a.add( Tuple.create( type, l.get( i ), String.valueOf( m.get( l.get( i ) ) ) ) );
				}
			}
			sp.edit().clear().apply();
			while ( fr.ready() ) {
				char t = (char) fr.read();
				if(t == '#') break;

				if(t == '\n') continue;

				if(t != 'I' && t != 'B' && t != 'S') {
					fr.close();
					sp.edit().clear().apply();
					for (Tuple<String, String, String> p : a) {
						String type = p.first;
						String key = p.second;
						String value = p.third;
						switch ( type ) {
							case "String":
								sp.edit().putString( key, value ).apply();
								break;
							case "Integer":
								sp.edit().putInt( key, Integer.parseInt( value ) ).apply();
								break;
							case "Boolean":
								sp.edit().putBoolean(key, Boolean.parseBoolean(value)).apply();
								break;
							case "Float":
								sp.edit().putFloat(key, Float.parseFloat(value)).apply();
								break;
							case "Long":
								sp.edit().putLong(key, Long.parseLong(value)).apply();
								break;
						}
					}
					Intent intent = new Intent(this, CatchService.class);
					intent.putExtra("action", "somethingWentWrong");
					intent.putExtra("mes", getResources().getString(R.string.on_err_in_backup_file));
					startActivity(intent);
					finish();
					return;
				}
				String tag = "";
				char read = (char) fr.read();
				while (read != '=') {
					tag = String.format("%s%s", tag, read);
					read = (char) fr.read();
				}
				String value = "";
				read = (char) fr.read();
				while (fr.ready() && read != ((char) 23)) {
					value = String.format("%s%c", value, read);
					read = (char) fr.read();
				}
				if (t == 'S') {
					sp.edit().putString(tag, value).apply();
				} else if (t == 'I') {
					sp.edit().putInt(tag, Integer.parseInt(value)).apply();
				} else {
					sp.edit().putBoolean(tag, Boolean.parseBoolean(value)).apply();
				}
			}
			fr.close();
			//postcreate();
			restartApp();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
	}
	String APPTYPE = BuildConfig.APPTYPE;

	public void create_import(View v) {
		ProgressDialog pd;
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setCancelable(false);
		File f = new File(Environment.getExternalStorageDirectory() + "/MST files");
		if (!f.isDirectory()) {
			f.mkdir();
		}
		String fileName = "NewMCalc" + (APPTYPE.equals("dev") ? "Dev" : "")  + ".imc";
		f = new File(Environment.getExternalStorageDirectory() + "/MST files/" + fileName);
		pd.show();
		try {
			FileWriter fw = new FileWriter(f, false);
			Map<String, ?> m = sp.getAll();
			fw.write("");
			Set<String> se = m.keySet();
			List<String> l = new ArrayList<>(se);
			for (int i = 0; i < l.size(); i++) {
				String ty = m.get(l.get(i)).getClass().getName();
				ty = ty.replace("java.lang.", "");
				ty = Character.toString(ty.charAt(0));
				fw.append(ty).append(l.get(i)).append("=").append(String.valueOf(m.get(l.get(i)))).append((char) 23);
			}
			pd.dismiss();
			fw.append("\n#Please do not change the values yourself.\n#This can lead to malfunctions or even to malfunction");
			fw.flush();
			Toast.makeText(getApplicationContext(), R.string.exported, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			pd.dismiss();
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
}