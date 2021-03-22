package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.maxsavteam.newmcalc2.R;

import java.util.ArrayList;

public class CalculatorEditText extends androidx.appcompat.widget.AppCompatEditText {

	private final float mMaximumTextSize;
	private final float mMinimumTextSize;
	private final float mStepTextSize;
	private final ArrayList<TextListener> mTextListeners = new ArrayList<>();

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
				(mMaximumTextSize - mMinimumTextSize) / 3 );
		boolean showKeyboard = a.getBoolean( R.styleable.CalculatorEditText_showKeyboardOnFocus, false );
		setShowSoftInputOnFocus( showKeyboard );
		a.recycle();
	}

	@Override
	public boolean onTextContextMenuItem(int id) {
		switch ( id ){
			case android.R.id.paste:
			case android.R.id.cut:
				return false;
			default:
				return super.onTextContextMenuItem( id );
		}
	}

	@FunctionalInterface
	public interface TextListener{
		void onTextChanged();
	}

	public void addListener(TextListener listener){
		mTextListeners.add( listener );
	}

	public void removeListener(TextListener listener){
		mTextListeners.remove( listener );
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		if(mTextListeners != null) {
			for (TextListener listener : mTextListeners) {
				if ( listener != null ) {
					listener.onTextChanged();
				}
			}
		}
		super.onTextChanged( text, start, lengthBefore, lengthAfter );
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize( widthMeasureSpec ) - getPaddingStart() - getPaddingEnd();

		float size = getVariableTextSize( getText(), width );
		if(size != getTextSize())
			setTextSize( TypedValue.COMPLEX_UNIT_PX, size );

		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
	}

	private float getVariableTextSize(CharSequence text, float width){
		TextPaint paint = new TextPaint();
		paint.set( getPaint() );

		float lastFitTextSize = mMinimumTextSize;
		while(lastFitTextSize < mMaximumTextSize){
			paint.setTextSize( Math.min( lastFitTextSize + mStepTextSize, mMaximumTextSize ) );
			if( Layout.getDesiredWidth(text, paint) > width)
				break;
			lastFitTextSize = paint.getTextSize();
		}

		return lastFitTextSize;
	}

}