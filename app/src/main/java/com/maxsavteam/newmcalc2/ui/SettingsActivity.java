package com.maxsavteam.newmcalc2.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.maxsavteam.newmcalc2.BuildConfig;
import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.types.Tuple;
import com.maxsavteam.newmcalc2.utils.ResultCodesConstants;
import com.maxsavteam.newmcalc2.widget.ButtonWithDropdown;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SettingsActivity extends ThemeActivity {

	private static final String TAG = Main2Activity.TAG + " Settings";
	private SharedPreferences sp;

	private static final int IMPORT_STORAGE_REQUEST = 0;

	private final ActivityResultLauncher<Intent> mCreateBackupLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> {
				if(result.getResultCode() == RESULT_OK){
					Intent data = result.getData();
					if(data != null){
						createSettingsBackupToFile( data.getData() );
					}
				}
			}
	);

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		//Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
		if ( id == android.R.id.home ) {
			onBackPressed();
		}
		return super.onOptionsItemSelected( item );
	}

	public void switchSave(View v) {
		Switch sw = (Switch) v;
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

	@SuppressLint("SourceLockedOrientationActivity")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sp = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_settings );

		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );

		if ( sp.getBoolean( "storage_denied", false ) ) {
			findViewById( R.id.import_export ).setVisibility( View.GONE );
		}

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
		Switch sw = findViewById( R.id.switchSaveOnExit );
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
		if ( checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED ) {
			String fileName = "MCalc.imc";
			File f = new File( Environment.getExternalStorageDirectory() + "/MST files/" + fileName );
			if ( !f.exists() ) {
				Toast.makeText( getApplicationContext(), R.string.export_file_not_found, Toast.LENGTH_LONG ).show();
				return;
			}
			showImportDialog();
		} else {
			requestPermissions( new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, IMPORT_STORAGE_REQUEST );
		}
	}

	private void showImportDialog() {
		CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder( this );
		builder
				.setMessage( getResources().getString( R.string.to_continue_need_to_restart_app ) + "\n" + getResources().getString( R.string.want_to_continue_question ) )
				.setCancelable( false )
				.setPositiveButton( R.string.restart, (dialog, which)->{
					importSettings();
					dialog.cancel();
				} )
				.setNegativeButton( R.string.no, (dialog, which)->dialog.cancel() );
		builder.show();
	}

	private void importSettings() {
		String fileName = "MCalc.imc";
		File f = new File( Environment.getExternalStorageDirectory() + "/MST files/" + fileName );
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
				if ( t == '#' ) {
					break;
				}

				if ( t == '\n' ) {
					continue;
				}

				if ( t != 'I' && t != 'B' && t != 'S' ) {
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
								sp.edit().putBoolean( key, Boolean.parseBoolean( value ) ).apply();
								break;
							case "Float":
								sp.edit().putFloat( key, Float.parseFloat( value ) ).apply();
								break;
							case "Long":
								sp.edit().putLong( key, Long.parseLong( value ) ).apply();
								break;
							default:
								break;
						}
					}
					// TODO: 12.08.2020 somethingWentWrong Activity
					finish();
					return;
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
			//postcreate();
			restartApp();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText( getApplicationContext(), e.toString(), Toast.LENGTH_LONG ).show();
		}
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
		try(OutputStream os = getContentResolver().openOutputStream(uri)){
			if(os != null)
				os.write( data.getBytes( StandardCharsets.UTF_8 ) );
		}catch (IOException e){
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
					.put( "value", value );
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
		} else if ( o instanceof Double ) {
			return "double";
		} else if ( o instanceof Float ) {
			return "float";
		} else if ( o instanceof Long ) {
			return "long";
		}
		return null;
	}

	private void export() {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.setType( "text/plain" );
		intent.putExtra( Intent.EXTRA_TITLE, "MCalc Backup.txt" );
		mCreateBackupLauncher.launch( intent );
	}

	public void initializeExport(View v) {
		export();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if ( requestCode == IMPORT_STORAGE_REQUEST ) {
			if ( grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED ) {
				initializeImport( null );
			}
		}
		super.onRequestPermissionsResult( requestCode, permissions, grantResults );
	}
}
