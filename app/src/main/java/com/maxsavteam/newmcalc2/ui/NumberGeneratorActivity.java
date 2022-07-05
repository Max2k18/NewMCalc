package com.maxsavteam.newmcalc2.ui;import android.annotation.SuppressLint;import android.os.Bundle;import android.util.TypedValue;import android.view.Gravity;import android.view.animation.AnimationUtils;import android.widget.Button;import android.widget.EditText;import android.widget.TextSwitcher;import android.widget.TextView;import android.widget.Toast;import com.maxsavteam.newmcalc2.R;import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;import com.maxsavteam.newmcalc2.widget.FullNumpadView;import java.math.BigInteger;import java.util.Locale;import java.util.Random;public class NumberGeneratorActivity extends ThemeActivity {	private final Random mRandom = new Random();	@SuppressLint("SetTextI18n")	@Override	protected void onCreate(Bundle savedInstanceState) {		super.onCreate( savedInstanceState );		setContentView( R.layout.activity_numgen );		setActionBar( R.id.toolbar );		displayHomeAsUp();		TextSwitcher textSwitcher = findViewById( R.id.textSwitcherNumGen );		textSwitcher.setFactory( ()->{			TextView textView = new TextView( this );			textView.setTextColor( super.textColor );			textView.setTextSize( TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize( R.dimen.num_gen_answer_text_size ) );			textView.setGravity( Gravity.CENTER );			textView.setTextIsSelectable( true );			return textView;		} );		textSwitcher.setInAnimation( AnimationUtils.loadAnimation( this, android.R.anim.fade_in ) );		textSwitcher.setOutAnimation( AnimationUtils.loadAnimation( this, android.R.anim.fade_out ) );		EditText minInputEditText = findViewById( R.id.edTextMin );		EditText maxInputEditText = findViewById( R.id.edTextMax );		FullNumpadView numpadView = findViewById( R.id.numgen_numpad );		numpadView.linkWith( minInputEditText );		numpadView.linkWith( maxInputEditText );		Button buttonGenerate = new Button( this, null, 0, R.style.FullNumpadViewButtonsStyle );		buttonGenerate.setText( "GEN" );		buttonGenerate.setTextSize( TypedValue.COMPLEX_UNIT_SP, 20 );		numpadView.addCustomButton( buttonGenerate );		buttonGenerate.setOnClickListener( v -> generate() );	}	private void generate(){		EditText minInputEditText = findViewById( R.id.edTextMin );		EditText maxInputEditText = findViewById( R.id.edTextMax );		String maxS = maxInputEditText.getText().toString();		String minS = minInputEditText.getText().toString();		if ( !minInputEditText.getText().toString().equals( "" ) && !maxInputEditText.getText().toString().equals( "" ) ) {			BigInteger b1 = new BigInteger( minS );			BigInteger b2 = new BigInteger( maxS );			BigInteger lmax = BigInteger.valueOf( Long.MAX_VALUE );			BigInteger lMin = BigInteger.valueOf( Long.MIN_VALUE );			if ( b1.compareTo( lMin ) < 0 || b1.compareTo( lmax ) > 0 || b2.compareTo( lMin ) < 0 || b2.compareTo( lmax ) > 0 ) {				Toast.makeText( this, getString( R.string.value_is_too_big ), Toast.LENGTH_LONG ).show();				return;			}			int minNum = Integer.parseInt( minInputEditText.getText().toString() );			int maxNum = Integer.parseInt( maxInputEditText.getText().toString() );			if ( minNum > maxNum ) {				int temp = minNum;				minNum = maxNum;				maxNum = temp;			}			int randomNum = minNum + mRandom.nextInt( maxNum - minNum + 1 );			((TextSwitcher) findViewById( R.id.textSwitcherNumGen )).setText( String.format( Locale.ROOT, "%d", randomNum ) );		}	}}