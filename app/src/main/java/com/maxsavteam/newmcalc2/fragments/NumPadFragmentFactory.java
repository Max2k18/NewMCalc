package com.maxsavteam.newmcalc2.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.crashlytics.internal.model.ImmutableList;
import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.adapters.ViewPagerAdapter;
import com.maxsavteam.newmcalc2.types.Pair;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class NumPadFragmentFactory implements ViewPagerAdapter.ViewPagerFragmentFactory {

	public static final int TYPE = 1;

	private final Context context;
	private final View.OnLongClickListener mCalculateButtonLongClickListener;

	private final List<Pair<String, View.OnClickListener>> scrollViewContent;

	public NumPadFragmentFactory(Context c,
								 View.OnLongClickListener calculateButtonLongClickListener,
								 View.OnClickListener justInsertOnClick,
								 View.OnClickListener insertBracketsOnClick,
								 View.OnClickListener insertFunctionOnClick,
								 View.OnClickListener insertBinaryOperatorOnClick,
								 View.OnClickListener insertSuffixOperatorOnClick) {
		this.context = c;
		mCalculateButtonLongClickListener = calculateButtonLongClickListener;

		scrollViewContent = Arrays.asList(
				new Pair<>( c.getString( R.string.simple_open_bracket ), insertBracketsOnClick ),
				new Pair<>( c.getString( R.string.simple_close_bracket ), insertBracketsOnClick ),
				new Pair<>( c.getString( R.string.semicolon ), justInsertOnClick ),

				new Pair<>( c.getString( R.string.sqrt ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.pow_sign ), insertBinaryOperatorOnClick ),
				new Pair<>( c.getString( R.string.percent ), insertSuffixOperatorOnClick ),

				new Pair<>( c.getString( R.string.sin ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.cos ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.tangent ), insertFunctionOnClick ),

				new Pair<>( c.getString( R.string.rad ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.deg ), insertFunctionOnClick ),

				new Pair<>( c.getString( R.string.pi ), justInsertOnClick ),
				new Pair<>( c.getString( R.string.fi ), justInsertOnClick ),
				new Pair<>( c.getString( R.string.euler_constant ), justInsertOnClick ),
				new Pair<>( c.getString( R.string.factorial ), insertSuffixOperatorOnClick ),

				new Pair<>( c.getString( R.string.asin ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.acos ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.arctangent ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.log ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.ln ), insertFunctionOnClick ),

				new Pair<>( c.getString( R.string.abs ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.average_function ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.gcd ), insertFunctionOnClick ),
				new Pair<>( c.getString( R.string.lcm ), insertFunctionOnClick ),

				new Pair<>( c.getString( R.string.round_open_bracket ), insertBracketsOnClick ),
				new Pair<>( c.getString( R.string.round_close_bracket ), insertBracketsOnClick ),
				new Pair<>( c.getString( R.string.floor_open_bracket ), insertBracketsOnClick ),
				new Pair<>( c.getString( R.string.floor_close_bracket ), insertBracketsOnClick ),
				new Pair<>( c.getString( R.string.ceil_open_bracket ), insertBracketsOnClick ),
				new Pair<>( c.getString( R.string.ceil_close_bracket ), insertBracketsOnClick )
		);
	}

	@Override
	public View justCreateView(ViewGroup parent) {
		return LayoutInflater.from( context ).inflate( R.layout.numpad_fragment_layout, parent, false );
	}

	@Override
	public void bindView(View view, int parentHeight) {
		Button b = view.findViewById( R.id.btnCalc );
		b.setOnLongClickListener( mCalculateButtonLongClickListener );

		Locale locale;
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
			locale = context.getResources().getConfiguration().getLocales().get( 0 );
		}else{
			locale = context.getResources().getConfiguration().locale;
		}
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
		b = view.findViewById( R.id.btnDot );
		b.setText( String.valueOf( symbols.getDecimalSeparator() ) );

		LinearLayout layout = view.findViewById( R.id.num_pad_scroll_layout );
		int buttonsPadding = (int) TypedValue
				.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						10f,
						context.getResources().getDisplayMetrics()
				);
		for(var p : scrollViewContent){
			Button button = new Button( context, null, R.style.MathOperationButtonStyle );
			button.setText( p.first );
			button.setOnClickListener( p.second );

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, parentHeight / 5 );
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
	}

	@Override
	public int getType() {
		return TYPE;
	}
}
