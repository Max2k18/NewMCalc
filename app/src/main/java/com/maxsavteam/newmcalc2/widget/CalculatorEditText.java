package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CalculatorEditText extends androidx.appcompat.widget.AppCompatEditText {

	public CalculatorEditText(@NonNull Context context) {
		super( context );
	}

	public CalculatorEditText(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
		super( context, attrs );
	}

	public CalculatorEditText(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		super( context, attrs, defStyleAttr );
	}

	private final ArrayList<TextListener> mTextListeners = new ArrayList<>();

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
}