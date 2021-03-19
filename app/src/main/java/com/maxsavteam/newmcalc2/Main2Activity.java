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
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
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
import com.maxsavitsky.exceptionhandler.ExceptionHandler;
import com.maxsavteam.calculator.CalculatorExpressionBracketsChecker;
import com.maxsavteam.calculator.exceptions.CalculatingException;
import com.maxsavteam.newmcalc2.adapters.MyFragmentPagerAdapter;
import com.maxsavteam.newmcalc2.core.CalculationError;
import com.maxsavteam.newmcalc2.core.CalculationResult;
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
import com.maxsavteam.newmcalc2.utils.FormatUtil;
import com.maxsavteam.newmcalc2.utils.HistoryManager;
import com.maxsavteam.newmcalc2.utils.MemorySaverReader;
import com.maxsavteam.newmcalc2.utils.RequestCodesConstants;
import com.maxsavteam.newmcalc2.utils.ResultCodesConstants;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.variables.Variable;
import com.maxsavteam.newmcalc2.variables.VariableUtils;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;
import com.maxsavteam.newmcalc2.widget.CustomTextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class Main2Activity extends ThemeActivity {

	public static final String TAG = "MCalc";

	private NavigationView mNavigationView;
	private SharedPreferences sp;

	private boolean was_error = false;
	private boolean isOtherActivityOpened = false;
	private boolean isBroadcastsRegistered;
	private boolean progressDialogShown = false;

	private ArrayList<BroadcastReceiver> registeredBroadcasts = new ArrayList<>();
	private CustomTextView mExample;
	private MemorySaverReader mMemorySaverReader;
	private BigDecimal[] memoryEntries;
	private CalculatorWrapper mCalculatorWrapper;
	private final Point displaySize = new Point();

	private String MULTIPLY_SIGN;
	private String FI;
	private String PI;
	private String E;
	private String original;
	private String bracketFloorOpen;
	private String bracketFloorClose;
	private String bracketCeilOpen;
	private String bracketCeilClose;

	private Timer mCoreTimer;
	private Thread mCoreThread = null;

	private int timerCountDown = 0;

	private ProgressDialog mProgressDialog;

	enum EnterModes {
		SIMPLE,
		AVERAGE,
		GEOMETRIC
	}

	private EnterModes exampleEnterMode = EnterModes.SIMPLE;

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

	private final View.OnLongClickListener mForAdditionalBtnsLongClick = view->{
		appendToExampleString( ( (Button) view ).getText().toString().substring( 1 ) );
		return true;
	};

	private final View.OnLongClickListener mReturnBack = v->{
		if ( !was_error ) {
			TextView answer = findViewById( R.id.AnswerStr );
			TextView example = findViewById( R.id.ExampleStr );
			if ( answer.getVisibility() == View.INVISIBLE || answer.getVisibility() == View.GONE ) {
				return false;
			}
			String txt = answer.getText().toString();
			answer.setText( example.getText().toString() );
			example.setText( txt );
			scrollExampleToEnd();
			return true;
		} else {
			return false;
		}
	};

	private final View.OnLongClickListener btnDeleteSymbolLongClick = (View v)->{
		deleteExample( findViewById( R.id.btnDelAll ) );
		return true;
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

	private void startGuide() {
		new MaterialTapTargetSequence()
				.addPrompt( Utils.getGuideTip( this, getString( R.string.panel ), getString( R.string.view_pager_guide1 ), R.id.viewpager, new RectanglePromptFocal() ) )
				.addPrompt( Utils.getGuideTip( this, getString( R.string.panel ), getString( R.string.view_pager_guide2 ), R.id.viewpager, new RectanglePromptFocal() ) )
				.show();
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

		mMemorySaverReader = new MemorySaverReader( this );
		memoryEntries = mMemorySaverReader.read();

		boolean isGuideFirstStart = sp.getBoolean( "guide_first_start", true );
		if ( isGuideFirstStart ) {
			sp.edit().putBoolean( "guide_first_start", false ).apply();
			startGuide();
		}

		addShortcutsToApp();

		restoreResultIfSaved();

		showOfferToRate();
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
				StringBuilder ans = new StringBuilder();
				while ( i < text.length() && text.charAt( i ) != ';' ) {
					ex.append( text.charAt( i ) );
					i++;
				}
				TextView ver = findViewById( R.id.ExampleStr );
				ver.setSelectAllOnFocus( false );
				ver.setText( ex.toString() );
				ver.setSelected( false );
				showExample();
				equallu( "all" );
				format( R.id.ExampleStr );
			}
		}
	}

	private void showOfferToRate() {
		boolean isOfferShowed = sp.getBoolean( "offer_was_showed", false );
		int showedCount = sp.getInt( "offers_count", 0 );
		if ( !isOfferShowed ) {
			if ( showedCount == 10 ) {
				showedCount = 0;
				CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder( this );
				builder.setTitle( R.string.please_rate_out_app )
						.setMessage( R.string.rate_message )
						.setCancelable( false )
						.setPositiveButton( R.string.rate, (dialog, i)->{
							Toast.makeText( this, getResources().getString( R.string.thank_you ), Toast.LENGTH_SHORT ).show();
							sp.edit().putBoolean( "offer_was_showed", true ).apply();
							Intent go_to = new Intent( Intent.ACTION_VIEW );
							go_to.setData( Uri.parse( getResources().getString( R.string.link_app_in_google_play ) ) );
							startActivity( go_to );
							dialog.cancel();
						} )
						.setNegativeButton( R.string.no_thanks, ( (dialog, which)->{
							Toast.makeText( this, ":(", Toast.LENGTH_SHORT ).show();
							sp.edit().putBoolean( "offer_was_showed", true ).apply();
							dialog.cancel();
						} ) )
						.setNeutralButton( R.string.later, ( (dialog, which)->{
							Toast.makeText( this, R.string.we_will_wait, Toast.LENGTH_SHORT ).show();
							dialog.cancel();
						} ) );

				builder.show();
			} else {
				showedCount++;
			}
			sp.edit().putInt( "offers_count", showedCount ).apply();
		}
	}

	/**
	 * Can receive 5 types: settings (go to Settings), numgen (go to Number Generator)<br>
	 * history (go to History), pass (go to Password Generator) <br>
	 * and bin (go to Number Systems Converter)
	 */
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

	private void setTextViewAnswerTextSizeToDefault() {
		TextView t = findViewById( R.id.AnswerStr );
		t.setText( "" );
		t.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 32 );
		t.setTextColor( super.textColor );
	}

	private void unregisterAllBroadcasts() {
		for (BroadcastReceiver broadcastReceiver : registeredBroadcasts) {
			unregisterReceiver( broadcastReceiver );
		}
		isBroadcastsRegistered = false;
		registeredBroadcasts = new ArrayList<>();
	}

	private void format(int id) {
		CustomTextView t = findViewById( id );
		String txt = t.getText().toString();
		if ( txt.equals( "" ) || txt.length() < 4 ) {
			return;
		}
		t.setText( FormatUtil.format( txt ) );
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
		Thread.setDefaultUncaughtExceptionHandler( new ExceptionHandler( this, null, true ) );
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

		mCalculatorWrapper = new CalculatorWrapper();

		FI = getResources().getString( R.string.fi );
		PI = getResources().getString( R.string.pi );
		E = getResources().getString( R.string.euler_constant );
		MULTIPLY_SIGN = getResources().getString( R.string.multiply );

		bracketFloorOpen = getResources().getString( R.string.floor_open_bracket );
		bracketFloorClose = getResources().getString( R.string.floor_close_bracket );
		bracketCeilOpen = getResources().getString( R.string.ceil_open_bracket );
		bracketCeilClose = getResources().getString( R.string.ceil_close_bracket );

		mExample = findViewById( R.id.ExampleStr );

		ImageButton imageButton = findViewById( R.id.btnDelete );
		imageButton.setOnLongClickListener( v->{
			deleteExample( v );
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
		CustomTextView exampleTextView = findViewById( R.id.ExampleStr );
		exampleTextView.addListener( this::updateExampleArrows );

		HorizontalScrollView exampleScrollView = findViewById( R.id.scrollview );
		exampleScrollView.setOnScrollChangeListener( (v, scrollX, scrollY, oldScrollX, oldScrollY)->updateExampleArrows() );

		CustomTextView answerTextView = findViewById( R.id.AnswerStr );
		answerTextView.addListener( this::updateAnswerArrows );

		HorizontalScrollView answerScrollView = findViewById( R.id.scrollViewAns );
		answerScrollView.setOnScrollChangeListener( (v, scrollX, scrollY, oldScrollX, oldScrollY)->updateAnswerArrows() );
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if ( requestCode == RequestCodesConstants.START_HISTORY && resultCode != ResultCodesConstants.RESULT_ERROR && data != null ) {
			String example = data.getStringExtra( "example" );
			if ( example != null && !example.equals( "" ) ) {
				String result = data.getStringExtra( "result" );

				if ( mExample.getText().toString().equals( "" ) ) {
					mExample.setText( example );
					showExample();
				} else {
					addStringExampleToTheExampleStr( result );
				}

				equallu( "not" );
			}
		}
		if ( requestCode == RequestCodesConstants.START_MEMORY_RECALL ) {
			if ( resultCode == ResultCodesConstants.RESULT_APPEND ) {
				addStringExampleToTheExampleStr( data.getStringExtra( "value" ) );
			} else if ( resultCode == ResultCodesConstants.RESULT_REFRESH ) {
				memoryEntries = mMemorySaverReader.read();
			}
		}
		if ( requestCode == RequestCodesConstants.START_ADD_VAR ) {
			setViewPager( ((ViewPager) findViewById( R.id.viewpager )).getCurrentItem() );
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
		fragments.add( new MathOperationsFragment( this ) );
		fragments.add( new VariablesFragment( this, mMemoryActionsLongClick, mOnVariableLongClick ) );

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
				} else if(position == fragments.size() - 1) {
					showWithAlpha( R.id.image_view_left );
					hideWithAlpha( R.id.image_view_right );
				}else{
					showWithAlpha( R.id.image_view_left );
					showWithAlpha( R.id.image_view_right );
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		} );
	}

	public void onClick(@NonNull View v) {
		if ( v.getId() == R.id.btnCalc ) {
			equallu( "all" );
		} else {
			Button btn = findViewById( v.getId() );
			String btntxt = btn.getText().toString().substring( 0, 1 );
			appendToExampleString( btntxt );
		}
	}

	public void onAdditionalClick(View v) {
		appendToExampleString( ( (Button) v ).getText().toString() );
	}

	private void addStringExampleToTheExampleStr(String s) {
		String value = s;
		String txt = mExample.getText().toString();
		if ( txt.equals( "" ) ) {
			mExample.setText( value );
			showExample();
			hideAnswer();
		} else {
			char last = txt.charAt( txt.length() - 1 );
			if ( new BigDecimal( value ).signum() < 0 ) {
				value = "(" + value + ")";
			}
			if ( Utils.isDigit( last ) || last == '%' || last == '!'
					|| Character.toString( last ).equals( FI )
					|| Character.toString( last ).equals( PI )
					|| Character.toString( last ).equals( E )  || isCloseBracket( last ) ) {
				mExample.setText( String.format( "%s%s%s", txt, MULTIPLY_SIGN, value ) );
			} else {
				mExample.setText( String.format( "%s%s", txt, value ) );
			}
			equallu( "not" );
		}
		format( R.id.ExampleStr );
		resizeText();
	}

	private boolean isSpecific(char last) {
		return isCloseBracket( last ) || last == '!' || last == '%' || Character.toString( last ).equals( PI ) || Character.toString( last ).equals( FI ) || Character.toString( last ).equals( E );
	}

	public void deleteExample(View v) {
		exampleEnterMode = EnterModes.SIMPLE;
		CustomTextView t = findViewById( R.id.ExampleStr );
		hideExample();
		t.setText( "" );
		setTextViewAnswerTextSizeToDefault();
		hideAnswer();
		was_error = false;
		sp.edit().remove( "saveResultText" ).apply();
		setTextViewsTextSizeToDefault();
	}

	private void resizeText() {
		FormatUtil.scaleText( this, mExample, findViewById( R.id.scrollview ).getWidth(), 32, 46 );
		FormatUtil.scaleText( this, findViewById( R.id.AnswerStr ), findViewById( R.id.scrollViewAns ).getWidth(), 29, 34 );
	}

	private void setTextViewsTextSizeToDefault() {
		CustomTextView txt = findViewById( R.id.ExampleStr );
		CustomTextView t = findViewById( R.id.AnswerStr );
		txt.setTextSize( TypedValue.COMPLEX_UNIT_SP, 46 );
		t.setTextSize( TypedValue.COMPLEX_UNIT_SP, 34 );
	}

	private void equallu(String type) {
		if ( was_error ) {
			setTextViewAnswerTextSizeToDefault();
			hideAnswer();
		}
		CustomTextView txt = findViewById( R.id.ExampleStr );
		String example = txt.getText().toString();
		format( R.id.ExampleStr );
		resizeText();
		int len = example.length();
		if ( len != 0 ) {
			showExample();
			scrollExampleToEnd();
		} else {
			return;
		}
		char last = example.charAt( len - 1 );
		if ( isBasicAction( last ) || isOpenBracket( last ) || Utils.isNumber( example ) ) {
			hideAnswer();
			return;
		}

		was_error = false;

		try {
			original = new CalculatorExpressionBracketsChecker().tryToCloseExpressionBrackets( example );
		}catch (CalculatingException e){
			writeCalculationError( getString( CalculatorWrapper.getStringResForErrorCode( CalculatingException.INVALID_BRACKETS_SEQUENCE ) ) );
			return;
		}

		mCoreThread = new Thread( ()->mCalculatorWrapper.prepareAndRun( example, type, mCoreInterface ) );

		startThreadController();
	}

	private void startThreadController() {
		mProgressDialog = new ProgressDialog( this );
		mProgressDialog.setCancelable( false );
		mProgressDialog.setMessage( Html.fromHtml( getResources().getString( R.string.in_calc_process_message ) ) );
		mProgressDialog.setButton( ProgressDialog.BUTTON_NEUTRAL, getResources().getString( R.string.cancel ), (dialog, which)->{
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

		if ( !BuildConfig.ISDEBUG ) {
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
			mProgressDialog.cancel();
			progressDialogShown = false;
		}
	}

	private void destroyThread() {
		if ( progressDialogShown ) {
			mProgressDialog.cancel();
			progressDialogShown = false;
		}

		mCoreThread.interrupt();
		hideAnswer();
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
					runOnUiThread( ()->mProgressDialog.show() );
				}
			} else if ( timerCountDown > 20 ) {
				if ( timerCountDown < 90 ) {
					if ( !mCoreThread.isAlive() || mCoreThread.isInterrupted() ) {
						Log.i( TAG, "cancel progress dialog" );
						runOnUiThread( ()->mProgressDialog.cancel() );
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

	private final CalculatorWrapper.CoreInterface mCoreInterface = new CalculatorWrapper.CoreInterface() {
		@Override
		public void onSuccess(CalculationResult calculationResult) {
			runOnUiThread( ()->{
				Log.v( "Main2Activity", "Killing timer from onSuccess" );
				killCoreTimer();
				if ( calculationResult.getResult() != null ) {
					writeCalculationResult( calculationResult.getType(), calculationResult.getResult() );
				} else {
					hideAnswer();
				}
			} );
		}

		@Override
		public void onError(CalculationError calculationError) {
			runOnUiThread( ()->{
				Log.v( "Main2Activity", "Killing core timer from onError" );
				killCoreTimer();
				if ( !calculationError.getStatus().equals( "Core" ) ) {
					if ( calculationError.getShortError().equals( "" ) ) {
						Toast t = Toast.makeText( Main2Activity.this, calculationError.getMessage(), Toast.LENGTH_LONG );
						t.setGravity( Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0 );
						t.show();
					} else {
						writeCalculationError( calculationError.getShortError() );
					}
				}
			} );

		}
	};

	private void writeCalculationError(String text) {
		TextView t = findViewById( R.id.AnswerStr );
		hideAnswer();
		t.setText( text );
		//t.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 32 );
		resizeText();
		t.setTextColor( Color.parseColor( "#FF4B32" ) );
		showAnswer();
		was_error = true;
	}

	private void writeCalculationResult(String type, BigDecimal result) {
		String ans = result.toPlainString();
		if ( ans.contains( "." ) && ans.length() - ans.indexOf( "." ) > 9 ) {
			BigDecimal d = result;
			d = d.divide( BigDecimal.ONE, 8, RoundingMode.HALF_EVEN );
			ans = Utils.deleteZeros( d.toPlainString() );
		}
		switch ( type ) {
			case "all":
				TextView tans = findViewById( R.id.ExampleStr );
				tans.setText( ans );
				format( R.id.ExampleStr );
				tans = findViewById( R.id.AnswerStr );
				tans.setText( original );
				showAnswer();
				showExample();

				HistoryManager.getInstance()
						.put( new HistoryEntry( original, result.toPlainString() ) )
						.save();

				if ( sp.getBoolean( "saveResult", false ) ) {
					sp.edit().putString( "saveResultText", original + ";" + result.toPlainString() ).apply();
				}

				scrollExampleToEnd( HorizontalScrollView.FOCUS_LEFT );
				break;
			case "not":
				TextView preans = findViewById( R.id.AnswerStr );
				preans.setText( ans );
				showExample();
				format( R.id.AnswerStr );
				showAnswer();
				setTextViewsTextSizeToDefault();
				scrollExampleToEnd();
				break;
			default:
				throw new IllegalArgumentException( "Arguments should be of two types: all, not" );
		}
		resizeText();
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
					if ( a != null ) {
						for (int i = 0; i < a.size(); i++) {
							if ( a.get( i ).getTag() == pos ) {
								addStringExampleToTheExampleStr( a.get( i ).getValue() );
								//break;
								return;
							}
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
		onMemoryPlusMinusButtonsClick( v, false );
	}

	public void onMemoryPlusMinusButtonsClick(View v, boolean isPreviouslyCalled) {
		TextView textExample = findViewById( R.id.ExampleStr );
		String text = textExample.getText().toString();
		if ( text.equals( "" ) ) {
			return;
		}

		BigDecimal temp;
		if ( !Utils.isNumber( text ) ) {
			equallu( "all" );
			if ( !isPreviouslyCalled ) {
				onMemoryPlusMinusButtonsClick( v, true );
			}
			return;
		} else {
			temp = new BigDecimal( Utils.deleteSpaces( text ) );
		}
		if ( v.getId() == R.id.btnMemPlus ) {
			temp = temp.add( memoryEntries[ 0 ] );
		} else if ( v.getId() == R.id.btnMemMinus ) {
			temp = memoryEntries[ 0 ].subtract( temp );
		}
		if ( isPreviouslyCalled ) {
			mReturnBack.onLongClick( findViewById( R.id.btnCalc ) );
		}
		memoryEntries[ 0 ] = temp;
		mMemorySaverReader.save( memoryEntries );
	}

	public void onMemoryStoreButtonClick(View view) {
		onMemoryStoreButtonClick( view, false );
	}

	public void onMemoryStoreButtonClick(View view, boolean isPreviouslyCalled) {
		TextView t = findViewById( R.id.ExampleStr );
		String txt = t.getText().toString();
		if ( txt.equals( "" ) ) {
			return;
		}
		BigDecimal temp;
		if ( !Utils.isNumber( txt ) ) {
			equallu( "all" );
			if ( !isPreviouslyCalled ) {
				onMemoryStoreButtonClick( view, true );
			}
			return;
		} else {
			temp = new BigDecimal( Utils.deleteSpaces( txt ) );
		}
		if ( isPreviouslyCalled ) {
			mReturnBack.onLongClick( findViewById( R.id.btnCalc ) );
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
		int requestCode = -1;
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

	private void showExample() {
		TextView t = findViewById( R.id.ExampleStr );
		//t.setTextIsSelectable(false);
		t.setVisibility( View.VISIBLE );
	}

	private void hideExample() {
		TextView t = findViewById( R.id.ExampleStr );
		t.setVisibility( View.INVISIBLE );
	}

	private void showAnswer() {
		TextView t = findViewById( R.id.AnswerStr );
		t.setVisibility( View.VISIBLE );
	}

	private void hideAnswer() {
		TextView t = findViewById( R.id.AnswerStr );
		t.setVisibility( View.INVISIBLE );
	}

	private void checkDot() {
		TextView t = findViewById( R.id.ExampleStr );
		String txt = t.getText().toString();
		int i = txt.length() - 1;
		if ( !Utils.isDigit( txt.charAt( i ) ) ) {
			return;
		}
		boolean dot = false;
		while ( i >= 0 && ( Utils.isDigit( txt.charAt( i ) ) || txt.charAt( i ) == '.' ) ) {
			if ( txt.charAt( i ) == '.' ) {
				dot = true;
				break;
			} else {
				i--;
			}
		}
		if ( !dot ) {
			t.setText( String.format( "%s.", txt ) );
		}
	}

	private boolean isOpenBracket(String str) {
		return str.equals( "(" ) ||
				str.equals( bracketCeilOpen ) ||
				str.equals( bracketFloorOpen ) ||
				str.equals( "[" );
	}

	private boolean isOpenBracket(char c) {
		String str = Character.toString( c );
		return isOpenBracket( str );
	}

	private boolean isCloseBracket(String str) {
		return str.equals( ")" ) ||
				str.equals( bracketFloorClose ) ||
				str.equals( bracketCeilClose ) ||
				str.equals( "]" );
	}

	private boolean isCloseBracket(char c) {
		return isCloseBracket( String.valueOf( c ) );
	}

	private String getTypeOfBracket(String bracket) {
		if ( bracket.equals( "(" ) || bracket.equals( ")" ) ) {
			return "simple";
		}
		if ( bracket.equals( "[" ) || bracket.equals( "]" ) ) {
			return "round";
		}
		if ( bracket.equals( bracketFloorClose ) || bracket.equals( bracketFloorOpen ) ) {
			return "floor";
		}
		if ( bracket.equals( bracketCeilOpen ) || bracket.equals( bracketCeilClose ) ) {
			return "ceil";
		}
		return "undefined";
	}

	private String getTypeOfBracket(char c) {
		return getTypeOfBracket( Character.toString( c ) );
	}

	private boolean isBasicAction(char c) {
		String s = String.valueOf( c );
		return c == '+' ||
				c == '-' ||
				s.equals( getResources().getString( R.string.multiply ) ) ||
				s.equals( getResources().getString( R.string.div ) );
	}

	private boolean isLetterOperator(String s) {
		return s.equals( "sin" ) ||
				s.equals( "log" ) ||
				s.equals( "tan" ) ||
				s.equals( "cos" ) ||
				s.equals( "ln" ) ||
				s.equals( "abs" ) ||
				s.equals( "rnd" );
	}

	@SuppressLint("SetTextI18n")
	public void appendToExampleString(String btntxt) {
		String txt = mExample.getText().toString();
		char last = 1;
		int len = txt.length();
		if ( len + btntxt.length() >= 1000 ) {
			return;
		}

		if ( len != 0 ) {
			last = txt.charAt( len - 1 );
		}

		Stack<String> bracketsStack = new Stack<>();
		for (int i = 0; i < len; i++) {
			if ( isOpenBracket( txt.charAt( i ) ) ) {
				bracketsStack.push( String.valueOf( txt.charAt( i ) ) );
			} else if ( isCloseBracket( txt.charAt( i ) ) ) {
				bracketsStack.pop();
			}
		}

		if ( btntxt.equals( "A" ) ) {
			if ( len == 0 ) {
				mExample.setText( btntxt + "(" );
				equallu( "not" );
				exampleEnterMode = EnterModes.AVERAGE;
				return;
			}
			if ( exampleEnterMode != EnterModes.SIMPLE ) {
				return;
			}

			if ( Utils.isDigit( last ) || isSpecific( last ) ) {
				mExample.setText( txt + MULTIPLY_SIGN + btntxt + "(" );
				equallu( "not" );
				exampleEnterMode = EnterModes.AVERAGE;
			} else if ( !Utils.isDigit( last ) && !isSpecific( last ) ) {
				mExample.setText( txt + btntxt + "(" );
				equallu( "not" );
				exampleEnterMode = EnterModes.AVERAGE;
			}
		} else if ( btntxt.equals( "G" ) ) {
			if ( len == 0 ) {
				mExample.setText( btntxt + "(" );
				equallu( "not" );
				exampleEnterMode = EnterModes.GEOMETRIC;
				return;
			}
			if ( exampleEnterMode != EnterModes.SIMPLE ) {
				return;
			}

			if ( Utils.isDigit( last ) || isSpecific( last ) ) {
				mExample.setText( txt + MULTIPLY_SIGN + btntxt + "(" );
				equallu( "not" );
				exampleEnterMode = EnterModes.GEOMETRIC;
			} else if ( !Utils.isDigit( last ) && !isSpecific( last ) ) {
				mExample.setText( txt + btntxt + "(" );
				equallu( "not" );
				exampleEnterMode = EnterModes.GEOMETRIC;
			}
		}
		if ( exampleEnterMode != EnterModes.SIMPLE ) {
			if ( btntxt.equals( ")" ) ) {
				if ( !Utils.isDigit( last ) ) {
					txt = txt.substring( 0, txt.length() - 1 );
				}
				mExample.setText( txt + btntxt );
				equallu( "not" );
				exampleEnterMode = EnterModes.SIMPLE;
				return;
			}
			if ( exampleEnterMode == EnterModes.AVERAGE && ( btntxt.length() > 1 || ( !btntxt.equals( "+" ) && !btntxt.equals( "." ) ) ) && !Utils.isDigit( btntxt.charAt( 0 ) ) ) {
				return;
			}
			if ( exampleEnterMode == EnterModes.GEOMETRIC && ( btntxt.length() > 1 || ( !btntxt.equals( MULTIPLY_SIGN ) && !btntxt.equals( "." ) ) ) && !Utils.isDigit( btntxt.charAt( 0 ) ) ) {
				return;
			}
		}

		if ( Utils.isDigit( btntxt ) ) {
			if ( len > 1 && ( last == '%' || isCloseBracket( last ) ) ) {
				mExample.setText( txt + MULTIPLY_SIGN + btntxt );
				equallu( "not" );
				return;
			}
			if ( txt.equals( "0" ) ) {
				mExample.setText( btntxt );
				return;
			}
			if ( len > 1 ) {
				if ( last == '0' && !Utils.isDigit( txt.charAt( len - 2 ) ) && txt.charAt( len - 2 ) != '.' ) {
					txt = txt.substring( 0, len - 1 ) + btntxt;
					mExample.setText( txt );
					equallu( "not" );
					return;
				}
			} else if ( len == 0 ) {
				mExample.setText( btntxt );
				showExample();
				return;
			}
		}

		if ( isCloseBracket( btntxt ) ) {
			if ( !bracketsStack.isEmpty() && !isOpenBracket( last ) ) {
				String typeOfBtnTxt = getTypeOfBracket( btntxt );
				if ( typeOfBtnTxt.equals( getTypeOfBracket( bracketsStack.peek() ) ) ) {
					if ( typeOfBtnTxt.equals( getTypeOfBracket( last ) ) ) {
						mExample.setText( txt + btntxt );
						equallu( "not" );
						return;
					}

					if ( isBasicAction( last ) ) {
						if ( len != 1 ) {
							txt = txt.substring( 0, len - 1 );
							mExample.setText( txt + btntxt );
							equallu( "not" );
						}
					} else {
						mExample.setText( txt + btntxt );
						equallu( "not" );
					}
				}
			}

			return;
		}

		if ( btntxt.equals( FI ) || btntxt.equals( PI ) || btntxt.equals( E ) ) {
			if ( len == 0 ) {
				mExample.setText( btntxt );
				equallu( "not" );
				return;
			} else {
				if ( isCloseBracket( last ) ) {
					mExample.setText( txt + MULTIPLY_SIGN + btntxt );
				} else {
					if ( !Utils.isDigit( txt.charAt( len - 1 ) ) ) {
						if ( txt.charAt( len - 1 ) != '.' ) {
							mExample.setText( txt + btntxt );
							equallu( "not" );
							return;
						}
					} else {
						mExample.setText( txt + btntxt );
						scrollExampleToEnd();
						equallu( "not" );
					}
				}
			}
			return;
		}

		if ( btntxt.equals( "^" ) ) {
			if ( len == 0 || isOpenBracket( last ) ) {
				return;
			}

			if ( Utils.isDigit( last ) || Character.toString( last ).equals( FI ) || Character.toString( last ).equals( PI ) || Character.toString( last ).equals( E ) ) {
				mExample.setText( txt + btntxt );
				equallu( "not" );
				return;
			}
			if ( !Utils.isDigit( last ) && ( last == '!' || last == '%' ) ) {
				mExample.setText( txt + btntxt );
				equallu( "not" );
			}
			return;
		}

		if ( btntxt.equals( "√" ) ) {
			if ( len == 0 ) {
				mExample.setText( btntxt );
				showExample();
				scrollExampleToEnd();
				return;
			} else {
				String x = "";
				for (int i = 0; i < len; i++) {
					if ( !Utils.isDigit( txt.charAt( i ) ) && txt.charAt( i ) != '.' && txt.charAt( i ) != ' ' ) {
						break;
					} else {
						if ( Utils.isDigit( txt.charAt( i ) ) || txt.charAt( i ) == '.' || txt.charAt( i ) == ' ' ) {
							x = String.format( "%s%c", x, txt.charAt( i ) );
						}
					}
				}
				if ( x.length() == len ) {
					x = Utils.deleteSpaces( x );
					x = Utils.deleteZeros( x );
					mExample.setText( btntxt + "(" + x + ")" );
					equallu( "all" );
					return;
				} else {
					if ( last == '.' ) {
						return;
					} else {
						if ( !Utils.isDigit( last ) ) {
							mExample.setText( txt + btntxt );
							equallu( "not" );
							showExample();
							scrollExampleToEnd();
						} else {
							if ( Utils.isDigit( last ) || isSpecific( last ) ) {
								mExample.setText( txt + MULTIPLY_SIGN + btntxt );
								equallu( "not" );
								showExample();
								scrollExampleToEnd();
							}
						}
					}
				}
			}
			return;
		}
		if ( isOpenBracket( btntxt ) ) {
			if ( len == 0 ) {
				mExample.setText( btntxt );
				showExample();
				return;
			}
			if ( last == '.' ) {
				return;
			}
			boolean isPreviousLogWithBase;
			int i = len - 1;
			StringBuilder sb = new StringBuilder();
			while ( i >= 0 && ( Utils.isDigit( txt.charAt( i ) ) || Utils.isLetter( txt.charAt( i ) ) || txt.charAt( i ) == '.' ) ) {
				sb.append( txt.charAt( i ) );
				i--;
			}
			String prev = sb.toString();
			isPreviousLogWithBase = prev.endsWith( "gol" ) && prev.length() > 3; // because result string is reversed
			if ( !isPreviousLogWithBase &&
					(
							Utils.isDigit( last ) ||
									last == '!' ||
									last == '%' ||
									Utils.isConstNum( last, this ) ||
									isCloseBracket( last )
					)
			) {
				mExample.setText( txt + MULTIPLY_SIGN + btntxt );
			} else {
				mExample.setText( txt + btntxt );
			}
			equallu( "not" );
			bracketsStack.push( btntxt );
			return;
		}
		if ( isLetterOperator( btntxt ) ) {
			if ( len == 0 ) {
				mExample.setText( btntxt );
			} else {
				if ( last == '.' ) {
					return;
				} else {
					if ( !Utils.isDigit( last ) && last != '!' && !Character.toString( last ).equals( FI ) && !Character.toString( last ).equals( PI ) && !Character.toString( last ).equals( E ) && !isCloseBracket( last ) ) {
						mExample.setText( txt + btntxt );
					} else {
						if ( !isOpenBracket( last ) && last != '^' ) {
							mExample.setText( txt + MULTIPLY_SIGN + btntxt );
						}
					}
				}
			}
			if ( btntxt.equals( "rnd" ) ) {
				mExample.setText( txt + "rnd(" );
			}
			equallu( "not" );
			return;
		}

		if ( len != 0 && last == '(' && btntxt.equals( "-" ) ) {
			mExample.setText( txt + btntxt );
			equallu( "not" );
			return;
		}

		if ( !Utils.isDigit( btntxt ) &&
				!Utils.isLetter( btntxt.charAt( 0 ) ) &&
				len != 0 &&
				(
						txt.charAt( len - 1 ) == 'π' ||
								txt.charAt( len - 1 ) == 'φ' ||
								txt.charAt( len - 1 ) == 'e'
				)
		) {
			mExample.setText( txt + btntxt );
			equallu( "not" );
			return;
		}

		if ( ( last == '!' || last == '%' ) && !btntxt.equals( "." ) && !btntxt.equals( "!" ) && !btntxt.equals( "%" ) ) {
			mExample.setText( txt + btntxt );
			equallu( "not" );
			return;
		}
		if ( btntxt.equals( "+" ) || btntxt.equals( "-" )
				|| btntxt.equals( getResources().getString( R.string.multiply ) )
				|| btntxt.equals( getResources().getString( R.string.div ) ) ) {
			if ( isOpenBracket( last ) && !btntxt.equals( "-" ) ) {
				return;
			} else if ( isOpenBracket( last ) ) {
				mExample.setText( txt + btntxt );
				equallu( "not" );
				return;
			}
		}

		if ( btntxt.equals( "." ) ) {
			if ( txt.equals( "" ) ) {
				mExample.setText( "0." );
			} else if ( !Utils.isDigit( txt.charAt( len - 1 ) ) && txt.charAt( len - 1 ) != '.' && last != '!' ) {
				mExample.setText( txt + "0." );
			} else {
				checkDot();
			}
			showExample();
		} else {
			if ( btntxt.equals( "!" ) || btntxt.equals( "%" ) ) {
				if ( !txt.equals( "" ) ) {
					if ( isOpenBracket( last ) ) {
						return;
					}
					if ( btntxt.equals( "!" ) ) {
						if ( last == '!' ) {
							mExample.setText( txt + btntxt );
							equallu( "not" );
							return;
						} else if ( Utils.isDigit( last ) ) {
							mExample.setText( txt + btntxt );
							equallu( "not" );
							return;
						}
					} else {
						if ( last != '%' && ( Utils.isDigit( last ) || isCloseBracket( last ) ) ) {
							mExample.setText( txt + btntxt );
							equallu( "not" );
							return;
						}
					}
					if ( len > 1 ) {
						String s = Character.toString( txt.charAt( len - 2 ) );
						if ( !isCloseBracket( last ) && !Utils.isDigit( last ) && ( !isOpenBracket( last ) && Utils.isLetter( txt.charAt( len - 2 ) )
								&& !s.equals( PI ) && !s.equals( FI ) && !s.equals( E ) ) ) {
							txt = txt.substring( 0, len - 1 );
							mExample.setText( txt + btntxt );
							equallu( "not" );
						} else {
							if ( Utils.isDigit( last ) || isCloseBracket( last ) ) {
								mExample.setText( txt + btntxt );
								equallu( "not" );
							}
						}
					}
				}
				return;
			}
			if ( len >= 1 && ( isCloseBracket( last ) && !Utils.isDigit( btntxt.charAt( 0 ) ) ) ) {
				mExample.setText( txt + btntxt );
				equallu( "not" );
			} else {
				if ( !txt.equals( "" ) ) {
					if ( !Utils.isDigit( btntxt.charAt( 0 ) ) ) {

						if ( !Utils.isDigit( txt.charAt( len - 1 ) ) && !Utils.isLetter( txt.charAt( len - 1 ) ) ) {
							if ( len != 1 ) {
								txt = txt.substring( 0, len - 1 );
								mExample.setText( txt + btntxt );
								equallu( "not" );
							}
						} else {
							mExample.setText( txt + btntxt );
							equallu( "not" );
						}
					} else {
						if ( Utils.isDigit( btntxt.charAt( 0 ) ) ) {
							mExample.setText( txt + btntxt );
							equallu( "not" );
						}
					}
				} else {
					if ( Utils.isDigit( btntxt.charAt( 0 ) ) || btntxt.equals( "-" ) ) {
						mExample.setText( btntxt );
						equallu( "not" );
					}
				}
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate( R.menu.menu_main, menu );
		return true;
	}

	public void delSymbol(View v) {
		TextView txt = findViewById( R.id.ExampleStr );
		String text = txt.getText().toString();
		int len = text.length();
		setTextViewAnswerTextSizeToDefault();
		if ( len != 0 ) {
			char last = text.charAt( len - 1 );
			int a = 1;
			if ( last == ')' && ( text.contains( "A" ) || text.contains( "G" ) ) ) {
				int i = len - 1;
				while ( i >= 1 && text.charAt( i ) != '(' ) {
					i--;
				}
				i--;
				if ( text.charAt( i ) == 'A' ) {
					exampleEnterMode = EnterModes.AVERAGE;
				} else if ( text.charAt( i ) == 'G' ) {
					exampleEnterMode = EnterModes.GEOMETRIC;
				}
			}
			if ( last == '(' && len > 1 ) {
				if ( text.charAt( len - 2 ) == 'A' || text.charAt( len - 2 ) == 'G' ) {
					a = 2;
					exampleEnterMode = EnterModes.SIMPLE;
				}
				if ( text.charAt( len - 2 ) == 'd' ) // rnd
				{
					a = 4;
				}
			}
			if ( Utils.isLetter( last ) ) { // sin cos tan ln abs rnd
				if ( last == 's' || last == 'g' ) {
					a = 3;
				}
				if ( last == 'n' ) {
					if ( text.charAt( len - 2 ) == 'l' ) {
						a = 2;
					} else if ( text.charAt( len - 2 ) == 'i' || text.charAt( len - 2 ) == 'a' ) {
						a = 3;
					}
				}
			}
			was_error = false;
			if ( text.length() - a == 0 ) {
				hideExample();
			}
			text = text.substring( 0, text.length() - a );
			txt.setText( text );
			equallu( "not" );
		}
		if ( txt.getText().toString().equals( "" ) ) {
			deleteExample( findViewById( R.id.btnDelAll ) );
		}
		scrollExampleToEnd();
	}

	private void scrollExampleToEnd(final int focus) {
		if ( findViewById( R.id.ExampleStr ).getVisibility() == View.INVISIBLE ) {
			return;
		}
		HorizontalScrollView scrollview = findViewById( R.id.scrollview );

		scrollview.post( ()->scrollview.fullScroll( focus ) );
		HorizontalScrollView scrollview1 = findViewById( R.id.scrollViewAns );
		scrollview1.post( ()->scrollview1.fullScroll( HorizontalScrollView.FOCUS_RIGHT ) );
	}

	private void scrollExampleToEnd() {
		scrollExampleToEnd( HorizontalScrollView.FOCUS_RIGHT );
	}
}
