package com.maxsavteam.newmcalc2.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.utils.ResultCodesConstants;
import com.maxsavteam.newmcalc2.widget.ButtonWithDropdown;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class SettingsActivity extends ThemeActivity {

	private static final String TAG = Main2Activity.TAG + " Settings";
	private SharedPreferences sp;

	private final ActivityResultLauncher<Intent> mCreateBackupLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result->{
				if ( result.getResultCode() == RESULT_OK ) {
					Intent data = result.getData();
					if ( data != null ) {
						createSettingsBackupToFile( data.getData() );
					}
				}
			}
	);

	private final ActivityResultLauncher<Intent> mChooseBackupFileLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result->{
				if ( result.getResultCode() == RESULT_OK ) {
					Intent data = result.getData();
					if ( data != null ) {
						importSettings( data.getData() );
					}
				}
			}
	);

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if ( id == android.R.id.home ) {
			onBackPressed();
		}
		return super.onOptionsItemSelected( item );
	}

	public void switchSave(View v) {
		SwitchMaterial sw = (SwitchMaterial) v;
		if ( v.getId() == R.id.switchSaveOnExit ) {
			if ( sw.isChecked() ) {
				sw.setText( R.string.switchSaveOn );
				sp.edit().putBoolean( "saveResult", true ).apply();
			} else {
				sw.setText( R.string.switchSaveOff );
				sp.edit().remove( "saveResult" ).apply();
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sp = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_settings );

		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null)
			actionBar.setDisplayHomeAsUpEnabled( true );

		ButtonWithDropdown button = findViewById( R.id.theme_dropdown_button );
		button.setElements( getResources().getStringArray( R.array.theme_states ) );
		button.setSelection( sp.getInt( "theme_state", 2 ) );
		button.setOnItemSelectedListener( index->{
			sp.edit().putInt( "theme_state", index ).apply();
			restartApp();
		} );
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate( savedInstanceState );
		SwitchMaterial sw = findViewById( R.id.switchSaveOnExit );
		sw.setChecked( sp.getBoolean( "saveResult", false ) );
		if ( sw.isChecked() ) {
			sw.setText( R.string.switchSaveOn );
		} else {
			sw.setText( R.string.switchSaveOff );
		}
		findViewById( R.id.btnExport ).setOnLongClickListener( v->{
			sp.edit().clear().apply();
			restartApp();
			return true;
		} );

		sw = findViewById( R.id.switchKeepScreenOn );
		sw.setChecked( sp.getBoolean( "keep_screen_on", false ) );
		sw.setOnCheckedChangeListener( (compoundButton, b)->sp.edit().putBoolean( "keep_screen_on", b ).apply() );

		TextView scale = findViewById( R.id.textViewScale );
		scale.setText( String.format( Locale.ROOT, "%d", sp.getInt( "rounding_scale", 8 ) ) );
	}

	private void restartApp() {
		setResult( ResultCodesConstants.RESULT_RESTART_APP );
		onBackPressed();
	}

	public void changeScaleClickListener(View v) {
		TextView scaleTextView = findViewById( R.id.textViewScale );
		int scale = Integer.parseInt( scaleTextView.getText().toString() );
		int id = v.getId();
		if ( id == R.id.btnMinusScale && scale > 2 ) {
			scale--;
		} else if ( id == R.id.btnPlusScale && scale < 15 ) {
			scale++;
		}
		scaleTextView.setText( String.format( Locale.ROOT, "%d", scale ) );
		sp.edit().putInt( "rounding_scale", scale ).apply();
	}

	public void initializeImport(View v) {
		Intent intent = new Intent( Intent.ACTION_OPEN_DOCUMENT );
		intent.setType( "text/plain" );
		mChooseBackupFileLauncher.launch( intent );
	}

	private void importSettings(Uri uri) {
		if ( importFromFile( uri ) ) {
			restartApp();
		} else {
			Toast.makeText( this, R.string.failed_to_import_settings, Toast.LENGTH_SHORT ).show();
		}
	}

	private boolean importFromFile(Uri uri) {
		String data;
		try (InputStream is = getContentResolver().openInputStream( uri ); ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			if ( is == null ) {
				throw new IOException();
			}
			int len;
			byte[] buffer = new byte[ 1024 ];
			while ( ( len = is.read( buffer ) ) != -1 ) {
				os.write( buffer, 0, len );
			}
			data = os.toString();
		} catch (IOException e) {
			return false;
		}
		SharedPreferences sp = null;
		SharedPreferences mainPrefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		try {
			sp = importFromJSON( data );
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if ( sp == null ) {
			boolean success;
			try {
				sp = importSettingsLegacy( data );
				success = sp != null;
			} catch (IOException e) {
				e.printStackTrace();
				success = false;
			}
			if ( !success ) {
				return false;
			}
		}
		copySharedPrefs( sp, mainPrefs );
		return true;
	}

	private void copySharedPrefs(SharedPreferences from, SharedPreferences to) {
		Map<String, ?> map = from.getAll();
		for (String key : map.keySet()) {
			Object val = map.get( key );
			if ( val == null ) {
				continue;
			}
			String type = getObjectType( val );
			if ( type != null ) {
				putObjectToSharedPreferences( to, type, key, val );
			}
		}
	}

	private SharedPreferences importFromJSON(String json) throws JSONException {
		SharedPreferences sp = getSharedPreferences( "temp_prefs", MODE_PRIVATE );
		sp.edit().clear().apply();
		JSONObject jsonObject = new JSONObject( json );
		JSONArray jsonArray = jsonObject.getJSONArray( "data" );
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject j = jsonArray.getJSONObject( i );
			String type = j.getString( "type" );
			String key = j.getString( "key" );
			Object value = j.get( "value" );
			putObjectToSharedPreferences( sp, type, key, value );
		}
		return sp;
	}

	private void putObjectToSharedPreferences(SharedPreferences sp, String type, String key, Object value) {
		Editor e = sp.edit();
		switch ( type ) {
			case "string": {
				e.putString( key, String.valueOf( value ) );
				break;
			}
			case "int": {
				e.putInt( key, Integer.parseInt( String.valueOf( value ) ) );
				break;
			}
			case "long": {
				e.putLong( key, Long.parseLong( String.valueOf( value ) ) );
				break;
			}
			case "float": {
				e.putFloat( key, Float.parseFloat( String.valueOf( value ) ) );
				break;
			}
			case "bool": {
				e.putBoolean( key, Boolean.parseBoolean( String.valueOf( value ) ) );
				break;
			}
			default:
				break;
		}
		e.apply();
	}

	private SharedPreferences importSettingsLegacy(String data) throws IOException {
		SharedPreferences sp = getSharedPreferences( "temp_prefs", MODE_PRIVATE );
		sp.edit().clear().apply();
		StringReader fr = new StringReader( data );
		while ( fr.ready() ) {
			char t = (char) fr.read();
			if ( t == '#' ) {
				break;
			}

			if ( t == '\n' ) {
				continue;
			}

			if ( t != 'I' && t != 'B' && t != 'S' ) {
				fr.close();
				return null;
			}
			String tag = "";
			char read = (char) fr.read();
			while ( read != '=' ) {
				tag = String.format( "%s%s", tag, read );
				read = (char) fr.read();
			}
			String value = "";
			read = (char) fr.read();
			while ( fr.ready() && read != ( (char) 23 ) ) {
				value = String.format( "%s%c", value, read );
				read = (char) fr.read();
			}
			if ( t == 'S' ) {
				sp.edit().putString( tag, value ).apply();
			} else if ( t == 'I' ) {
				sp.edit().putInt( tag, Integer.parseInt( value ) ).apply();
			} else {
				sp.edit().putBoolean( tag, Boolean.parseBoolean( value ) ).apply();
			}
		}
		fr.close();
		return sp;
	}

	private void createSettingsBackupToFile(Uri uri) {
		String data;
		try {
			JSONObject jsonObject = createSettingsBackupJSON();
			data = jsonObject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			Log.i( TAG, "createSettingsBackupToFile: " + e );
			Toast.makeText( this, e.toString(), Toast.LENGTH_SHORT ).show();
			return;
		}
		try (OutputStream os = getContentResolver().openOutputStream( uri )) {
			if ( os != null ) {
				os.write( data.getBytes( StandardCharsets.UTF_8 ) );
			}
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText( this, e.toString(), Toast.LENGTH_SHORT ).show();
			return;
		}
		Toast.makeText( this, R.string.successfully, Toast.LENGTH_SHORT ).show();
	}

	private JSONObject createSettingsBackupJSON() throws JSONException {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		Map<String, ?> map = sp.getAll();
		JSONArray jsonArray = new JSONArray();
		for (String s : map.keySet()) {
			Object value = map.get( s );
			if ( value == null ) {
				continue;
			}
			String type = getObjectType( value );
			if ( type == null ) {
				continue;
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject
					.put( "type", type )
					.put( "value", value )
					.put( "key", s );
			jsonArray.put( jsonObject );
		}
		return new JSONObject()
				.put( "data", jsonArray );
	}

	private String getObjectType(Object o) {
		if ( o instanceof Integer ) {
			return "int";
		} else if ( o instanceof String ) {
			return "string";
		} else if ( o instanceof Float ) {
			return "float";
		} else if ( o instanceof Long ) {
			return "long";
		} else if ( o instanceof Boolean ) {
			return "bool";
		}
		return null;
	}

	private void export() {
		Intent intent = new Intent( Intent.ACTION_CREATE_DOCUMENT );
		intent.setType( "text/plain" );
		intent.putExtra( Intent.EXTRA_TITLE, "MCalc Backup.txt" );
		mCreateBackupLauncher.launch( intent );
	}

	public void initializeExport(View v) {
		export();
	}

}
