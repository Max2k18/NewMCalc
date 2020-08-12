package com.maxsavteam.newmcalc2.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class PasswordGeneratorActivity extends ThemeActivity {

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
	}

	public void copy(View v){
		android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		if(clipboard != null){
			TextView t = findViewById(R.id.txtGenedPass);
			clipboard.setText(t.getText().toString());
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.copied), Toast.LENGTH_SHORT).show();
		}
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
		if ( super.isDarkMode ) {
			btn.setTextColor( getResources().getColor( R.color.black ) );
			btn.setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.white ) ) );
		} else {
			btn.setTextColor( getResources().getColor( R.color.white ) );
			btn.setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.black ) ) );
		}
		mPassLen = Integer.parseInt( btn.getText().toString() );
	}

	private void setBackground() {
		ArrayList<Button> b = new ArrayList<>();
		b.add( findViewById( R.id.btnPass6 ) );
		b.add( findViewById( R.id.btnPass8 ) );
		b.add( findViewById( R.id.btnPass12 ) );
		b.add( findViewById( R.id.btnPass16 ) );
		if ( super.isDarkMode ) {
			for (int i = 0; i < b.size(); i++) {
				b.get( i ).setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.black ) ) );
				b.get( i ).setTextColor( getResources().getColor( R.color.white ) );
			}
		} else {
			for (int i = 0; i < b.size(); i++) {
				b.get( i ).setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.white ) ) );
				b.get( i ).setTextColor( getResources().getColor( R.color.black ) );
			}
		}
	}

	public void genpass(View v) {
		if ( mPassLen != 0 ) {
			if ( mCheckBoxes.size() != 0 ) {
				String resource_str = "";
				for (CheckBox checkBox : mCheckBoxes) {
					resource_str = (String) ( resource_str + checkBox.getTag() );
				}

				String pass = "";
				for (int i = 0, a; i < mPassLen; i++) {
					a = ThreadLocalRandom.current().nextInt( 0, resource_str.length() );
					pass = String.format( "%s%s", pass, resource_str.charAt( a ) );
				}
				TextView t = findViewById( R.id.txtGenedPass );
				t.setText( pass );
				LinearLayout lay = findViewById( R.id.linearLayout2 );
				if ( lay.getVisibility() != View.VISIBLE ) {
					lay.setVisibility( View.VISIBLE );
					lay.animate().alpha( 1 ).setDuration( 100 ).start();
				}
			} else {
				Toast.makeText( this, getString( R.string.pass_sw_all_off ), Toast.LENGTH_LONG ).show();
			}
		}
	}
}