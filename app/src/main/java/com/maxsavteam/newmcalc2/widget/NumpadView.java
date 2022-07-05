package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;

import androidx.annotation.Nullable;

import com.maxsavteam.newmcalc2.R;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NumpadView extends LinearLayout {

	protected static final int LAST_ROW_ID = View.generateViewId();
	private static final int SEPARATOR_BUTTON_ID = View.generateViewId();

	private final List<Button> digitButtons = new ArrayList<>();

	private boolean isDecimalSeparatorEnabled;

	public NumpadView(Context context) {
		this( context, null );
	}

	public NumpadView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
		this( context, attrs, 0 );
	}

	public NumpadView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		this( context, attrs, defStyleAttr, 0 );
	}

	public NumpadView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super( context, attrs, defStyleAttr, defStyleRes );

		TypedArray array = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.NumpadView,
				0, 0
		);
		isDecimalSeparatorEnabled = array.getBoolean( R.styleable.NumpadView_showDecimalSeparator, true );

		array.recycle();

		LinearLayout container = new LinearLayout( getContext() );
		container.setOrientation( VERTICAL );
		container.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT ) );

		for (int startDigit = 7; startDigit >= 1; startDigit -= 3) {
			LinearLayout row = new LinearLayout( getContext() );
			row.setOrientation( HORIZONTAL );
			row.setLayoutParams( getDefaultParamsForElements() );
			for (int i = startDigit; i <= startDigit + 2; i++) {
				Button button = new Button( getContext(), null, 0, R.style.NumPadButtonStyle2 );
				button.setText( String.valueOf( i ) );
				button.setLayoutParams( getDefaultParamsForElements() );
				row.addView( button );
				digitButtons.add( button );
			}
			container.addView( row );
		}

		addLastRow( container );

		addView( container );
	}

	protected void addLastRow(LinearLayout container) {
		LinearLayout row = new LinearLayout( getContext() );
		row.setOrientation( HORIZONTAL );
		row.setLayoutParams( getDefaultParamsForElements() );
		row.setId( LAST_ROW_ID );

		row.addView( createSpace() );

		Button zeroButton = new Button( getContext(), null, 0, R.style.NumPadButtonStyle2 );
		zeroButton.setLayoutParams( getDefaultParamsForElements() );
		zeroButton.setText( "0" );
		row.addView( zeroButton );
		digitButtons.add( zeroButton );

		Button separatorButton = new Button( getContext(), null, 0, R.style.NumPadButtonStyle2 );
		separatorButton.setId( SEPARATOR_BUTTON_ID );
		separatorButton.setLayoutParams( getDefaultParamsForElements() );
		updateSeparatorButtonText(separatorButton);
		row.addView( separatorButton );
		if(!isDecimalSeparatorEnabled)
			separatorButton.setVisibility( INVISIBLE );

		container.addView( row );
	}

	private Space createSpace(){
		Space space = new Space( getContext() );
		space.setLayoutParams( getDefaultParamsForElements() );
		return space;
	}

	protected LayoutParams getDefaultParamsForElements() {
		return new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1 );
	}

	public void setDecimalSeparatorEnabled(boolean decimalSeparatorEnabled) {
		isDecimalSeparatorEnabled = decimalSeparatorEnabled;

		if(isDecimalSeparatorEnabled)
			findViewById( SEPARATOR_BUTTON_ID ).setVisibility( VISIBLE );
		else
			findViewById( SEPARATOR_BUTTON_ID ).setVisibility( INVISIBLE );
	}

	public void setDigitButtonOnClickListener(DigitButtonOnClickListener onClickListener) {
		for (Button button : digitButtons) {
			if ( onClickListener == null ) {
				button.setOnClickListener( null );
			} else {
				button.setOnClickListener( v->{
					int digit = Integer.parseInt( button.getText().toString() );
					onClickListener.onClick( digit );
				} );
			}
		}
	}

	public interface DigitButtonOnClickListener {
		void onClick(int digit);
	}

	public void setSeparatorOnClickListener(SeparatorButtonOnClickListener onClickListener) {
		Button separatorButton = findViewById( SEPARATOR_BUTTON_ID );
		if ( onClickListener == null ) {
			separatorButton.setOnClickListener( null );
		} else {
			separatorButton.setOnClickListener( v->{
				char symbol = separatorButton.getText().charAt( 0 );
				onClickListener.onClick( symbol );
			} );
		}
	}

	public interface SeparatorButtonOnClickListener {
		void onClick(char symbol);
	}

	public void updateLocale() {
		Button separatorButton = findViewById( SEPARATOR_BUTTON_ID );
		updateSeparatorButtonText(separatorButton);
	}

	private void updateSeparatorButtonText(Button separatorButton) {
		Locale locale = getContext().getResources().getConfiguration().getLocales().get( 0 );
		DecimalFormatSymbols symbols = new DecimalFormatSymbols( locale );
		separatorButton.setText( Character.toString( symbols.getDecimalSeparator() ) );
	}

}
