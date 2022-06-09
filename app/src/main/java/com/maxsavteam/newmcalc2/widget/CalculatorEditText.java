package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.maxsavteam.newmcalc2.Main2Activity;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.utils.FormatUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class CalculatorEditText extends androidx.appcompat.widget.AppCompatEditText {

	private static final String TAG = Main2Activity.TAG + " CalculatorEdtiText";

	private final float mMaximumTextSize;
	private final float mMinimumTextSize;
	private final float mStepTextSize;
	private final ArrayList<TextListener> mTextListeners = new ArrayList<>();
	private boolean isEditing = false;
	private DecimalFormat mDecimalFormat;

	public CalculatorEditText(@NonNull Context context) {
		this( context, null );
	}

	public CalculatorEditText(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
		this( context, attrs, android.R.attr.editTextStyle );
	}

	public CalculatorEditText(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		super( context, attrs, defStyleAttr );

		TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.CalculatorEditText, defStyleAttr, 0 );
		mMaximumTextSize = a.getDimension( R.styleable.CalculatorEditText_maxTextSize, getTextSize() );
		mMinimumTextSize = a.getDimension( R.styleable.CalculatorEditText_minTextSize, getTextSize() );
		mStepTextSize = a.getDimension( R.styleable.CalculatorEditText_stepTextSize,
				( mMaximumTextSize - mMinimumTextSize ) / 3 );
		boolean showKeyboard = a.getBoolean( R.styleable.CalculatorEditText_showKeyboardOnFocus, false );
		setShowSoftInputOnFocus( showKeyboard );
		a.recycle();

		updateLocale();
	}

	public void updateLocale() {
		mDecimalFormat = FormatUtils.getDecimalFormat();
	}

	@Override
	public boolean onTextContextMenuItem(int id) {
		switch ( id ) {
			case android.R.id.paste:
			case android.R.id.cut:
				return false;
			default:
				return super.onTextContextMenuItem( id );
		}
	}

	@FunctionalInterface
	public interface TextListener {
		void onTextChanged();
	}

	public void addListener(TextListener listener) {
		mTextListeners.add( listener );
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		if ( mTextListeners != null && !isEditing ) {
			for (TextListener listener : mTextListeners) {
				if ( listener != null ) {
					listener.onTextChanged();
				}
			}
			isEditing = true;
			String oldText = getText().toString();
			String newText = formatText( oldText );
			int oldSelection = getSelectionStart();
			setText( newText );
			int newSelection = findNewSelection( oldText, newText, oldSelection );
			setSelection( newSelection );
			isEditing = false;
		}
	}

	private int findNewSelection(String oldText, String newText, int oldSelection) {
		return newText.length() - ( oldText.length() - oldSelection );
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		if ( mDecimalFormat == null ) {
			return;
		}
		if ( selStart == selEnd ) {
			selStart = Math.min( getText().length(), selStart );
			DecimalFormatSymbols symbols = mDecimalFormat.getDecimalFormatSymbols();
			if ( selStart > 0 && getText().charAt( selStart - 1 ) == symbols.getGroupingSeparator() ) {
				setSelection( selStart - 1 );
			}
		}
		super.onSelectionChanged( selStart, selEnd );
	}

	@Override
	public void setSelection(int index) {
		super.setSelection( Math.max( index, 0 ) );
	}

	@NonNull
	@Override
	public Editable getText() {
		Editable sup = super.getText();
		if ( sup == null ) {
			return new SpannableStringBuilder( "" );
		}
		return sup;
	}

	private String formatText(String text) {
		DecimalFormatSymbols symbols = mDecimalFormat.getDecimalFormatSymbols();
		String decimalSeparator = String.valueOf( symbols.getDecimalSeparator() );
		FormatUtils.Formatter formatter = number->{
			if ( number.equals( "." ) ) {
				return decimalSeparator;
			}
			String formatted = mDecimalFormat.format( new BigDecimal( number ) );
			if(number.startsWith( "." )){
				formatted = formatted.substring( 1 ); // remove leading zero
			}else if ( number.endsWith( "." ) ) {
				formatted += decimalSeparator;
			}
			return formatted;
		};
		return FormatUtils.formatExpression( text, formatter, symbols );
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize( widthMeasureSpec ) - getPaddingStart() - getPaddingEnd();

		float size = getVariableTextSize( getText(), width );
		if ( size != getTextSize() ) {
			setTextSize( TypedValue.COMPLEX_UNIT_PX, size );
		}

		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
	}

	private float getVariableTextSize(CharSequence text, float width) {
		TextPaint paint = new TextPaint();
		paint.set( getPaint() );

		float lastFitTextSize = mMinimumTextSize;
		while ( lastFitTextSize < mMaximumTextSize ) {
			paint.setTextSize( Math.min( lastFitTextSize + mStepTextSize, mMaximumTextSize ) );
			if ( Layout.getDesiredWidth( text, paint ) > width ) {
				break;
			}
			lastFitTextSize = paint.getTextSize();
		}

		return lastFitTextSize;
	}

}