package com.maxsavteam.newmcalc2.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.maxsavteam.newmcalc2.BuildConfig;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.utils.ResultCodes;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.variables.Variable;
import com.maxsavteam.newmcalc2.variables.VariableUtils;

import java.util.ArrayList;

public class VariableEditorActivity extends ThemeActivity {
	private EditText mName, mValue;
	private int mTag;

	private ArrayList<Variable> mVariables;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Utils.defaultActivityAnim( this );
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if ( item.getItemId() == android.R.id.home ) {
			onBackPressed();
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_variable_editor );

		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );

		mName = findViewById( R.id.name );
		mValue = findViewById( R.id.value );

		Intent intent = getIntent();
		if(intent.getBooleanExtra( "is_existing", false )){
			mName.setText( intent.getStringExtra( "name" ) );
			mValue.setText( intent.getStringExtra( "value" ) );
			findViewById(R.id.btnDelVar).setVisibility(View.VISIBLE);
		}

		mTag = intent.getIntExtra( "tag", 1 );

		mVariables = VariableUtils.readVariables();
	}

	public void applyVariable(View v) {
		String varName = mName.getText().toString().trim();
		String varValue = mValue.getText().toString().trim();

		if ( varName.isEmpty() || varValue.isEmpty() ) {
			TextView warn = findViewById( R.id.lblWarn );
			warn.setText( R.string.fill_empty_field );
			warn.setVisibility( View.VISIBLE );
			return;
		}
		if ( varName.equals( "+" ) ) {
			TextView warn = findViewById( R.id.lblWarn );
			warn.setText( R.string.invalid_name );
			warn.setVisibility( View.VISIBLE );
			return;
		}

		for (Variable variable : mVariables) {
			if ( variable.getTag() == mTag ) {
				variable.setName( varName );
				variable.setValue( varValue );
				saveAndExit();

				return;
			}
		}

		mVariables.add( new Variable( varName, varValue, mTag ) );
		saveAndExit();
	}

	private void saveAndExit(){
		VariableUtils.saveVariables( mVariables );
		Intent in = new Intent( BuildConfig.APPLICATION_ID + ".VARIABLES_SET_CHANGED" );
		sendBroadcast( in );
		setResult( ResultCodes.RESULT_APPLY );
		onBackPressed();
	}

	public void deleteVariable(View v){
		for(Variable variable : mVariables){
			if(variable.getTag() == mTag){
				mVariables.remove( variable );
				VariableUtils.saveVariables( mVariables );
				Intent in = new Intent( BuildConfig.APPLICATION_ID + ".VARIABLES_SET_CHANGED" );
				sendBroadcast( in );
				setResult( ResultCodes.RESULT_APPLY );
				onBackPressed();

				break;
			}
		}
	}
}