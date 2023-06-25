package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;

public class CustomTextView extends AppCompatTextView {

    private final ArrayList<TextListener> mTextListeners = new ArrayList<>();

    public CustomTextView(@NonNull Context context) {
        super(context);
    }

    public CustomTextView(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface TextListener {
        void onTextChanged();
    }

    public void addListener(TextListener textListener) {
        mTextListeners.add(textListener);
    }

    public void removeListener(TextListener textListener) {
        mTextListeners.remove(textListener);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (mTextListeners != null) {
            for (TextListener listener : mTextListeners) {
                if (listener != null) {
                    listener.onTextChanged();
                }
            }
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }
}
