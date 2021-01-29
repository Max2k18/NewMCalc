package com.maxsavteam.newmcalc2.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.maxsavteam.newmcalc2.BuildConfig;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.util.Locale;

public class AboutAppActivity extends ThemeActivity {

	private final String debugInfoStr = "Android Version: " + Build.VERSION.RELEASE + "\n" +
			"Android SDK: " + Build.VERSION.SDK_INT + "\n\n" +
			BuildConfig.APPLICATION_ID + "\n" +
			"Build number: " + BuildConfig.VERSION_CODE + "\n" +
			"CD: " + BuildConfig.COMPILE_DATE + "\n\n" +
			"Compilation date: " + BuildConfig.COMPILE_TIME + "\n" +
			"App type: " + BuildConfig.APPTYPE + "\n" +
			"Build type: " + BuildConfig.BUILD_TYPE + "\n\n" +
			"Core version: " + BuildConfig.CoreVersion;

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
		}
		startActivity( in );
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Utils.defaultActivityAnim( this );
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if(item.getItemId() == android.R.id.home)
			onBackPressed();
		return super.onOptionsItemSelected( item );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_about_app );

		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );
		//toolbar.getNavigationIcon().setColorFilter( super.textColor, PorterDuff.Mode.SRC_ATOP );

		( (TextView) findViewById( R.id.version ) ).setText( BuildConfig.VERSION_NAME );

		ImageView img = findViewById( R.id.appIcon );
		img.setOnLongClickListener( v->{
			CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder( AboutAppActivity.this );
			builder.setMessage( debugInfoStr )
					.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.cancel();
						}
					} )
					.setCancelable( false );

			builder.create().show();
			return true;
		} );
	}
}