package com.maxsavteam.newmcalc2.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;

import java.math.BigDecimal;

public class NumberSystemConverterActivity extends ThemeActivity {
	private final String[] data = { "2", "8", "10", "16" };
	private String[] translated_from;
	private int fromSys = 10;
	private int toSys = 2;
	private EditText fromText;
	private EditText toText;

	private int positionInSet(int sys) {
		for (int i = 0; i < data.length; i++) {
			String s = data[ i ];
			if ( s.equals( Integer.toString( sys ) ) ) {
				return i;
			}
		}

		return 10;
	}

	private void applyTheme() {
		setActionBar( R.id.toolbar );
		displayHomeAsUp();

		EditText e = findViewById( R.id.edNumTo );
		e.setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.colorAccent ) ) );
		e = findViewById( R.id.edNumFrom );
		e.setBackgroundTintList( ColorStateList.valueOf( getResources().getColor( R.color.colorAccent ) ) );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_number_system );
		translated_from = new String[ data.length ];
		Spinner from = findViewById( R.id.chooseFromWhichSys );
		Spinner to = findViewById( R.id.chooseToWhichSys );

		applyTheme();
		ArrayAdapter<String> ar = new ArrayAdapter<>( this, android.R.layout.simple_spinner_item, data );
		ar.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

		from.setAdapter( ar );
		from.setSelection( 2 );
		from.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				int choice = Integer.parseInt( data[ position ] );
				if ( choice == toSys ) {
					toSys = fromSys;
					to.setSelection( positionInSet( toSys ) );
				}
				fromSys = choice;
				if ( fromSys <= 10 ) {
					fromText.setInputType( InputType.TYPE_CLASS_NUMBER );
				} else {
					fromText.setInputType( InputType.TYPE_CLASS_TEXT );
				}
				fromText.setText( translated_from[ positionInSet( fromSys ) ] );
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		} );
		to.setAdapter( ar );
		to.setSelection( 0 );
		to.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				//toSys = Integer.valueOf(data[position]);
				int choice = Integer.parseInt( data[ position ] );
				if ( choice == fromSys ) {
					fromSys = toSys;
					from.setSelection( positionInSet( fromSys ) );
					fromText.setText( translated_from[ positionInSet( fromSys ) ] );
				}
				toSys = choice;
				//toText.setText(translated_to[position_in_set(toSys)]);
				translate();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		} );

		fromText = findViewById( R.id.edNumFrom );
		toText = findViewById( R.id.edNumTo );
		toText.setTextIsSelectable( true );
		fromText.setFocusable( true );
		fromText.addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if ( count > 0 ) {
					char last = s.charAt( start );
					if ( ( fromSys == 16 && !( last >= '0' && last <= '9' ) && !( ( last >= 'a' && last <= 'f' ) || ( last >= 'A' && last <= 'F' ) ) )
							|| ( fromSys == 2 && !( last >= '0' && last <= '1' ) )
							|| ( fromSys == 10 && !( last >= '0' && last <= '9' ) )
							|| ( fromSys == 8 && !( last >= '0' && last <= '7' ) ) ) {
						if ( fromText.getText().length() == 1 ) {
							fromText.setText( "" );
							return;
						}
						int curPos = fromText.getSelectionEnd() - 1;
						fromText.setText( s.subSequence( 0, start ).toString().concat( s.subSequence( start + 1, s.length() ).toString() ) );
						if ( curPos > fromText.getText().length() ) {
							curPos = fromText.getText().length();
						}
						fromText.setSelection( curPos );
					}
				}
				translate();
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		} );
	}

	private void translate() {
		String from = fromText.getText().toString();
		if ( !from.equals( "" ) ) {
			String result;
			translated_from[ positionInSet( fromSys ) ] = from;
			if ( fromSys == 10 ) {
				result = from;
			} else {
				result = toDecimal( from, fromSys ).toPlainString();
			}

			result = fromDecimal( new BigDecimal( result ), toSys );
			toText.setText( result );
		} else {
			toText.setText( "" );
		}
	}

	private int returnNumber(char c) {
		char upperC = Character.toUpperCase( c );
		if ( upperC >= '0' && upperC <= '9' ) {
			return Integer.parseInt( Character.toString( upperC ) );
		} else if ( upperC >= 'A' && upperC <= 'Z' ) {
			return 10 + ( ( (int) upperC ) - ( (int) 'A' ) );
		}
		return '0';
	}

	private char returnCharFromNum(int n) {
		if ( n >= 0 && n <= 9 ) {
			return ( (char) ( ( (int) '0' ) + n ) );
		} else {
			return ( (char) ( ( (int) 'A' ) + ( n - 10 ) ) );
		}
	}

	private BigDecimal toDecimal(String s, final int k) {
		BigDecimal x = BigDecimal.ZERO;
		for (int i = 0; i < s.length(); i++) {
			x = x.multiply( BigDecimal.valueOf( k ) );
			x = x.add( BigDecimal.valueOf( returnNumber( s.charAt( i ) ) ) );
		}
		return x;
	}

	public String fromDecimal(BigDecimal dec, final int k) {
		String ans = "";
		BigDecimal bd = dec;
		do {
			BigDecimal[] bg = bd.divideAndRemainder( BigDecimal.valueOf( k ) );
			ans = String.format( "%s%s", returnCharFromNum( bg[ 1 ].intValue() ), ans );
			bd = bg[ 0 ];
		} while ( bd.signum() > 0 );

		return ans;
	}
}
