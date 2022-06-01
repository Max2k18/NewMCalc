package com.maxsavteam.newmcalc2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.maxsavteam.newmcalc2.BuildConfig;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;
import com.maxsavteam.newmcalc2.utils.ResultCodesConstants;
import com.maxsavteam.newmcalc2.variables.Variable;
import com.maxsavteam.newmcalc2.variables.VariableUtils;

import java.util.ArrayList;

public class VariableEditorActivity extends ThemeActivity {
	private EditText editTextVariableName, editTextVariableValue;
	private int tag;

	private ArrayList<Variable> variables;

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if ( item.getItemId() == android.R.id.home ) {
			onBackPressed();
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_variable_editor );

		setActionBar( R.id.toolbar );
		displayHomeAsUp();

		editTextVariableName = findViewById( R.id.name );
		editTextVariableValue = findViewById( R.id.value );

		Intent intent = getIntent();
		if(intent.getBooleanExtra( "is_existing", false )){
			editTextVariableName.setText( intent.getStringExtra( "name" ) );
			editTextVariableValue.setText( intent.getStringExtra( "value" ) );
			findViewById(R.id.btnDelVar).setVisibility(View.VISIBLE);
		}

		tag = intent.getIntExtra( "tag", 1 );

		variables = VariableUtils.readVariables();
	}

	public void applyVariable(View v) {
		TextView warn = findViewById( R.id.lblWarn );

		String varName = editTextVariableName.getText().toString().trim();
		String varValue = editTextVariableValue.getText().toString().trim();

		if ( varName.isEmpty() || varValue.isEmpty() ) {
			warn.setText( R.string.fill_empty_field );
			warn.setVisibility( View.VISIBLE );
			return;
		}
		if ( varName.equals( "+" ) ) {
			warn.setText( R.string.invalid_name );
			warn.setVisibility( View.VISIBLE );
			return;
		}

		for (Variable variable : variables) {
			if ( variable.getTag() == tag ) {
				variable.setName( varName );
				variable.setValue( varValue );
				saveAndExit();

				return;
			}
		}

		variables.add( new Variable( varName, varValue, tag ) );
		saveAndExit();
	}

	private void saveAndExit(){
		VariableUtils.saveVariables( variables );
		Intent in = new Intent( BuildConfig.APPLICATION_ID + ".VARIABLES_SET_CHANGED" );
		sendBroadcast( in );
		setResult( ResultCodesConstants.RESULT_APPLY );
		onBackPressed();
	}

	public void deleteVariable(View v){
		for(Variable variable : variables){
			if(variable.getTag() == tag ){
				variables.remove( variable );
				VariableUtils.saveVariables( variables );
				Intent in = new Intent( BuildConfig.APPLICATION_ID + ".VARIABLES_SET_CHANGED" );
				sendBroadcast( in );
				setResult( ResultCodesConstants.RESULT_APPLY );
				onBackPressed();

				break;
			}
		}
	}
}