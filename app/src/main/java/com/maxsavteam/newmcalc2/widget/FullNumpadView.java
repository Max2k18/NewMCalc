package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.maxsavteam.newmcalc2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Numpad with backspace and clear buttons
 */
public class FullNumpadView extends LinearLayout {

	private final FlexboxLayout buttonsLayout;
	private final NumpadView numpadView;

	private final List<EditText> linkedEditTexts = new ArrayList<>();

	public FullNumpadView(Context context) {
		this( context, null );
	}

	public FullNumpadView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
		this( context, attrs, 0 );
	}

	public FullNumpadView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		this( context, attrs, defStyleAttr, 0 );
	}

	public FullNumpadView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super( context, attrs, defStyleAttr, defStyleRes );

		buttonsLayout = createButtonsLayout();
		buttonsLayout.setVisibility( GONE );

		numpadView = createNumpadView();

		TypedArray array = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.FullNumpadView,
				0, 0
		) ;
		setSigned( array.getBoolean( R.styleable.FullNumpadView_signed, false ) );
		setDecimalSeparatorEnabled( array.getBoolean( R.styleable.FullNumpadView_showDecimalSeparator, false ) );

		array.recycle();

		setOrientation( HORIZONTAL );
		addView( numpadView, new LayoutParams( 0, LayoutParams.MATCH_PARENT, 1 ) );
		addView( buttonsLayout, new LayoutParams( 0, LayoutParams.MATCH_PARENT, 0.2f ) );
	}

	private FlexboxLayout createButtonsLayout() {
		FlexboxLayout layout = new FlexboxLayout( getContext() );
		layout.setFlexDirection( FlexDirection.COLUMN );
		layout.setJustifyContent( JustifyContent.SPACE_AROUND );

		layout.addView( createClearButton() );

		layout.addView( createBackspaceButton() );

		return layout;
	}

	private Button createClearButton(){
		Button clearButton = new Button( getContext(), null, 0, R.style.FullNumpadViewButtonsStyle );
		clearButton.setText( "C" );
		clearButton.setOnClickListener( v->{
			EditText editText = findFocusedEditText();
			if(editText == null)
				return;
			editText.getText().clear();
		} );
		clearButton.setLayoutParams( createCommonLayoutParams() );
		return clearButton;
	}

	private ImageButton createBackspaceButton(){
		ImageButton backspaceButton = new ImageButton( getContext(), null, 0, R.style.FullNumpadViewButtonsStyle );

		backspaceButton.setImageResource( R.drawable.ic_backspace_black );

		int padding = convertDpToPx( 15 );
		backspaceButton.setPadding( padding, 0, padding, 0 );
		backspaceButton.setOnClickListener( v->{
			EditText editText = findFocusedEditText();
			if(editText == null)
				return;
			Editable editable = editText.getText();
			int selection = editText.getSelectionStart();
			if ( selection != 0 ) {
				editable.delete( selection - 1, selection );
			}
		} );
		FlexboxLayout.LayoutParams params = createCommonLayoutParams();
		params.topMargin = 0;
		backspaceButton.setLayoutParams( params );

		return backspaceButton;
	}

	private FlexboxLayout.LayoutParams createCommonLayoutParams(){
		FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
		params.setFlexBasisPercent( .4f );

		int marginsVertical = convertDpToPx( 20 );
		int marginsHorizontal = convertDpToPx( 10 );

		params.setMargins( marginsHorizontal, marginsVertical, marginsHorizontal, marginsVertical );

		return params;
	}

	private int convertDpToPx(int value){
		return (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				value,
				getContext().getResources().getDisplayMetrics()
		);
	}

	private NumpadView createNumpadView() {
		NumpadView numpadView = new NumpadView( getContext() );
		numpadView.setDecimalSeparatorEnabled( false );
		numpadView.setDigitButtonOnClickListener( digit->{
			EditText editText = findFocusedEditText();
			if ( editText == null ) {
				return;
			}
			insert( String.valueOf( digit ), editText );
		} );
		numpadView.setMinusButtonOnClickListener( ()->{
			EditText editText = findFocusedEditText();
			if ( editText == null ) {
				return;
			}
			insertMinus( editText );
		} );
		numpadView.setSeparatorOnClickListener( separator -> {
			EditText editText = findFocusedEditText();
			if ( editText == null ) {
				return;
			}
			insertSeparator( separator, editText );
		} );

		return numpadView;
	}

	private void insertSeparator(char separator, EditText editText){
		String text = editText.getText().toString();
		if(text.contains( String.valueOf( separator ) ))
			return;
		insert( String.valueOf( separator ), editText );
	}

	private void insertMinus(EditText editText){
		String text = editText.getText().toString();
		if(text.contains( "-" ))
			return;
		int selection = editText.getSelectionStart();
		if(selection > 0)
			return;
		insert( "-", editText );
	}

	private void insert(String text, EditText editText) {
		Editable editable = editText.getText();
		editable.insert( editText.getSelectionStart(), text );
	}

	private EditText findFocusedEditText() {
		for (EditText editText : linkedEditTexts)
			if ( editText.isFocused() ) {
				return editText;
			}
		return null;
	}

	public void linkWith(EditText editText) {
		for (EditText et : linkedEditTexts) {
			if ( et == editText ) {
				return;
			}
		}
		linkedEditTexts.add( editText );
		editText.setShowSoftInputOnFocus( false );
		showButtonsLayout();
	}

	private void showButtonsLayout(){
		if(buttonsLayout.getVisibility() == VISIBLE)
			return;
		buttonsLayout.setVisibility( VISIBLE );
		((LayoutParams) numpadView.getLayoutParams()).weight = .8f;
	}

	public void addCustomButton(Button button){
		button.setBackgroundResource( R.drawable.full_numpad_view_buttons_background );
		var params = createCommonLayoutParams();
		params.topMargin = 0;
		buttonsLayout.addView( button, params );
	}

	public void setSigned(boolean isSigned){
		numpadView.setSigned( isSigned );
	}

	public void setDecimalSeparatorEnabled(boolean isDecimalSeparatorEnabled){
		numpadView.setDecimalSeparatorEnabled( isDecimalSeparatorEnabled );
	}

	public void setFirstNDigitButtonsEnabled(int count, boolean enabled) {
		numpadView.setFirstNDigitButtonsEnabled( count, enabled );
	}

	public void setDigitButtonEnabled(int digit, boolean enabled) {
		numpadView.setDigitButtonEnabled( digit, enabled );
	}

}
