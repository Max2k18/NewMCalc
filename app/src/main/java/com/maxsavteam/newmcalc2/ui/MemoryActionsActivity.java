package com.maxsavteam.newmcalc2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.calculator.results.List;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.adapters.WindowRecallAdapter;
import com.maxsavteam.newmcalc2.core.CalculatorWrapper;
import com.maxsavteam.newmcalc2.utils.MemorySaverReader;
import com.maxsavteam.newmcalc2.utils.ResultCodesConstants;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MemoryActionsActivity extends ThemeActivity {
	private ArrayList<List> memoryEntries;
	private String type;
	private MemorySaverReader memorySaverReader;

	private void clearAll() {
		new CustomAlertDialogBuilder( this )
				.setMessage( R.string.delete_all_memories_quest )
				.setPositiveButton( R.string.yes, (dialogInterface1, i)->{
					memorySaverReader.save( null );
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
		}else if(id == R.id.item_delete){
			clearAll();
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate( R.menu.memory_actions_menu, menu );
		return super.onCreateOptionsMenu( menu );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_memory_actions );

		memorySaverReader = new MemorySaverReader();

		type = getIntent().getStringExtra( "type" );

		Toolbar toolbar = findViewById( R.id.toolbar );
		toolbar.setTitle( type.equals( "rc" ) ? R.string.get_from_memory : R.string.put_in_memory );
		setSupportActionBar( toolbar );
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null)
			actionBar.setDisplayHomeAsUpEnabled( true );

		memoryEntries = memorySaverReader.read();
		RecyclerView rv = findViewById( R.id.memory_actions_rv );
		WindowRecallAdapter adapter = new WindowRecallAdapter( memoryEntries, (view, position)->{
			if ( type.equals( "st" ) ) {
				setResult( RESULT_OK, new Intent().putExtra( "position", position ) );
			} else if ( type.equals( "rc" ) ) {
				Intent intent = new Intent();
				intent.putExtra( "position", position );
				setResult( ResultCodesConstants.RESULT_APPEND, intent );
			}
			onBackPressed();
		} );
		LinearLayoutManager manager = new LinearLayoutManager( this );
		manager.setOrientation( RecyclerView.VERTICAL );
		rv.setLayoutManager( manager );
		rv.setAdapter( adapter );
	}
}
