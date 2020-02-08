package com.maxsavteam.newmcalc2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;
import java.util.Objects;

public class ErrorHandlerActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_error_handler );

		Intent intent = getIntent();
		File file = new File( Objects.requireNonNull( intent.getStringExtra( "path" ) ) );
		ProgressDialog pr = new ProgressDialog( this );
		pr.setCancelable( false );
		pr.setMessage( "Something gone wrong. We're sending report" );

	}
}
