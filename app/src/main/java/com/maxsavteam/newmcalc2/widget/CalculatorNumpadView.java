package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class CalculatorNumpadView extends NumpadView {
    public CalculatorNumpadView(Context context) {
        this(context, null);
    }

    public CalculatorNumpadView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalculatorNumpadView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CalculatorNumpadView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setCalcButton(Button button) {
        LinearLayout lastRow = findViewById(LAST_ROW_ID);
        lastRow.findViewById(MINUS_BUTTON_ID).setVisibility(GONE);
        lastRow.addView(button, getDefaultParamsForElements());
    }

}
