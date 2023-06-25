package com.maxsavteam.newmcalc2.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.adapters.HistoryAdapter;
import com.maxsavteam.newmcalc2.core.CalculatorWrapper;
import com.maxsavteam.newmcalc2.entity.HistoryEntry;
import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;
import com.maxsavteam.newmcalc2.utils.FormatUtils;
import com.maxsavteam.newmcalc2.utils.HistoryManager;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class HistoryActivity extends ThemeActivity {

	public static final String TAG = Main2Activity.TAG + " History";

	private HistoryAdapter adapter;

	private SharedPreferences sp;
	private ArrayList<HistoryEntry> mEntries = new ArrayList<>();
	private RecyclerView rv;
	private boolean needToCreateMenu = false;

	private final HistoryAdapter.AdapterCallback adapterCallback = new HistoryAdapter.AdapterCallback() {
		@Override
		public void onItemClick(View view, int position) {
			String example = mEntries.get( position ).getExample();
			String answer = mEntries.get( position ).getAnswer();
			setResult( RESULT_OK, new Intent().putExtra( "example", example ).putExtra( "result", answer ) );
			onBackPressed();
		}

		@Override
		public void onEditDescriptionButtonClick(int position) {
			final EditText input = new EditText( HistoryActivity.this );
			input.setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.colorAccent ) ) );
			input.setTextColor( getColor(R.color.white) );
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
			input.setLayoutParams( lp );
			HistoryEntry entry = mEntries.get(position);
			if(entry.getDescription() != null)
				input.setText( entry.getDescription() );

			AlertDialog alertDialog = new CustomAlertDialogBuilder( HistoryActivity.this )
					.setCancelable( false )
					.setView( input )
					.setMessage( R.string.edit_description_message_text )
					.setTitle( R.string.desc )
					.setPositiveButton( "OK", (dialog, which)->{
						String newDesc = input.getText().toString();
						newDesc = Utils.trim( newDesc );
						entry.setDescription( newDesc );
						HistoryManager
								.getInstance()
								.change( position, entry )
								.save();
						adapter.updateDescription( newDesc, position );
					} )
					.setNegativeButton( R.string.cancel, (dialog, which)->dialog.cancel() )
					.create();
			alertDialog.show();
		}

		@Override
		public void onDeleteButtonClick(int position) {
			AlertDialog alertDialog = new CustomAlertDialogBuilder(HistoryActivity.this)
					.setTitle(R.string.delete_entry)
					.setMessage(R.string.delete_entry_message_text)
					.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel())
					.setPositiveButton(R.string.yes, (dialog, which) -> {
						mEntries.remove(position);
						HistoryManager.getInstance()
								.remove(position)
								.save();
						adapter.removeItem(position);
						updateUI();
					})
					.create();
			alertDialog.show();
		}
	};

	private void setupRecyclerView(HistoryAdapter.ExampleCalculator exampleCalculator) {
		LinearLayoutManager lay = new LinearLayoutManager( this );
		lay.setOrientation( RecyclerView.VERTICAL );
		adapter = new HistoryAdapter( this, mEntries, adapterCallback, exampleCalculator );
		rv.setAdapter( adapter );
		rv.setLayoutManager( lay );
	}

	private void updateUI(){
		needToCreateMenu = !mEntries.isEmpty();
		invalidateOptionsMenu();
		if ( mEntries.size() == 0 ) {
			setContentView( R.layout.layout_history_not_found );
			Toolbar toolbar = findViewById( R.id.toolbar );
			toolbar.setTitle( "" );
			setSupportActionBar( toolbar );
			if(getSupportActionBar() != null)
				getSupportActionBar().setDisplayHomeAsUpEnabled( true );
			TextView t = findViewById( R.id.txtHistoryNotFound );
			String[] strings = getResources().getStringArray( R.array.history_not_found );
			t.setText( String.format( "%s\n%s\n%s", strings[ 0 ], strings[ 1 ], strings[ 2 ] ) );
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate( R.menu.menu_history, menu );
		if ( !needToCreateMenu ) {
			menu.removeItem( R.id.clear_history );
		}
		return super.onCreateOptionsMenu( menu );
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		//Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
		if ( id == R.id.clear_history ) {
			CustomAlertDialogBuilder build = new CustomAlertDialogBuilder( this );
			build.setMessage( R.string.confirm_cls_history )
					.setCancelable( false )
					.setPositiveButton( R.string.yes, (dialog, which)->{
						dialog.cancel();
						sp.edit().remove( "history" ).apply();
						mEntries = new ArrayList<>();
						HistoryManager.getInstance().clear().save();
						updateUI();
					} ).setNegativeButton( R.string.no, (dialog, which)->dialog.cancel() );
			build.create().show();
		}
		return super.onOptionsItemSelected( item );
	}

	@SuppressLint({ "ClickableViewAccessibility", "SetTextI18n" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sp = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_history );

		setActionBar( R.id.toolbar );
		displayHomeAsUp();

		rv = findViewById( R.id.rv_view );

		rv.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if ( sp.getBoolean( "history_guide", true ) ) {
					if ( mEntries.size() > 0 ) {
						sp.edit().putBoolean( "history_guide", false ).apply();
						Utils.getGuideTip(
								HistoryActivity.this,
								getString( R.string.history ),
								getString( R.string.history_guide ),
								R.id.cardView,
								new RectanglePromptFocal()
						).show();
						rv.getViewTreeObserver().removeOnGlobalLayoutListener( this );
					}
				}
			}
		} );

		mEntries = new ArrayList<>();
		DecimalFormat decimalFormat = FormatUtils.getDecimalFormat();
		FormatUtils.Formatter formatter = number -> {
			if(number.equals( "." ))
				return "0";
			return decimalFormat.format( new BigDecimal( number ) );
		};
		for(HistoryEntry entry : HistoryManager.getInstance().getHistory()){
			mEntries
					.add( new HistoryEntry(
							FormatUtils.formatExpression( entry.getExample(), formatter, FormatUtils.getRootLocaleFormatSymbols() ),
							null, // calculate later if necessary
							entry.getDescription()
					) );
		}
		setupRecyclerView(example -> CalculatorWrapper.getInstance().calculate( example ).format( decimalFormat ) );
		updateUI();
	}
}
