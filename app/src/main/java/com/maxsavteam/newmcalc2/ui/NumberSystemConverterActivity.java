package com.maxsavteam.newmcalc2.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.widget.NumpadView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class NumberSystemConverterActivity extends BaseConverterActivity {

	private final List<Button> letterButtons = new ArrayList<>();

	private static final String BIN = "BIN";
	private static final String OCT = "OCT";
	private static final String DEC = "DEC";
	private static final String HEX = "HEX";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );

		setTitle( R.string.number_system_converter );

		setNumpadViewDecimalSeparatorEnabled( false );
		setStatusVisible( false );
		setRefreshLayoutEnabled( false );

		setButtonsWeight( .3f );

		LinearLayout numpadContainer = findViewById( R.id.numpad_view_container );
		numpadContainer.addView( createLettersLayout(), 0 );

		List<String> items = List.of(BIN, OCT, DEC, HEX);
		List<String> dropdownItems = List.of(
				BIN + " " + getString( R.string.number_system_converter_binary ),
				OCT + " " + getString( R.string.number_system_converter_octal ),
				DEC + " " + getString( R.string.number_system_converter_decimal ),
				HEX + " " + getString( R.string.number_system_converter_hexadecimal )
		);

		displayData( items, dropdownItems, items );
	}

	private LinearLayout createLettersLayout(){
		LinearLayout layout = new LinearLayout( this );
		layout.setOrientation( LinearLayout.HORIZONTAL );
		layout.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
		for(char i = 'A'; i <= 'F'; i++){
			Button button = new Button( this, null, 0, R.style.NumberSystemConverterLetterButtonStyle );
			button.setText( String.valueOf( i ) );
			button.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1 ) );
			button.setTextColor( NumpadView.createColorStateListForDigitButton( this ) );
			char finalI = i;
			button.setOnClickListener( v->{
				EditText editText = findFocusedEditText();
				if(editText != null){
					insertLetter( editText, finalI );
				}
			} );
			layout.addView( button );
			letterButtons.add( button );
		}
		return layout;
	}

	private void insertLetter(EditText editText, char letter){
		Editable editable = editText.getText();
		editable.insert( editText.getSelectionStart(), String.valueOf( letter ) );
	}

	private void setLetterButtonsEnabled(boolean enabled){
		for(Button button : letterButtons)
			button.setEnabled( enabled );
	}

	@Override
	protected void onItemSelected(int index, String itemId) {
		if(index == getCurrentIndex()){
			findFocusedEditText().setText( "0" );
			updateNumpad( itemId );
		}
	}

	@Override
	protected void onFocusChanged(int index, String itemId) {
		updateNumpad( itemId );
	}

	private void updateNumpad(String itemId){
		setLetterButtonsEnabled( itemId.equals( HEX ) );
		int enabledButtonsCount;
		if(itemId.equals( HEX ) || itemId.equals( DEC ))
			enabledButtonsCount = 10;
		else if(itemId.equals( OCT ))
			enabledButtonsCount = 8;
		else
			enabledButtonsCount = 2;
		getFullNumpadView().setFirstNDigitButtonsEnabled( enabledButtonsCount, true );
	}

	private BigInteger convertFromBaseToDec(String amount, int base){
		if(base == 10)
			return new BigInteger( amount );
		BigInteger result = BigInteger.ZERO;
		BigInteger baseBig = BigInteger.valueOf( base );
		for(int i = 0; i < amount.length(); i++){
			char c = amount.charAt( i );
			int x;
			if(c >= '0' && c <= '9')
				x = c - '0';
			else
				x = c - 'A' + 10;

			result = result.multiply( baseBig ).add( BigInteger.valueOf( x ) );
		}
		return result;
	}

	private String convertFromDecToBase(BigInteger integer, int base){
		if(base == 10)
			return integer.toString();
		if(integer.compareTo( BigInteger.ZERO ) == 0)
			return "0";
		StringBuilder result = new StringBuilder();
		BigInteger baseBig = BigInteger.valueOf( base );
		while(integer.signum() > 0){
			BigInteger remainder = integer.remainder( baseBig );
			int x = remainder.intValue();
			char c;
			if(x < 10)
				c = (char)(x + '0');
			else
				c = (char)('A' + x - 10);
			integer = integer.divide( baseBig );
			result.append( c );
		}
		return result.reverse().toString();
	}

	@Override
	protected String convert(String sourceItemId, String targetItemId, String amount) {
		int sourceBase = getBase( sourceItemId );
		int targetBase = getBase( targetItemId );

		if(sourceBase == targetBase)
			return amount;

		BigInteger integer = convertFromBaseToDec( amount, sourceBase );
		return convertFromDecToBase( integer, targetBase );
	}

	private int getBase(String id){
		switch ( id ) {
			case BIN:
				return 2;
			case OCT:
				return 8;
			case DEC:
				return 10;
			default:
				return 16;
		}
	}

	@Override
	protected int getFieldsCount() {
		return 2;
	}

	@Override
	protected String getSharedPrefsName() {
		return "number_system_converter";
	}

	@Override
	protected String getDefaultItemId(int index) {
		return null;
	}

	@Override
	protected String getLastAmountDefaultValue() {
		return "0";
	}

}
