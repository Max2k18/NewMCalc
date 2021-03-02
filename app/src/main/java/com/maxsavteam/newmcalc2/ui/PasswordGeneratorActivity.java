package com.maxsavteam.newmcalc2.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PasswordGeneratorActivity extends ThemeActivity {

	private final Random mRandom = new Random();

	@Override
	public void onBackPressed() {
		if(getIntent().getStringExtra( "type" ).equals( "shortcut" ))
			startActivity(new Intent(this, Main2Activity.class));
		super.onBackPressed();
		Utils.defaultActivityAnim( this );
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if(item.getItemId() == android.R.id.home)
			onBackPressed();
		return super.onOptionsItemSelected( item );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_password_generator );

		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );
		getSupportActionBar().setDisplayHomeAsUpEnabled( true );

		String[] stringArray = getResources().getStringArray( R.array.switches );
		for (int i = 0; i < mCheckBoxIds.length; i++) {
			int id = mCheckBoxIds[ i ];
			CheckBox checkBox = findViewById( id );
			checkBox.setText( stringArray[ i ] );
			checkBox.setTag( characters[ i ] );
		}

		int[] ids = new int[]{
				R.id.btnPass6,
				R.id.btnPass8,
				R.id.btnPass12,
				R.id.btnPass16
		};
		WindowManager windowManager = (WindowManager) getSystemService( Context.WINDOW_SERVICE );
		if ( windowManager != null ) {
			Display d = windowManager.getDefaultDisplay();
			Point size = new Point();
			d.getSize( size );
			for (int id : ids) {
				Button btn = findViewById( id );
				ViewGroup.LayoutParams lay = btn.getLayoutParams();
				lay.height = size.x / 5;
				lay.width = size.x / 5;
				btn.setLayoutParams( lay );
			}
		}
		setBackground();
		buttonOnClick( findViewById( R.id.btnPass8 ) );

		mCheckBoxes.add( findViewById( R.id.swCap ) );
		mCheckBoxes.add( findViewById( R.id.swLowerCase ) );
		mCheckBoxes.add( findViewById( R.id.swDigits ) );

		TextSwitcher textSwitcher = findViewById( R.id.textSwitcherPass );
		textSwitcher.setFactory( ()->{
			TextView textView = new TextView( this );
			textView.setTextColor( super.textColor );
			textView.setTextSize( TypedValue.COMPLEX_UNIT_SP, 30 );
			textView.setGravity( Gravity.CENTER );
			textView.setTextIsSelectable( true );
			return textView;
		} );
		textSwitcher.setInAnimation( AnimationUtils.loadAnimation( this, android.R.anim.fade_in ) );
		textSwitcher.setOutAnimation( AnimationUtils.loadAnimation( this, android.R.anim.fade_out ) );

		findViewById( R.id.btnGenPass ).setOnClickListener( v -> {
			if ( mPassLen != 0 ) {
				if ( mCheckBoxes.size() != 0 ) {
					String resource_str = "";
					for (CheckBox checkBox : mCheckBoxes) {
						resource_str = String.format( "%s%s", resource_str, checkBox.getTag() );
					}

					ArrayList<Character> characters = new ArrayList<>();
					for(int i = 0; i < resource_str.length(); i++){
						characters.add( resource_str.charAt( i ) );
					}
					Collections.shuffle( characters, mRandom );

					String pass = "";
					for (int i = 0, a; i < mPassLen; i++) {
						a = mRandom.nextInt(characters.size());
						pass = String.format( "%s%c", pass, characters.get( a ) );
					}
					textSwitcher.setText( pass );
				} else {
					Toast.makeText( this, getString( R.string.pass_sw_all_off ), Toast.LENGTH_LONG ).show();
				}
			}
		} );
	}

	private final int[] mCheckBoxIds = new int[]{
			R.id.swCap,
			R.id.swLowerCase,
			R.id.swDigits,
			R.id.swHyphen,
			R.id.swUnderScore,
			R.id.swSpace,
			R.id.swSpecial,
			R.id.swPar
	};

	private final String[] characters = new String[]{
			"MNBVCXZLKJHGFDSAPOIUYTREWQ",
			"qwertyuiopasdfghjklzxcvbnm",
			"0123456789",
			"-",
			"_",
			" ",
			"<$@>",
			"{[()]}"
	};

	private int mPassLen = 0;

	private final ArrayList<CheckBox> mCheckBoxes = new ArrayList<>();

	public void onSwClick(View v) {
		CheckBox sw = (CheckBox) v;
		if ( sw.isChecked() ) {
			mCheckBoxes.add( sw );
		} else {
			mCheckBoxes.remove( sw );
		}
	}

	public void buttonOnClick(View v) {
		Button btn = findViewById( v.getId() );
		setBackground();
		btn.setTextColor( super.windowBackgroundColor );
		btn.setBackgroundTintList( ColorStateList.valueOf( super.textColor ) );
		mPassLen = Integer.parseInt( btn.getText().toString() );
	}

	private void setBackground() {
		ArrayList<Button> b = new ArrayList<>();
		b.add( findViewById( R.id.btnPass6 ) );
		b.add( findViewById( R.id.btnPass8 ) );
		b.add( findViewById( R.id.btnPass12 ) );
		b.add( findViewById( R.id.btnPass16 ) );
		for(Button button : b){
			button.setTextColor( super.textColor );
			button.setBackgroundTintList( ColorStateList.valueOf( super.windowBackgroundColor ) );
		}
	}
}