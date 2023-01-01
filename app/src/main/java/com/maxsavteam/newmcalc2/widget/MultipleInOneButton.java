package com.maxsavteam.newmcalc2.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import java.util.List;

public class MultipleInOneButton extends AppCompatButton {

	private boolean hasButtons = false;
	private View.OnClickListener onClickListener;

	public MultipleInOneButton(@NonNull Context context) {
		this( context, null );
	}

	public MultipleInOneButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
		this( context, attrs, 0 );
	}

	public MultipleInOneButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		super( context, attrs, defStyleAttr );
	}

	public void setButtons(List<MultipleInOneButton> buttons){
		PopupWindow popupWindow = new PopupWindow( getContext() );
		popupWindow.setContentView( createPopupContentView( buttons, popupWindow ) );
		popupWindow.setOutsideTouchable( true );

		super.setOnLongClickListener( v -> {
			popupWindow.showAsDropDown( this, 0, (int) (-getHeight() * 1.75) );
			return true;
		} );

		hasButtons = !buttons.isEmpty();
		invalidate();
	}

	private View createPopupContentView(List<MultipleInOneButton> buttons, PopupWindow popupWindow){
		LinearLayout linearLayout = new LinearLayout( getContext() );
		linearLayout.setOrientation( LinearLayout.HORIZONTAL );
		int margin = (int) TypedValue
				.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 10f,
						getResources().getDisplayMetrics()
				);
		for(int i = 0; i < buttons.size(); i++) {
			var b = buttons.get( i );

			ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT );
			if(i > 0)
				layoutParams.setMarginStart( margin );
			linearLayout.addView( b, layoutParams );

			var listener = b.getOnClickListener();
			b.setOnClickListener( v->{
				listener.onClick( v );
				popupWindow.dismiss();
			} );
		}
		int padding = (int) (getResources().getDisplayMetrics().density * 8);
		linearLayout.setPadding( padding, 0, padding, 0 );
		return linearLayout;
	}

	@Override
	public void setOnLongClickListener(@Nullable @org.jetbrains.annotations.Nullable View.OnLongClickListener l) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
		super.setOnClickListener( onClickListener );
	}

	public OnClickListener getOnClickListener() {
		return onClickListener;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw( canvas );
		// draw dot at the right bottom corner if has buttons
		if(hasButtons){
			float x = getWidth() - getPaddingRight() - 20;
			float y = getHeight() - getPaddingBottom() - 20;
			canvas.drawCircle( x, y, 10, getPaint() );
		}
	}
}
