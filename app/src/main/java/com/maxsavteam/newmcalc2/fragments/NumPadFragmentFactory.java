package com.maxsavteam.newmcalc2.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.adapters.ViewPagerAdapter;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class NumPadFragmentFactory implements ViewPagerAdapter.ViewPagerFragmentFactory {

	public static final int TYPE = 1;

	private final Context context;
	private final View.OnLongClickListener calculateButtonLongClickListener;

	private ArrayList<ButtonConfiguration> buttonConfigurations;

	private final View.OnClickListener justInsertOnClick;
	private final View.OnClickListener insertBracketsOnClick;
	private final View.OnClickListener insertFunctionOnClick;
	private final View.OnClickListener insertBinaryOperatorOnClick;
	private final View.OnClickListener insertSuffixOperatorOnClick;

	public NumPadFragmentFactory(Context context,
								 View.OnLongClickListener calculateButtonLongClickListener,
								 View.OnClickListener justInsertOnClick,
								 View.OnClickListener insertBracketsOnClick,
								 View.OnClickListener insertFunctionOnClick,
								 View.OnClickListener insertBinaryOperatorOnClick,
								 View.OnClickListener insertSuffixOperatorOnClick) {
		this.context = context;
		this.calculateButtonLongClickListener = calculateButtonLongClickListener;
		this.justInsertOnClick = justInsertOnClick;
		this.insertBracketsOnClick = insertBracketsOnClick;
		this.insertFunctionOnClick = insertFunctionOnClick;
		this.insertBinaryOperatorOnClick = insertBinaryOperatorOnClick;
		this.insertSuffixOperatorOnClick = insertSuffixOperatorOnClick;

		setupButtonConfigurations();
	}

	private void setupButtonConfigurations() {
		if ( buttonConfigurations != null ) {
			buttonConfigurations.clear();
		}

		buttonConfigurations = new ArrayList<>( Arrays.asList(
				new ButtonConfiguration( context.getString( R.string.simple_open_bracket ), insertBracketsOnClick ),
				new ButtonConfiguration( context.getString( R.string.simple_close_bracket ), insertBracketsOnClick ),
				new ButtonConfiguration( context.getString( R.string.semicolon ), justInsertOnClick ),

				new ButtonConfiguration( context.getString( R.string.sqrt ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.pow_sign ), insertBinaryOperatorOnClick ),
				new ButtonConfiguration( context.getString( R.string.percent ), insertSuffixOperatorOnClick ),

				new ButtonConfiguration( context.getString( R.string.sin ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.cos ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.tangent ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.cotangent ), insertFunctionOnClick ),

				new ButtonConfiguration( context.getString( R.string.rad ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.deg ), insertFunctionOnClick ),

				new ButtonConfiguration( context.getString( R.string.pi ), justInsertOnClick ),
				new ButtonConfiguration( context.getString( R.string.fi ), justInsertOnClick ),
				new ButtonConfiguration( context.getString( R.string.euler_constant ), justInsertOnClick ),
				new ButtonConfiguration( context.getString( R.string.factorial ), insertSuffixOperatorOnClick ),

				new ButtonConfiguration( context.getString( R.string.asin ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.acos ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.arctangent ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.arccotangent ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.log ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.ln ), insertFunctionOnClick ),

				new ButtonConfiguration( context.getString( R.string.abs ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.average_function ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.gcd ), insertFunctionOnClick ),
				new ButtonConfiguration( context.getString( R.string.lcm ), insertFunctionOnClick ),

				new ButtonConfiguration( context.getString( R.string.round_open_bracket ), insertBracketsOnClick ),
				new ButtonConfiguration( context.getString( R.string.round_close_bracket ), insertBracketsOnClick ),
				new ButtonConfiguration( context.getString( R.string.floor_open_bracket ), insertBracketsOnClick ),
				new ButtonConfiguration( context.getString( R.string.floor_close_bracket ), insertBracketsOnClick ),
				new ButtonConfiguration( context.getString( R.string.ceil_open_bracket ), insertBracketsOnClick ),
				new ButtonConfiguration( context.getString( R.string.ceil_close_bracket ), insertBracketsOnClick )
		) );
	}

	@Override
	public View justCreateView(ViewGroup parent) {
		return LayoutInflater.from( context ).inflate( R.layout.numpad_fragment_layout, parent, false );
	}

	@Override
	public void bindView(View view, int parentHeight) {
		setupButtonConfigurations();

		Button b = view.findViewById( R.id.btnCalc );
		b.setOnLongClickListener( calculateButtonLongClickListener );

		Locale locale = context.getResources().getConfiguration().getLocales().get( 0 );
		DecimalFormatSymbols symbols = new DecimalFormatSymbols( locale );
		b = view.findViewById( R.id.btnDot );
		b.setText( String.valueOf( symbols.getDecimalSeparator() ) );

		LinearLayout layout = view.findViewById( R.id.num_pad_scroll_layout );
		int buttonsPadding = (int) TypedValue
				.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						10f,
						context.getResources().getDisplayMetrics()
				);
		for (var p : buttonConfigurations) {
			Button button = new Button( context, null, R.style.MathOperationButtonStyle );
			button.setText( p.text );
			button.setOnClickListener( p.onClickListener );

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, parentHeight / 6 );
			button.setLayoutParams( params );

			button.setPadding( 0, buttonsPadding, 0, buttonsPadding );

			TypedValue typedValue = new TypedValue();
			button.setTextColor( Color.WHITE );

			context.getTheme().resolveAttribute( R.attr.selectableItemBackgroundBorderless, typedValue, true );
			button.setBackgroundResource( typedValue.resourceId );

			button.setTextSize( TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize( R.dimen.math_operations_text_size ) );
			button.setGravity( Gravity.CENTER );
			button.setTypeface( Typeface.DEFAULT_BOLD );

			layout.addView( button );
		}

		setupScrollViewArrows( view );
	}

	private void setupScrollViewArrows(View view) {
		ScrollView scrollView = view.findViewById( R.id.scrollView2 );
		View arrowUpView = view.findViewById( R.id.numpad_arrow_up );
		View arrowDownView = view.findViewById( R.id.numpad_arrow_down );
		hide( arrowUpView );
		scrollView.setOnScrollChangeListener( (v, scrollX, scrollY, oldScrollX, oldScrollY)->{
			if ( scrollY == 0 ) { // up
				hide( arrowUpView );
				show( arrowDownView );
			} else if ( scrollView.getChildAt( 0 ).getBottom() <= ( scrollView.getHeight() + scrollView.getScrollY() ) ) {
				hide( arrowDownView );
				show( arrowUpView );
			} else {
				show( arrowUpView );
				show( arrowDownView );
			}
		} );
	}

	private void hide(View view) {
		view.animate().alpha( 0f ).setDuration( 100 ).start();
	}

	private void show(View view) {
		view.animate().alpha( 1f ).setDuration( 100 ).start();
	}

	@Override
	public int getType() {
		return TYPE;
	}

	private static class ButtonConfiguration {
		public final String text;
		public final View.OnClickListener onClickListener;

		public ButtonConfiguration(String text, View.OnClickListener onClickListener) {
			this.text = text;
			this.onClickListener = onClickListener;
		}
	}

}
