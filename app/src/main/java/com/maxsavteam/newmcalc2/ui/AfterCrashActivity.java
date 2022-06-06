package com.maxsavteam.newmcalc2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.io.FileReader;
import java.io.IOException;

public class AfterCrashActivity extends ThemeActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_after_crash );

		Button btn = findViewById(R.id.btnRestartApp);

		btn.setOnClickListener( v->{
			Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage( getPackageName() );
			if(intent != null) {
				intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
				startActivity( intent );
				finish();
			}
		} );

		String path = getIntent().getStringExtra( "path" );
		String msg = "";
		try (FileReader fr = new FileReader( path )) {
			while(fr.ready()){
				msg = String.format( "%s%c", msg, (char) fr.read() );
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String finalMsg = msg;
		btn.setOnLongClickListener( v->{
			if(!finalMsg.isEmpty()){
				CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder( this );
				builder
						.setMessage( finalMsg )
						.setPositiveButton( "OK", ((dialog, which) -> dialog.cancel()) )
						.show();
			}
			return true;
		});

	}
}