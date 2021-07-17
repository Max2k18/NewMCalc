package com.maxsavteam.newmcalc2.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.maxsavteam.calculator.Calculator;
import com.maxsavteam.newmcalc2.BuildConfig;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

public class AboutAppActivity extends ThemeActivity {

	private final String debugInfoStr = "Android Version: " + Build.VERSION.RELEASE + "\n" +
			"Android SDK: " + Build.VERSION.SDK_INT + "\n\n" +
			BuildConfig.APPLICATION_ID + "\n" +
			"Build number: " + BuildConfig.VERSION_CODE + "\n" +
			"CD: " + BuildConfig.COMPILE_DATE + "\n\n" +
			"Compilation date: " + BuildConfig.COMPILE_TIME + "\n" +
			"Build type: " + BuildConfig.BUILD_TYPE + "\n\n" +
			"Core version: v" + Calculator.VERSION;

	public void social(View v) {
		Intent in = new Intent( Intent.ACTION_VIEW );
		String dynamic_vk = "https://maxsavteam.page.link/VK";
		String dynamic_inst = "https://maxsavteam.page.link/Instagram";
		if ( v.getId() == R.id.vkBtn ) {
			in.setData( Uri.parse( dynamic_vk ) );
		} else if ( v.getId() == R.id.instBtn ) {
			in.setData( Uri.parse( dynamic_inst ) );
		} else if ( v.getId() == R.id.siteBtn ) {
			in.setData( Uri.parse( "https://maxsavteam.com" ) );
		} else if ( v.getId() == R.id.playMarketBtn ) {
			in.setData( Uri.parse( getResources().getString( R.string.link_app_in_google_play ) ) );
		} else if( v.getId() == R.id.githubBtn ){
			in.setData( Uri.parse( "https://github.com/MaxSavTeam/MCalc" ) );
		}
		startActivity( in );
	}

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
		setContentView( R.layout.activity_about_app );

		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );

		( (TextView) findViewById( R.id.version ) ).setText( BuildConfig.VERSION_NAME );

		ImageView img = findViewById( R.id.appIcon );
		img.setOnLongClickListener( v->{
			CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder( AboutAppActivity.this );
			builder.setMessage( debugInfoStr )
					.setPositiveButton( "OK", (dialogInterface, i)->dialogInterface.cancel() )
					.setCancelable( false );

			if ( BuildConfig.DEBUG ) {
				builder.setNegativeButton( "Ba-dum!", ( (dialog, which)->{
					throw new RuntimeException( "Test exception" );
				} ) );
			}

			builder.create().show();
			return true;
		} );
	}
}