package com.maxsavteam.newmcalc2.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.flexbox.FlexboxLayout;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ui.base.ThemeActivity;
import com.maxsavteam.newmcalc2.widget.ButtonWithDropdown;
import com.maxsavteam.newmcalc2.widget.FullNumpadView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class BaseConverterActivity extends ThemeActivity {

	private static final String TAG = "BaseConverterActivity";
	private final List<LinearLayout> fieldsLayouts = new ArrayList<>();
	private final List<ButtonWithDropdown> buttons = new ArrayList<>();
	private final List<EditText> editTexts = new ArrayList<>();

	private List<String> displayItems;
	private List<String> itemsIds;

	protected SharedPreferences sharedPreferences;

	private SwipeRefreshLayout swipeRefreshLayout;
	private TextView textViewStatus;
	private FlexboxLayout flexboxLayout;
	private FullNumpadView fullNumpadView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_converter );

		setActionBar( R.id.toolbar );
		displayHomeAsUp();

		sharedPreferences = getSharedPreferences( getSharedPrefsName(), MODE_PRIVATE );

		swipeRefreshLayout = findViewById( R.id.converter_refreshLayout );
		swipeRefreshLayout.setOnRefreshListener( ()->{
			swipeRefreshLayout.setRefreshing( false );
			startDataLoading( true );
		} );

		textViewStatus = findViewById( R.id.converter_status );

		flexboxLayout = findViewById( R.id.flexboxLayout );

		createFields();

		fullNumpadView = findViewById( R.id.converter_numpad_view );
		for (var editText : editTexts) {
			fullNumpadView.linkWith( editText );
		}

		startDataLoading( false );
	}

	protected int getCurrentIndex() {
		for (int i = 0; i < editTexts.size(); i++) {
			EditText editText = editTexts.get( i );
			if ( editText.isFocused() ) {
				return i;
			}
		}
		return -1;
	}

	protected EditText findFocusedEditText() {
		int index = getCurrentIndex();
		if ( index == -1 ) {
			return null;
		}
		return editTexts.get( index );
	}

	protected String getCurrentSelectedItemId() {
		int index = getCurrentIndex();
		if ( index == -1 ) {
			return null;
		}
		int selectedItem = buttons.get( index ).getSelectedItemIndex();
		return itemsIds.get( selectedItem );
	}

	protected void setRefreshLayoutEnabled(boolean enabled) {
		swipeRefreshLayout.setEnabled( enabled );
	}

	protected void setRefreshLayoutRefreshing(boolean refreshing) {
		swipeRefreshLayout.setRefreshing( refreshing );
	}

	protected void setStatusVisible(boolean visible) {
		textViewStatus.setVisibility( visible ? ViewGroup.VISIBLE : ViewGroup.GONE );
	}

	protected void setStatusText(String text) {
		textViewStatus.setText( text );
	}

	protected void setNumpadViewDecimalSeparatorEnabled(boolean enabled) {
		fullNumpadView.setDecimalSeparatorEnabled( enabled );
	}

	private void createFields() {
		for (int i = 0; i < getFieldsCount(); i++) {
			LinearLayout layout = new LinearLayout( this );
			layout.setOrientation( LinearLayout.HORIZONTAL );
			layout.setGravity( Gravity.CENTER );
			fieldsLayouts.add( layout );

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( 0, LinearLayout.LayoutParams.WRAP_CONTENT, .5f );

			ButtonWithDropdown button = new ButtonWithDropdown( this );
			button.setBackground( null );
			button.setLayoutParams( new LinearLayout.LayoutParams( params ) );
			layout.addView( button );
			buttons.add( button );
			setButtonWithDropdownItemSelectedListener( button, i );

			EditText editText = new EditText( this, null, 0, R.style.ConverterEditTextStyle );
			editText.setLayoutParams( new LinearLayout.LayoutParams( params ) );
			layout.addView( editText );
			editTexts.add( editText );
			setEditTextWatcher( editText, i );
			setEditTextOnFocusChangeListener( editText );

			flexboxLayout.addView( layout, new FlexboxLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
		}
	}

	protected void setButtonsWeight(float weight){
		for(int i = 0; i < buttons.size(); i++){
			Button button = buttons.get( i );
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
			params.weight = weight;
			button.setLayoutParams( params );

			EditText editText = editTexts.get( i );
			params = (LinearLayout.LayoutParams) editText.getLayoutParams();
			params.weight = 1 - weight;
			editText.setLayoutParams( params );
		}
	}

	private void setButtonWithDropdownItemSelectedListener(ButtonWithDropdown button, int buttonIndex) {
		button.setOnItemSelectedListener( index->{
			String itemId = itemsIds.get( index );
			sharedPreferences.edit()
					.putString( "lastSelected" + buttonIndex, itemId )
					.apply();

			onItemSelected( buttonIndex, itemId );

			convert( getFocusedEditTextIndex() );
		} );
	}

	private int getFocusedEditTextIndex() {
		for (int i = 0; i < editTexts.size(); i++) {
			if ( editTexts.get( i ).hasFocus() ) {
				return i;
			}
		}
		return -1;
	}

	private void setEditTextWatcher(EditText editText, int editTextIndex) {
		editText.addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if ( editText.hasFocus() ) {
					convert( editTextIndex );
					saveCurrentAmount( editTextIndex );
				}
			}
		} );
	}

	private void setEditTextOnFocusChangeListener(EditText editText) {
		editText.setOnFocusChangeListener( (v, hasFocus)->{
			if ( hasFocus ) {
				int index = getCurrentIndex();
				String itemId = getCurrentSelectedItemId();
				onFocusChanged( index, itemId );
			}
		} );
	}

	private void saveCurrentAmount(int index) {
		EditText focusedEditText = editTexts.get( index );
		String amount = focusedEditText.getText().toString();
		if ( amount.isEmpty() ) {
			amount = getLastAmountDefaultValue();
		}
		sharedPreferences.edit()
				.putInt( "lastSourceIndex", index )
				.putString( "lastAmount", amount )
				.apply();
	}

	private void convert(int sourceIndex) {
		EditText sourceEditText = editTexts.get( sourceIndex );

		String sourceValue = sourceEditText.getText().toString();
		if ( sourceValue.isEmpty() ) {
			for (int i = 0; i < editTexts.size(); i++) {
				EditText editText = editTexts.get( i );
				if ( i != sourceIndex ) {
					editText.setText( "" );
				}
			}
			return;
		}

		sourceEditText.requestFocus();

		String sourceItemId = itemsIds.get( buttons.get( sourceIndex ).getSelectedItemIndex() );

		for (int i = 0; i < getFieldsCount(); i++) {
			if ( i == sourceIndex ) {
				continue;
			}
			String value = convert( sourceItemId, itemsIds.get( buttons.get( i ).getSelectedItemIndex() ), sourceValue );
			editTexts.get( i ).setText( value );
		}
	}

	protected void startDataLoading(boolean isUserInitiated) {
		setRefreshLayoutRefreshing( true );
		flexboxLayout.setVisibility( View.GONE );
	}

	protected void displayData(List<String> displayItems, List<String> itemsIds) {
		displayData( displayItems, displayItems, itemsIds );
	}

	protected void displayData(List<String> displayItems, List<String> dropdownItems, List<String> itemsIds) {
		if ( new HashSet<>( List.of( displayItems.size(), dropdownItems.size(), itemsIds.size() ) ).size() != 1 ) {
			throw new IllegalArgumentException( "Lists must have same size" );
		}

		this.displayItems = new ArrayList<>( displayItems );
		this.itemsIds = new ArrayList<>( itemsIds );

		String[] displayItemsArray = displayItems.toArray( new String[ 0 ] );
		String[] dropdownItemsArray = dropdownItems.toArray( new String[ 0 ] );

		for (int i = 0; i < buttons.size(); i++) {
			ButtonWithDropdown button = buttons.get( i );
			button.setElements( displayItemsArray, dropdownItemsArray );

			String lastSelected = sharedPreferences.getString( "lastSelected" + i, getDefaultItemId( i ) );
			int index = Math.max( 0, itemsIds.indexOf( lastSelected ) );
			button.setSelection( index );
		}

		restoreLastAmount();

		setRefreshLayoutRefreshing( false );
		flexboxLayout.setVisibility( View.VISIBLE );
	}

	private void restoreLastAmount() {
		String lastAmount = sharedPreferences.getString( "lastAmount", null );
		if ( lastAmount == null || lastAmount.isEmpty() ) {
			lastAmount = getLastAmountDefaultValue();
		}
		int sourceIndex = sharedPreferences.getInt( "lastSourceIndex", 0 );
		setAmountToIndex( lastAmount, sourceIndex );
	}

	private void setAmountToIndex(String amount, int index) {
		EditText editText = editTexts.get( index );
		editText.requestFocus();
		editText.setText( amount );
		editText.setSelection( amount.length() );
	}

	protected FullNumpadView getFullNumpadView() {
		return fullNumpadView;
	}

	protected void onFocusChanged(int index, String itemId) {}

	protected void onItemSelected(int index, String itemId) {}

	protected abstract String convert(String sourceItemId, String targetItemId, String amount);

	protected abstract int getFieldsCount();

	protected abstract String getSharedPrefsName();

	protected abstract String getDefaultItemId(int index);

	protected abstract String getLastAmountDefaultValue();

}
