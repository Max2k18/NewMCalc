package com.maxsavteam.newmcalc2.fragments.fragment1;

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
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxsavteam.newmcalc2.R;

public class Fragment1 extends Fragment {
	private final Context mContext;
	private final View.OnLongClickListener[] mLongClickListeners;

	public Fragment1(InitializationObject initializationObject) {
		mContext = initializationObject.getContext();
		this.mLongClickListeners = initializationObject.getLongClickListeners();
	}

	@Override
	public View getView() {
		return view;
	}

	private View view;

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
			btn.setOnLongClickListener( mLongClickListeners[ 0 ] );
			String num = btn.getText().toString().substring( 0, 1 );
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
				btn.setText( Html.fromHtml( num + "<sup><small><small><small>" + arr[ ii ] + "</small></small></small></sup>", Html.FROM_HTML_MODE_COMPACT ) );
			} else {
				btn.setText( Html.fromHtml( num + "<sup><small><small><small>" + arr[ ii ] + "</small></small></small></sup>" ) );
			}
		}
		Button b = view.findViewById( R.id.btnCalc );
		b.setOnLongClickListener( mLongClickListeners[ 1 ] );

		ImageButton imageButton = view.findViewById( R.id.btnDelete );
		imageButton.setOnLongClickListener( mLongClickListeners[ 2 ] );

		WindowManager windowManager = (WindowManager) mContext.getSystemService( Context.WINDOW_SERVICE );
		if ( mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && windowManager != null ) {
			TableLayout tbl = view.findViewById( R.id.tbl );
			ViewGroup.LayoutParams tblLayoutParams = tbl.getLayoutParams();
			Display display = windowManager.getDefaultDisplay();
			Point displaySize = new Point();
			display.getSize( displaySize );
			tblLayoutParams = view.getLayoutParams();
			tblLayoutParams.height = displaySize.y;
			view.setLayoutParams( tblLayoutParams );
		}
		return view;
	}

	public static class InitializationObject {
		private Context mContext;
		private View.OnLongClickListener[] mLongClickListeners;
		private int mActionBarHeight;

		/*int getActionBarHeight() {
			return mActionBarHeight;
		}

		public FragmentOneInitializationObject setActionBarHeight(int actionBarHeight) {
			mActionBarHeight = actionBarHeight;
			return this;
		}*/

		public Context getContext() {
			return mContext;
		}

		public InitializationObject setContext(Context context) {
			mContext = context;
			return this;
		}

		View.OnLongClickListener[] getLongClickListeners() {
			return mLongClickListeners;
		}

		public InitializationObject setLongClickListeners(View.OnLongClickListener[] longClickListeners) {
			mLongClickListeners = longClickListeners;
			return this;
		}
	}
}
