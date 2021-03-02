package com.maxsavteam.newmcalc2.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.adapters.WindowRecallAdapter;
import com.maxsavteam.newmcalc2.utils.MemorySaverReader;
import com.maxsavteam.newmcalc2.utils.ResultCodesConstants;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.math.BigDecimal;

public class MemoryActionsActivity extends ThemeActivity implements WindowRecallAdapter.WindowRecallAdapterCallback {
	private BigDecimal[] barr;
	private WindowRecallAdapter adapter;
	private String type;
	private Intent activity_intent;
	private MemorySaverReader memorySaverReader;

	public void cancel_all(View v) {
		onBackPressed();
	}

	public void clear_all(View v) {
		new CustomAlertDialogBuilder( this )
				.setMessage( R.string.delete_all_memories_quest )
				.setPositiveButton( R.string.yes, (dialogInterface1, i)->{
					barr = new BigDecimal[ 10 ];
					for (int ii = 0; ii < 10; ii++) {
						barr[ ii ] = BigDecimal.valueOf( 0 );
					}
					memorySaverReader.save( barr );
					adapter.notifyDataSetChanged();
					dialogInterface1.cancel();
					setResult( ResultCodesConstants.RESULT_REFRESH );
					onBackPressed();
				} )
				.setNegativeButton( R.string.no, (dialogInterface1, i)->dialogInterface1.cancel() )
				.show();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		if ( id == android.R.id.home ) {
			onBackPressed();
		}
		return super.onOptionsItemSelected( item );
	}

	@SuppressLint("SourceLockedOrientationActivity")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_memory_actions );

		memorySaverReader = new MemorySaverReader( this );

		activity_intent = getIntent();
		type = activity_intent.getStringExtra( "type" );

		Toolbar toolbar = findViewById( R.id.toolbar );
		toolbar.setTitle( ( type.equals( "rc" ) ? "Recall Memory" : "Store Memory" ) );
		setSupportActionBar( toolbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );

		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

		barr = memorySaverReader.read();
		RecyclerView rv = findViewById( R.id.memory_actions_rv );
		adapter = new WindowRecallAdapter( this, barr );
		adapter.setInterface( this );
		LinearLayoutManager manager = new LinearLayoutManager( this );
		manager.setOrientation( RecyclerView.VERTICAL );
		rv.setLayoutManager( manager );
		rv.setAdapter( adapter );
	}

	@Override
	public void onRecallClick(View view, int position) {
		if ( type.equals( "st" ) ) {
			String value = activity_intent.getStringExtra( "value" );
			barr[ position ] = new BigDecimal( value );
			memorySaverReader.save( barr );
			setResult( ResultCodesConstants.RESULT_REFRESH );
			onBackPressed();
		} else if ( type.equals( "rc" ) ) {
			Intent intent = new Intent();
			intent.putExtra( "value", barr[ position ].toString() );
			setResult( ResultCodesConstants.RESULT_APPEND, intent );
			onBackPressed();
		}
	}
}
