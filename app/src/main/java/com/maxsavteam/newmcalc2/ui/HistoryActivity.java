package com.maxsavteam.newmcalc2.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc2.BuildConfig;
import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.adapters.HistoryAdapter;
import com.maxsavteam.newmcalc2.swipes.SwipeController;
import com.maxsavteam.newmcalc2.swipes.SwipeControllerActions;
import com.maxsavteam.newmcalc2.types.HistoryEntry;
import com.maxsavteam.newmcalc2.utils.Constants;
import com.maxsavteam.newmcalc2.utils.HistoryStorageProtocolsFormatter;
import com.maxsavteam.newmcalc2.utils.ResultCodes;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class HistoryActivity extends ThemeActivity implements HistoryAdapter.AdapterCallback {

	private static final String TAG = Main2Activity.TAG + " History";

	private HistoryAdapter adapter;

	private SharedPreferences sp;
	private Intent history_action;
	private ArrayList<HistoryEntry> mEntries = new ArrayList<>();
	private RecyclerView rv;
	private boolean needToCreateMenu = false;
	private Menu mMenu;
	private int LOCAL_HISTORY_STORAGE_PROTOCOL_VERSION;

	@Override
	public void onBackPressed() {
		setResult( ResultCodes.RESULT_NORMAL, history_action );
		if ( start_type.equals( "shortcut" ) ) {
			startActivity( new Intent( this, Main2Activity.class ) );
		}
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		saveHistory();
		super.onPause();
	}

	private void onSomethingWentWrong() {
		Toast.makeText( this, getResources().getString( R.string.smth_went_wrong ), Toast.LENGTH_LONG ).show();
		history_action.putExtra( "error", true );
		setResult( ResultCodes.RESULT_ERROR, history_action );
		super.onBackPressed();
	}

	@Override
	public void onItemClick(View view, int position) {
		if ( position == POSITION_TO_DEL ) {
			cancelTimer();
			animate_hide();
		}
		//history_action = new Intent(BuildConfig.APPLICATION_ID + ".HISTORY_ACTION");
		history_action.putExtra( "example", mEntries.get( position ).getExample() ).putExtra( "result", mEntries.get( position ).getAnswer() );
		setResult( ResultCodes.RESULT_NORMAL, history_action );
		super.onBackPressed();
		//Toast.makeText(this, "You clicked " + adapter.getItem(position).get(0) + " " + adapter.getItem(position).get(0) + " on row number " + position, Toast.LENGTH_SHORT).show();
	}

	private int POSITION_TO_DEL = -1;

	public void onDelete(int position) {
		if ( POSITION_TO_DEL != -1 ) {
			if ( POSITION_TO_DEL < position ) {
				position--;
			}
			delete();
		}

		POSITION_TO_DEL = position;
		adapter.setWaitingToDelete( POSITION_TO_DEL );
		showDeleteCountdownAndRun();
	}

	private final int SECONDS_BEFORE_DELETE = 5;

	@SuppressLint("DefaultLocale")
	private void setupTimer() {
		if ( mTimer != null ) {
			mTimer.cancel();
		}
		countdown_del = SECONDS_BEFORE_DELETE;
		mTimer = new Timer();
		cancel = findViewById( R.id.cancel_delete );
		tCount = cancel.findViewById( R.id.txtCountDown );
		tCount.setText( String.format( "%d", countdown_del ) );
		MyTimer myTimer = new MyTimer();
		mTimer.schedule( myTimer, 600, 1000 );
	}

	View.OnLongClickListener forceDelete = v->{
		delete();
		animate_hide();
		cancelTimer();
		setupRecyclerView();
		return true;
	};

	private void cancelTimer() {
		if ( mTimer != null ) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	@SuppressLint("DefaultLocale")
	private void showDeleteCountdownAndRun() {
		LinearLayout lay = findViewById( R.id.cancel_delete );
		lay.setVisibility( View.VISIBLE );
		( (TextView) lay.findViewById( R.id.txtCountDown ) ).setText( String.format( "%d", SECONDS_BEFORE_DELETE ) );
		Animation animation = AnimationUtils.loadAnimation( getApplicationContext(), R.anim.anim_scale_show );
		//animation.setDuration(500);
		animation.setAnimationListener( new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@SuppressLint("SetTextI18n")
			@Override
			public void onAnimationEnd(Animation animation) {
				//setupTimer();
				if ( !sp.getBoolean( "force_delete_guide_was_showed", false ) ) {
					findViewById( R.id.btnCancel ).setEnabled( false );
					new MaterialTapTargetPrompt.Builder( HistoryActivity.this )
							.setTarget( R.id.btnCancel )
							.setPrimaryText( getResources().getString( R.string.force_delete ) )
							.setFocalColour( Color.TRANSPARENT )
							.setSecondaryText( getResources().getString( R.string.force_delete_guide_text ) )
							.setPromptStateChangeListener( new MaterialTapTargetPrompt.PromptStateChangeListener() {
								@Override
								public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
									if ( state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED ) {
										sp.edit().putBoolean( "force_delete_guide_was_showed", true ).apply();
										setupTimer();
										findViewById( R.id.btnCancel ).setEnabled( true );
									}
								}
							} ).show();
				} else {
					setupTimer();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		} );
		lay.setAnimation( animation );
		animation.start();
	}

	@Override
	public void onDescriptionDelete(int position) {
		AlertDialog alertDialog = new CustomAlertDialogBuilder( this )
				.setCancelable( false )
				.setNegativeButton( R.string.no, (dialogInterface, i)->dialogInterface.cancel() )
				.setPositiveButton( R.string.yes, (dialogInterface, i)->{
					mEntries.get( position ).setDescription( null );

					adapter.updateDescription( null, position );
				} )
				.setTitle( R.string.confirm )
				.setMessage( R.string.confirm_del_desc )
				.create();
		alertDialog.show();
	}

	@Override
	public void onDescriptionEdit(int position) {
		final EditText input = new EditText( this );
		input.setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.colorAccent ) ) );
		input.setTextColor( super.textColor );
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
		input.setLayoutParams( lp );
		input.setText( mEntries.get( position ).getDescription() );

		AlertDialog al = new CustomAlertDialogBuilder( this )
				.setCancelable( false )
				.setView( input )
				.setMessage( R.string.enter_text )
				.setTitle( R.string.desc )
				.setPositiveButton( "OK", (dialog, which)->{
					String newDesc = input.getText().toString();
					newDesc = Utils.trim( newDesc );
					mEntries.get( position ).setDescription( newDesc );
					adapter.updateDescription( newDesc, position );
				} )
				.setNegativeButton( R.string.cancel, (dialog, which)->dialog.cancel() )
				.create();
		al.show();
	}

	@Override
	public void onDescriptionAdd(int position) {
		final EditText input = new EditText( this );
		input.setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.colorAccent ) ) );
		input.setTextColor( super.textColor );
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
		input.setLayoutParams( lp );

		AlertDialog al = new CustomAlertDialogBuilder( this )
				.setCancelable( false )
				.setView( input )
				.setMessage( R.string.enter_text )
				.setTitle( R.string.desc )
				.setPositiveButton( "OK", (dialog, which)->{
					String newDesc = input.getText().toString();
					newDesc = Utils.trim( newDesc );
					if ( newDesc.isEmpty() ) {
						return;
					}
					mEntries.get( position ).setDescription( newDesc );
					adapter.updateDescription( newDesc, position );
				} )
				.setNegativeButton( R.string.cancel, (dialog, which)->dialog.cancel() )
				.create();
		al.show();
	}

	private void saveHistory() {
		StringBuilder save = new StringBuilder();
		int len = mEntries.size();
		for (int i = 0; i < len; i++) {
			save.append( mEntries.get( i ).getExample() );
			if ( mEntries.get( i ).getDescription() != null ) {
				save.append( Constants.HISTORY_DESC_SEPARATOR ).append( mEntries.get( i ).getDescription() );
			}
			save.append( ( (char) 30 ) ).append( mEntries.get( i ).getAnswer() ).append( ( (char) 29 ) );
		}
		sp.edit().putString( "history", save.toString() ).apply();

	}

	private void delete() {
		cancelTimer();
		mEntries.remove( POSITION_TO_DEL );
		adapter.setWaitingToDelete( -1 );
		adapter.remove( POSITION_TO_DEL );
		POSITION_TO_DEL = -1;
		saveHistory();
	}

	private SwipeController mSwipeController = null;

	private void setupRecyclerView() {
		if ( mEntries.size() == 0 ) {
			needToCreateMenu = false;
			if ( mMenu != null ) {
				mMenu.removeItem( R.id.clear_history );
				invalidateOptionsMenu();
			}
			setContentView( R.layout.history_not_found );
			Toolbar toolbar = findViewById( R.id.toolbar );
			toolbar.setTitle( "" );
			setSupportActionBar( toolbar );
			getSupportActionBar().setDisplayHomeAsUpEnabled( true );
			TextView t = findViewById( R.id.txtHistoryNotFound );
			String[] strings = getResources().getStringArray( R.array.history_not_found );
			t.setText( String.format( "%s\n%s\n%s", strings[ 0 ], strings[ 1 ], strings[ 2 ] ) );
		} else {
			LinearLayoutManager lay = new LinearLayoutManager( this );
			lay.setOrientation( RecyclerView.VERTICAL );
			adapter = new HistoryAdapter( this, mEntries, this );
			rv.setAdapter( adapter );
			rv.setLayoutManager( lay );

			RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration() {
				@Override
				public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
					mSwipeController.onDraw( c );
					super.onDraw( c, parent, state );
				}
			};

			mSwipeController = new SwipeController( new SwipeControllerActions() {
				@Override
				public void onRightClicked(int position) {
					if ( position != POSITION_TO_DEL ) {
						onDelete( position );
					}
				}

				@Override
				public void onLeftClicked(int position) {
					if ( position != POSITION_TO_DEL ) {
						adapter.toggleDescriptionLayoutVisibility( position );
					}
				}
			}, this );

			ItemTouchHelper itemTouchHelper = new ItemTouchHelper( mSwipeController );
			itemTouchHelper.attachToRecyclerView( rv );
			rv.addItemDecoration( itemDecoration );
		}
	}

	public void cancel(View v) {
		cancelTimer();
		adapter.setWaitingToDelete( -1 );
		POSITION_TO_DEL = -1;
		animate_hide();
	}

	Timer mTimer;
	int countdown_del = 6;
	TextView tCount;
	LinearLayout cancel;

	class MyTimer extends TimerTask {
		@SuppressLint("SetTextI18n")
		@Override
		public void run() {
			if ( countdown_del != 0 ) {
				countdown_del--;
				runOnUiThread( ()->tCount.setText( Integer.toString( countdown_del ) ) );
			} else {
				cancelTimer();
				runOnUiThread( HistoryActivity.this::delete );
				animate_hide();
			}
		}
	}

	public void animate_hide() {
		Animation anim = AnimationUtils.loadAnimation( getApplicationContext(), R.anim.anim_scale_hide );
		try {
			cancel.clearAnimation();
			cancel.setAnimation( anim );
			anim.setAnimationListener( new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					cancel.setVisibility( View.GONE );
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			} );
			anim.start();
		} catch (Exception e) {
			onSomethingWentWrong();
		}
	}

	String start_type;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate( R.menu.menu_history, menu );
		mMenu = menu;
		if ( !needToCreateMenu ) {
			mMenu.removeItem( R.id.clear_history );
		}
		return super.onCreateOptionsMenu( menu );
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		//Toast.makeText(getApplicationContext(), Integer.toString(id) + " " + Integer.toString(R.id.home), Toast.LENGTH_SHORT).show();
		if ( id == android.R.id.home ) {
			onBackPressed();
		} else if ( id == R.id.clear_history ) {
			CustomAlertDialogBuilder build = new CustomAlertDialogBuilder( this );
			build.setMessage( R.string.confirm_cls_history )
					.setCancelable( false )
					.setPositiveButton( R.string.yes, (dialog, which)->{
						dialog.cancel();
						sp.edit().remove( "history" ).apply();
						mEntries = new ArrayList<>();
						setupRecyclerView();
					} ).setNegativeButton( R.string.no, (dialog, which)->dialog.cancel() );
			build.create().show();
		}
		return super.onOptionsItemSelected( item );
	}

	private void prepareHistoryForRecyclerView() {
		String his = sp.getString( "history", null );
		//reformatHistory();
		if ( his != null ) {
			ProgressDialog progressDialog = new ProgressDialog( this );
			progressDialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
			progressDialog.show();
			needToCreateMenu = true;
			this.invalidateOptionsMenu();
			int i = 0;
			String ex, ans;
			while ( i < his.length() ) {// && his.charAt(i) != Constants.HISTORY_ENTRIES_SEPARATOR){
				boolean was_dot = false;
				ex = ans = "";
				while ( i < his.length() ) {
					if ( his.charAt( i ) == Constants.HISTORY_ENTRIES_SEPARATOR ) {
						i++;
						break;
					}
					if ( his.charAt( i ) == Constants.HISTORY_IN_ENTRY_SEPARATOR ) {
						i++;
						was_dot = true;
						continue;
					}
					if ( !was_dot ) {
						ex = String.format( "%s%c", ex, his.charAt( i ) );
					} else {
						ans = String.format( "%s%c", ans, his.charAt( i ) );
					}
					i++;
				}
				String description = null;
				if ( ex.contains( Character.toString( Constants.HISTORY_DESC_SEPARATOR ) ) ) {
					int j = 0;
					while ( j < ex.length() && ex.charAt( j ) != Constants.HISTORY_DESC_SEPARATOR ) {
						j++;
					}
					description = ex.substring( j + 1 );
					ex = ex.substring( 0, j );
				}
				mEntries.add( new HistoryEntry( ex, ans, description ) );
			}
			progressDialog.dismiss();
		}
		setupRecyclerView();
	}

	private void runReformat() {
		try {
			ProgressDialog pd = new ProgressDialog( this );
			pd.requestWindowFeature( Window.FEATURE_NO_TITLE );
			pd.setCancelable( false );
			new HistoryStorageProtocolsFormatter( this ).reformatHistory( LOCAL_HISTORY_STORAGE_PROTOCOL_VERSION, Constants.HISTORY_STORAGE_PROTOCOL_VERSION );
			pd.dismiss();
			prepareHistoryForRecyclerView();
		} catch (StringIndexOutOfBoundsException e) {
			onSomethingWentWrong();
		}
	}

	@SuppressLint({ "ClickableViewAccessibility", "SetTextI18n" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sp = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_history );

		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );

		history_action = new Intent( BuildConfig.APPLICATION_ID + ".HISTORY_ACTION" );
		history_action.putExtra( "example", "" ).putExtra( "result", "" );

		Button btn = findViewById( R.id.btnCancel );
		btn.setOnLongClickListener( forceDelete );

		rv = findViewById( R.id.rv_view );

		rv.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if ( sp.getBoolean( "history_guide", true ) ) {
					if ( mEntries.size() > 0 ) {
						sp.edit().putBoolean( "history_guide", false ).apply();
						Utils.GuideMaker.getGuideTip(
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

		start_type = getIntent().getStringExtra( "start_type" );
		LOCAL_HISTORY_STORAGE_PROTOCOL_VERSION = sp.getInt( "local_history_storage_protocol_version", 1 );
		String history = sp.getString( "history", null );
		if ( history != null ) {
			if ( LOCAL_HISTORY_STORAGE_PROTOCOL_VERSION < Constants.HISTORY_STORAGE_PROTOCOL_VERSION ) {
				CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder( this );
				builder
						.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
								runReformat();
							}
						} )
						.setCancelable( false )
						.setMessage( R.string.confirm_history_reformat );
				builder.create().show();
			} else if ( LOCAL_HISTORY_STORAGE_PROTOCOL_VERSION > Constants.HISTORY_STORAGE_PROTOCOL_VERSION ) {
				setContentView( R.layout.activity_history_protocols_donot_match );
				TextView note = findViewById( R.id.lblProtocolsDoNotMatch );
				String text = "";
				String[] arr = getResources().getStringArray( R.array.protocols_do_not_match );
				for (String s : arr) {
					text = String.format( "%s\n%s", text, s );
				}
				note.setText( text );
				needToCreateMenu = false;
				if ( mMenu != null ) {
					mMenu.removeItem( R.id.clear_history );
					this.invalidateOptionsMenu();
				}
			} else {
				prepareHistoryForRecyclerView();
			}
		} else {
			sp.edit().putInt( "local_history_storage_protocol_version", Constants.HISTORY_STORAGE_PROTOCOL_VERSION ).apply();
			prepareHistoryForRecyclerView();
		}
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate( savedInstanceState );
		if ( !needToCreateMenu && mMenu != null ) {
			mMenu.removeItem( R.id.clear_history );
			this.invalidateOptionsMenu();
		}
	}
}
