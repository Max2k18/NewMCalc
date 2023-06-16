package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.maxsavteam.newmcalc2.R;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NumpadView extends LinearLayout {

	protected static final int LAST_ROW_ID = View.generateViewId();
	protected static final int SEPARATOR_BUTTON_ID = View.generateViewId();
	protected static final int MINUS_BUTTON_ID = View.generateViewId();
	protected static final int LEFT_ARROW_BUTTON_ID = View.generateViewId();
	protected static final int RIGHT_ARROW_BUTTON_ID = View.generateViewId();

	private final List<Button> digitButtons;

	private boolean isDecimalSeparatorEnabled;
	private boolean isSigned;
	private boolean showNavigationArrows;

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

		digitButtons = new ArrayList<>( 10 );
		for (int i = 0; i < 10; i++)
			digitButtons.add( null );

		TypedArray array = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.NumpadView,
				0, 0
		);
		isDecimalSeparatorEnabled = array.getBoolean( R.styleable.NumpadView_showDecimalSeparator, true );
		isSigned = array.getBoolean( R.styleable.NumpadView_signed, false );

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
				button.setTextColor( createColorStateListForDigitButton( getContext() ) );
				row.addView( button );
				digitButtons.set( i, button );
			}
			container.addView( row );
		}

		addLastRow( container );

		addView( container );
	}

	public void setFirstNDigitButtonsEnabled(int count, boolean enabled) {
		int c = Math.min( count, digitButtons.size() );
		for (int i = 0; i < digitButtons.size(); i++) {
			Button button = digitButtons.get( i );
			if ( i < c ) {
				button.setEnabled( enabled );
			} else {
				button.setEnabled( false );
			}
		}
	}

	public void setDigitButtonEnabled(int digit, boolean enabled) {
		digitButtons.get( digit ).setEnabled( enabled );
	}

	protected void addLastRow(LinearLayout container) {
		LinearLayout row = new LinearLayout( getContext() );
		row.setOrientation( HORIZONTAL );
		row.setLayoutParams( getDefaultParamsForElements() );
		row.setId( LAST_ROW_ID );

		ImageButton leftArrowButton = new ImageButton(getContext(), null, 0, R.style.NumPadButtonStyle2);
		leftArrowButton.setImageTintList(ColorStateList.valueOf(getContext().getColor(R.color.colorAccent)));
		leftArrowButton.setId(LEFT_ARROW_BUTTON_ID);
		leftArrowButton.setImageResource(R.drawable.baseline_keyboard_arrow_left_24);
		row.addView(leftArrowButton, getDefaultParamsForElements());
		if(!showNavigationArrows)
			leftArrowButton.setVisibility(GONE);

		Button minusButton = new Button( getContext(), null, 0, R.style.NumPadButtonStyle2 );
		minusButton.setText( "-" );
		minusButton.setId( MINUS_BUTTON_ID );
		row.addView( minusButton, getDefaultParamsForElements() );
		if ( !isSigned ) {
			minusButton.setVisibility( showNavigationArrows ? GONE : INVISIBLE );
		}

		Button zeroButton = new Button( getContext(), null, 0, R.style.NumPadButtonStyle2 );
		zeroButton.setLayoutParams( getDefaultParamsForElements() );
		zeroButton.setText( "0" );
		row.addView( zeroButton );
		digitButtons.set( 0, zeroButton );

		Button separatorButton = new Button( getContext(), null, 0, R.style.NumPadButtonStyle2 );
		separatorButton.setId( SEPARATOR_BUTTON_ID );
		separatorButton.setLayoutParams( getDefaultParamsForElements() );
		updateSeparatorButtonText( separatorButton );
		row.addView( separatorButton );
		if ( !isDecimalSeparatorEnabled ) {
			separatorButton.setVisibility( showNavigationArrows ? GONE : INVISIBLE );
		}

		ImageButton rightArrowButton = new ImageButton(getContext(), null, 0, R.style.NumPadButtonStyle2);
		rightArrowButton.setImageResource(R.drawable.baseline_keyboard_arrow_right_24);
		rightArrowButton.setImageTintList(ColorStateList.valueOf(getContext().getColor(R.color.colorAccent)));
		rightArrowButton.setId(RIGHT_ARROW_BUTTON_ID);
		row.addView(rightArrowButton, getDefaultParamsForElements());
		if(!showNavigationArrows)
			rightArrowButton.setVisibility(GONE);

		container.addView( row );
	}

	protected LayoutParams getDefaultParamsForElements() {
		return new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1 );
	}

	public void setDecimalSeparatorEnabled(boolean decimalSeparatorEnabled) {
		isDecimalSeparatorEnabled = decimalSeparatorEnabled;

		if ( isDecimalSeparatorEnabled ) {
			findViewById( SEPARATOR_BUTTON_ID ).setVisibility( VISIBLE );
		} else {
			findViewById( SEPARATOR_BUTTON_ID ).setVisibility( INVISIBLE );
		}
	}

	public boolean isDecimalSeparatorEnabled() {
		return isDecimalSeparatorEnabled;
	}

	public void setShowNavigationArrows(boolean showNavigationArrows) {
		this.showNavigationArrows = showNavigationArrows;

		if(showNavigationArrows){
			findViewById(LEFT_ARROW_BUTTON_ID).setVisibility(VISIBLE);
			findViewById(RIGHT_ARROW_BUTTON_ID).setVisibility(VISIBLE);
			if(!isSigned)
				findViewById(MINUS_BUTTON_ID).setVisibility(GONE);
			if(!isDecimalSeparatorEnabled)
				findViewById(SEPARATOR_BUTTON_ID).setVisibility(GONE);
		}else{
			findViewById(LEFT_ARROW_BUTTON_ID).setVisibility(GONE);
			findViewById(RIGHT_ARROW_BUTTON_ID).setVisibility(GONE);
			if(!isSigned)
				findViewById(MINUS_BUTTON_ID).setVisibility(INVISIBLE);
			if(!isDecimalSeparatorEnabled)
				findViewById(SEPARATOR_BUTTON_ID).setVisibility(INVISIBLE);
		}
	}

	public boolean isShowNavigationArrows() {
		return showNavigationArrows;
	}

	public void setArrowButtonOnClickListener(ArrowButtonOnClickListener onClickListener){
		for(ImageButton button : new ImageButton[]{
				findViewById(LEFT_ARROW_BUTTON_ID),
				findViewById(RIGHT_ARROW_BUTTON_ID)
		}){
			if(onClickListener == null)
				button.setOnClickListener(null);
			else{
				button.setOnClickListener(v -> onClickListener.onClick(
						button.getId() == LEFT_ARROW_BUTTON_ID
							? ArrowButtonOnClickListener.Direction.LEFT
							: ArrowButtonOnClickListener.Direction.RIGHT));
			}
		}
	}

	public interface ArrowButtonOnClickListener {
		enum Direction {
			LEFT, RIGHT
		}
		void onClick(Direction direction);
	}

	public void setSigned(boolean isSigned) {
		this.isSigned = isSigned;
		if ( isSigned ) {
			findViewById( MINUS_BUTTON_ID ).setVisibility( VISIBLE );
		} else {
			findViewById( MINUS_BUTTON_ID ).setVisibility( INVISIBLE );
		}
	}

	public void setMinusButtonOnClickListener(MinusButtonOnClickListener listener) {
		Button minusButton = findViewById( MINUS_BUTTON_ID );
		if ( listener == null ) {
			minusButton.setOnClickListener( null );
		} else {
			minusButton.setOnClickListener( v->listener.onClick() );
		}
	}

	public interface MinusButtonOnClickListener {
		void onClick();
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
		updateSeparatorButtonText( separatorButton );
	}

	private void updateSeparatorButtonText(Button separatorButton) {
		Locale locale = getContext().getResources().getConfiguration().getLocales().get( 0 );
		DecimalFormatSymbols symbols = new DecimalFormatSymbols( locale );
		separatorButton.setText( Character.toString( symbols.getDecimalSeparator() ) );
	}

	public static ColorStateList createColorStateListForDigitButton(Context context) {
		int[][] states = new int[][]{
				new int[]{ android.R.attr.state_enabled },
				new int[]{ -android.R.attr.state_enabled }
		};

		TypedArray array = context.obtainStyledAttributes(
				new int[]{ R.attr.textColor }
		);
		int color = array.getColor( 0, 0 );
		array.recycle();

		int[] colors = new int[]{
				color,
				context.getResources().getColor( R.color.light_gray )
		};
		return new ColorStateList( states, colors );
	}

}
