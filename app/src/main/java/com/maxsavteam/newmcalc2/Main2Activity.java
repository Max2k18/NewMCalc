package com.maxsavteam.newmcalc2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.graphics.Rect;
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
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.maxsavteam.newmcalc2.adapters.MyFragmentPagerAdapter;
import com.maxsavteam.newmcalc2.core.CalculationCore;
import com.maxsavteam.newmcalc2.core.CalculationError;
import com.maxsavteam.newmcalc2.fragments.fragment1.Fragment1;
import com.maxsavteam.newmcalc2.fragments.fragment2.Fragment2;
import com.maxsavteam.newmcalc2.ui.AboutAppActivity;
import com.maxsavteam.newmcalc2.ui.HistoryActivity;
import com.maxsavteam.newmcalc2.ui.MemoryActionsActivity;
import com.maxsavteam.newmcalc2.ui.NumberGeneratorActivity;
import com.maxsavteam.newmcalc2.ui.NumberSystemConverterActivity;
import com.maxsavteam.newmcalc2.ui.PasswordGeneratorActivity;
import com.maxsavteam.newmcalc2.ui.SettingsActivity;
import com.maxsavteam.newmcalc2.ui.VariableEditorActivity;
import com.maxsavteam.newmcalc2.utils.Constants;
import com.maxsavteam.newmcalc2.utils.CoreInterruptedError;
import com.maxsavteam.newmcalc2.utils.Format;
import com.maxsavteam.newmcalc2.utils.MemorySaverReader;
import com.maxsavteam.newmcalc2.utils.RequestCodes;
import com.maxsavteam.newmcalc2.utils.ResultCodes;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.variables.Variable;
import com.maxsavteam.newmcalc2.variables.VariableUtils;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
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
	private ArrayList<BroadcastReceiver> registeredBroadcasts = new ArrayList<>();
	private TextView mExample;
	private MemorySaverReader mMemorySaverReader;
	//private char last;
	private BigDecimal[] memoryEntries;
	private String MULTIPLY_SIGN;
	private String FI, PI, original;
	private CalculationCore mCalculationCore;
	private final Point displaySize = new Point();
	private String bracketFloorOpen, bracketFloorClose,
			bracketCeilOpen, bracketCeilClose;

	View.OnLongClickListener mOnVariableLongClick = v->{
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

	@Override
	public void onBackPressed() {
		CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder( this );
		builder.setTitle( R.string.exit )
				.setMessage( R.string.areyousureexit )
				.setCancelable( false )
				.setNegativeButton( R.string.no, (dialog, which)->{
					dialog.cancel();
				} )
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
				.addPrompt( Utils.getGuideTip( this, getString( R.string.action_line ), getString( R.string.action_line_guide ), R.id.bottom_line, new RectanglePromptFocal() ) )
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

		//showSidebarGuide();

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
				show_str();
				equallu( "all" );
				format( R.id.ExampleStr );
			}
		}
		boolean offer_to_rate = sp.getBoolean( "offer_was_showed", false );
		int offers_count = sp.getInt( "offers_count", 0 );
		if ( !offer_to_rate ) {
			if ( offers_count == 10 ) {
				offers_count = 0;
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
				offers_count++;
			}
			sp.edit().putInt( "offers_count", offers_count ).apply();
		}
	}

	String APPTYPE = BuildConfig.APPTYPE;

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
		}
	}

	private void setTextViewAnswerTextSizeToDefault() {
		TextView t = findViewById( R.id.AnswerStr );
		t.setText( "" );
		t.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 32 );
		t.setTextColor( super.textColor );
	}

	private void restartActivity() {
		Intent in = new Intent( this, Main2Activity.class );
		this.startActivity( in );
		this.finish();
	}

	private void unregisterAllBroadcasts() {
		for (BroadcastReceiver broadcastReceiver : registeredBroadcasts) {
			unregisterReceiver( broadcastReceiver );
		}
		isBroadcastsRegistered = false;
		registeredBroadcasts = new ArrayList<>();
	}

	private void format(int id) {
		TextView t = findViewById( id );
		String txt = t.getText().toString();
		if ( txt.equals( "" ) || txt.length() < 4 ) {
			return;
		}
		t.setText( Format.format( txt ) );
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

	private void showToastAboveButton(View v, String message) {
		int xOffset = 0;
		int yOffset = 0;
		Rect gvr = new Rect();

		View parent = (View) v.getParent();
		int parentHeight = parent.getHeight();

		if ( v.getGlobalVisibleRect( gvr ) ) {
			View root = v.getRootView();

			int halfWidth = root.getRight() / 2;
			int halfHeight = root.getBottom() / 2;

			int parentCenterX = ( ( gvr.right - gvr.left ) / 2 ) + gvr.left;

			int parentCenterY = ( ( gvr.bottom - gvr.top ) / 2 ) + gvr.top;

			if ( parentCenterY <= halfHeight ) {
				yOffset = -( halfHeight - parentCenterY ) - parentHeight;
			} else {
				yOffset = ( parentCenterY - halfHeight ) - parentHeight;
			}

			if ( parentCenterX < halfWidth ) {
				xOffset = -( halfWidth - parentCenterX );
			}

			if ( parentCenterX >= halfWidth ) {
				xOffset = parentCenterX - halfWidth;
			}
		}
		try {
			Toast toast = Toast.makeText( Main2Activity.this, message, Toast.LENGTH_LONG );
			toast.setGravity( Gravity.CENTER, xOffset, yOffset );
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText( Main2Activity.this, e.toString(), Toast.LENGTH_LONG ).show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utils.setContext( this );
		Thread.setDefaultUncaughtExceptionHandler( new ExceptionHandler() );
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

		mCalculationCore = new CalculationCore( this, mCoreInterface );

		FI = getResources().getString( R.string.fi );
		PI = getResources().getString( R.string.pi );
		MULTIPLY_SIGN = getResources().getString( R.string.multiply );

		bracketFloorOpen = getResources().getString( R.string.bracket_floor_open );
		bracketFloorClose = getResources().getString( R.string.bracket_floor_close );
		bracketCeilOpen = getResources().getString( R.string.bracket_ceil_open );
		bracketCeilClose = getResources().getString( R.string.bracket_ceil_close );

		mExample = findViewById( R.id.ExampleStr );

		applyTheme();

		Intent startIntent = getIntent();
		if ( startIntent.getBooleanExtra( "shortcut_action", false ) ) {
			String whereWeNeedToGoToAnotherActivity = startIntent.getStringExtra( "to_" );
			if ( whereWeNeedToGoToAnotherActivity != null ) {
				goToAdditionalActivities( whereWeNeedToGoToAnotherActivity );
			}
		}

		setViewPager( 0 );
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if ( requestCode == RequestCodes.START_HISTORY ) {
			if ( resultCode != ResultCodes.RESULT_ERROR && data != null ) {
				String example = data.getStringExtra( "example" );
				if ( example != null && !example.equals( "" ) ) {
					String result = data.getStringExtra( "result" );

					if ( mExample.getText().toString().equals( "" ) ) {
						mExample.setText( example );
						show_str();
					} else {
						addStringExampleToTheExampleStr( result );
					}

					equallu( "not" );
				}
			}
		}
		if ( requestCode == RequestCodes.START_MEMORY_RECALL ) {
			if ( resultCode == ResultCodes.RESULT_APPEND ) {
				addStringExampleToTheExampleStr( data.getStringExtra( "value" ) );
			} else if ( resultCode == ResultCodes.RESULT_REFRESH ) {
				memoryEntries = mMemorySaverReader.read();
			}
		}
		if ( requestCode == RequestCodes.START_ADD_VAR ) {
			setViewPager( 1 );
		}
		super.onActivityResult( requestCode, resultCode, data );
	}

	private void setViewPager(int which) {
		Fragment1.InitializationObject fragmentOneInitializationObject =
				new Fragment1.InitializationObject()
						.setContext( this )
						.setLongClickListeners( new View.OnLongClickListener[]{
								mForAdditionalBtnsLongClick,
								returnback,
								btnDeleteSymbolLongClick
						} );
		Fragment2.InitializationObject fragmentTwoInitializationObject =
				new Fragment2.InitializationObject()
						.setContext( this )
						.setLongClickListeners(
								new View.OnLongClickListener[]{
										mMemoryActionsLongClick,
										mOnVariableLongClick
								}
						);
		MyFragmentPagerAdapter.InitializationObject initializationObject =
				new MyFragmentPagerAdapter.InitializationObject()
						.setFragmentManager( getSupportFragmentManager() )
						.setContext( this )
						.setInitializationObject( fragmentTwoInitializationObject )
						.setInitializationObject( fragmentOneInitializationObject );
		MyFragmentPagerAdapter myFragmentPagerAdapter =
				new MyFragmentPagerAdapter( initializationObject );
		//}
		ViewPager viewPager = findViewById( R.id.viewpager );
		ViewGroup.LayoutParams lay = viewPager.getLayoutParams();
		lay.height = displaySize.y / 2;
		viewPager.setLayoutParams( lay );
		viewPager.setAdapter( myFragmentPagerAdapter );
		viewPager.setCurrentItem( which );
		Space space = findViewById( R.id.space_between_pager_and_str );
		ViewGroup.LayoutParams spaceLayoutParams = space.getLayoutParams();
		spaceLayoutParams.height = displaySize.y / 11;
		space.setLayoutParams( spaceLayoutParams );
		viewPager.addOnPageChangeListener( new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				if(position == 0){
					findViewById( R.id.image_view_left ).animate().alpha( 0f ).setDuration( 100 ).start();
					findViewById( R.id.image_view_right ).animate().alpha( 1f ).setDuration( 100 ).start();
				}else{
					findViewById( R.id.image_view_left ).animate().alpha( 1f ).setDuration( 100 ).start();
					findViewById( R.id.image_view_right ).animate().alpha( 0f ).setDuration( 100 ).start();
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

	private void addStringExampleToTheExampleStr(String value) {
		TextView t = findViewById( R.id.ExampleStr );
		String txt = t.getText().toString();
		if ( txt.equals( "" ) ) {
			t.setText( value );
			show_str();
			hideAns();
			format( R.id.ExampleStr );
			resizeText();
		} else {
			char last = txt.charAt( txt.length() - 1 );
			if ( new BigDecimal( value ).signum() < 0 ) {
				value = "(" + value + ")";
			}
			if ( Utils.isDigit( last ) || last == '%' || last == '!'
					|| Character.toString( last ).equals( FI )
					|| Character.toString( last ).equals( PI ) || last == 'e' || isCloseBracket( last ) ) {
				t.setText( String.format( "%s%s%s", txt, MULTIPLY_SIGN, value ) );
				equallu( "not" );
			} else {
				t.setText( String.format( "%s%s", txt, value ) );
				equallu( "not" );
			}
			format( R.id.ExampleStr );
			resizeText();
		}
	}

	private boolean isSpecific(char last) {
		return isCloseBracket( last ) || last == '!' || last == '%' || Character.toString( last ).equals( PI ) || Character.toString( last ).equals( FI ) || last == 'e';
	}

	View.OnLongClickListener mForAdditionalBtnsLongClick = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			appendToExampleString( ( (Button) view ).getText().toString().substring( 1 ) );
			return true;
		}
	};

	private View.OnLongClickListener returnback = v->{
		if ( !was_error ) {
			TextView back = findViewById( R.id.AnswerStr ), str = findViewById( R.id.ExampleStr );
			if ( back.getVisibility() == View.INVISIBLE || back.getVisibility() == View.GONE ) {
				return false;
			}
			String txt = back.getText().toString();
			back.setText( str.getText().toString() );
			str.setText( txt );
			scrollExampleToEnd();
			return true;
		} else {
			return false;
		}
	};

	private View.OnLongClickListener btnDeleteSymbolLongClick = (View v)->{
		deleteExample( findViewById( R.id.btnDelAll ) );
		return true;
	};

	enum EnterModes {
		SIMPLE,
		AVERAGE,
		GEOMETRIC
	}

	EnterModes exampleEnterMode = EnterModes.SIMPLE;

	public void deleteExample(View v) {
		exampleEnterMode = EnterModes.SIMPLE;
		TextView t = findViewById( R.id.ExampleStr );
		hide_str();
		t.setText( "" );
		setTextViewAnswerTextSizeToDefault();
		hideAns();
		was_error = false;
		sp.edit().remove( "saveResultText" ).apply();
		setTextViewsTextSizeToDefault();
	}

	private void resizeText() {
		Format.scaleText( this, mExample, findViewById( R.id.scrollview ).getWidth(), 32, 46 );
		Format.scaleText( this, findViewById( R.id.AnswerStr ), findViewById( R.id.scrollViewAns ).getWidth(), 29, 34 );
	}

	private void setTextViewsTextSizeToDefault() {
		TextView txt = findViewById( R.id.ExampleStr );
		TextView t = findViewById( R.id.AnswerStr );
		txt.setTextSize( TypedValue.COMPLEX_UNIT_SP, 46 );
		t.setTextSize( TypedValue.COMPLEX_UNIT_SP, 34 );
	}

	private final View.OnLongClickListener mMemoryActionsLongClick = (View v)->{
		if ( v.getId() == R.id.btnMR ) {
			openMemory( "rc" );
		} else {
			openMemory( "st" );
		}
		return true;
	};

	private Thread mCoreThread = null;
	private int timerCountDown = 0;
	private boolean progressDialogShown = false;
	private ProgressDialog mProgressDialog;

	private void equallu(String type) {
		if ( was_error ) {
			setTextViewAnswerTextSizeToDefault();
			hideAns();
		}
		TextView txt = findViewById( R.id.ExampleStr );
		String example = txt.getText().toString();
		format( R.id.ExampleStr );
		resizeText();
		int len = example.length();
		if ( len != 0 ) {
			show_str();
			scrollExampleToEnd();
		} else {
			return;
		}
		char last = example.charAt( len - 1 );
		if ( isBasicAction( last ) || isOpenBracket( last ) || Utils.isNumber( example ) ) {
			hideAns();
			return;
		}

		try {
			example = mCalculationCore.checkBrackets( example );
		} catch (EmptyStackException e) {
			e.printStackTrace();
			return;
		}

		was_error = false;
		original = example;

		String finalExample = example;
		mCoreThread = new Thread( ()->{
			try {
				mCalculationCore.prepareAndRun( finalExample, type );
			} catch (CoreInterruptedError e) {
				// stop thread
			}
		} );
		mProgressDialog = new ProgressDialog( this );
		mProgressDialog.setCancelable( false );
		mProgressDialog.setMessage( Html.fromHtml( getResources().getString( R.string.in_calc_process_message ) ) );
		mProgressDialog.setButton( ProgressDialog.BUTTON_NEUTRAL, getResources().getString( R.string.cancel ), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				destroyThread();
				Toast.makeText( Main2Activity.this, "Calculation process stopped", Toast.LENGTH_SHORT ).show();
				dialog.cancel();
			}
		} );
		timerCountDown = 0;

		if ( mCoreTimer != null ) {
			mCoreTimer.cancel(); ;
			mCoreTimer.purge();
		}
		mCoreTimer = new Timer();
		mCoreThread.start();

		if ( !BuildConfig.ISDEBUG ) {
			mCoreTimer.schedule( new CoreController(), 0, 100 );
		}
	}

	Timer mCoreTimer;

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
		hideAns();
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

	CalculationCore.CoreInterface mCoreInterface = new CalculationCore.CoreInterface() {
		@Override
		public void onSuccess(CalculationCore.CalculationResult calculationResult) {
			runOnUiThread( new Runnable() {
				@Override
				public void run() {
					Log.v( "Main2Activity", "Killing timer from onSuccess" );
					killCoreTimer();
					if ( calculationResult.getResult() != null ) {
						writeCalculationResult( calculationResult.getType(), calculationResult.getResult() );
					} else {
						hideAns();
					}
				}
			} );
		}

		@Override
		public void onError(CalculationError calculationError) {
			runOnUiThread( new Runnable() {
				@Override
				public void run() {
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
				}
			} );

		}
	};

	private void writeCalculationError(String text) {
		TextView t = findViewById( R.id.AnswerStr );
		hideAns();
		t.setText( text );
		t.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 32 );
		t.setTextColor( Color.parseColor( "#FF4B32" ) );
		show_ans();
		was_error = true;
	}

	private void writeCalculationResult(String type, BigDecimal result) {
		String ans = result.toPlainString();
		if ( ans.contains( "." ) ) {
			if ( ans.length() - ans.indexOf( "." ) > 9 ) {
				BigDecimal d = result;
				d = d.divide( BigDecimal.ONE, 8, RoundingMode.HALF_EVEN );
				ans = Utils.deleteZeros( d.toPlainString() );
			}
		}
		switch ( type ) {
			case "all":
				TextView tans = findViewById( R.id.ExampleStr );
				tans.setText( ans );
				format( R.id.ExampleStr );
				tans = findViewById( R.id.AnswerStr );
				tans.setText( original );
				show_ans();
				show_str();

				putToHistory( original, result.toPlainString() );

				if ( sp.getBoolean( "saveResult", false ) ) {
					sp.edit().putString( "saveResultText", original + ";" + result.toPlainString() ).apply();
				}

				scrollExampleToEnd( HorizontalScrollView.FOCUS_LEFT );
				break;
			case "not":
				TextView preans = findViewById( R.id.AnswerStr );
				preans.setText( ans );
				show_str();
				format( R.id.AnswerStr );
				show_ans();
				setTextViewsTextSizeToDefault();
				scrollExampleToEnd();
				break;
			default:
				throw new IllegalArgumentException( "Arguments should be of two types: all, not" );
		}
		resizeText();
	}

	private void putToHistory(String example, String result) {
		String his = sp.getString( "history", null );
		String formattedResult = Format.format( result );
		if ( his != null ) {
			if ( sp.getInt( "local_history_storage_protocol_version", 1 ) < Constants.HISTORY_STORAGE_PROTOCOL_VERSION ) {
				Toast.makeText( this, "The record was not saved because the format of the history record does not match the new format." +
								" To fix this, go to the \"History\" section and in the window that appears, click the \"OK\" button.",
						Toast.LENGTH_LONG ).show();
			} else {
				if ( formattedResult.length() > 2000 ) {
					Toast.makeText( this, "The record was not saved due to too long", Toast.LENGTH_LONG ).show();
					return;
				}
				if ( !his.startsWith( String.format( "%s%c%s", example, ( (char) 30 ), formattedResult ) ) ) {
					his = String.format( "%s%c%s%c%s", example, ( (char) 30 ), formattedResult, ( (char) 29 ), his );
					sp.edit().putString( "history", his ).apply();
				}
			}
		} else {
			his = String.format( "%s%c%s%c", example, ( (char) 30 ), formattedResult, ( (char) 29 ) );
			sp.edit().putString( "history", his ).apply();
			sp.edit().putInt( "local_history_storage_protocol_version", Constants.HISTORY_STORAGE_PROTOCOL_VERSION ).apply();
		}
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

	int countOfMemoryPlusMinusMethodCalls = 0;

	public void onMemoryPlusMinusButtonsClick(View v) {
		TextView textExample = findViewById( R.id.ExampleStr );
		String text = textExample.getText().toString();
		if ( text.equals( "" ) ) {
			return;
		}

		BigDecimal temp;
		if ( !Utils.isNumber( text ) ) {
			equallu( "all" );
			if ( countOfMemoryPlusMinusMethodCalls < 1 ) {
				countOfMemoryPlusMinusMethodCalls++;
				onMemoryPlusMinusButtonsClick( v );
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
		if ( countOfMemoryPlusMinusMethodCalls > 0 ) {
			returnback.onLongClick( findViewById( R.id.btnCalc ) );
		}
		memoryEntries[ 0 ] = temp;
		mMemorySaverReader.save( memoryEntries );
	}

	int countOfMemoryStoreMethodCalls = 0;

	public void onMemoryStoreButtonClick(View view) {
		TextView t = findViewById( R.id.ExampleStr );
		String txt = t.getText().toString();
		if ( txt.equals( "" ) ) {
			return;
		}
		BigDecimal temp;
		if ( !Utils.isNumber( txt ) ) {
			equallu( "all" );
			if ( countOfMemoryStoreMethodCalls < 1 ) {
				countOfMemoryStoreMethodCalls++;
				onMemoryStoreButtonClick( view );
			}
			return;
		} else {
			temp = new BigDecimal( Utils.deleteSpaces( txt ) );
		}
		if ( countOfMemoryStoreMethodCalls > 0 ) {
			returnback.onLongClick( findViewById( R.id.btnCalc ) );
		}
		countOfMemoryStoreMethodCalls = 0;
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
			requestCode = RequestCodes.START_HISTORY;
		} else if ( cls.equals( MemoryActionsActivity.class ) ) {
			if ( possibleExtras.getStringExtra( "type" ).equals( "rc" ) ) {
				requestCode = RequestCodes.START_MEMORY_RECALL;
			} else {
				requestCode = RequestCodes.START_MEMORY_STORE;
			}
		} else if ( cls.equals( VariableEditorActivity.class ) ) {
			requestCode = RequestCodes.START_ADD_VAR;
		}
		startActivityForResult( intent, requestCode );
	}

	private void show_str() {
		TextView t = findViewById( R.id.ExampleStr );
		//t.setTextIsSelectable(false);
		t.setVisibility( View.VISIBLE );
	}

	private void hide_str() {
		TextView t = findViewById( R.id.ExampleStr );
		t.setVisibility( View.INVISIBLE );
	}

	private void show_ans() {
		TextView t = findViewById( R.id.AnswerStr );
		t.setVisibility( View.VISIBLE );
	}

	private void hideAns() {
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
				if ( Utils.isDigit( last ) ) {
					mExample.setText( txt + btntxt );
					equallu( "not" );
				} else {
					txt = txt.substring( 0, txt.length() - 1 );
					mExample.setText( txt + btntxt );
					equallu( "not" );
				}
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
				show_str();
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

		if ( btntxt.equals( FI ) || btntxt.equals( PI ) || btntxt.equals( "e" ) ) {
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

			if ( Utils.isDigit( last ) || Character.toString( last ).equals( FI ) || Character.toString( last ).equals( PI ) || last == 'e' ) {
				mExample.setText( txt + btntxt );
				equallu( "not" );
				return;
			}
			if ( !Utils.isDigit( last ) ) {
				if ( last == '!' || last == '%' ) {
					mExample.setText( txt + btntxt );
					equallu( "not" );
				}
			}
			return;
		}

		if ( btntxt.equals( "√" ) ) {
			if ( len == 0 ) {
				mExample.setText( btntxt );
				show_str();
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
							show_str();
							scrollExampleToEnd();
						} else {
							if ( Utils.isDigit( last ) || isSpecific( last ) ) {
								mExample.setText( txt + MULTIPLY_SIGN + btntxt );
								equallu( "not" );
								show_str();
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
				show_str();
				return;
			}
			if ( last == '.' ) {
				return;
			}
			if ( ( Utils.isDigit( last ) || last == '!' || last == '%' || Utils.isConstNum( last, this ) || isCloseBracket( last ) ) && !txt.equals( "" ) ) {
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
					if ( !Utils.isDigit( last ) && last != '!' && !Character.toString( last ).equals( FI ) && !Character.toString( last ).equals( PI ) && last != 'e' && !isCloseBracket( last ) ) {
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

		if ( !Utils.isDigit( btntxt ) && !Utils.isLetter( btntxt.charAt( 0 ) ) ) {
			if ( len != 0 ) {
				if ( txt.charAt( len - 1 ) == 'π' ||
						txt.charAt( len - 1 ) == 'φ' ||
						txt.charAt( len - 1 ) == 'e' ) {
					mExample.setText( txt + btntxt );
					equallu( "not" );
					return;
				}
			}
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
			show_str();
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
								&& !s.equals( PI ) && !s.equals( FI ) && txt.charAt( len - 2 ) != 'e' ) ) {
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
				hide_str();
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
