package com.maxsavteam.newmcalc2.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
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

import com.maxsavteam.newmcalc2.App;
import com.maxsavteam.newmcalc2.BuildConfig;
import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.adapters.HistoryAdapter;
import com.maxsavteam.newmcalc2.entity.HistoryEntry;
import com.maxsavteam.newmcalc2.swipes.SwipeController;
import com.maxsavteam.newmcalc2.swipes.SwipeControllerActions;
import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;
import com.maxsavteam.newmcalc2.utils.FormatUtils;
import com.maxsavteam.newmcalc2.utils.HistoryManager;
import com.maxsavteam.newmcalc2.utils.ResultCodesConstants;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class HistoryActivity extends ThemeActivity implements HistoryAdapter.AdapterCallback {

	public static final String TAG = Main2Activity.TAG + " History";

	private HistoryAdapter adapter;

	private SharedPreferences sp;
	private Intent mHistoryAction;
	private ArrayList<HistoryEntry> mEntries = new ArrayList<>();
	private RecyclerView rv;
	private boolean needToCreateMenu = false;
	private Menu mMenu;
	private SwipeController mSwipeController = null;
	private int mPositionToDel = -1;

	@Override
	public void onBackPressed() {
		setResult( ResultCodesConstants.RESULT_NORMAL, mHistoryAction );
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		HistoryManager.getInstance().save();
		super.onPause();
	}

	private void onSomethingWentWrong() {
		Toast.makeText( this, getResources().getString( R.string.smth_went_wrong ), Toast.LENGTH_LONG ).show();
		mHistoryAction.putExtra( "error", true );
		setResult( ResultCodesConstants.RESULT_ERROR, mHistoryAction );
		super.onBackPressed();
	}

	@Override
	public void onItemClick(View view, int position) {
		if ( position == mPositionToDel ) {
			cancelTimer();
			startHideAnimation();
		}
		String example = mEntries.get( position ).getExample();
		String answer = mEntries.get( position ).getAnswer();
		mHistoryAction
				.putExtra( "example", example )
				.putExtra( "result", answer );
		setResult( ResultCodesConstants.RESULT_NORMAL, mHistoryAction );
		super.onBackPressed();
	}

	public void onDelete(int position) {
		if ( mPositionToDel != -1 ) {
			if ( mPositionToDel < position ) {
				position--;
			}
			delete();
		}

		mPositionToDel = position;
		adapter.setWaitingToDelete( mPositionToDel );
		showDeleteCountdownAndRun();
	}

	private final int SECONDS_BEFORE_DELETE = 5;

	private CountDownTimer mCountDownTimer;
	private TextView mCountDownTextView;
	private LinearLayout mCancelLayout;

	@SuppressLint("DefaultLocale")
	private void setupTimer() {
		if ( mCountDownTimer != null ) {
			mCountDownTimer.cancel();
		}
		mCancelLayout = findViewById( R.id.cancel_delete );
		mCountDownTextView = mCancelLayout.findViewById( R.id.txtCountDown );
		mCountDownTextView.setText( String.format( "%d", SECONDS_BEFORE_DELETE ) );
		mCountDownTimer = new CountDownTimer( SECONDS_BEFORE_DELETE * 1000, 1000 ){
			@Override
			public void onTick(long millisUntilFinished) {
				runOnUiThread( ()->{
					mCountDownTextView.setText( String.format( "%d", millisUntilFinished / 1000L ) );
				} );
			}

			@Override
			public void onFinish() {
				cancelTimer();
				runOnUiThread( HistoryActivity.this::delete );
				startHideAnimation();
			}
		};
		mCountDownTimer.start();
	}

	private final View.OnLongClickListener forceDelete = v->{
		delete();
		startHideAnimation();
		cancelTimer();
		setupRecyclerView();
		return true;
	};

	private void cancelTimer() {
		if ( mCountDownTimer != null ) {
			mCountDownTimer.cancel();
			mCountDownTimer = null;
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
							.setPromptStateChangeListener( (prompt, state)->{
								if ( state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED ) {
									sp.edit().putBoolean( "force_delete_guide_was_showed", true ).apply();
									setupTimer();
									findViewById( R.id.btnCancel ).setEnabled( true );
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
					HistoryManager
							.getInstance()
							.change( position, mEntries.get( position ) )
							.save();
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
					HistoryManager
							.getInstance()
							.change( position, mEntries.get( position ) )
							.save();
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
					HistoryManager
							.getInstance()
							.change( position, mEntries.get( position ) )
							.save();
					adapter.updateDescription( newDesc, position );
				} )
				.setNegativeButton( R.string.cancel, (dialog, which)->dialog.cancel() )
				.create();
		al.show();
	}

	private void delete() {
		cancelTimer();
		mEntries.remove( mPositionToDel );
		HistoryManager.getInstance()
				.remove( mPositionToDel )
				.save();
		adapter.setWaitingToDelete( -1 );
		adapter.remove( mPositionToDel );
		mPositionToDel = -1;
	}

	private void setupRecyclerView() {
		needToCreateMenu = !mEntries.isEmpty();
		invalidateOptionsMenu();
		if ( mEntries.size() == 0 ) {
			setContentView( R.layout.layout_history_not_found );
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
					if ( position != mPositionToDel ) {
						onDelete( position );
					}
				}

				@Override
				public void onLeftClicked(int position) {
					if ( position != mPositionToDel ) {
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
		mPositionToDel = -1;
		startHideAnimation();
	}

	public void startHideAnimation() {
		Animation anim = AnimationUtils.loadAnimation( getApplicationContext(), R.anim.anim_scale_hide );
		try {
			mCancelLayout.clearAnimation();
			mCancelLayout.setAnimation( anim );
			anim.setAnimationListener( new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mCancelLayout.setVisibility( View.GONE );
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
						setupRecyclerView();
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

		mHistoryAction = new Intent( BuildConfig.APPLICATION_ID + ".HISTORY_ACTION" );
		mHistoryAction.putExtra( "example", "" ).putExtra( "result", "" );

		Button btn = findViewById( R.id.btnCancel );
		btn.setOnLongClickListener( forceDelete );

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
							decimalFormat.format( new BigDecimal( entry.getAnswer() ) ),
							entry.getDescription()
					) );
		}
		setupRecyclerView();
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
