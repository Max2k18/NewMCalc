package com.maxsavteam.newmcalc2.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

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
			"Core version: v" + BuildConfig.CALCULATOR_CORE_VERSION;

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

		TextView t = findViewById( R.id.text_based_on_calculator_core );
		t.setText( String.format( getString( R.string.based_on_calculator_core_version ), BuildConfig.CALCULATOR_CORE_VERSION ) );

		t = findViewById( R.id.calculator_core_text_view_link );
		t.setMovementMethod( LinkMovementMethod.getInstance() );
		String link = "https://github.com/MaxSavTeam/calculator-core";
		String cut = link.substring( 0, 35 ) + "...";
		Spannable spannable = new SpannableStringBuilder(cut);
		spannable.setSpan( new ClickableSpan() {
			@Override
			public void onClick(@NonNull View widget) {
				startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse( link )) );
			}
		}, 0, cut.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
		t.setText( spannable );
	}
}