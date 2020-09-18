package com.maxsavteam.newmcalc2.widget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.content.Context;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.utils.Utils;

public class CustomAlertDialogBuilder extends AlertDialog.Builder {
	public CustomAlertDialogBuilder(Context context) {
		this( context, R.style.DarkAlertDialog );
	}

	public CustomAlertDialogBuilder(Context context, int themeResId) {
		super( context, themeResId );
	}

	@NonNull
	@Override
	public AlertDialog create() {
		AlertDialog alertDialog = super.create();
		Utils.recolorButtons( alertDialog, getContext() );

		return alertDialog;
	}

	@Override
	public AlertDialog show() {
		AlertDialog alertDialog = create();
		alertDialog.show();
		return alertDialog;
	}
}
