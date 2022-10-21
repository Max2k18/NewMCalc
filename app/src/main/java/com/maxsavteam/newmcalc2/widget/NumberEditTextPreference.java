package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;

public class NumberEditTextPreference extends EditTextPreference {

   public NumberEditTextPreference(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
      super( context, attrs, defStyleAttr, defStyleRes );
      init();
   }

   public NumberEditTextPreference(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
      super( context, attrs, defStyleAttr );
      init();
   }

   public NumberEditTextPreference(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
      super( context, attrs );
      init();
   }

   public NumberEditTextPreference(@NonNull Context context) {
      super( context );
      init();
   }

   private void init(){
      setOnBindEditTextListener( editText->editText.setInputType( InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED ) );
   }

}
