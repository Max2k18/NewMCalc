package com.maxsavteam.newmcalc2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.maxsavteam.newmcalc2.types.Tuple;
import com.maxsavteam.newmcalc2.utils.ResultCodes;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.utils.VariableUtils;

import java.util.ArrayList;
import java.util.Objects;

import static com.maxsavteam.newmcalc2.R.color.white;

public class CatchService extends ThemeActivity {

	private boolean somethingWentWrong = false;

	@Override
	public void onBackPressed(){
		if(!somethingWentWrong) {
			super.onBackPressed();
			Utils.defaultActivityAnim( this );
		}
	}

	private void finishAct() {
		finish();
		overridePendingTransition( R.anim.activity_in1, R.anim.activity_out1 );
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		//Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
		if ( id == android.R.id.home ) {
			onBackPressed();
		}
		return super.onOptionsItemSelected( item );
	}

	private EditText value, name;
	private int tag;

	public void applyVariable(View view) {
		String name_s = name.getText().toString();
		String value_s = value.getText().toString();
		if(!name_s.equals("") && !value_s.equals("")) {
			if ( name_s.equals( "+" ) ) {
				return;
			}

			ArrayList<Tuple<Integer, String, String>> a = VariableUtils.readVariables( this );
			if ( a == null ) {
				a = new ArrayList<>();
			}
			//a.add(new Pair<>(tag, new Pair<>(name_s, value_s)));
			for (int i = 0; i < a.size(); i++) {
				if ( a.get( i ).first == tag ) {
					a.remove( i );
					//break;
				}
			}
			a.add( Tuple.create( tag, name_s, value_s ) );
			VariableUtils.saveVariables( a, this );
			Intent in = new Intent( BuildConfig.APPLICATION_ID + ".VARIABLES_SET_CHANGED" );
			sendBroadcast( in );
			setResult( ResultCodes.RESULT_APPLY );
		}else{
			TextView warn = findViewById(R.id.lblWarn);
			warn.setText(R.string.fill_empty_field);
			warn.setVisibility(View.VISIBLE);
		}
	}

	public void deleteVariable(View v) {
		ArrayList<Tuple<Integer, String, String>> a = VariableUtils.readVariables( this );
		if ( a == null ) {
			return;
		}
		for (int i = 0; i < a.size(); i++) {
			if ( a.get( i ).first == tag ) {
				a.remove( i );
				VariableUtils.saveVariables( a, this );
				Intent in = new Intent( BuildConfig.APPLICATION_ID + ".VARIABLES_SET_CHANGED" );
				sendBroadcast( in );
				onBackPressed();
				break;
			}
		}
	}

	@SuppressLint({"SetTextI18n", "SourceLockedOrientationActivity"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean darkMode = sp.getBoolean( "dark_mode", false );
		super.onCreate(savedInstanceState);
		//sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Intent in = getIntent();
		String action = in.getStringExtra( "action" );
		if ( action == null ) {
			finish();
			return;
		}
		if(action.equals("add_var")){
			setContentView(R.layout.activity_add_var);
			tag = in.getIntExtra("tag", 1);
			value = findViewById(R.id.value);
			name = findViewById(R.id.name);
			name.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					String str = name.getText().toString();
					if(str.length() > 20){
						name.setText(str.substring(0, 20));
						name.append("");
					}
				}

				@Override
				public void afterTextChanged(Editable s) {

				}
			});
			if(in.getBooleanExtra("is_existing", false)){
				value.setText(in.getStringExtra("value"));
				name.setText(in.getStringExtra("name"));
				findViewById(R.id.btnDelVar).setVisibility(View.VISIBLE);
			}
			Button b = findViewById(R.id.btnSaveVar);
			int[] textViewIds = new int[]{
					R.id.lblNameOfVar,
					R.id.lblValueOfVar
			};
			int[] editTextIds = new int[]{
					R.id.name,
					R.id.value
			};
			if( darkMode ) {
				getWindow().setBackgroundDrawableResource(R.drawable.black);
				for (int id : textViewIds) {
					TextView t = findViewById( id );
					t.setTextColor( Color.WHITE );
				}
			} else {
				getWindow().setBackgroundDrawableResource( R.drawable.white );
				for (int id : textViewIds) {
					TextView t = findViewById( id );
					t.setTextColor( Color.BLACK );
				}
			}
			( (Button) findViewById( R.id.btnDelVar ) ).setTextColor( Color.WHITE );
			b.setTextColor( Color.WHITE );
			b.setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.colorAccent ) ) );
			for (int id : editTextIds) {
				EditText e = findViewById( id );
				e.setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.colorAccent ) ) );
			}
		}else if(action.equals("somethingWentWrong")){
			darkMode = sp.getBoolean("dark_mode", false);
			setContentView(R.layout.smth_went_wrong);
			somethingWentWrong = true;
			Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
			String mes = in.getStringExtra("mes");
			if( darkMode ) {
				TextView t = findViewById(R.id.lblSomethingWentWrong);
				t.setTextColor(Color.WHITE);
				t = findViewById(R.id.lblMes);
				t.setTextColor(Color.WHITE);
				getWindow().setBackgroundDrawableResource(R.drawable.black);
			}else{
				getWindow().setBackgroundDrawableResource(R.drawable.white);
			}
			if(mes != null){
				TextView t = findViewById(R.id.lblMes);
				t.setText(mes);
			}
			Button btn = findViewById(R.id.btnRestartApp);
			btn.setTextColor(Color.WHITE);
			btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
			btn.setOnClickListener( (view) -> restartApp() );
		}
	}

	private void restartApp() {
		Intent intent = new Intent( this, Main2Activity.class );
		this.startActivity( intent );
		this.finishAffinity();
		overridePendingTransition( R.anim.activity_in1, R.anim.activity_out1 );
	}

	public void viewReport() {
		Intent intent;

	}
}
