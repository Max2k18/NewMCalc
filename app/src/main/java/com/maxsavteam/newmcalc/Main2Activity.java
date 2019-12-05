package com.maxsavteam.newmcalc;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.maxsavteam.newmcalc.adapters.FragmentAdapterInitializationObject;
import com.maxsavteam.newmcalc.adapters.MyFragmentPagerAdapter;
import com.maxsavteam.newmcalc.core.CalculationResult;
import com.maxsavteam.newmcalc.core.CoreMain;
import com.maxsavteam.newmcalc.error.Error;
import com.maxsavteam.newmcalc.memory.MemorySaverReader;
import com.maxsavteam.newmcalc.utils.Constants;
import com.maxsavteam.newmcalc.utils.Format;
import com.maxsavteam.newmcalc.utils.MyTuple;
import com.maxsavteam.newmcalc.utils.Utils;
import com.maxsavteam.newmcalc.viewpagerfragment.fragment1.FragmentOneInitializationObject;
import com.maxsavteam.newmcalc.viewpagerfragment.fragment2.FragmentTwoInitializationObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Main2Activity extends AppCompatActivity implements CoreMain.CoreLinkBridge {

	private AppBarConfiguration mAppBarConfiguration;
	private NavigationView mNavigationView;
	private SharedPreferences sp;
	private boolean was_error = false;
	private boolean DarkMode = true;
	private boolean isOtherActivityOpened = false;
	private boolean isBroadcastsRegistered;
	private ArrayList<BroadcastReceiver> registeredBroadcasts = new ArrayList<>();
	private TextView t;
	private MemorySaverReader mMemorySaverReader;
	private char last;
	private BigDecimal[] memoryEntries;
	private String MULTIPLY_SIGN;
	private String FI, PI, original;
	private CoreMain mCoreMain;
	private ViewPager mViewPager;
	private  Point displaySize = new Point();
	private Thread coreThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			DarkMode = sp.getBoolean("dark_mode", false);
			if (DarkMode)
				setTheme(android.R.style.Theme_Material_NoActionBar);
			else
				setTheme(R.style.AppThemeMainActivity);
			setContentView(R.layout.activity_main2);
			Toolbar toolbar = findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
			getSupportActionBar().setTitle("New MCalc");
			DrawerLayout drawer = findViewById(R.id.drawer_layout);
			ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
					this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
			drawer.setDrawerListener(toggle);
			toggle.syncState();
			mNavigationView = findViewById(R.id.nav_view);
			mNavigationView.setBackgroundColor(Color.BLACK);
			mNavigationView.setNavigationItemSelectedListener(menuItem -> {
				if (menuItem.getItemId() == R.id.nav_settings) {
					goToAdditionalActivities("settings");
				} else if (menuItem.getItemId() == R.id.nav_history) {
					goToAdditionalActivities("history");
				} else if (menuItem.getItemId() == R.id.nav_numbersysconverter) {
					goToAdditionalActivities("bin");
				} else if (menuItem.getItemId() == R.id.nav_passgen) {
					goToAdditionalActivities("pass");
				} else if (menuItem.getItemId() == R.id.nav_numgen) {
					goToAdditionalActivities("numgen");
				}
				menuItem.setChecked(false);
				drawer.closeDrawer(GravityCompat.START);
				return true;
			});

			mAppBarConfiguration = new AppBarConfiguration.Builder(
					R.id.nav_history,
					R.id.nav_numbersysconverter,
					R.id.nav_passgen,
					R.id.nav_numgen,
					R.id.nav_settings
			)
					.setDrawerLayout(drawer)
					.build();

			mCoreMain = new CoreMain(this);
			mCoreMain.setInterface(this);
			FI = getResources().getString(R.string.fi);
			PI = getResources().getString(R.string.pi);
			MULTIPLY_SIGN = getResources().getString(R.string.multiply);

			applyTheme();

			Intent startIntent = getIntent();
			if (startIntent.getBooleanExtra("shortcut_action", false)) {
				String whereWeNeedToGoToAnotherActivity = startIntent.getStringExtra("to_");
				if (whereWeNeedToGoToAnotherActivity != null) {
					goToAdditionalActivities(whereWeNeedToGoToAnotherActivity);
				}
			}

			setViewPager(0);
		}catch(Exception e){
			goToActivity(CatchService.class, new Intent().putExtra("action", "somethingWentWrong"));
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.exit)
				.setMessage(R.string.areyousureexit)
				.setCancelable(false)
				.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel())
				.setPositiveButton(R.string.yes, (dialog, which) -> {
					dialog.cancel();
					finishAndRemoveTask();
					overridePendingTransition(R.anim.abc_popup_enter,R.anim.alpha_hide);
				});
		AlertDialog al_1 = builder.create();
		if(al_1.getWindow() != null) {
			if (DarkMode)
				al_1.getWindow().setBackgroundDrawableResource(R.drawable.grey);

			Utils.recolorAlertDialogButtons(al_1, this);
		}
		al_1.show();
		//super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.about) {
			goToActivity(CatchService.class, new Intent().putExtra("action", "about_app"));
			return true;
		}else if(id == R.id.what_new){
			Intent in = new Intent(Intent.ACTION_VIEW);
			in.setData(Uri.parse("https://newmcalc.maxsav.team/what-new/#" + BuildConfig.VERSION_NAME));
			startActivity(in);
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("SourceLockedOrientationActivity")
	private void applyTheme() {
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		if(windowManager != null) {
			Display d = windowManager.getDefaultDisplay();
			d.getSize(displaySize);
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		View header = mNavigationView.getHeaderView(0);
		ViewGroup.LayoutParams headerLayoutParams = header.getLayoutParams();
		headerLayoutParams.height = displaySize.y / 3;
		header.setLayoutParams(headerLayoutParams);
		ImageButton imageButton = header.findViewById(R.id.imgBtnHeader);
		imageButton.setOnClickListener(v -> {
			Intent in = new Intent(Intent.ACTION_VIEW);
			in.setData(Uri.parse("https://newmcalc.maxsav.team/"));
			startActivity(in);
		});
		int[] headersIds = new int[]{
				R.id.headerTitle
		};
		int navDefaultTextColor;
		int navIconColor;
		if (DarkMode) {
			getWindow().setBackgroundDrawableResource(R.drawable.black);
			getWindow().setNavigationBarColor(Color.BLACK);
			TextView t;
			int[] ids = new int[]{
					R.id.AnswerStr,
					R.id.ExampleStr
			};
			for (int id : ids) {
				t = findViewById(id);
				t.setTextColor(getResources().getColor(R.color.white));
			}
			header.setBackgroundColor(Color.BLACK);
			for(int id : headersIds){
				((TextView) header.findViewById(id)).setTextColor(Color.WHITE);
			}
			navDefaultTextColor = Color.WHITE;
			navIconColor = Color.WHITE;
			mNavigationView.setBackgroundColor(Color.BLACK);
		} else {
			TextView t;
			getWindow().setBackgroundDrawableResource(R.drawable.white);
			getWindow().setNavigationBarColor(Color.WHITE);
			int[] ids = new int[]{
					R.id.AnswerStr,
					R.id.ExampleStr
			};
			for (int id : ids) {
				t = findViewById(id);
				t.setTextColor(getResources().getColor(R.color.black));
			}
			header.setBackgroundColor(Color.WHITE);
			for(int id : headersIds){
				((TextView) header.findViewById(id)).setTextColor(Color.BLACK);
			}
			navDefaultTextColor = Color.BLACK;
			navIconColor = Color.BLACK;
			mNavigationView.setBackgroundColor(Color.WHITE);
		}

		ColorStateList navMenuTextList = new ColorStateList(
				new int[][]{
						new int[]{android.R.attr.state_checked},
						new int[]{-android.R.attr.state_checked}
				},
				new int[]{
						navDefaultTextColor,
						navDefaultTextColor
				}
		);
		ColorStateList navMenuIconColors = new ColorStateList(
				new int[][]{
						new int[]{android.R.attr.state_checked},
						new int[]{-android.R.attr.state_checked}
				},
				new int[]{
						navIconColor,
						navIconColor
				}
		);
		mNavigationView.setItemIconTintList(navMenuIconColors);
		//mNavigationView.setItemBackgroundResource(R.drawable.grey);
		mNavigationView.setItemTextColor(navMenuTextList);
		Toolbar toolbar = findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setElevation(0);
			if (DarkMode) {
				toolbar.setBackgroundColor(Color.BLACK);
				toolbar.setTitleTextColor(Color.WHITE);
				toolbar.setNavigationIcon(R.drawable.ic_menu_white);

			} else {
				toolbar.setBackgroundColor(Color.WHITE);
				toolbar.setTitleTextColor(Color.BLACK);
				toolbar.setNavigationIcon(R.drawable.ic_menu);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode == 10 && grantResults.length == 2){
			if(grantResults[0] == PackageManager.PERMISSION_DENIED
					|| grantResults[1] == PackageManager.PERMISSION_DENIED){
				sp.edit().putBoolean("storage_denied", true).apply();
			}
			if(grantResults[0] == PackageManager.PERMISSION_GRANTED
					&& grantResults[1] == PackageManager.PERMISSION_GRANTED)
				sp.edit().remove("storage_denied").remove("never_request_permissions").apply();
		}
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		registerBroadcastReceivers();

		boolean isFirstStart = sp.getBoolean("first_start", true);

		mMemorySaverReader = new MemorySaverReader(this);
		memoryEntries = mMemorySaverReader.read();

		boolean read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		boolean write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		if(!sp.getBoolean("never_request_permissions", false)
				&& (!read || !write)){
			@SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.never_show_again, null);
			AlertDialog request = new AlertDialog.Builder(this)
					.setTitle(R.string.confirm)
					.setView(v)
					.setMessage(R.string.activity_requet_permissions)
					.setNeutralButton("OK", (dialogInterface, i) -> {
						if(((CheckBox) v.findViewById(R.id.never_show_again)).isChecked()) {
							sp.edit().putBoolean("never_request_permissions", true).apply();
						}
						ActivityCompat
								.requestPermissions(this,
										new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
												Manifest.permission.WRITE_EXTERNAL_STORAGE},
										10);
					})
					.create();
			request.show();
		}
		if(read && write){
			sp.edit().remove("storage_denied").remove("never_request_permissions").apply();
		}
		if(!isFirstStart)
			showWhatNewWindow();

		showSidebarGuide();

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
			ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
			Intent t = new Intent(Intent.ACTION_VIEW, null, this, Main2Activity.class);
			t.putExtra("shortcut_action", true);
			t.putExtra("to_", "numgen");
			ShortcutInfo shortcut1 = new ShortcutInfo.Builder(getApplicationContext(), "id1")
					.setLongLabel(getResources().getString(R.string.randomgen))
					.setShortLabel(getResources().getString(R.string.randomgen))
					.setIcon(Icon.createWithResource(this, R.drawable.dice))
					.setIntent(t)
					.build();
			t.putExtra("to_", "pass");
			ShortcutInfo shortcut2 = new ShortcutInfo.Builder(getApplicationContext(), "id2")
					.setLongLabel(getResources().getString(R.string.passgen))
					.setShortLabel(getResources().getString(R.string.passgen))
					.setIcon(Icon.createWithResource(this, R.drawable.passgen))
					.setIntent(t)
					.build();

			t.putExtra("to_", "history");
			ShortcutInfo shortcut3 = new ShortcutInfo.Builder(getApplicationContext(), "id3")
					.setLongLabel(getResources().getString(R.string.hitory))
					.setShortLabel(getResources().getString(R.string.hitory))
					.setIcon(Icon.createWithResource(this, R.drawable.history))
					.setIntent(t)
					.build();
			t.putExtra("to_", "bin");
			ShortcutInfo shortCutNumSys = new ShortcutInfo.Builder(getApplicationContext(), "idNumSys")
					.setLongLabel(getResources().getString(R.string.number_system_convrter))
					.setShortLabel(getResources().getString(R.string.number_system_convrter))
					.setIcon(Icon.createWithResource(this, R.drawable.binary))
					.setIntent(t)
					.build();


			if (shortcutManager != null) {
				shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut3, shortCutNumSys, shortcut2, shortcut1));
			}
		}

		if(sp.getBoolean("saveResult", false)){
			String text = sp.getString("saveResultText", null);
			if(text != null){
				int i = 0;
				StringBuilder ex = new StringBuilder();
				StringBuilder ans = new StringBuilder();
				while(i < text.length() && text.charAt(i) != ';'){
					ex.append(text.charAt(i));
					i++;
				}
				TextView ver = findViewById(R.id.ExampleStr);
				ver.setSelectAllOnFocus(false);
				ver.setText(ex.toString());
				ver.setSelected(false);
				show_str();
				equallu("all");
				format(R.id.ExampleStr);
			}
		}
		boolean offer_to_rate = sp.getBoolean("offer_was_showed", false);
		int offers_count = sp.getInt("offers_count", 0);
		if(!offer_to_rate){
			if(offers_count == 10){
				offers_count = 0;
				AlertDialog rate;
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
						.setTitle(R.string.please_rate_out_app)
						.setMessage(R.string.rate_message)
						.setCancelable(false)
						.setPositiveButton(R.string.rate, (dialog, i) -> {
							Toast.makeText(this, getResources().getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
							sp.edit().putBoolean("offer_was_showed", true).apply();
							Intent go_to = new Intent(Intent.ACTION_VIEW);
							go_to.setData(Uri.parse(getResources().getString(R.string.link_app_in_google_play)));
							startActivity(go_to);
							dialog.cancel();
						})
						.setNegativeButton(R.string.no_thanks, ((dialog, which) -> {
							Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show();
							sp.edit().putBoolean("offer_was_showed", true).apply();
							dialog.cancel();
						}))
						.setNeutralButton(R.string.later, ((dialog, which) -> {
							Toast.makeText(this, R.string.we_will_wait, Toast.LENGTH_SHORT).show();
							dialog.cancel();
						}));

				rate = builder.create();
				Window rateWindow = rate.getWindow();
				if(rateWindow != null) {
					if (DarkMode)
						rateWindow.setBackgroundDrawableResource(R.drawable.grey);
				}
				rate.show();
			}else{
				offers_count++;
			}
			sp.edit().putInt("offers_count", offers_count).apply();
		}

		//setViewPager(0);
	}


	private void showSidebarGuide(){
		boolean guideShowed = sp.getBoolean("sidebar_guide_showed", false);
		if(!guideShowed){
			AlertDialog alertDialog;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
					.setTitle(R.string.important_information)
					.setMessage(Html.fromHtml(getResources().getString(R.string.sidebar_guide_message)))
					.setCancelable(false)
					.setPositiveButton("OK", (dialog, which) -> {
						sp.edit().putBoolean("sidebar_guide_showed", true).apply();
						dialog.cancel();
					});
			alertDialog = builder.create();
			Window window = alertDialog.getWindow();
			if(window != null){
				if(DarkMode)
					window.setBackgroundDrawableResource(R.drawable.grey);

				Button btn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				if(btn != null)
					btn.setTextColor(getResources().getColor(R.color.colorAccent));
			}
			alertDialog.show();
		}
	}

	String APPTYPE = BuildConfig.APPTYPE;

	private void showWhatNewWindow(){

		try {
			if (!sp.getBoolean(BuildConfig.VERSION_NAME + ".VER.SHOWED", false)
					&& APPTYPE.equals("stable")
					&& BuildConfig.WhatNewIsExisting) {
				AlertDialog window;
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(false)
						.setMessage(R.string.what_new_window_text)
						.setTitle(R.string.important)
						.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel()).setPositiveButton(R.string.view, ((dialog, which) -> {
					Intent in = new Intent(Intent.ACTION_VIEW);
					in.setData(Uri.parse("https://newmcalc.maxsav.team/what-new/#" + BuildConfig.VERSION_NAME));
					startActivity(in);
				}));
				window = builder.create();
				Window alertWindow = window.getWindow();
				if(alertWindow != null) {
					if (DarkMode) {
						alertWindow.setBackgroundDrawableResource(R.drawable.grey);
					}
				}
				Utils.recolorAlertDialogButtons(window, this);
				//window.getButton(1).setTextColor(getResources().getColor(R.color.colorAccent));
				Map<String, ?> m = sp.getAll();
				for (Map.Entry<String, ?> e : m.entrySet()) {
					if (!e.getKey().equals(BuildConfig.VERSION_NAME + ".VER.SHOWED")
							&& e.getKey().contains(".VER.SHOW")) {
						sp.edit().remove(e.getKey()).apply();
					}
				}
				sp.edit().putBoolean(BuildConfig.VERSION_NAME + ".VER.SHOWED", true).apply();
				window.show();
			}
		}catch (Exception e){
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Can receive 5 types: settings (go to Settings), numgen (go to Number Generator)<br>
	 *     history (go to History), pass (go to Password Generator) <br>
	 *         and bin (go to Number Systems Converter)
	 * */
	private void goToAdditionalActivities( @NonNull String where){
		switch (where) {
			case "settings":
				goToActivity(Settings.class, new Intent()
						.putExtra("action", "simple")
						.putExtra("start_type", "app"));
				break;
			case "numgen":
				goToActivity(NumberPasswordGeneratorActivity.class, new Intent()
						.putExtra("type", "number")
						.putExtra("start_type", "app"));
				break;
			case "history":
				goToActivity(History.class, new Intent()
						.putExtra("start_type", "app"));
				break;
			case "pass":
				goToActivity(NumberPasswordGeneratorActivity.class, new Intent()
						.putExtra("start_type", "app")
						.putExtra("type", "pass"));
				break;
			case "bin":
				goToActivity(NumberSystemConverterActivity.class, new Intent()
						.putExtra("start_type", "app"));
				break;
		}
	}

	private void setTextViewAnswerTextSizeToDefault(){
		TextView t = findViewById(R.id.AnswerStr);
		t.setText("");
		t.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32);
		t.setTextColor(DarkMode ? Color.WHITE : Color.BLACK);
	}

	private void restartActivity(){
		Intent in = new Intent(this, Main2Activity.class);
		this.startActivity(in);
		this.finish();
	}

	private void unregisterAllBroadcasts(){
		for(BroadcastReceiver broadcastReceiver : registeredBroadcasts){
			unregisterReceiver(broadcastReceiver);
		}
		isBroadcastsRegistered = false;
		registeredBroadcasts = new ArrayList<>();
	}

	private void format(int id){
		TextView t = findViewById(id);
		String txt = t.getText().toString();
		if(txt.equals("") || txt.length() < 4)
			return;
		t.setText(Format.format(txt));
	}

	@Override
	protected void onPause() {
		if(!isOtherActivityOpened)
			unregisterAllBroadcasts();
		super.onPause();
	}

	@Override
	protected void onResume() {
		isOtherActivityOpened = false;
		if(!isBroadcastsRegistered){
			registerBroadcastReceivers();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		if(!isOtherActivityOpened)
			unregisterAllBroadcasts();
		super.onStop();
	}

	@Override
	protected void onNightModeChanged(int mode) {
		if(AppCompatDelegate.MODE_NIGHT_YES == mode && !sp.getBoolean("force_enable_white",false)){
			sp.edit().putBoolean("dark_mode", true).apply();
			restartActivity();
		}else if(AppCompatDelegate.MODE_NIGHT_NO == mode && !sp.getBoolean("force_enable_dark", false)){
			sp.edit().putBoolean("dark_mode", false).apply();
			restartActivity();
		}
		super.onNightModeChanged(mode);
	}

	private void registerBroadcastReceivers(){
		BroadcastReceiver on_memory_edited = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				memoryEntries = mMemorySaverReader.read();
			}
		};
		registerReceiver(on_memory_edited,
				new IntentFilter(BuildConfig.APPLICATION_ID + ".MEMORY_EDITED"));
		registeredBroadcasts.add(on_memory_edited);

		BroadcastReceiver on_var_edited = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				setViewPager(1);
			}
		};
		registerReceiver(on_var_edited,
				new IntentFilter(BuildConfig.APPLICATION_ID + ".VARIABLES_SET_CHANGED"));
		registeredBroadcasts.add(on_var_edited);

		BroadcastReceiver on_recall_mem = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				addStringExampleToTheExampleStr(intent.getStringExtra("value"));
			}
		};
		registerReceiver(on_recall_mem,
				new IntentFilter(BuildConfig.APPLICATION_ID + ".RECALL_MEM"));
		registeredBroadcasts.add(on_recall_mem);

		BroadcastReceiver on_his_action = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				t = findViewById(R.id.ExampleStr);
				if(!intent.getBooleanExtra("error", false)) {
					String example = intent.getStringExtra("example");
					if (example != null && !example.equals("")) {
						String result = intent.getStringExtra("result");

						if (t.getText().toString().equals("")) t.setText(example);
						else addStringExampleToTheExampleStr(result);

						equallu("not");
					}
				}
			}
		};
		registerReceiver(on_his_action, new IntentFilter(BuildConfig.APPLICATION_ID + ".HISTORY_ACTION"));
		registeredBroadcasts.add(on_his_action);
		isBroadcastsRegistered = true;
	}

	private void addStringExampleToTheExampleStr(String value){
		TextView t = findViewById(R.id.ExampleStr);
		String txt = t.getText().toString();
		if(txt.equals("")) {
			t.setText(value);
			show_str();
			hide_ans();
			format(R.id.ExampleStr);
			resizeText();
		}else{
			char last = txt.charAt(txt.length() - 1);
			if(new BigDecimal(value).signum() < 0)
				value = "(" + value + ")";
			if(Utils.isDigit(last) || last == '%' || last == '!'
					|| Character.toString(last).equals(FI)
					|| Character.toString(last).equals(PI) || last == 'e' || last == ')'){
				t.setText(String.format("%s%s%s", txt, MULTIPLY_SIGN, value));
				equallu("not");
			}else{
				t.setText(String.format("%s%s", txt, value));
				equallu("not");
			}
			format(R.id.ExampleStr);
			resizeText();
		}
	}

	private void setViewPager(int which){
		try {
			FragmentOneInitializationObject fragmentOneInitializationObject =
					new FragmentOneInitializationObject()
							.setContext(this)
							.setLongClickListeners(new View.OnLongClickListener[]{
									mForAdditionalBtnsLongClick,
									returnback,
									btnDeleteSymbolLongClick
							});
			FragmentTwoInitializationObject fragmentTwoInitializationObject =
					new FragmentTwoInitializationObject()
							.setContext(this)
							.setLongClickListeners(
									new View.OnLongClickListener[]{
											memory_actions,
											on_var_long_click
									}
							);
			FragmentAdapterInitializationObject initializationObject =
					new FragmentAdapterInitializationObject()
							.setFragmentManager(getSupportFragmentManager())
							.setContext(this)
							.setFragmentTwoInitializationObject(fragmentTwoInitializationObject)
							.setFragmentOneInitializationObject(fragmentOneInitializationObject);
			MyFragmentPagerAdapter myFragmentPagerAdapter =
					new MyFragmentPagerAdapter(initializationObject);
			//}
			mViewPager = findViewById(R.id.viewpager);
			ViewGroup.LayoutParams lay = mViewPager.getLayoutParams();
			lay.height = displaySize.y / 2;
			mViewPager.setLayoutParams(lay);
			mViewPager.setAdapter(myFragmentPagerAdapter);
			mViewPager.setCurrentItem(which);
			Space space = findViewById(R.id.space_between_pager_and_str);
			ViewGroup.LayoutParams spaceLayoutParams = space.getLayoutParams();
			spaceLayoutParams.height = displaySize.y / 11;
			space.setLayoutParams(spaceLayoutParams);
		}catch (Exception e){
			Toast.makeText(this, "at setViewPager\n" + e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	public void onClick(@NonNull View v){
		if(v.getId() == R.id.btnCalc){
			equallu("all");
		}else {
			Button btn = findViewById(v.getId());
			t = findViewById(R.id.ExampleStr);
			String btntxt = btn.getText().toString().substring(0, 1);
			appendToExampleString(btntxt);
		}
	}

	View.OnLongClickListener on_var_long_click = v -> {
		Button btn = (Button) v;
		int pos = Integer.parseInt(btn.getTag().toString());
		Intent in = new Intent();
		in.putExtra("action", "add_var").putExtra("tag", pos).putExtra("is_existing", true);
		ArrayList<MyTuple<Integer, String, String>> a = Utils.readVariables(Main2Activity.this);
		if(a != null) {
			for (int i = 0; i < a.size(); i++) {
				if (a.get(i).first == pos) {
					in.putExtra("name", btn.getText().toString()).putExtra("value", a.get(i).third);
					break;
				}
			}
			goToActivity(CatchService.class, in);
		}
		return true;
	};

	boolean isSpecific(char last){
		return last == ')' || last == '!' || last == '%' || Character.toString(last).equals(PI) || Character.toString(last).equals(FI) || last == 'e';
	}

	View.OnLongClickListener mForAdditionalBtnsLongClick = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			TextView t = findViewById(R.id.ExampleStr);
			appendToExampleString(((Button) view).getText().toString().substring(1));
			return true;
		}
	};

	private View.OnLongClickListener returnback = v -> {
		if(!was_error) {
			TextView back = findViewById(R.id.AnswerStr), str = findViewById(R.id.ExampleStr);
			if (back.getVisibility() == View.INVISIBLE || back.getVisibility() == View.GONE) {
				return false;
			}
			String txt = back.getText().toString();
			back.setText(str.getText().toString());
			str.setText(txt);
			scrollExampleToEnd();
			return true;
		}else
			return false;
	};

	private View.OnLongClickListener btnDeleteSymbolLongClick = (View v) -> {
		deleteExample(findViewById(R.id.btnDelAll));
		return true;
	};

	enum EnterModes{
		SIMPLE,
		AVERAGE,
		GEOMETRIC
	}

	EnterModes exampleEnterMode = EnterModes.SIMPLE;

	public void deleteExample(View v){
		exampleEnterMode = EnterModes.SIMPLE;
		TextView t = findViewById(R.id.ExampleStr);
		hide_str();
		t.setText("");
		setTextViewAnswerTextSizeToDefault();
		hide_ans();
		was_error = false;
		sp.edit().remove("saveResultText").apply();
		setTextViewsTextSizeToDefault();
	}

	public void resizeText(){
		TextView txt = findViewById(R.id.ExampleStr);
		TextView t = findViewById(R.id.AnswerStr);

		String stri = txt.getText().toString(), strians = t.getText().toString();
		Rect bounds = new Rect();
		Rect boundsans = new Rect();
		Paint textPaint = txt.getPaint();
		Paint PaintAns = t.getPaint();
		textPaint.getTextBounds(stri, 0, stri.length(), bounds);
		PaintAns.getTextBounds(strians, 0, strians.length(), boundsans);
		int twidth = bounds.width();
		Display dis = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		dis.getSize(size);
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 46, getResources().getDisplayMetrics());
		int width = size.x - px - 45, answidth = size.x - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54, getResources().getDisplayMetrics());
		while(twidth >= width && txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity > 32){
			txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 1);
			if(boundsans.width() >= answidth)
				t.setTextSize(TypedValue.COMPLEX_UNIT_SP, t.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 1);
			textPaint.getTextBounds(stri, 0, stri.length(), bounds);
			PaintAns.getTextBounds(strians, 0, strians.length(), boundsans);
			twidth = bounds.width();
		}
		while(bounds.width() >= answidth && t.getTextSize() / getResources().getDisplayMetrics().scaledDensity > 29){
			t.setTextSize(TypedValue.COMPLEX_UNIT_SP, t.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 1);
			PaintAns.getTextBounds(strians, 0, strians.length(), boundsans);
		}
		if (twidth < width && txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity < 46){
			txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, txt.getTextSize() / getResources().getDisplayMetrics().scaledDensity + 1);
			t.setTextSize(TypedValue.COMPLEX_UNIT_SP, t.getTextSize() / getResources().getDisplayMetrics().scaledDensity + 1);
			textPaint.getTextBounds(stri, 0, stri.length(), bounds);
		}
	}

	private void setTextViewsTextSizeToDefault(){
		TextView txt = findViewById(R.id.ExampleStr);
		TextView t = findViewById(R.id.AnswerStr);
		txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 46);
		t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
	}

	private View.OnLongClickListener memory_actions = (View v) -> {
		if(v.getId() == R.id.btnMR) showMemAlert("rc");
		else showMemAlert("st");
		return true;
	};

	private void equallu(String type){
		if(was_error){
			setTextViewAnswerTextSizeToDefault();
			hide_ans();
		}
		TextView txt = findViewById(R.id.ExampleStr);
		String example = txt.getText().toString();
		format(R.id.ExampleStr);
		int len = example.length();
		if(len != 0) {
			show_str();
			scrollExampleToEnd();
		}else {
			return;
		}
		char last = example.charAt(len - 1);
		if(!Utils.isDigit(last) && last != ')' && last != '!' && last != '%'
				&& !Character.toString(last).equals(FI) && !Character.toString(last).equals(PI) && last != 'e') {
			hide_ans();
			return;
		}
		if(last == '(' || Utils.isNumber(example)) {
			hide_ans();
			return;
		}
		int brackets = 0;
		if(example.contains("(") || example.contains(")")) {
			for (int i = 0; i < example.length(); i++) {
				if (example.charAt(i) == '(')
					brackets++;
				else if (example.charAt(i) == ')')
					brackets--;
			}
			if (brackets > 0) {
				for (int i = 0; i < brackets; i++) {
					example = String.format("%s%s", example, ")");
				}
			} else if (brackets < 0) {
				//was_error = true;
				//onError(new Error().setStatus("Core"));
				return;
			}
		}

		resizeText();

		was_error = false;
		original = example;
		mCoreMain.prepareAndRun(example, type);
	}

	@Override
	public void onSuccess(CalculationResult calculationResult) {
		if(calculationResult.getResult() != null) {
			writeCalculationResult(calculationResult.getType(), calculationResult.getResult());
		}else{
			hide_ans();
		}
	}

	@Override
	public void onError(Error error) {
		if(!error.getStatus().equals("Core")) {
			if(error.getShortError().equals("")){
				Toast t = Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG);
				t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
				t.show();
			}else {
				writeCalculationError(error.getShortError());
			}
		}
	}

	private void writeCalculationError(String text){
		TextView t = findViewById(R.id.AnswerStr);
		hide_ans();
		t.setText(text);
		t.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32);
		t.setTextColor(Color.parseColor("#FF4B32"));
		show_ans();
		was_error = true;
	}

	private void writeCalculationResult(String type, BigDecimal result){
		String ans = result.toPlainString();
		if(ans.contains(".")) {
			if (ans.length() - ans.indexOf(".") > 9){
				BigDecimal d = result;
				d = d.divide(BigDecimal.ONE, 8, RoundingMode.HALF_EVEN);
				ans = Utils.deleteZeros(d.toPlainString());
			}
		}
		switch (type) {
			case "all":
				TextView tans = findViewById(R.id.ExampleStr);
				tans.setText(ans);
				format(R.id.ExampleStr);
				tans = findViewById(R.id.AnswerStr);
				tans.setText(original);
				show_ans();
				show_str();
				resizeText();

				String his = sp.getString("history", null);
				String formattedResult = Format.format(result.toPlainString());
				if(his != null){
					if(sp.getInt("local_history_storage_protocol_version", 1) < Constants.HISTORY_STORAGE_PROTOCOL_VERSION){
						Toast.makeText(this, "The record was not saved because the format of the history record does not match the new format." +
								" To fix this, go to the \"History\" section and in the window that appears, click the \"OK\" button.",
								Toast.LENGTH_LONG).show();
					}else {
						if (!his.startsWith(String.format("%s%c%s", original, ((char) 30), formattedResult))) {
							his = String.format("%s%c%s%c%s", original, ((char) 30), formattedResult, ((char) 29), his);
							sp.edit().putString("history", his).apply();
						}
					}
				}else{
					his = String.format("%s%c%s%c", original, ((char) 30), formattedResult, ((char) 29));
					sp.edit().putString("history", his).apply();
					sp.edit().putInt("local_history_storage_protocol_version", Constants.HISTORY_STORAGE_PROTOCOL_VERSION).apply();
				}

				if (sp.getBoolean("saveResult", false))
					sp.edit().putString("saveResultText", original + ";" + result.toPlainString()).apply();

				scrollExampleToEnd(HorizontalScrollView.FOCUS_LEFT);
				break;
			case "not":
				TextView preans = findViewById(R.id.AnswerStr);
				preans.setText(ans);
				show_str();
				format(R.id.AnswerStr);
				show_ans();
				setTextViewsTextSizeToDefault();
				resizeText();
				scrollExampleToEnd();
				break;
			default:
				throw new IllegalArgumentException("Arguments should be of two types: all, not");
		}
	}

	public void variableClick(View v){
		try {
			Button btn = (Button) v;
			int pos = Integer.parseInt(btn.getTag().toString());
			String text = btn.getText().toString();
			if (text.equals("+")) {
				Intent in = new Intent();
				in.putExtra("action", "add_var").putExtra("tag", pos);
				TextView t = findViewById(R.id.ExampleStr);
				String ts = t.getText().toString();
				if(!ts.equals("") && ts.length() < 1000){
					ts = Utils.deleteSpaces(ts);
					if(Utils.isNumber(ts))
						in.putExtra("value", ts).putExtra("name", "").putExtra("is_existing", true);
				}
				goToActivity(CatchService.class, in);
			} else {
				String var_arr = sp.getString("variables", null);
				if (var_arr == null) {
					btn.setText("+");
				} else {
					ArrayList<MyTuple<Integer, String, String>> a = Utils.readVariables(this);
					if (a != null) {
						for(int i = 0; i < a.size(); i++){
							if(a.get(i).first == pos){
								addStringExampleToTheExampleStr(a.get(i).third);
								//break;
								return;
							}
						}
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	private void showMemAlert(final String type){
		Intent in = new Intent();
		in.putExtra("type", type);
		if(type.equals("st")){
			TextView t = findViewById(R.id.ExampleStr);
			if(Utils.isNumber(t.getText().toString()))
				in.putExtra("value", t.getText().toString());
			else return;
		}
		goToActivity(MemoryActionsActivity.class, in);
	}

	int countOfMemoryPlusMinusMethodCalls = 0;

	public void onMemoryPlusMinusButtonsClick(View v){
		TextView textExample = findViewById(R.id.ExampleStr);
		String text = textExample.getText().toString();
		if(text.equals(""))
			return;

		BigDecimal temp;
		if(!Utils.isNumber(text)){
			equallu("all");
			if(countOfMemoryPlusMinusMethodCalls < 1){
				countOfMemoryPlusMinusMethodCalls++;
				onMemoryPlusMinusButtonsClick(v);
			}
			return;
		}else{
			temp = new BigDecimal(Utils.deleteSpaces(text));
		}
		if(v.getId() == R.id.btnMemPlus) {
			temp = temp.add(memoryEntries[0]);
		}else if(v.getId() == R.id.btnMemMinus){
			temp = memoryEntries[0].subtract(temp);
		}
		if(countOfMemoryPlusMinusMethodCalls > 0)
			returnback.onLongClick(findViewById(R.id.btnCalc));
		memoryEntries[0] = temp;
		mMemorySaverReader.save(memoryEntries);
	}

	int countOfMemoryStoreMethodCalls = 0;

	public void onMemoryStoreButtonClick(View view){
		TextView t = findViewById(R.id.ExampleStr);
		String txt = t.getText().toString();
		if(txt.equals(""))
			return;
		BigDecimal temp;
		if(!Utils.isNumber(txt)){
			equallu("all");
			if (countOfMemoryStoreMethodCalls < 1) {
				countOfMemoryStoreMethodCalls++;
				onMemoryStoreButtonClick(view);
			}
			return;
		}else{
			temp = new BigDecimal(Utils.deleteSpaces(txt));
		}
		if(countOfMemoryStoreMethodCalls > 0)
			returnback.onLongClick(findViewById(R.id.btnCalc));
		countOfMemoryStoreMethodCalls = 0;
		memoryEntries[0] = temp;
		mMemorySaverReader.save(memoryEntries);

	}

	public void onMemoryRecallButtonClick(View view){
		String value = memoryEntries[0].toString();
		addStringExampleToTheExampleStr(value);
	}

	private void goToActivity(Class <?> cls, Intent possibleExtras){
		Intent intent = new Intent(this, cls);
		if (possibleExtras != null && possibleExtras.getExtras() != null) {
			intent.putExtras(possibleExtras.getExtras());
		}
		isOtherActivityOpened = true;
		startActivity(intent);
		overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
	}

	private void show_str(){
		TextView t = findViewById(R.id.ExampleStr);
		//t.setTextIsSelectable(false);
		t.setVisibility(View.VISIBLE);
	}
	private void hide_str(){
		TextView t = findViewById(R.id.ExampleStr);
		t.setVisibility(View.INVISIBLE);
	}

	private void show_ans(){
		TextView t = findViewById(R.id.AnswerStr);
		t.setVisibility(View.VISIBLE);
	}
	private void hide_ans(){
		TextView t = findViewById(R.id.AnswerStr);
		t.setVisibility(View.INVISIBLE);
	}

	private void check_dot(){
		TextView t = findViewById(R.id.ExampleStr);
		String txt = t.getText().toString();
		int i = txt.length()-1;
		if(!Utils.isDigit(txt.charAt(i))){
			return;
		}
		boolean dot = false;
		while(i >= 0 && (Utils.isDigit(txt.charAt(i)) || txt.charAt(i) == '.')){
			if(txt.charAt(i) == '.'){
				dot = true;
				break;
			}else{
				i--;
			}
		}
		if(!dot){
			t.setText(String.format("%s.", txt));
		}
	}

	@SuppressLint("SetTextI18n")
	public void appendToExampleString(String btntxt){
		t = findViewById(R.id.ExampleStr);
		String txt = t.getText().toString();
		int len = txt.length();
		if(len >= 30000)
			return;
		if(len != 0)
			last = txt.charAt(len-1);

		int brackets = 0;
		if(txt.contains("(") || txt.contains(")")) {
			for (int i = 0; i < len; i++) {
				if (txt.charAt(i) == '(')
					brackets++;
				else if (txt.charAt(i) == ')')
					brackets--;
			}
		}

		if(len == 0 && btntxt.equals(")"))
			return;

		if(btntxt.equals("A")){
			if(len == 0){
				t.setText(btntxt + "(");
				equallu("not");
				exampleEnterMode = EnterModes.AVERAGE;
				return;
			}
			if(exampleEnterMode != EnterModes.SIMPLE)
				return;

			if(Utils.isDigit(last) || isSpecific(last)){
				t.setText(txt + MULTIPLY_SIGN + btntxt + "(");
				equallu("not");
				exampleEnterMode = EnterModes.AVERAGE;
			}else if(!Utils.isDigit(last) && !isSpecific(last)){
				t.setText(txt + btntxt + "(");
				equallu("not");
				exampleEnterMode = EnterModes.AVERAGE;
			}
		}else if(btntxt.equals("G")){
			if(len == 0){
				t.setText(btntxt + "(");
				equallu("not");
				exampleEnterMode = EnterModes.GEOMETRIC;
				return;
			}
			if(exampleEnterMode != EnterModes.SIMPLE)
				return;

			if(Utils.isDigit(last) || isSpecific(last)){
				t.setText(txt + MULTIPLY_SIGN + btntxt + "(");
				equallu("not");
				exampleEnterMode = EnterModes.GEOMETRIC;
			}else if(!Utils.isDigit(last) && !isSpecific(last)){
				t.setText(txt + btntxt + "(");
				equallu("not");
				exampleEnterMode = EnterModes.GEOMETRIC;
			}
		}
		if(exampleEnterMode != EnterModes.SIMPLE){
			if(btntxt.equals(")")){
				if(Utils.isDigit(last)){
					t.setText(txt + btntxt);
					equallu("not");
				}else{
					txt = txt.substring(0, txt.length() - 1);
					t.setText(txt + btntxt);
					equallu("not");
				}
				exampleEnterMode = EnterModes.SIMPLE;
				return;
			}
			if (exampleEnterMode == EnterModes.AVERAGE && (btntxt.length() > 1 || (!btntxt.equals("+") && !btntxt.equals("."))) && !Utils.isDigit(btntxt.charAt(0))) {
				return;
			}
			if (exampleEnterMode == EnterModes.GEOMETRIC && (btntxt.length() > 1 || (!btntxt.equals(MULTIPLY_SIGN) && !btntxt.equals("."))) && !Utils.isDigit(btntxt.charAt(0))) {
				return;
			}
		}

		if(Utils.isDigit(btntxt)){
			if(len > 1 && last == '%'){
				t.setText(txt + MULTIPLY_SIGN + btntxt);
				equallu("not");
				return;
			}
			if(txt.equals("0")) {
				t.setText(btntxt);
				return;
			}
			if(len > 1){
				if(last == '0' && !Utils.isDigit(txt.charAt(len - 2)) && txt.charAt(len - 2) != '.'){
					txt = txt.substring(0, len - 1) + btntxt;
					t.setText(txt);
					equallu("not");
					return;
				}
			}else if (len == 0){
				t.setText(btntxt);
				show_str();
				return;
			}
		}

		if(btntxt.equals(")")){
			if(brackets > 0){
				if(last == ')'){
					t.setText(txt + btntxt);
					equallu("not");
					return;
				}
				if(last == '(')
					return;
				if(!Utils.isDigit(last) && !Character.toString(last).equals(getResources().getString(R.string.pi))
						&& !Character.toString(last).equals(getResources().getString(R.string.fi)) && last != 'e' && last != '!' && last != '%'){
					if(len != 1){
						txt = txt.substring(0, len-1);
						t.setText(txt + btntxt);
						equallu("not");
					}
				}else{
					t.setText(txt + btntxt);
					equallu("not");
				}
			}
			return;
		}

		if(btntxt.equals(FI) || btntxt.equals(PI) || btntxt.equals("e")){
			if(len == 0){
				t.setText(btntxt);
				equallu("not");
				return;
			}else{
				if(last == ')'){
					t.setText(txt + MULTIPLY_SIGN + btntxt);
				}else {
					if (!Utils.isDigit(txt.charAt(len - 1))) {
						if (txt.charAt(len - 1) != '.') {
							t.setText(txt + btntxt);
							equallu("not");
							return;
						}
					} else {
						t.setText(txt + btntxt);
						scrollExampleToEnd();
					}
				}
			}
			return;
		}

		if(btntxt.equals("^")){
			if(len == 0 || last == '(')
				return;

			if(Utils.isDigit(last) || Character.toString(last).equals(FI) || Character.toString(last).equals(PI) || last == 'e'){
				t.setText(txt + btntxt + "(");
				equallu("not");
				return;
			}
			if(!Utils.isDigit(last)){
				if(last == '!' || last == '%'){
					t.setText(txt + btntxt + "(");
					equallu("not");
				}
			}
			return;
		}

		if(btntxt.equals("√")){
			if(len == 0){
				t.setText(btntxt + "(");
				show_str();
				scrollExampleToEnd();
				return;
			}else{
				String x = "";
				for(int i = 0; i < len; i++){
					if(!Utils.isDigit(txt.charAt(i)) && txt.charAt(i) != '.' && txt.charAt(i) != ' '){
						break;
					}else{
						if(Utils.isDigit(txt.charAt(i)) || txt.charAt(i) == '.' || txt.charAt(i) == ' ')
							x = String.format("%s%c", x, txt.charAt(i));
					}
				}
				if(x.length() == len){
					x = Utils.deleteSpaces(x);
					x = Utils.deleteZeros(x);
					t.setText(btntxt + "(" + x + ")");
					equallu("all");
					return;
				}else{
					if(last == '.'){
						return;
					}else{
						if(!Utils.isDigit(last)){
							t.setText(txt + btntxt + "(");
							equallu("not");
							show_str();
							scrollExampleToEnd();
						}else{
							if(Utils.isDigit(last) || isSpecific(last)){
								t.setText(txt + MULTIPLY_SIGN + btntxt + "(");
								equallu("not");
								show_str();
								scrollExampleToEnd();
							}
						}
					}
				}
			}
			return;
		}
		if(btntxt.equals("(")){
			if(last == '.')
				return;
			if((Utils.isDigit(last) || last == '!' || last == '%' || Utils.isConstNum(last, this) || last == ')') && !txt.equals("")){
				t.setText(txt + MULTIPLY_SIGN + btntxt);
				equallu("not");
			}else{
				t.setText(txt + btntxt);
				equallu("not");
			}
			return;
		}
		if(btntxt.equals("sin") || btntxt.equals("log") || btntxt.equals("tan") || btntxt.equals("cos") || btntxt.equals("ln")){
			if(len == 0){
				t = findViewById(R.id.ExampleStr);
				t.setText(btntxt + "(");
				equallu("not");
			}else{
				if(last == '.'){
					return;
				}else{
					if(!Utils.isDigit(last) && last != '!' && !Character.toString(last).equals(FI) && !Character.toString(last).equals(PI) && last != 'e' && last != ')'){
						t.setText(txt + btntxt + "(");
						equallu("not");
					}else {
						if (last != '(' && last != '^') {
							t.setText(txt + MULTIPLY_SIGN + btntxt + "(");
							equallu("not");
						}
					}
				}
			}
			return;
		}

		if(len != 0 && last == '(' && btntxt.equals("-")){
			t.setText(txt + btntxt);
			equallu("not");
			return;
		}

		if(!Utils.isDigit(btntxt) && !Utils.islet(btntxt.charAt(0))){
			if(len != 0){
				if(txt.charAt(len-1) == 'π' ||
						txt.charAt(len-1) == 'φ' ||
						txt.charAt(len-1) == 'e'){
					t.setText(txt + btntxt);
					equallu("not");
					return;
				}
			}
		}

		if((last == '!' || last == '%') && !btntxt.equals(".") && !btntxt.equals("!") && !btntxt.equals("%")){
			t.setText(txt + btntxt);
			equallu("not");
			return;
		}
		if(btntxt.equals("+") || btntxt.equals("-")
				|| btntxt.equals(getResources().getString(R.string.multiply))
				|| btntxt.equals(getResources().getString(R.string.div))){
			if(last == '(' && !btntxt.equals("-")) {
				return;
			}else if(last == '('){
				t.setText(txt + btntxt);
				equallu("not");
				return;
			}
		}

		if(btntxt.equals(".")){
			if(txt.equals(""))
				t.setText("0.");
			else
			if(!Utils.isDigit(txt.charAt(len-1)) && txt.charAt(len-1) != '.' && last != '!')
				t.setText(txt + "0.");
			else
				check_dot();
		}else{
			if(btntxt.equals("!") || btntxt.equals("%")){
				if(!txt.equals("")){
					if(last == '(')
						return;
					if(btntxt.equals("!")){
						if(last == '!'){
							if(txt.charAt(len - 2) != '!'){
								t.setText(txt + btntxt);
								equallu("not");
								return;
							}
						}else if(Utils.isDigit(last)){
							t.setText(txt + btntxt);
							equallu("not");
							return;
						}
					}else{
						if(last != '%' && (Utils.isDigit(last) || last == ')')){
							t.setText(txt + btntxt);
							equallu("not");
							return;
						}
					}
					if(len > 1) {
						String s = Character.toString(txt.charAt(len - 2));
						if (last != ')' && !Utils.isDigit(last) && (len > 1 && last != '(' && Utils.islet(txt.charAt(len - 2))
								&& !s.equals(PI) && !s.equals(FI) && txt.charAt(len - 2) != 'e')) {
							txt = txt.substring(0, len - 1);
							t.setText(txt + btntxt);
							equallu("not");
						} else {
							if (Utils.isDigit(last) || last == ')') {
								t.setText(txt + btntxt);
								equallu("not");
							}
						}
					}
				}
				return;
			}
			if(len >= 1 && (last == ')' && !Utils.isDigit(btntxt.charAt(0)))) {
				t.setText(txt + btntxt);
				equallu("not");
			}else{
				if(!txt.equals("")){
					if(!Utils.isDigit(btntxt.charAt(0))){

						if(!Utils.isDigit(txt.charAt(len-1))){
							if(len != 1){
								txt = txt.substring(0, len-1);
								t.setText(txt + btntxt);
								equallu("not");
							}
						}else{
							t.setText(txt + btntxt);
							equallu("not");
						}
					}else{
						if(Utils.isDigit(btntxt.charAt(0))){
							t.setText(txt + btntxt);
							equallu("not");
						}
					}
				}else{
					if(Utils.isDigit(btntxt.charAt(0)) || btntxt.equals("-")){
						t.setText(btntxt);
						equallu("not");
					}
				}
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		if(!BuildConfig.WhatNewIsExisting || !APPTYPE.equals("stable")){
			menu.removeItem(R.id.what_new);
		}
		return true;
	}

	/*@Override
	public boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}*/

	public void delSymbol(View v){
		TextView txt = findViewById(R.id.ExampleStr);
		String text = txt.getText().toString();
		int len = text.length();
		setTextViewAnswerTextSizeToDefault();
		if(len != 0){
			char last = text.charAt(len - 1);
			int a = 1;
			if(last == ')' && (text.contains("A") || text.contains("G"))){
				int i = len-1;
				while(i >= 1 && text.charAt(i) != '(')
					i--;
				i--;
				if(text.charAt(i) == 'A'){
					exampleEnterMode = EnterModes.AVERAGE;
				}else if(text.charAt(i) == 'G'){
					exampleEnterMode = EnterModes.GEOMETRIC;
				}
			}
			if(last == '(' && len > 1){
				if(text.charAt(text.length() - 2) == '√'
						|| text.charAt(len - 2) == '^') {
					a = 2;
				}
				if( text.charAt(len - 2) == 'A' || text.charAt(len-2) == 'G'){
					a = 2;
					exampleEnterMode = EnterModes.SIMPLE;
				}
				if(text.charAt(len - 2) == 's' || text.charAt(len - 2) == 'g')
					a = 4;
				if(text.charAt(len - 2) == 'n'){
					if(text.charAt(len - 3) == 'l')
						a = 3;
					else if(text.charAt(len - 3) == 'i' || text.charAt(len - 3) == 'a')
						a = 4;
				}
			}
			was_error = false;
			if(text.length() - a == 0)
				hide_str();
			text = text.substring(0, text.length() - a);
			txt.setText(text);
			equallu("not");
		}
		if(txt.getText().toString().equals("")){
			deleteExample(findViewById(R.id.btnDelAll));
		}
		scrollExampleToEnd();
	}

	private void scrollExampleToEnd(final int focus){
		if(findViewById(R.id.ExampleStr).getVisibility() == View.INVISIBLE)
			return;
		HorizontalScrollView scrollview = findViewById(R.id.scrollview);

		//scrollview.postDelayed(() -> scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT), 2L);
		scrollview.post(new Runnable() {
			@Override
			public void run() {
				scrollview.fullScroll(focus);
			}
		});
		//scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
		HorizontalScrollView scrollview1 = findViewById(R.id.scrollViewAns);
		//scrollview1.postDelayed(() -> scrollview1.fullScroll(HorizontalScrollView.FOCUS_RIGHT), 2L);
		scrollview1.post(new Runnable() {
			@Override
			public void run() {
				scrollview1.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		});
		//scrollview1.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
	}

	private void scrollExampleToEnd(){
		if(findViewById(R.id.ExampleStr).getVisibility() == View.INVISIBLE)
			return;
		HorizontalScrollView scrollview = findViewById(R.id.scrollview);

		//scrollview.postDelayed(() -> scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT), 2L);
		scrollview.post(new Runnable() {
			@Override
			public void run() {
				scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		});
		//scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
		HorizontalScrollView scrollview1 = findViewById(R.id.scrollViewAns);
		//scrollview1.postDelayed(() -> scrollview1.fullScroll(HorizontalScrollView.FOCUS_RIGHT), 2L);
		scrollview1.post(new Runnable() {
			@Override
			public void run() {
				scrollview1.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		});
		//scrollview1.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
	}
}
