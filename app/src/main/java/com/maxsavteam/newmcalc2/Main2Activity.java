package com.maxsavteam.newmcalc2;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.haseebazeem.sampleGif.GifImageView;
import com.maxsavteam.calculator.CalculatorExpressionFormatter;
import com.maxsavteam.calculator.CalculatorExpressionTokenizer;
import com.maxsavteam.calculator.exceptions.CalculationException;
import com.maxsavteam.calculator.results.NumberList;
import com.maxsavteam.newmcalc2.adapters.ViewPagerAdapter;
import com.maxsavteam.newmcalc2.core.CalculationMode;
import com.maxsavteam.newmcalc2.core.CalculationResult;
import com.maxsavteam.newmcalc2.core.CalculatorWrapper;
import com.maxsavteam.newmcalc2.entity.HistoryEntry;
import com.maxsavteam.newmcalc2.fragment.viewpager.NumPadFragmentFactory;
import com.maxsavteam.newmcalc2.fragment.viewpager.VariablesFragmentFactory;
import com.maxsavteam.newmcalc2.memory.MemoryReader;
import com.maxsavteam.newmcalc2.memory.MemorySaver;
import com.maxsavteam.newmcalc2.ui.AboutAppActivity;
import com.maxsavteam.newmcalc2.ui.CurrencyConverterActivity;
import com.maxsavteam.newmcalc2.ui.HistoryActivity;
import com.maxsavteam.newmcalc2.ui.MemoryActionsActivity;
import com.maxsavteam.newmcalc2.ui.NumberGeneratorActivity;
import com.maxsavteam.newmcalc2.ui.NumberSystemConverterActivity;
import com.maxsavteam.newmcalc2.ui.PasswordGeneratorActivity;
import com.maxsavteam.newmcalc2.ui.SettingsActivity;
import com.maxsavteam.newmcalc2.ui.VariableEditorActivity;
import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;
import com.maxsavteam.newmcalc2.utils.FormatUtils;
import com.maxsavteam.newmcalc2.utils.HistoryManager;
import com.maxsavteam.newmcalc2.utils.ResultCodesConstants;
import com.maxsavteam.newmcalc2.utils.UpdateMessagesContainer;
import com.maxsavteam.newmcalc2.utils.Utils;
import com.maxsavteam.newmcalc2.variables.Variable;
import com.maxsavteam.newmcalc2.variables.VariableUtils;
import com.maxsavteam.newmcalc2.widget.CalculatorEditText;
import com.maxsavteam.newmcalc2.widget.CustomAlertDialogBuilder;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main2Activity extends ThemeActivity {

	public static final String TAG = "MCalc";

	private static final String LAST_EXPRESSION_SHARED_PREFS_KEY = "last_expression";

	private NavigationView mNavigationView;
	private SharedPreferences sharedPreferences;

	private boolean progressDialogShown = false;

	private List<NumberList> memoryEntries;

	private CalculatorWrapper mCalculatorWrapper;
	private final Point displaySize = new Point();

	private String MULTIPLY_SIGN;
	private String DIVISION_SIGN;

	private Timer mCoreTimer;
	private Thread mCoreThread = null;

	private int timerCountDown = 0;

	private CalculationResult lastCalculatedResult;

	private ProgressDialog mThreadControllerProgressDialog;

	private DecimalFormat mDecimalFormat;

	private static class MemoryStartTypes {
		public static final String RECALL = "rc", STORE = "st";
	}

	private final ActivityResultLauncher<Intent> mHistoryLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result->{
				Intent data = result.getData();
				if ( result.getResultCode() != ResultCodesConstants.RESULT_ERROR && data != null ) {
					CalculatorEditText exampleEditText = findViewById( R.id.ExampleStr );
					String example = data.getStringExtra( "example" );
					if ( example != null && !example.equals( "" ) ) {
						if(exampleEditText.getText().length() == 0)
							addStringExampleToTheExampleStr( example );
						else
							addStringExampleToTheExampleStr( data.getStringExtra( "result" ) );
					}
				}
			}
	);

	private final ActivityResultLauncher<Intent> mMemoryRecallLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result->{
				if ( result.getResultCode() == ResultCodesConstants.RESULT_APPEND ) {
					Intent data = result.getData();
					if ( data != null ) {
						int position = data.getIntExtra( "position", 0 );
						addToCurrentExpression( memoryEntries.get( position ) );
					}
				}
			}
	);

	private final ActivityResultLauncher<Intent> memoryStoreLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result->{
				if ( result.getResultCode() == RESULT_OK ) {
					Intent data = result.getData();
					if ( data != null ) {
						int position = data.getIntExtra( "position", 0 );
						Optional<NumberList> optionalNumberList = getCurrentCalculatedValue();
						optionalNumberList.ifPresent( numberList->{
							memoryEntries.set( position, numberList );
							MemorySaver.save( sharedPreferences, memoryEntries );
						} );
					}
				}
			}
	);

	private final ActivityResultLauncher<Intent> mVariablesEditorLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result->setViewPager( ( (ViewPager2) findViewById( R.id.viewpager ) ).getCurrentItem() )
	);

	private final ActivityResultLauncher<Intent> mSettingsLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result->{
				if ( result.getResultCode() == ResultCodesConstants.RESULT_RESTART_APP ) {
					Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage( getPackageName() );
					if ( intent != null ) {
						intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
						intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK );
						startActivity( intent );
					}
				}
			}
	);

	private final ActivityResultLauncher<Intent> mDefaultActivityLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result->{}
	);

	private final View.OnLongClickListener mOnVariableLongClick = v->{
		Button btn = (Button) v;
		int pos = Integer.parseInt( btn.getTag().toString() );
		Intent in = new Intent( this, VariableEditorActivity.class );
		in.putExtra( "tag", pos ).putExtra( "is_existing", true );
		ArrayList<Variable> a = VariableUtils.readVariables();
		for (int i = 0; i < a.size(); i++) {
			if ( a.get( i ).getTag() == pos ) {
				in.putExtra( "name", btn.getText().toString() ).putExtra( "value", a.get( i ).getValue() );
				break;
			}
		}
		mVariablesEditorLauncher.launch( in );
		return true;
	};

	private final View.OnLongClickListener mReturnBack = v->{
		if ( lastCalculatedResult != null ) {
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
			openMemory( MemoryStartTypes.RECALL );
		} else {
			openMemory( MemoryStartTypes.STORE );
		}
		return true;
	};

	private List<String> functionsWithNecessaryOpenBracket;

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		if ( id == R.id.about ) {
			startActivity( new Intent( this, AboutAppActivity.class ) );
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

	private void restoreResultIfSaved() {
		if ( sharedPreferences.getBoolean( getString( R.string.pref_save_result ), false ) ) {
			String expression = sharedPreferences.getString( LAST_EXPRESSION_SHARED_PREFS_KEY, "" );
			FormatUtils.Formatter formatter = number -> {
				if(number.equals( "." ))
					return "0";
				return mDecimalFormat.format( new BigDecimal( number ) );
			};
			String formatted;
			if( expression.isEmpty() )
				formatted = "";
			else
				formatted = FormatUtils.formatExpression( expression, formatter, FormatUtils.getRootLocaleFormatSymbols() );

			insert( formatted );
		}
	}

	private static class AdditionalActivities {
		public static final String HISTORY = "history";
		public static final String SETTINGS = "settings";
		public static final String NUMBER_GENERATOR = "numgen";
		public static final String PASSWORD_GENERATOR = "passgen";
		public static final String NUMBER_SYSTEMS_CONVERTER = "bin";
		public static final String CURRENCY_CONVERTER = "cc";
	}

	private void goToAdditionalActivities(String where) {
		Map<String, ActivityResultLauncher<Intent>> launcherMap = Map.of(
				AdditionalActivities.SETTINGS, mSettingsLauncher,
				AdditionalActivities.NUMBER_GENERATOR, mDefaultActivityLauncher,
				AdditionalActivities.PASSWORD_GENERATOR, mDefaultActivityLauncher,
				AdditionalActivities.HISTORY, mHistoryLauncher,
				AdditionalActivities.NUMBER_SYSTEMS_CONVERTER, mDefaultActivityLauncher,
				AdditionalActivities.CURRENCY_CONVERTER, mDefaultActivityLauncher
		);
		Map<String, Class<?>> activityMap = Map.of(
				AdditionalActivities.SETTINGS, SettingsActivity.class,
				AdditionalActivities.NUMBER_GENERATOR, NumberGeneratorActivity.class,
				AdditionalActivities.PASSWORD_GENERATOR, PasswordGeneratorActivity.class,
				AdditionalActivities.HISTORY, HistoryActivity.class,
				AdditionalActivities.NUMBER_SYSTEMS_CONVERTER, NumberSystemConverterActivity.class,
				AdditionalActivities.CURRENCY_CONVERTER, CurrencyConverterActivity.class
		);
		if ( !launcherMap.containsKey( where ) || !activityMap.containsKey( where ) ) {
			return;
		}
		ActivityResultLauncher<Intent> launcher = launcherMap.get( where );
		Class<?> activity = activityMap.get( where );
		if ( launcher != null ) {
			launcher.launch( new Intent( this, activity ) );
		}
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged( newConfig );

		App.getInstance().updateAppLocale();

		CalculatorWrapper.getInstance().updateLocale();

		DecimalFormat newDecimalFormat = FormatUtils.getDecimalFormat();

		CalculatorEditText exampleEditText = findViewById( R.id.ExampleStr );
		exampleEditText.updateLocale();
		reformatTextView( exampleEditText, newDecimalFormat );

		CalculatorEditText answerEditText = findViewById( R.id.AnswerStr );
		answerEditText.updateLocale();
		reformatTextView( answerEditText, newDecimalFormat );

		mDecimalFormat = newDecimalFormat;

		RecyclerView.Adapter<?> viewPagerAdapter = ( (ViewPager2) findViewById( R.id.viewpager ) ).getAdapter();
		if ( viewPagerAdapter != null ) {
			viewPagerAdapter.notifyItemChanged( 0 ); // update numpad
		}
	}

	private void reformatTextView(TextView textView, DecimalFormat newDecimalFormat) {
		String current = textView.getText().toString();
		if ( current.length() > 0 ) {
			String normalized = FormatUtils.normalizeNumbersInExample( current, mDecimalFormat );

			String reformattedExample = FormatUtils.formatNumbersInExpression( normalized, newDecimalFormat );
			textView.setText( reformattedExample );
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		setContentView( R.layout.activity_main2 );
		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		initializeDrawer();

		mCalculatorWrapper = CalculatorWrapper.getInstance();

		mDecimalFormat = FormatUtils.getDecimalFormat();

		MULTIPLY_SIGN = getString( R.string.multiply );
		DIVISION_SIGN = getString( R.string.div );

		functionsWithNecessaryOpenBracket = List.of(
				"A",
				getString( R.string.gcd ),
				getString( R.string.lcm ),
				getString( R.string.abs )
		);

		EditText editText = findViewById( R.id.ExampleStr );
		editText.setShowSoftInputOnFocus( false );
		editText.setSelection( 0 );

		ImageButton imageButton = findViewById( R.id.btnDelete );
		imageButton.setOnLongClickListener( v->{
			onClear( v );
			return true;
		} );

		applyTheme();

		checkShortcutAction();

		setViewPager( 0 );

		initializeScrollViews();

		showWhatNew();

		getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );

		if ( GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable( getApplicationContext() ) == ConnectionResult.SUCCESS ) {
			showRatePromptIfNeeded();
			checkForUpdatesAndStartFlow();
		}

		enableElPrimoEasterEgg();

		memoryEntries = MemoryReader.read( sharedPreferences );

		restoreResultIfSaved();

		if ( sharedPreferences.getBoolean( "keep_screen_on", false ) ) {
			getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		}
	}

	private void initializeDrawer() {
		DrawerLayout drawer = findViewById( R.id.drawer_layout );
		Toolbar toolbar = findViewById( R.id.toolbar );
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
		drawer.setDrawerListener( toggle );
		toggle.syncState();
		mNavigationView = findViewById( R.id.nav_view );
		mNavigationView.setBackgroundColor( Color.BLACK );
		mNavigationView.setNavigationItemSelectedListener( menuItem->{
			Map<Integer, String> map = Map.of(
					R.id.nav_settings, AdditionalActivities.SETTINGS,
					R.id.nav_history, AdditionalActivities.HISTORY,
					R.id.nav_numbersysconverter, AdditionalActivities.NUMBER_SYSTEMS_CONVERTER,
					R.id.nav_passgen, AdditionalActivities.PASSWORD_GENERATOR,
					R.id.nav_numgen, AdditionalActivities.NUMBER_GENERATOR,
					R.id.nav_cc, AdditionalActivities.CURRENCY_CONVERTER
			);
			int itemId = menuItem.getItemId();
			if ( map.containsKey( itemId ) ) {
				goToAdditionalActivities( map.get( itemId ) );
			}
			menuItem.setChecked( false );

			drawer.closeDrawer( GravityCompat.START );
			return true;
		} );
	}

	private void checkForUpdatesAndStartFlow() {
		AppUpdateManager manager = AppUpdateManagerFactory.create( getApplicationContext() );
		manager.getAppUpdateInfo()
				.addOnSuccessListener( appUpdateInfo->{
					if ( appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ) {
						manager.startUpdateFlow(
								appUpdateInfo,
								this,
								AppUpdateOptions.newBuilder( AppUpdateType.FLEXIBLE ).build()
						);
					}
				} );
	}

	private void showRatePromptIfNeeded() {
		int startCount = sharedPreferences.getInt( "start_count", 0 );
		if ( startCount >= 3 ) {
			sharedPreferences.edit().putInt( "start_count", 0 ).apply();
			ReviewManager reviewManager = ReviewManagerFactory.create( getApplicationContext() );
			reviewManager.requestReviewFlow().addOnCompleteListener( task->{
				if ( task.isSuccessful() ) {
					reviewManager.launchReviewFlow( this, task.getResult() );
				} else {
					Log.i( TAG, "requestReviewFlow: " + task.getException() );
				}
			} );
		} else {
			sharedPreferences.edit().putInt( "start_count", startCount + 1 ).apply();
		}
	}

	private void enableElPrimoEasterEgg() {
		if ( getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT ) {
			return;
		}
		GifImageView imageView = findViewById( R.id.gif_image_view_el_primo );
		imageView.stop();
		findViewById( R.id.btnDelAll ).setOnLongClickListener( v->{
			imageView.setVisibility( View.VISIBLE );
			imageView.start();
			Executors.newSingleThreadScheduledExecutor().schedule(
					()->runOnUiThread( ()->{
						imageView.stop();
						imageView.setVisibility( View.GONE );
					} ),
					3,
					TimeUnit.SECONDS
			);
			Toast.makeText( this, "Ilyash guliash", Toast.LENGTH_SHORT ).show();
			return true;
		} );
	}

	private void checkShortcutAction() {
		Intent startIntent = getIntent();
		if ( startIntent.getBooleanExtra( "shortcut_action", false ) ) {
			String action = startIntent.getStringExtra( "to_" );
			if ( action != null ) {
				goToAdditionalActivities( action );
			}
		}
	}

	private void showWhatNew() {
		String version = BuildConfig.VERSION_NAME;
		if ( !App.getInstance().isFirstStart() && UpdateMessagesContainer.isReleaseNoteExists( version ) && !UpdateMessagesContainer.isReleaseNoteShown( version ) ) {
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
			AlertDialog d = builder.create();
			d.show();

			TextView textView = d.findViewById( android.R.id.message );
			if ( textView != null ) {
				textView.setMovementMethod( LinkMovementMethod.getInstance() );
			}
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

	private void showWithAlpha(int id) {
		findViewById( id ).animate().alpha( 1f ).setDuration( 100 ).start();
	}

	private void hideWithAlpha(int id) {
		findViewById( id ).animate().alpha( 0f ).setDuration( 100 ).start();
	}

	private void setViewPager(int which) {
		ArrayList<ViewPagerAdapter.ViewPagerFragmentFactory> factories = new ArrayList<>();
		NumPadFragmentFactory.Configuration configuration = new NumPadFragmentFactory.Configuration( this )
				.setCalculateButtonClickListener( v->calculate( CalculationMode.FULL_ANSWER ) )
				.setCalculateButtonLongClickListener( mReturnBack )
				.setDigitButtonClickListener( digit->insert( String.valueOf( digit ) ) )
				.setSeparatorButtonClickListener( separator->insertDecimalSeparator() )
				.setBracketButtonClickListener( this::insert )
				.setFunctionButtonClickListener( this::insertFunction )
				.setBinaryOperatorButtonClickListener( this::insertBinaryOperator )
				.setSuffixOperatorButtonClickListener( this::insertSuffixOperator )
				.setOnJustInsertButtonClickListener( this::insert );
		factories.add( new NumPadFragmentFactory( configuration ) );
		factories.add( new VariablesFragmentFactory( this, mMemoryActionsLongClick, mOnVariableLongClick ) );

		ViewPager2 viewPager = findViewById( R.id.viewpager );
		ViewPagerAdapter viewPagerAdapter =
				new ViewPagerAdapter( factories, viewPager::getHeight );
		viewPager.setAdapter( viewPagerAdapter );
		viewPager.setCurrentItem( which );
		TypedValue data = new TypedValue();
		getTheme().resolveAttribute( R.attr.textColor, data, true );
		int defaultArrowsColor = data.data;
		ImageView imageViewRightArrow = findViewById( R.id.image_view_right );
		ArgbEvaluator evaluator = new ArgbEvaluator();
		viewPager.registerOnPageChangeCallback( new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				if ( position == 0 ) {
					hideWithAlpha( R.id.image_view_left );
					showWithAlpha( R.id.image_view_right );

				} else if ( position == factories.size() - 1 ) {
					showWithAlpha( R.id.image_view_left );
					hideWithAlpha( R.id.image_view_right );
				} else {
					showWithAlpha( R.id.image_view_left );
					showWithAlpha( R.id.image_view_right );
				}
				if ( position == 0 ) {
					ObjectAnimator
							.ofObject(
									imageViewRightArrow,
									"colorFilter",
									evaluator,
									defaultArrowsColor,
									Color.WHITE
							)
							.setDuration( 200L )
							.start();
				} else {
					ObjectAnimator
							.ofObject(
									imageViewRightArrow,
									"colorFilter",
									evaluator,
									Color.WHITE,
									defaultArrowsColor
							)
							.setDuration( 200L )
							.start();
				}
			}
		} );
	}

	private void addStringExampleToTheExampleStr(String s, boolean wrapIfNeeded) {
		EditText editText = findViewById( R.id.ExampleStr );
		Editable e = editText.getText();
		if ( e == null || e.toString().isEmpty() ) {
			insert( s );
		} else {
			if ( wrapIfNeeded ) {
				insert( "(" + s + ")" );
			} else {
				insert( s );
			}
		}
	}

	private void addStringExampleToTheExampleStr(String s) {
		addStringExampleToTheExampleStr( s, true );
	}

	private void addToCurrentExpression(NumberList r) {
		if ( r.isSingleNumber() ) {
			addStringExampleToTheExampleStr( formatNumber( r.getSingleNumberIfTrue() ) );
		} else {
			addStringExampleToTheExampleStr( r.format( mDecimalFormat ), false );
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
				Intent in = new Intent( this, VariableEditorActivity.class );
				in.putExtra( "tag", pos );
				if ( lastCalculatedResult != null ) {
					NumberList r = lastCalculatedResult.getResult();
					if ( r.isSingleNumber() ) {
						in.putExtra( "value", r.getSingleNumberIfTrue().toPlainString() ).putExtra( "name", "" ).putExtra( "is_existing", true );
					}
				}
				mVariablesEditorLauncher.launch( in );
			} else {
				String var_arr = sharedPreferences.getString( "variables", null );
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

	private String getCurrentExpression() {
		EditText editText = findViewById( R.id.ExampleStr );
		Editable editable = editText.getText();
		return editable == null ? "" : editable.toString();
	}

	/**
	 * Returns result from {@code lastCalculatedResult} or user-entered number
	 */
	private Optional<NumberList> getCurrentCalculatedValue() {
		if ( lastCalculatedResult != null ) {
			return Optional.of( lastCalculatedResult.getResult() );
		}
		String currentExpression = getCurrentExpression();
		if ( Utils.isNumber( currentExpression, mDecimalFormat.getDecimalFormatSymbols() ) ) {
			Number parsedNumber = mDecimalFormat.parse( currentExpression, new ParsePosition( 0 ) );
			if ( parsedNumber == null ) {
				return Optional.empty();
			}
			BigDecimal bigDecimal = new BigDecimal( parsedNumber.toString() );
			return Optional.of( NumberList.of( bigDecimal ) );
		}
		return Optional.empty();
	}

	private void openMemory(final String type) {
		Intent in = new Intent( this, MemoryActionsActivity.class );
		in.putExtra( "type", type );
		if ( type.equals( MemoryStartTypes.STORE ) ) {
			if ( lastCalculatedResult != null || Utils.isNumber( getCurrentExpression(), mDecimalFormat.getDecimalFormatSymbols() ) ) {
				memoryStoreLauncher.launch( in );
			}
		} else {
			mMemoryRecallLauncher.launch( in );
		}
	}

	public void onMemoryPlusMinusButtonsClick(View v) {
		if ( lastCalculatedResult == null ) {
			return;
		}

		NumberList result = lastCalculatedResult.getResult();

		NumberList firstInMemory = memoryEntries.get( 0 );
		if ( !result.isSingleNumber() && !firstInMemory.isSingleNumber() ) {
			Toast.makeText( this, R.string.addition_and_substracting_of_lists_is_not_supported_yet, Toast.LENGTH_SHORT ).show();
			return;
		}
		NumberList finalResult;
		if ( result.isSingleNumber() && firstInMemory.isSingleNumber() ) {
			BigDecimal a = firstInMemory.getSingleNumberIfTrue();
			BigDecimal b = result.getSingleNumberIfTrue();
			if ( v.getId() == R.id.btnMemPlus ) {
				finalResult = NumberList.of( a.add( b ) );
			} else {
				finalResult = NumberList.of( a.subtract( b ) );
			}
		} else {
			char op = '+';
			if ( v.getId() == R.id.btnMemMinus ) {
				op = '-';
			}
			try {
				finalResult = CalculatorWrapper.getInstance()
						.calculate( firstInMemory.format() + op + result.format() );
			} catch (Exception e) {
				Toast.makeText( this, R.string.some_error_occurred, Toast.LENGTH_SHORT ).show();
				return;
			}
		}
		memoryEntries.set( 0, finalResult );
		MemorySaver.save( sharedPreferences, memoryEntries );
	}

	public void onMemoryStoreButtonClick(View view) {
		Optional<NumberList> optionalNumberList = getCurrentCalculatedValue();
		optionalNumberList.ifPresent( result->{
			memoryEntries.set( 0, result );
			MemorySaver.save( sharedPreferences, memoryEntries );
		} );
	}

	public void onMemoryRecallButtonClick(View view) {
		addToCurrentExpression( memoryEntries.get( 0 ) );
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

	private boolean isConstant(String s) {
		return s.equals( getString( R.string.pi ) )
				|| s.equals( getString( R.string.fi ) )
				|| s.equals( getString( R.string.euler_constant ) );
	}

	public void insertFunction(String functionName) {
		EditText editText = findViewById( R.id.ExampleStr );
		String currentExpression = editText.getText().toString();
		int selection = editText.getSelectionStart();
		if ( !currentExpression.isEmpty() && selection > 0 ) {
			char last = currentExpression.charAt( selection - 1 );
			if ( !isConstant( String.valueOf( last ) ) && Character.isLetter( last ) ) {
				insert( "(" );
			}
		}
		insert( functionName );
		if ( functionsWithNecessaryOpenBracket.contains( functionName ) ) {
			insert( "(" );
		}
	}

	private void insertDecimalSeparator() {
		Locale locale = getResources().getConfiguration().getLocales().get( 0 );
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols( locale );
		char decimalSeparator = decimalFormatSymbols.getDecimalSeparator();
		char groupingSeparator = decimalFormatSymbols.getGroupingSeparator();
		EditText editText = findViewById( R.id.ExampleStr );
		String text = editText.getText().toString();
		int selection = editText.getSelectionStart();
		int numberStart = selection; // number in that case can include grouping separators and multiple(!) decimal separators
		int numberEnd = Math.min(text.length(), selection + 1);
		while ( numberStart > 0
				&& ( Character.isDigit( text.charAt( numberStart - 1 ) )
				|| text.charAt( numberStart - 1 ) == groupingSeparator
				|| text.charAt( numberStart - 1 ) == decimalSeparator ) ) {
			numberStart--;
		}
		while ( numberEnd < text.length()
				&& ( Character.isDigit( text.charAt( numberEnd ) )
				|| text.charAt( numberEnd ) == groupingSeparator
				|| text.charAt( numberEnd ) == decimalSeparator ) ) {
			numberEnd++;
		}

		String number = text.substring( numberStart, numberEnd );
		if(number.chars().filter( ch -> ch == decimalSeparator ).count() >= 2)
			// maximum two separators can be allowed, because two separators can be used to split number in two
			// example, "5.2" -> "25.2" -> "255.2" -> "2.55.2" -> "2.5*5.2"
			return;
		insert( String.valueOf( decimalSeparator ) );
	}

	private void insert(String s) {
		EditText editText = findViewById( R.id.ExampleStr );
		Editable e = editText.getText();
		int selection = editText.getSelectionStart();
		e.insert( selection, s );
		editText.requestFocus();
		calculate( CalculationMode.PRE_ANSWER );
	}

	public void onClear(View v) {
		clearFormulaEditText();
		clearAnswer();
		lastCalculatedResult = null;
		sharedPreferences.edit().remove( LAST_EXPRESSION_SHARED_PREFS_KEY ).apply();
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

	private void calculate(CalculationMode mode) {
		EditText txt = findViewById( R.id.ExampleStr );
		String example = txt.getText().toString();

		saveExpressionIfEnabled( example );

		lastCalculatedResult = null;

		if ( Utils.isNumber( example, mDecimalFormat.getDecimalFormatSymbols() ) || example.isEmpty() ) {
			clearAnswer();
			return;
		}

		String formatted;
		try {
			formatted = mCalculatorWrapper.getCalculator().formatExpression( example );
		} catch (CalculationException e) {
			writeCalculationError( getString( CalculatorWrapper.getStringResForErrorCode( CalculationException.INVALID_BRACKETS_SEQUENCE ) ) );
			return;
		}

		String finalFormatted = formatted;
		mCoreThread = new Thread( ()->{
			FirebaseCrashlytics.getInstance().log( "Now calculating: " + example + "; formatted: " + finalFormatted );
			try {
				NumberList res = mCalculatorWrapper.calculate( finalFormatted );
				lastCalculatedResult = new CalculationResult()
						.setMode( mode )
						.setResult( res )
						.setExpression( finalFormatted );
				runOnUiThread( ()->writeResult( mode, res, finalFormatted ) );
				if ( mode == CalculationMode.FULL_ANSWER && sharedPreferences.getBoolean( getString( R.string.pref_save_history ), true ) ) {
					saveToHistory( finalFormatted, res );
				}
				if ( mode == CalculationMode.FULL_ANSWER ) {
					// important to call with decimalFormat,
					// because normalization method will be called in saveExpressionIfEnabled
					saveExpressionIfEnabled( res.format( mDecimalFormat ) );
				}
			} catch (CalculationException e) {
				if ( mode == CalculationMode.FULL_ANSWER ) {
					int res = CalculatorWrapper.getStringResForErrorCode( e.getErrorCode() );
					int stringRes;
					if ( res != -1 ) {
						stringRes = res;
					} else {
						stringRes = R.string.error;
					}
					runOnUiThread( ()->writeCalculationError( getString( stringRes ) ) );
				} else {
					runOnUiThread( Main2Activity.this::clearAnswer );
				}
			}
			killCoreTimer();
		} );

		startThreadController();
	}

	private void saveExpressionIfEnabled(String expression) {
		String normalizedExpression = FormatUtils.normalizeNumbersInExample( expression, mDecimalFormat );
		sharedPreferences.edit()
				.putString( LAST_EXPRESSION_SHARED_PREFS_KEY, normalizedExpression )
				.apply();
	}

	private void saveToHistory(String expression, NumberList result) {
		String example = FormatUtils.normalizeNumbersInExample( expression, mDecimalFormat );
		HistoryManager.getInstance()
				.put( new HistoryEntry( example, result.format() ) )
				.save();
	}

	private void startThreadController() {
		mThreadControllerProgressDialog = new ProgressDialog( this, R.style.DarkAlertDialog );
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
		if ( !BuildConfig.DEBUG ) {
			mCoreTimer.schedule( new CoreController(), 0, 100 );
		}
		mCoreThread.start();
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
			calculate( CalculationMode.PRE_ANSWER );
		}
	}

	private String formatNumber(BigDecimal num) {
		return mDecimalFormat.format( num );
	}

	private void writeResult(CalculationMode mode, NumberList result, String formattedExample) {
		EditText editText = findViewById( R.id.ExampleStr );
		CalculatorEditText answerTextView = findViewById( R.id.AnswerStr );

		clearAnswer();
		if ( mode == CalculationMode.PRE_ANSWER ) {
			answerTextView.setText( result.format( mDecimalFormat ) );
		} else {
			answerTextView.setText( formattedExample );
			String formattedNum = result.format( mDecimalFormat );
			editText.setText( formattedNum );
			editText.setSelection( formattedNum.length() );
		}
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {
		getMenuInflater().inflate( R.menu.menu_main, menu );
		return true;
	}

	private void scrollAnswerToEnd() {
		HorizontalScrollView scrollView = findViewById( R.id.scrollViewAns );
		scrollView.post( ()->scrollView.fullScroll( HorizontalScrollView.FOCUS_RIGHT ) );
	}
}
