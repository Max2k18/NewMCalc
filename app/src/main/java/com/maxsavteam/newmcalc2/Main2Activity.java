package com.maxsavteam.newmcalc2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.maxsavteam.calculator.CalculatorExpressionFormatter;
import com.maxsavteam.calculator.CalculatorExpressionTokenizer;
import com.maxsavteam.calculator.exceptions.CalculatingException;
import com.maxsavteam.calculator.utils.CalculatorUtils;
import com.maxsavteam.newmcalc2.adapters.MyFragmentPagerAdapter;
import com.maxsavteam.newmcalc2.core.CalculatorWrapper;
import com.maxsavteam.newmcalc2.fragments.MathOperationsFragment;
import com.maxsavteam.newmcalc2.fragments.NumPadFragment;
import com.maxsavteam.newmcalc2.fragments.VariablesFragment;
import com.maxsavteam.newmcalc2.types.HistoryEntry;
import com.maxsavteam.newmcalc2.ui.AboutAppActivity;
import com.maxsavteam.newmcalc2.ui.HistoryActivity;
import com.maxsavteam.newmcalc2.ui.MemoryActionsActivity;
import com.maxsavteam.newmcalc2.ui.NumberGeneratorActivity;
import com.maxsavteam.newmcalc2.ui.NumberSystemConverterActivity;
import com.maxsavteam.newmcalc2.ui.PasswordGeneratorActivity;
import com.maxsavteam.newmcalc2.ui.SettingsActivity;
import com.maxsavteam.newmcalc2.ui.VariableEditorActivity;
import com.maxsavteam.newmcalc2.utils.FormatUtils;
import com.maxsavteam.newmcalc2.utils.HistoryManager;
import com.maxsavteam.newmcalc2.utils.MemorySaverReader;
import com.maxsavteam.newmcalc2.utils.RequestCodesConstants;
import com.maxsavteam.newmcalc2.utils.ResultCodesConstants;
import com.maxsavteam.newmcalc2.utils.UpdateMessagesContainer;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.variables.Variable;
import com.maxsavteam.newmcalc2.variables.VariableUtils;
import com.maxsavteam.newmcalc2.widget.CalculatorEditText;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Main2Activity extends ThemeActivity {

	public static final String TAG = "MCalc";

	private NavigationView mNavigationView;
	private SharedPreferences sp;

	private boolean isOtherActivityOpened = false;
	private boolean isBroadcastsRegistered;
	private boolean progressDialogShown = false;
	private boolean wasError = false;

	private ArrayList<BroadcastReceiver> registeredBroadcasts = new ArrayList<>();
	private MemorySaverReader mMemorySaverReader;
	private BigDecimal[] memoryEntries;
	private CalculatorWrapper mCalculatorWrapper;
	private final Point displaySize = new Point();

	private String MULTIPLY_SIGN;
	private String DIVISION_SIGN;

	private Timer mCoreTimer;
	private Thread mCoreThread = null;

	private int timerCountDown = 0;
	private final static int ROUND_SCALE = 8;

	private ProgressDialog mThreadControllerProgressDialog;

	private DecimalFormat mDecimalFormat;

	enum CalculateMode {
		PRE_ANSWER,
		FULL_ANSWER
	}

	private final View.OnLongClickListener mOnVariableLongClick = v->{
		Button btn = (Button) v;
		int pos = Integer.parseInt( btn.getTag().toString() );
		Intent in = new Intent();
		in.putExtra( "tag", pos ).putExtra( "is_existing", true );
		ArrayList<Variable> a = VariableUtils.readVariables();
		for (int i = 0; i < a.size(); i++) {
			if ( a.get( i ).getTag() == pos ) {
				in.putExtra( "name", btn.getText().toString() ).putExtra( "value", a.get( i ).getValue() );
				break;
			}
		}
		goToActivity( VariableEditorActivity.class, in );
		return true;
	};

	private final View.OnLongClickListener mReturnBack = v->{
		if ( !wasError ) {
			TextView answer = findViewById( R.id.AnswerStr );
			CalculatorEditText example = findViewById( R.id.ExampleStr );
			if ( answer.getVisibility() == View.INVISIBLE || answer.getVisibility() == View.GONE ) {
				return false;
			}
			String txt = answer.getText().toString();
			answer.setText( example.getText() );
			example.setText( txt );
			example.setSelection( txt.length() );
			return true;
		} else {
			return false;
		}
	};

	private final View.OnLongClickListener mMemoryActionsLongClick = (View v)->{
		if ( v.getId() == R.id.btnMR ) {
			openMemory( "rc" );
		} else {
			openMemory( "st" );
		}
		return true;
	};

	@Override
	public void onBackPressed() {
		CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder( this );
		builder.setTitle( R.string.exit )
				.setMessage( R.string.areyousureexit )
				.setCancelable( false )
				.setNegativeButton( R.string.no, (dialog, which)->dialog.cancel() )
				.setPositiveButton( R.string.yes, (dialog, which)->{
					dialog.cancel();
					super.onBackPressed();
				} );
		builder.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if ( id == R.id.about ) {
			goToActivity( AboutAppActivity.class, null );
			return true;
		} else if ( id == R.id.changelog ) {
			Intent in = new Intent( Intent.ACTION_VIEW );
			in.setData( Uri.parse( Utils.MCALC_SITE + "changelog#" + BuildConfig.VERSION_NAME ) );
			startActivity( in );
		}
		return super.onOptionsItemSelected( item );
	}

	@SuppressLint("SourceLockedOrientationActivity")
	private void applyTheme() {
		WindowManager windowManager = (WindowManager) getSystemService( Context.WINDOW_SERVICE );
		if ( windowManager != null ) {
			Display d = windowManager.getDefaultDisplay();
			d.getSize( displaySize );
		}
		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
		View header = mNavigationView.getHeaderView( 0 );
		ViewGroup.LayoutParams headerLayoutParams = header.getLayoutParams();
		headerLayoutParams.height = displaySize.y / 3;
		header.setLayoutParams( headerLayoutParams );
		ImageButton imageButton = header.findViewById( R.id.imgBtnHeader );
		imageButton.setOnClickListener( v->{
			Intent in = new Intent( Intent.ACTION_VIEW );
			in.setData( Uri.parse( Utils.MCALC_SITE ) );
			startActivity( in );
		} );
		if ( super.isDarkMode ) {
			mNavigationView.setBackgroundColor( Color.BLACK );
		} else {
			mNavigationView.setBackgroundColor( Color.WHITE );
		}

		ColorStateList navMenuTextList = new ColorStateList(
				new int[][]{
						new int[]{ android.R.attr.state_checked },
						new int[]{ -android.R.attr.state_checked }
				},
				new int[]{
						super.textColor,
						super.textColor
				}
		);
		ColorStateList navMenuIconColors = new ColorStateList(
				new int[][]{
						new int[]{ android.R.attr.state_checked },
						new int[]{ -android.R.attr.state_checked }
				},
				new int[]{
						super.textColor,
						super.textColor
				}
		);
		mNavigationView.setItemIconTintList( navMenuIconColors );
		//mNavigationView.setItemBackgroundResource(R.drawable.grey);
		mNavigationView.setItemTextColor( navMenuTextList );
		Toolbar toolbar = findViewById( R.id.toolbar );
		if ( toolbar != null ) {
			toolbar.setElevation( 0 );
			if ( super.isDarkMode ) {
				toolbar.setBackgroundColor( Color.BLACK );
				toolbar.setTitleTextColor( Color.WHITE );
				toolbar.setNavigationIcon( R.drawable.ic_menu_white );
			} else {
				toolbar.setBackgroundColor( Color.WHITE );
				toolbar.setTitleTextColor( Color.BLACK );
				toolbar.setNavigationIcon( R.drawable.ic_menu );
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if ( requestCode == 10 && grantResults.length == 2 ) {
			if ( grantResults[ 0 ] == PackageManager.PERMISSION_DENIED
					|| grantResults[ 1 ] == PackageManager.PERMISSION_DENIED ) {
				sp.edit().putBoolean( "storage_denied", true ).apply();
			}
			if ( grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED
					&& grantResults[ 1 ] == PackageManager.PERMISSION_GRANTED ) {
				sp.edit().remove( "storage_denied" ).remove( "never_request_permissions" ).apply();
			}
		}
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate( savedInstanceState );

		registerBroadcastReceivers();

		mMemorySaverReader = new MemorySaverReader();
		memoryEntries = mMemorySaverReader.read();

		addShortcutsToApp();

		restoreResultIfSaved();
	}

	private void addShortcutsToApp() {
		if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1 ) {
			ShortcutManager shortcutManager = getSystemService( ShortcutManager.class );
			Intent t = new Intent( Intent.ACTION_VIEW, null, this, Main2Activity.class );
			t.putExtra( "shortcut_action", true );
			t.putExtra( "to_", "numgen" );
			ShortcutInfo shortcut1 = new ShortcutInfo.Builder( getApplicationContext(), "id1" )
					.setLongLabel( getResources().getString( R.string.random_number_generator ) )
					.setShortLabel( getResources().getString( R.string.random_number_generator ) )
					.setIcon( Icon.createWithResource( this, R.drawable.ic_dice ) )
					.setIntent( t )
					.build();
			t.putExtra( "to_", "pass" );
			ShortcutInfo shortcut2 = new ShortcutInfo.Builder( getApplicationContext(), "id2" )
					.setLongLabel( getResources().getString( R.string.password_generator ) )
					.setShortLabel( getResources().getString( R.string.password_generator ) )
					.setIcon( Icon.createWithResource( this, R.drawable.ic_passgen ) )
					.setIntent( t )
					.build();

			t.putExtra( "to_", "history" );
			ShortcutInfo shortcut3 = new ShortcutInfo.Builder( getApplicationContext(), "id3" )
					.setLongLabel( getResources().getString( R.string.history ) )
					.setShortLabel( getResources().getString( R.string.history ) )
					.setIcon( Icon.createWithResource( this, R.drawable.ic_history ) )
					.setIntent( t )
					.build();
			t.putExtra( "to_", "bin" );
			ShortcutInfo shortCutNumSys = new ShortcutInfo.Builder( getApplicationContext(), "idNumSys" )
					.setLongLabel( getResources().getString( R.string.number_system_converter ) )
					.setShortLabel( getResources().getString( R.string.number_system_converter ) )
					.setIcon( Icon.createWithResource( this, R.drawable.ic_binary ) )
					.setIntent( t )
					.build();


			if ( shortcutManager != null ) {
				shortcutManager.setDynamicShortcuts( Arrays.asList( shortcut3, shortCutNumSys, shortcut2, shortcut1 ) );
			}
		}
	}

	private void restoreResultIfSaved() {
		if ( sp.getBoolean( "saveResult", false ) ) {
			String text = sp.getString( "saveResultText", null );
			if ( text != null ) {
				int i = 0;
				StringBuilder ex = new StringBuilder();
				while ( i < text.length() && text.charAt( i ) != ';' ) {
					ex.append( text.charAt( i ) );
					i++;
				}
				insert( ex.toString() );
				calculate( CalculateMode.FULL_ANSWER );
			}
		}
	}

	private void goToAdditionalActivities(@NonNull String where) {
		switch ( where ) {
			case "settings":
				goToActivity( SettingsActivity.class, new Intent()
						.putExtra( "action", "simple" )
						.putExtra( "start_type", "app" ) );
				break;
			case "numgen":
				goToActivity( NumberGeneratorActivity.class, new Intent()
						.putExtra( "type", "number" )
						.putExtra( "start_type", "app" ) );
				break;
			case "history":
				goToActivity( HistoryActivity.class, new Intent()
						.putExtra( "start_type", "app" ) );
				break;
			case "pass":
				goToActivity( PasswordGeneratorActivity.class, new Intent()
						.putExtra( "start_type", "app" )
						.putExtra( "type", "pass" ) );
				break;
			case "bin":
				goToActivity( NumberSystemConverterActivity.class, new Intent()
						.putExtra( "start_type", "app" ) );
				break;
			default:
				break;
		}
	}

	private void unregisterAllBroadcasts() {
		for (BroadcastReceiver broadcastReceiver : registeredBroadcasts) {
			unregisterReceiver( broadcastReceiver );
		}
		isBroadcastsRegistered = false;
		registeredBroadcasts = new ArrayList<>();
	}

	@Override
	protected void onPause() {
		if ( !isOtherActivityOpened ) {
			unregisterAllBroadcasts();
		}
		getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		super.onPause();
	}

	@Override
	protected void onResume() {
		isOtherActivityOpened = false;
		if ( !isBroadcastsRegistered ) {
			registerBroadcastReceivers();
		}
		if ( sp.getBoolean( "keep_screen_on", false ) ) {
			getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		if ( !isOtherActivityOpened ) {
			unregisterAllBroadcasts();
		}
		super.onStop();
	}

	private void registerBroadcastReceivers() {
		BroadcastReceiver on_var_edited = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				setViewPager( 1 );
			}
		};
		registerReceiver( on_var_edited,
				new IntentFilter( BuildConfig.APPLICATION_ID + ".VARIABLES_SET_CHANGED" ) );
		registeredBroadcasts.add( on_var_edited );

		isBroadcastsRegistered = true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utils.setContext( this );
		super.onCreate( savedInstanceState );
		sp = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		setContentView( R.layout.activity_main2 );
		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		DrawerLayout drawer = findViewById( R.id.drawer_layout );
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
		drawer.setDrawerListener( toggle );
		toggle.syncState();
		mNavigationView = findViewById( R.id.nav_view );
		mNavigationView.setBackgroundColor( Color.BLACK );
		mNavigationView.setNavigationItemSelectedListener( menuItem->{
			if ( menuItem.getItemId() == R.id.nav_settings ) {
				goToAdditionalActivities( "settings" );
			} else if ( menuItem.getItemId() == R.id.nav_history ) {
				goToAdditionalActivities( "history" );
			} else if ( menuItem.getItemId() == R.id.nav_numbersysconverter ) {
				goToAdditionalActivities( "bin" );
			} else if ( menuItem.getItemId() == R.id.nav_passgen ) {
				goToAdditionalActivities( "pass" );
			} else if ( menuItem.getItemId() == R.id.nav_numgen ) {
				goToAdditionalActivities( "numgen" );
			}
			menuItem.setChecked( false );

			drawer.closeDrawer( GravityCompat.START );
			return true;
		} );

		mCalculatorWrapper = CalculatorWrapper.getInstance();

		mDecimalFormat = new DecimalFormat("#,##0.###", new DecimalFormatSymbols( App.getInstance().getAppLocale() ));
		mDecimalFormat.setParseBigDecimal( true );
		mDecimalFormat.setMaximumFractionDigits( 8 );

		MULTIPLY_SIGN = getResources().getString( R.string.multiply );
		DIVISION_SIGN = getResources().getString( R.string.div );

		EditText editText = findViewById( R.id.ExampleStr );
		editText.setShowSoftInputOnFocus( false );
		editText.setSelection( 0 );

		ImageButton imageButton = findViewById( R.id.btnDelete );
		imageButton.setOnLongClickListener( v->{
			onClear( v );
			return true;
		} );

		applyTheme();

		Intent startIntent = getIntent();
		if ( startIntent.getBooleanExtra( "shortcut_action", false ) ) {
			String whereWeNeedToGoToAnotherActivity = startIntent.getStringExtra( "to_" );
			if ( whereWeNeedToGoToAnotherActivity != null ) {
				goToAdditionalActivities( whereWeNeedToGoToAnotherActivity );
			}
		}

		setViewPager( 0 );

		initializeScrollViews();

		showWhatNew();
	}

	private void showWhatNew() {
		String version = BuildConfig.VERSION_NAME;
		if ( UpdateMessagesContainer.isReleaseNoteExists( version ) && !UpdateMessagesContainer.isReleaseNoteShown( version ) ) {
			Spanned spanned = Html.fromHtml( getString( UpdateMessagesContainer.getStringIdForNote( version ) ) );
			CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder( this );
			builder.setTitle( R.string.whats_new )
					.setMessage( spanned )
					.setCancelable( false )
					.setPositiveButton( R.string.ok, ( (dialog, which)->{
						SharedPreferences sharedPreferences = getSharedPreferences( "shown_release_notes", MODE_PRIVATE );
						sharedPreferences.edit().putBoolean( version, true ).apply();
						dialog.cancel();
					} ) );
			builder.show();
		}
	}

	private void updateExampleArrows() {
		HorizontalScrollView scrollView = findViewById( R.id.scrollview );

		if ( scrollView.canScrollHorizontally( 1 ) ) {
			showWithAlpha( R.id.example_right );
		} else {
			hideWithAlpha( R.id.example_right );
		}

		if ( scrollView.canScrollHorizontally( -1 ) ) {
			showWithAlpha( R.id.example_left );
		} else {
			hideWithAlpha( R.id.example_left );
		}
	}

	private void updateAnswerArrows() {
		HorizontalScrollView scrollView = findViewById( R.id.scrollViewAns );

		if ( scrollView.canScrollHorizontally( 1 ) ) {
			showWithAlpha( R.id.answer_right );
		} else {
			hideWithAlpha( R.id.answer_right );
		}

		if ( scrollView.canScrollHorizontally( -1 ) ) {
			showWithAlpha( R.id.answer_left );
		} else {
			hideWithAlpha( R.id.answer_left );
		}
	}

	private void initializeScrollViews() {
		CalculatorEditText exampleEditText = findViewById( R.id.ExampleStr );
		exampleEditText.addListener( this::updateExampleArrows );

		HorizontalScrollView exampleScrollView = findViewById( R.id.scrollview );
		exampleScrollView.setOnScrollChangeListener( (v, scrollX, scrollY, oldScrollX, oldScrollY)->updateExampleArrows() );

		CalculatorEditText answerTextView = findViewById( R.id.AnswerStr );
		answerTextView.addListener( this::updateAnswerArrows );
		answerTextView.addListener( this::scrollAnswerToEnd );

		HorizontalScrollView answerScrollView = findViewById( R.id.scrollViewAns );
		answerScrollView.setOnScrollChangeListener( (v, scrollX, scrollY, oldScrollX, oldScrollY)->updateAnswerArrows() );
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if ( requestCode == RequestCodesConstants.START_HISTORY && resultCode != ResultCodesConstants.RESULT_ERROR && data != null ) {
			String example = data.getStringExtra( "example" );
			if ( example != null && !example.equals( "" ) ) {
				String result = data.getStringExtra( "result" );

				addStringExampleToTheExampleStr( result );
			}
		}
		if ( requestCode == RequestCodesConstants.START_MEMORY_RECALL ) {
			if ( resultCode == ResultCodesConstants.RESULT_APPEND ) {
				if ( data != null ) {
					addStringExampleToTheExampleStr( data.getStringExtra( "value" ) );
				}
			} else if ( resultCode == ResultCodesConstants.RESULT_REFRESH ) {
				memoryEntries = mMemorySaverReader.read();
			}
		}
		if ( requestCode == RequestCodesConstants.START_ADD_VAR ) {
			setViewPager( ( (ViewPager) findViewById( R.id.viewpager ) ).getCurrentItem() );
		}
		if ( resultCode == ResultCodesConstants.RESULT_RESTART_APP ) {
			//Intent intent = new Intent( this, Main2Activity.class );
			Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage( getPackageName() );
			if(intent != null) {
				intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
				startActivity( intent );
				finish();
			}
		}
		super.onActivityResult( requestCode, resultCode, data );
	}

	private void showWithAlpha(int id) {
		findViewById( id ).animate().alpha( 1f ).setDuration( 100 ).start();
	}

	private void hideWithAlpha(int id) {
		findViewById( id ).animate().alpha( 0f ).setDuration( 100 ).start();
	}

	private void setViewPager(int which) {
		ArrayList<Fragment> fragments = new ArrayList<>();
		fragments.add( new NumPadFragment( this, mReturnBack ) );
		fragments.add( new MathOperationsFragment() );
		fragments.add( new VariablesFragment( mMemoryActionsLongClick, mOnVariableLongClick ) );

		MyFragmentPagerAdapter myFragmentPagerAdapter =
				new MyFragmentPagerAdapter( getSupportFragmentManager(), fragments );

		ViewPager viewPager = findViewById( R.id.viewpager );
		ViewGroup.LayoutParams lay = viewPager.getLayoutParams();
		lay.height = displaySize.y / 2;
		viewPager.setLayoutParams( lay );
		viewPager.setAdapter( myFragmentPagerAdapter );
		viewPager.setCurrentItem( which );
		viewPager.addOnPageChangeListener( new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				if ( position == 0 ) {
					hideWithAlpha( R.id.image_view_left );
					showWithAlpha( R.id.image_view_right );
				} else if ( position == fragments.size() - 1 ) {
					showWithAlpha( R.id.image_view_left );
					hideWithAlpha( R.id.image_view_right );
				} else {
					showWithAlpha( R.id.image_view_left );
					showWithAlpha( R.id.image_view_right );
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		} );
	}

	private void addStringExampleToTheExampleStr(String s) {
		EditText editText = findViewById( R.id.ExampleStr );
		Editable e = editText.getText();
		if ( e == null || e.toString().isEmpty() ) {
			insert( s );
		} else {
			insert( "(" + s + ")" );
		}
	}

	private void writeCalculationError(String text) {
		TextView t = findViewById( R.id.AnswerStr );
		t.setText( text );
		t.setTextColor( Color.parseColor( "#FF4B32" ) );
	}

	public void variableClick(View v) {
		try {
			Button btn = (Button) v;
			int pos = Integer.parseInt( btn.getTag().toString() );
			String text = btn.getText().toString();
			if ( text.equals( "+" ) ) {
				Intent in = new Intent();
				in.putExtra( "tag", pos );
				TextView t = findViewById( R.id.ExampleStr );
				String ts = t.getText().toString();
				if ( !ts.equals( "" ) && ts.length() < 1000 ) {
					ts = Utils.deleteSpaces( ts );
					if ( Utils.isNumber( ts ) ) {
						in.putExtra( "value", ts ).putExtra( "name", "" ).putExtra( "is_existing", true );
					}
				}
				goToActivity( VariableEditorActivity.class, in );
			} else {
				String var_arr = sp.getString( "variables", null );
				if ( var_arr == null ) {
					btn.setText( "+" );
				} else {
					ArrayList<Variable> a = VariableUtils.readVariables();
					for (int i = 0; i < a.size(); i++) {
						if ( a.get( i ).getTag() == pos ) {
							addStringExampleToTheExampleStr( a.get( i ).getValue() );
							//break;
							return;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText( this, e.toString(), Toast.LENGTH_LONG ).show();
		}
	}

	private void openMemory(final String type) {
		Intent in = new Intent();
		in.putExtra( "type", type );
		if ( type.equals( "st" ) ) {
			TextView t = findViewById( R.id.ExampleStr );
			if ( Utils.isNumber( t.getText().toString() ) ) {
				in.putExtra( "value", t.getText().toString() );
			} else {
				return;
			}
		}
		goToActivity( MemoryActionsActivity.class, in );
	}

	public void onMemoryPlusMinusButtonsClick(View v) {
		TextView textExample = findViewById( R.id.ExampleStr );
		String text = textExample.getText().toString();
		if ( text.equals( "" ) ) {
			return;
		}

		BigDecimal temp;
		try {
			temp = CalculatorWrapper.getInstance().calculate( text );
		} catch (CalculatingException | NumberFormatException e) {
			Log.i( TAG, "onMemoryPlusMinusButtonsClick: " + e );
			Toast.makeText( this, R.string.some_error_occurred, Toast.LENGTH_SHORT ).show();
			return;
		}
		if ( v.getId() == R.id.btnMemPlus ) {
			temp = temp.add( memoryEntries[ 0 ] );
		} else if ( v.getId() == R.id.btnMemMinus ) {
			temp = memoryEntries[ 0 ].subtract( temp );
		}
		memoryEntries[ 0 ] = temp;
		mMemorySaverReader.save( memoryEntries );
	}

	public void onMemoryStoreButtonClick(View view) {
		TextView t = findViewById( R.id.ExampleStr );
		String text = t.getText().toString();
		if ( text.equals( "" ) ) {
			return;
		}
		BigDecimal temp;
		try {
			temp = CalculatorWrapper.getInstance().calculate( text );
		} catch (CalculatingException | NumberFormatException e) {
			Log.i( TAG, "onMemoryPlusMinusButtonsClick: " + e );
			Toast.makeText( this, R.string.some_error_occurred, Toast.LENGTH_SHORT ).show();
			return;
		}
		memoryEntries[ 0 ] = temp;
		mMemorySaverReader.save( memoryEntries );
	}

	public void onMemoryRecallButtonClick(View view) {
		String value = memoryEntries[ 0 ].toString();
		addStringExampleToTheExampleStr( value );
	}

	private void goToActivity(Class<?> cls, Intent possibleExtras) {
		Intent intent = new Intent( this, cls );
		if ( possibleExtras != null && possibleExtras.getExtras() != null ) {
			intent.putExtras( possibleExtras.getExtras() );
		}
		int requestCode = 0;
		isOtherActivityOpened = true;
		if ( cls.equals( HistoryActivity.class ) ) {
			requestCode = RequestCodesConstants.START_HISTORY;
		} else if ( cls.equals( MemoryActionsActivity.class ) ) {
			if ( possibleExtras.getStringExtra( "type" ).equals( "rc" ) ) {
				requestCode = RequestCodesConstants.START_MEMORY_RECALL;
			} else {
				requestCode = RequestCodesConstants.START_MEMORY_STORE;
			}
		} else if ( cls.equals( VariableEditorActivity.class ) ) {
			requestCode = RequestCodesConstants.START_ADD_VAR;
		}
		startActivityForResult( intent, requestCode );
	}

	public void insertBinaryOperatorOnClick(View v) {
		insertBinaryOperator( ( (Button) v ).getText().toString() );
	}

	private void insertBinaryOperator(String s) {
		EditText editText = findViewById( R.id.ExampleStr );
		Editable e = editText.getText();
		int selection = editText.getSelectionStart();
		if ( e == null || e.toString().isEmpty() || selection == 0 ) {
			if ( s.equals( "-" ) ) {
				insert( s );
			}
			return;
		}
		String atSelection = e.subSequence( selection - 1, selection ).toString();
		if ( isBinaryOperator( atSelection ) ) {
			deleteSymbol();
		}
		insert( s );
	}

	public void insertSuffixOperatorOnClick(View v) {
		insertSuffixOperator( ( (Button) v ).getText().toString() );
	}

	private void insertSuffixOperator(String s) {
		EditText editText = findViewById( R.id.ExampleStr );
		Editable e = editText.getText();
		int selection = editText.getSelectionStart();
		if ( e == null || e.toString().isEmpty() || selection == 0 ) {
			return;
		}
		String atSelection = e.subSequence( selection - 1, selection ).toString();
		if ( isBinaryOperator( atSelection ) ) {
			deleteSymbol();
		}
		insert( s );
	}

	private boolean isBinaryOperator(String s) {
		return s.equals( "+" ) ||
				s.equals( "-" ) ||
				s.equals( MULTIPLY_SIGN ) ||
				s.equals( DIVISION_SIGN ) ||
				s.equals( "^" );
	}

	public void insertFunction(View v) {
		justInsert( v );
	}

	public void insertBracket(View v) {
		justInsert( v );
	}

	public void justInsert(View v) {
		insert( ( (Button) v ).getText().toString() );
	}

	public void insertDot(View v) {
		Locale locale;
		if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ) {
			locale = getResources().getConfiguration().getLocales().get( 0 );
		}else{
			locale = getResources().getConfiguration().locale;
		}
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
		char decimalSeparator = decimalFormatSymbols.getDecimalSeparator();
		char groupingSeparator = decimalFormatSymbols.getGroupingSeparator();
		EditText editText = findViewById( R.id.ExampleStr );
		String text = editText.getText().toString();
		int selection = editText.getSelectionStart();
		boolean wasDot = false;
		int i = selection - 1;
		while ( i >= 0 && ( CalculatorUtils.isDigit( text.charAt( i ) ) || text.charAt( i ) == decimalSeparator || text.charAt( i ) == groupingSeparator ) ) {
			if ( text.charAt( i ) == decimalSeparator ) {
				wasDot = true;
				break;
			}
			i--;
		}
		if ( !wasDot ) {
			i = selection + 1;
			while ( i < text.length() && ( CalculatorUtils.isDigit( text.charAt( i ) ) || text.charAt( i ) == decimalSeparator || text.charAt( i ) == groupingSeparator ) ) {
				if ( text.charAt( i ) == decimalSeparator ) {
					wasDot = true;
					break;
				}
				i++;
			}
			if ( !wasDot ) {
				insert( String.valueOf( decimalSeparator ) );
			}
		}
	}

	private void insert(String s) {
		EditText editText = findViewById( R.id.ExampleStr );
		Editable e = editText.getText();
		int selection = editText.getSelectionStart();
		e.insert( selection, s );
		editText.requestFocus();
		calculate( CalculateMode.PRE_ANSWER );
	}

	public void onClear(View v) {
		clearFormulaEditText();
		clearAnswer();
	}

	private void clearAnswer() {
		CalculatorEditText textView = findViewById( R.id.AnswerStr );
		textView.setText( "" );
		textView.setTextColor( super.textColor );
	}

	private void clearFormulaEditText() {
		EditText editText = findViewById( R.id.ExampleStr );
		editText.setText( "" );
		editText.setSelection( 0 );
	}

	public void onEqual(View v) {
		calculate( CalculateMode.FULL_ANSWER );
	}

	private void calculate(CalculateMode mode) {
		EditText txt = findViewById( R.id.ExampleStr );
		String example = txt.getText().toString();
		wasError = false;

		if ( Utils.isNumber( example ) || example.isEmpty() ) {
			clearAnswer();
			return;
		}

		String formatted;

		try {
			CalculatorExpressionFormatter formatter = new CalculatorExpressionFormatter();
			formatted = formatter.tryToCloseExpressionBrackets( example );
			formatted = formatter.formatNearBrackets( formatted );

			CalculatorExpressionTokenizer tokenizer = new CalculatorExpressionTokenizer();
			tokenizer.setReplacementMap( mCalculatorWrapper.getReplacementMap() );
			formatted = tokenizer.localizeExpression( formatted );
		} catch (CalculatingException e) {
			writeCalculationError( getString( CalculatorWrapper.getStringResForErrorCode( CalculatingException.INVALID_BRACKETS_SEQUENCE ) ) );
			return;
		}

		String finalFormatted = formatted;
		mCoreThread = new Thread( ()->{
			FirebaseCrashlytics.getInstance().log( "Now calculating: " + example + "; formatted: " + finalFormatted  );
			try {
				BigDecimal res = mCalculatorWrapper.calculate( finalFormatted );
				BigDecimal scaledRes = res.scale() > ROUND_SCALE ? res.setScale( ROUND_SCALE, RoundingMode.HALF_EVEN ) : res;
				runOnUiThread( ()->writeResult( mode, scaledRes, finalFormatted ) );
			} catch (CalculatingException e) {
				FirebaseCrashlytics.getInstance().recordException( e );
				wasError = true;
				if ( mode == CalculateMode.FULL_ANSWER ) {
					int res = CalculatorWrapper.getStringResForErrorCode( e.getErrorCode() );
					int stringRes;
					if ( res != -1 )
						stringRes = res;
					else
						stringRes = R.string.error;
					runOnUiThread( ()->writeCalculationError( getString( stringRes ) ) );
				} else {
					runOnUiThread( Main2Activity.this::clearAnswer );
				}
			}
			killCoreTimer();
		} );

		startThreadController();
	}

	private void startThreadController() {
		mThreadControllerProgressDialog = new ProgressDialog( this );
		mThreadControllerProgressDialog.setCancelable( false );
		mThreadControllerProgressDialog.setMessage( Html.fromHtml( getResources().getString( R.string.in_calc_process_message ) ) );
		mThreadControllerProgressDialog.setButton( ProgressDialog.BUTTON_NEUTRAL, getResources().getString( R.string.cancel ), (dialog, which)->{
			destroyThread();
			Toast.makeText( Main2Activity.this, "Calculation process stopped", Toast.LENGTH_SHORT ).show();
			dialog.cancel();
		} );
		timerCountDown = 0;

		if ( mCoreTimer != null ) {
			mCoreTimer.cancel();
			mCoreTimer.purge();
		}
		mCoreTimer = new Timer();
		mCoreThread.start();

		if ( !BuildConfig.DEBUG ) {
			mCoreTimer.schedule( new CoreController(), 0, 100 );
		}
	}

	private void killCoreTimer() {
		if ( mCoreTimer != null ) {
			mCoreTimer.cancel();
			mCoreTimer.purge();
			mCoreTimer = null;
		}
		if ( progressDialogShown ) {
			mThreadControllerProgressDialog.cancel();
			progressDialogShown = false;
		}
	}

	private void destroyThread() {
		if ( progressDialogShown ) {
			mThreadControllerProgressDialog.cancel();
			progressDialogShown = false;
		}

		mCoreThread.interrupt();
		killCoreTimer();
	}

	private class CoreController extends TimerTask {
		@Override
		public void run() {
			final String TAG = Main2Activity.TAG + " Timer";
			++timerCountDown;
			Log.i( TAG, "Timer running. Countdown = " + timerCountDown + ". " + ( (double) timerCountDown / 10 ) + " seconds passed." );
			Runtime runtime = Runtime.getRuntime();
			runtime.gc();
			double freeMemory = (double) runtime.freeMemory();
			double d = runtime.totalMemory() * 0.05;
			Log.i( TAG, "Total memory: " + runtime.totalMemory() + ". Free memory: " + freeMemory + ". 5% of total memory: " + d );
			if ( freeMemory < d ) {
				Log.i( TAG, "Thread destroyed due to lack of memory. Free memory: " + freeMemory + " bytes. Total memory: " + Runtime.getRuntime().totalMemory() );
				destroyThread();
				runOnUiThread( ()->Toast.makeText( Main2Activity.this, R.string.thread_destroy_reason, Toast.LENGTH_LONG ).show() );
			}
			if ( timerCountDown == 20 ) {
				if ( !mCoreThread.isAlive() || mCoreThread.isInterrupted() ) {
					Log.i( TAG, "Something gone wrong. Thread is not alive. Killing timer" );
					killCoreTimer();
				} else if ( !progressDialogShown ) {
					progressDialogShown = true;
					Log.i( TAG, "showing dialog" );
					runOnUiThread( ()->mThreadControllerProgressDialog.show() );
				}
			} else if ( timerCountDown > 20 ) {
				if ( timerCountDown < 90 ) {
					if ( !mCoreThread.isAlive() || mCoreThread.isInterrupted() ) {
						Log.i( TAG, "cancel progress dialog" );
						runOnUiThread( ()->mThreadControllerProgressDialog.cancel() );
						killCoreTimer();
					}
				} else {
					if ( mCoreThread.isAlive() ) {
						Log.i( TAG, "Time to kill process" );
						runOnUiThread( ()->{
							destroyThread();
							Toast.makeText( Main2Activity.this, "The process was interrupted due to too long execution time.", Toast.LENGTH_LONG ).show();
						} );
					}
					killCoreTimer();
				}
			}
		}
	}

	public void deleteSymbolOnClick(View v) {
		deleteSymbol();
	}

	private void deleteSymbol() {
		EditText editText = findViewById( R.id.ExampleStr );
		Editable e = editText.getText();
		if ( e == null || e.toString().isEmpty() ) {
			return;
		}
		int selection = editText.getSelectionStart();
		if ( selection != 0 ) {
			e.delete( selection - 1, selection );
			calculate( CalculateMode.PRE_ANSWER );
		}
	}

	private String formatNumber(BigDecimal num){
		return mDecimalFormat.format( num );
	}

	private void writeResult(CalculateMode mode, BigDecimal result, String formattedExample) {
		EditText editText = findViewById( R.id.ExampleStr );
		CalculatorEditText answerTextView = findViewById( R.id.AnswerStr );

		if ( mode == CalculateMode.PRE_ANSWER ) {
			answerTextView.setText( formatNumber( result ) );
		} else {
			clearAnswer();
			answerTextView.setText( formattedExample );
			String formattedNum = formatNumber( result );
			editText.setText( formattedNum );
			editText.setSelection( formattedNum.length() );

			String example = FormatUtils.normalizeNumbersInExample( formattedExample, mDecimalFormat );

			HistoryManager.getInstance()
					.put( new HistoryEntry( example, result.toPlainString() ) )
					.save();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate( R.menu.menu_main, menu );
		return true;
	}

	private void scrollAnswerToEnd() {
		HorizontalScrollView scrollView = findViewById( R.id.scrollViewAns );
		scrollView.post( ()->scrollView.fullScroll( HorizontalScrollView.FOCUS_RIGHT ) );
	}
}
