package com.maxsavteam.newmcalc2.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxsavteam.newmcalc2.R;

public class NumPadFragment extends Fragment {
	private final Context mContext;
	private View view;

	private final View.OnLongClickListener mCalculateButtonLongClickListener;
	private final View.OnLongClickListener mDeleteButtonLongClickListener;
	private final View.OnLongClickListener mNumPadButtonsLongClickListener;

	public NumPadFragment(Context context, View.OnLongClickListener calculateButtonLongClickListener, View.OnLongClickListener deleteButtonLongClickListener, View.OnLongClickListener numPadButtonsLongClickListener) {
		mContext = context;
		mCalculateButtonLongClickListener = calculateButtonLongClickListener;
		mDeleteButtonLongClickListener = deleteButtonLongClickListener;
		mNumPadButtonsLongClickListener = numPadButtonsLongClickListener;
	}

	@Override
	public View getView() {
		return view;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		//view = inflater.inflate( R.layout.fragment_1, container, false );
		view = LayoutInflater.from( mContext ).inflate( R.layout.fragment_1, container, false );
		String[] arr = mContext.getResources().getStringArray( R.array.additional_chars );
		int[] btnIds = {
				R.id.btn7,
				R.id.btn8,
				R.id.btn4,
				R.id.btn5,
				R.id.btn1,
				R.id.btn2,

				R.id.btn9,
				R.id.btnMult,
				R.id.btn6,
				R.id.btnDiv,
				R.id.btn3,
				R.id.btnPlus,

				R.id.btnZero,
				R.id.btnDot,
				R.id.btnMinus
		};
		for (int ii = 0; ii < btnIds.length; ii++) {
			Button btn = view.findViewById( btnIds[ ii ] );
			btn.setTransformationMethod( null );
			btn.setOnLongClickListener( mNumPadButtonsLongClickListener );
			String num = btn.getText().toString().substring( 0, 1 );
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
				btn.setText( Html.fromHtml( num + "<sup><small><small><small>" + arr[ ii ] + "</small></small></small></sup>", Html.FROM_HTML_MODE_COMPACT ) );
			} else {
				btn.setText( Html.fromHtml( num + "<sup><small><small><small>" + arr[ ii ] + "</small></small></small></sup>" ) );
			}
		}


		Button b = view.findViewById( R.id.btnCalc );
		b.setOnLongClickListener( mCalculateButtonLongClickListener );

		ImageButton imageButton = view.findViewById( R.id.btnDelete );
		imageButton.setOnLongClickListener( mDeleteButtonLongClickListener );

		WindowManager windowManager = (WindowManager) mContext.getSystemService( Context.WINDOW_SERVICE );
		if ( mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && windowManager != null ) {
			Display display = windowManager.getDefaultDisplay();
			Point displaySize = new Point();
			display.getSize( displaySize );
			ViewGroup.LayoutParams tblLayoutParams = view.getLayoutParams();
			tblLayoutParams.height = displaySize.y;
			view.setLayoutParams( tblLayoutParams );
		}
		return view;
	}
}
