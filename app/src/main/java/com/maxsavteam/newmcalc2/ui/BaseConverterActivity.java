package com.maxsavteam.newmcalc2.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

public abstract class BaseConverterActivity extends ThemeActivity {

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
		for(var editText : editTexts) {
			fullNumpadView.linkWith( editText );
		}

		startDataLoading( false );
	}

	protected void setRefreshLayoutEnabled(boolean enabled) {
		swipeRefreshLayout.setEnabled( enabled );
	}

	protected void setRefreshLayoutRefreshing(boolean refreshing) {
		swipeRefreshLayout.setRefreshing( refreshing );
	}

	protected void setStatusVisible(boolean visible){
		textViewStatus.setVisibility( visible ? ViewGroup.VISIBLE : ViewGroup.GONE );
	}

	protected void setStatusText(String text){
		textViewStatus.setText( text );
	}

	protected void setNumpadViewDecimalSeparatorEnabled(boolean enabled) {
		fullNumpadView.setDecimalSeparatorEnabled( enabled );
	}

	private void createFields(){
		for(int i = 0; i < getFieldsCount(); i++){
			LinearLayout layout = new LinearLayout( this );
			layout.setOrientation( LinearLayout.HORIZONTAL );
			fieldsLayouts.add( layout );

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1 );

			ButtonWithDropdown button = new ButtonWithDropdown( this );
			button.setBackground( null );
			layout.addView( button, params );
			buttons.add( button );
			setButtonWithDropdownItemSelectedListener( button, i );

			EditText editText = new EditText( this, null, 0, R.style.ConverterEditTextStyle );
			layout.addView( editText, params );
			editTexts.add( editText );
			setEditTextWatcher( editText, i );

			flexboxLayout.addView( layout, new FlexboxLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
		}
	}

	private void setButtonWithDropdownItemSelectedListener(ButtonWithDropdown button, int buttonIndex){
		button.setOnItemSelectedListener( index -> {
			sharedPreferences.edit()
					.putString( "lastSelected" + buttonIndex, itemsIds.get( index ) )
					.apply();

			convert( getFocusedEditTextIndex() );
		} );
	}

	private int getFocusedEditTextIndex(){
		for(int i = 0; i < editTexts.size(); i++){
			if(editTexts.get( i ).hasFocus()){
				return i;
			}
		}
		return -1;
	}

	private void setEditTextWatcher(EditText editText, int editTextIndex){
		editText.addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(editText.hasFocus()) {
					convert( editTextIndex );
					saveCurrentAmount( editTextIndex );
				}
			}
		} );
	}

	private void saveCurrentAmount(int index){
		EditText focusedEditText = editTexts.get( index );
		String amount = focusedEditText.getText().toString();
		if(amount.isEmpty())
			amount = getLastAmountDefaultValue();
		sharedPreferences.edit()
				.putInt( "lastSourceIndex", index )
				.putString( "lastAmount", amount )
				.apply();
	}

	private void convert(int sourceIndex){
		EditText sourceEditText = editTexts.get( sourceIndex );

		String sourceValue = sourceEditText.getText().toString();
		if(sourceValue.isEmpty()){
			for (int i = 0; i < editTexts.size(); i++) {
				EditText editText = editTexts.get( i );
				if(i != sourceIndex){
					editText.setText( "" );
				}
			}
			return;
		}

		sourceEditText.requestFocus();

		String sourceItemId = itemsIds.get( buttons.get( sourceIndex ).getSelectedItem() );

		for(int i = 0; i < getFieldsCount(); i++){
			if(i == sourceIndex){
				continue;
			}
			String value = convert( sourceItemId, itemsIds.get( buttons.get( i ).getSelectedItem() ), sourceValue );
			editTexts.get( i ).setText( value );
		}
	}

	protected void startDataLoading(boolean isUserInitiated){
		setRefreshLayoutRefreshing( true );
		flexboxLayout.setVisibility( View.GONE );
	}

	protected void displayData(List<String> displayItems, List<String> itemsIds){
		if(displayItems.size() != itemsIds.size())
			throw new IllegalArgumentException("displayItems and itemsIds must have the same size");

		this.displayItems = displayItems;
		this.itemsIds = itemsIds;

		for (int i = 0; i < buttons.size(); i++) {
			ButtonWithDropdown button = buttons.get( i );
			button.setElements( displayItems.toArray() );

			String lastSelected = sharedPreferences.getString( "lastSelected" + i, getDefaultItemId( i ) );
			int index = Math.max( 0, itemsIds.indexOf( lastSelected ) );
			button.setSelection( index );
		}

		restoreLastAmount();

		setRefreshLayoutRefreshing( false );
		flexboxLayout.setVisibility( View.VISIBLE );
	}

	private void restoreLastAmount(){
		String lastAmount = sharedPreferences.getString( "lastAmount", null );
		if(lastAmount == null || lastAmount.isEmpty())
			lastAmount = getLastAmountDefaultValue();
		int sourceIndex = sharedPreferences.getInt( "lastSourceIndex", 0 );
		setAmountToIndex( lastAmount, sourceIndex );
	}

	private void setAmountToIndex(String amount, int index){
		EditText editText = editTexts.get( index );
		editText.requestFocus();
		editText.setText( amount );
		editText.setSelection( amount.length() );
	}

	protected abstract String convert(String sourceItemId, String targetItemId, String amount);

	protected abstract int getFieldsCount();

	protected abstract String getSharedPrefsName();

	protected abstract String getDefaultItemId(int index);

	protected abstract String getLastAmountDefaultValue();

}
