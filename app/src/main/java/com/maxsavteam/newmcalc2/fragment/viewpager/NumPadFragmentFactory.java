package com.maxsavteam.newmcalc2.fragment.viewpager;

import android.content.Context;
import android.content.res.TypedArray;
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
import com.maxsavteam.newmcalc2.widget.NumpadView;

import java.util.ArrayList;
import java.util.Arrays;

public class NumPadFragmentFactory implements ViewPagerAdapter.ViewPagerFragmentFactory {

	public static final int TYPE = 1;

	private final Context context;
	private final View.OnClickListener calculateButtonClickListener;
	private final View.OnLongClickListener calculateButtonLongClickListener;
	private final NumpadView.DigitButtonOnClickListener digitButtonOnClickListener;
	private final NumpadView.SeparatorButtonOnClickListener separatorButtonOnClickListener;

	private ArrayList<ButtonConfiguration> buttonConfigurations;

	private final View.OnClickListener justInsertOnClick;
	private final View.OnClickListener insertBracketsOnClick;
	private final View.OnClickListener insertFunctionOnClick;
	private final View.OnClickListener insertBinaryOperatorOnClick;
	private final View.OnClickListener insertSuffixOperatorOnClick;

	// TODO: 04.07.2022 refactor
	public NumPadFragmentFactory(Context context,
								 View.OnClickListener calculateButtonClickListener,
								 View.OnLongClickListener calculateButtonLongClickListener,
								 NumpadView.DigitButtonOnClickListener digitButtonOnClickListener,
								 NumpadView.SeparatorButtonOnClickListener separatorButtonOnClickListener,
								 View.OnClickListener justInsertOnClick,
								 View.OnClickListener insertBracketsOnClick,
								 View.OnClickListener insertFunctionOnClick,
								 View.OnClickListener insertBinaryOperatorOnClick,
								 View.OnClickListener insertSuffixOperatorOnClick) {
		this.context = context;
		this.calculateButtonClickListener = calculateButtonClickListener;
		this.calculateButtonLongClickListener = calculateButtonLongClickListener;
		this.digitButtonOnClickListener = digitButtonOnClickListener;
		this.separatorButtonOnClickListener = separatorButtonOnClickListener;
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

				new ButtonConfiguration( context.getString( R.string.degree_sign ), insertSuffixOperatorOnClick ),
				new ButtonConfiguration( context.getString( R.string.grad_sign ), insertSuffixOperatorOnClick ),

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
		return LayoutInflater.from( context ).inflate( R.layout.fragment_numpad, parent, false );
	}

	@Override
	public void bindView(View view, int parentHeight) {
		setupButtonConfigurations();

		NumpadView numpadView = view.findViewById( R.id.numpad_view );
		numpadView.setCustomButton( createCalcButton(), NumpadView.CustomButtonPosition.RIGHT );
		numpadView.setDigitButtonOnClickListener( digitButtonOnClickListener );
		numpadView.setSeparatorOnClickListener( separatorButtonOnClickListener );
		numpadView.updateLocale();

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

	private Button createCalcButton() {
		Button button = new Button( context, null, 0, R.style.NumPadButtonStyle );
		button.setText( R.string.eq );
		button.setTextColor( getColorFromAttribute( R.attr.colorAccent ) );
		button.setOnClickListener( calculateButtonClickListener );
		button.setOnLongClickListener( calculateButtonLongClickListener );
		return button;
	}

	private int getColorFromAttribute(int attrId) {
		TypedArray array = context.getTheme().obtainStyledAttributes( new int[]{ attrId } );
		int color = array.getColor( 0, 0 );
		array.recycle();
		return color;
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
