package com.maxsavteam.newmcalc2.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.fragment.SettingsPreferencesFragment;
import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;
import com.maxsavteam.newmcalc2.utils.ResultCodesConstants;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SettingsActivity extends ThemeActivity {

	private static final String TAG = Main2Activity.TAG + " Settings";

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

	private final SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (sharedPreferences, key)->{
		if(key.equals( getString( R.string.pref_theme ) )){
			restartApp();
		}else if(key.equals( getString( R.string.pref_save_history ) )){
			boolean value = sharedPreferences.getBoolean( key, true );
			if(!value){
				AlertDialog.Builder builder = new CustomAlertDialogBuilder( this )
						.setTitle( R.string.warning )
						.setMessage( R.string.settings_save_history_agreement )
						.setNegativeButton( R.string.no, (dialog, which) ->{
							sharedPreferences.edit().putBoolean( getString( R.string.pref_save_history ), true ).apply();
							dialog.cancel();
						} )
						.setPositiveButton( R.string.yes, (dialog, which) ->dialog.cancel() )
						.setCancelable( false );
				runOnUiThread( builder::show );
			}
		}
	};

	private final Preference.OnPreferenceChangeListener onPreferenceChangeListener = (preference, newValue)->{
		String key = preference.getKey();
		SharedPreferences sharedPreferences = preference.getSharedPreferences();
		if(key.equals( getString( R.string.pref_theme ) )){
			restartApp();
		}else if(key.equals( getString( R.string.pref_save_history ) )){
			boolean value = (boolean) newValue;
			if(!value){
				AlertDialog.Builder builder = new CustomAlertDialogBuilder( this )
						.setTitle( R.string.warning )
						.setMessage( R.string.settings_save_history_agreement )
						.setNegativeButton( R.string.no, (dialog, which) ->{
							if(sharedPreferences != null) {
								sharedPreferences.edit().putBoolean( getString( R.string.pref_save_history ), true ).apply();
								((SwitchPreference) preference).setChecked( true );
								preference.callChangeListener( true );
							}
							dialog.cancel();
						} )
						.setPositiveButton( R.string.yes, (dialog, which) ->dialog.cancel() )
						.setCancelable( false );
				runOnUiThread( builder::show );
			}
		}
		return true;
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate( R.menu.menu_settings, menu );
		return super.onCreateOptionsMenu( menu );
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if(item.getItemId() == R.id.item_import){
			initializeImport();
		}else if(item.getItemId() == R.id.item_export){
			export();
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_settings );

		setActionBar( R.id.toolbar );
		displayHomeAsUp();

		SettingsPreferencesFragment fragment = new SettingsPreferencesFragment();
		PreferenceManager.getDefaultSharedPreferences( this )
						.registerOnSharedPreferenceChangeListener( onSharedPreferenceChangeListener );
		getSupportFragmentManager()
				.beginTransaction()
				.replace( R.id.fl_settings, fragment )
				.runOnCommit( ()->fragment.registerOnPreferenceChangedListener( onPreferenceChangeListener ) )
				.commit();
	}

	private void restartApp() {
		setResult( ResultCodesConstants.RESULT_RESTART_APP );
		finish();
	}

	public void initializeImport() {
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

}
