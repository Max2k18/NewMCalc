package com.maxsavteam.newmcalc2.fragment.viewpager;

import static com.maxsavteam.newmcalc2.fragment.viewpager.NumPadFragmentFactory.ButtonConfiguration.create;

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

import androidx.annotation.DrawableRes;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.adapters.ViewPagerAdapter;
import com.maxsavteam.newmcalc2.widget.CalculatorNumpadView;
import com.maxsavteam.newmcalc2.widget.MultipleInOneButton;
import com.maxsavteam.newmcalc2.widget.NumpadView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NumPadFragmentFactory implements ViewPagerAdapter.ViewPagerFragmentFactory {

	public static final int TYPE = 1;

	private final Context context;
	private final Configuration configuration;

	private ArrayList<ButtonConfiguration> buttonConfigurations;

	public NumPadFragmentFactory(Configuration configuration) {
		this.context = configuration.getContext();
		this.configuration = configuration;

		setupButtonConfigurations();
	}

	private String getButtonText(View view){
		return ((Button) view).getText().toString();
	}

	private void setupButtonConfigurations() {
		if ( buttonConfigurations != null ) {
			buttonConfigurations.clear();
		}

		View.OnClickListener insertBracketsOnClick =
				v->configuration.bracketButtonClickListener.onClick( getButtonText( v ) );
		View.OnClickListener justInsertOnClick =
				v->configuration.onJustInsertButtonClickListener.onClick( getButtonText( v ) );
		View.OnClickListener insertFunctionOnClick =
				v->configuration.functionButtonClickListener.onClick( getButtonText( v ) );
		View.OnClickListener insertBinaryOperatorOnClick =
				v->configuration.binaryOperatorButtonClickListener.onClick( getButtonText( v ) );
		View.OnClickListener insertSuffixOperatorOnClick =
				v->configuration.suffixOperatorButtonClickListener.onClick( getButtonText( v ) );

		buttonConfigurations = new ArrayList<>( Arrays.asList(
				create( context.getString( R.string.simple_open_bracket ), insertBracketsOnClick ),
				create( context.getString( R.string.simple_close_bracket ), insertBracketsOnClick ),
				create( context.getString( R.string.semicolon ), justInsertOnClick ),

				create( context.getString( R.string.sqrt ), insertFunctionOnClick ),
				create( context.getString( R.string.pow_sign ), insertBinaryOperatorOnClick ),
				create( context.getString( R.string.percent ), insertSuffixOperatorOnClick ),

				create( context.getString( R.string.sin ), insertFunctionOnClick,
						create( context.getString( R.string.asin ), insertFunctionOnClick ),
						create( context.getString( R.string.csc ), insertFunctionOnClick ) ),
				create( context.getString( R.string.cos ), insertFunctionOnClick,
						create( context.getString( R.string.acos ), insertFunctionOnClick ),
						create( context.getString( R.string.sec ), insertFunctionOnClick ) ),
				create( context.getString( R.string.tangent ), insertFunctionOnClick,
						create( context.getString( R.string.arctangent ), insertFunctionOnClick ) ),
				create( context.getString( R.string.cotangent ), insertFunctionOnClick,
						create( context.getString( R.string.arccotangent ), insertFunctionOnClick ) ),

				create( context.getString( R.string.degree_sign ), insertSuffixOperatorOnClick, create( context.getString( R.string.grad_sign ), insertSuffixOperatorOnClick ) ),

				create( context.getString( R.string.pi ), justInsertOnClick,
						create( context.getString( R.string.fi ), justInsertOnClick ),
						create( context.getString( R.string.euler_constant ), justInsertOnClick ) ),
				create( context.getString( R.string.factorial ), insertSuffixOperatorOnClick ),

				create( context.getString( R.string.log ), insertFunctionOnClick ),
				create( context.getString( R.string.ln ), insertFunctionOnClick ),

				create( context.getString( R.string.abs ), insertFunctionOnClick ),
				create( context.getString( R.string.average_function ), insertFunctionOnClick ),
				create( context.getString( R.string.gcd ), insertFunctionOnClick ),
				create( context.getString( R.string.lcm ), insertFunctionOnClick ),

				create( context.getString( R.string.round_open_bracket ), insertBracketsOnClick ),
				create( context.getString( R.string.round_close_bracket ), insertBracketsOnClick ),
				create( context.getString( R.string.floor_open_bracket ), insertBracketsOnClick ),
				create( context.getString( R.string.floor_close_bracket ), insertBracketsOnClick ),
				create( context.getString( R.string.ceil_open_bracket ), insertBracketsOnClick ),
				create( context.getString( R.string.ceil_close_bracket ), insertBracketsOnClick )
		) );
	}

	@Override
	public View justCreateView(ViewGroup parent) {
		return LayoutInflater.from( context ).inflate( R.layout.fragment_numpad, parent, false );
	}

	@Override
	public void bindView(View view, int parentHeight) {
		setupButtonConfigurations();

		CalculatorNumpadView numpadView = view.findViewById( R.id.numpad_view );
		numpadView.setCalcButton( createCalcButton() );
		numpadView.setDigitButtonOnClickListener( configuration.digitButtonClickListener );
		numpadView.setSeparatorOnClickListener( configuration.separatorButtonClickListener );
		numpadView.updateLocale();

		LinearLayout layout = view.findViewById( R.id.num_pad_scroll_layout );
		int buttonsPadding = (int) TypedValue
				.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP,
						10f,
						context.getResources().getDisplayMetrics()
				);
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute( R.attr.selectableItemBackgroundBorderless, typedValue, true );
		int backgroundResource = typedValue.resourceId;
		for (var p : buttonConfigurations) {
			MultipleInOneButton button = createButtonFromConfiguration( p, buttonsPadding, backgroundResource );
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, parentHeight / 6 );
			button.setLayoutParams( params );

			layout.addView( button );
		}

		setupScrollViewArrows( view );
	}

	private MultipleInOneButton createButtonFromConfiguration(ButtonConfiguration configuration, int buttonsPadding, @DrawableRes int backgroundResource) {
		MultipleInOneButton button = new MultipleInOneButton( context, null, R.style.MathOperationButtonStyle );
		button.setText( configuration.text );
		button.setOnClickListener( configuration.onClickListener );

		button.setPadding( 0, buttonsPadding, 0, buttonsPadding );

		button.setTextColor( Color.WHITE );

		button.setBackgroundResource( backgroundResource );

		button.setTextSize( TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize( R.dimen.math_operations_text_size ) );
		button.setGravity( Gravity.CENTER );
		button.setTypeface( Typeface.DEFAULT_BOLD );

		if(configuration.configurations.size() > 0){
			List<MultipleInOneButton> buttons = new ArrayList<>();
			for(var p : configuration.configurations)
				buttons.add( createButtonFromConfiguration( p, buttonsPadding, backgroundResource ) );
			button.setButtons( buttons );
		}

		return button;
	}

	private Button createCalcButton() {
		Button button = new Button( context, null, 0, R.style.NumPadButtonStyle );
		button.setText( R.string.eq );
		button.setTextColor( getColorFromAttribute( R.attr.colorAccent ) );
		button.setOnClickListener( configuration.calculateButtonClickListener );
		button.setOnLongClickListener( configuration.calculateButtonLongClickListener );
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

	public static class ButtonConfiguration {
		public final String text;
		public final View.OnClickListener onClickListener;
		public final List<ButtonConfiguration> configurations;

		public ButtonConfiguration(String text, View.OnClickListener onClickListener, ButtonConfiguration... configurations) {
			this.text = text;
			this.onClickListener = onClickListener;
			this.configurations = Arrays.asList( configurations );
		}
		
		public static ButtonConfiguration create(String text, View.OnClickListener onClickListener, ButtonConfiguration... configurations) {
			return new ButtonConfiguration( text, onClickListener, configurations );
		}		
	}

	public interface ButtonClickListener {
		void onClick(String text);
	}

	public static class Configuration {

		private final Context context;

		private View.OnClickListener calculateButtonClickListener;
		private View.OnLongClickListener calculateButtonLongClickListener;
		private NumpadView.DigitButtonOnClickListener digitButtonClickListener;
		private NumpadView.SeparatorButtonOnClickListener separatorButtonClickListener;
		private ButtonClickListener bracketButtonClickListener;
		private ButtonClickListener functionButtonClickListener;
		private ButtonClickListener binaryOperatorButtonClickListener;
		private ButtonClickListener suffixOperatorButtonClickListener;
		private ButtonClickListener onJustInsertButtonClickListener;

		public Configuration(Context context) {
			this.context = context;
		}

		public Context getContext() {
			return context;
		}

		public Configuration setCalculateButtonClickListener(View.OnClickListener calculateButtonClickListener) {
			this.calculateButtonClickListener = calculateButtonClickListener;
			return this;
		}

		public Configuration setCalculateButtonLongClickListener(View.OnLongClickListener calculateButtonLongClickListener) {
			this.calculateButtonLongClickListener = calculateButtonLongClickListener;
			return this;
		}

		public Configuration setDigitButtonClickListener(NumpadView.DigitButtonOnClickListener digitButtonClickListener) {
			this.digitButtonClickListener = digitButtonClickListener;
			return this;
		}

		public Configuration setSeparatorButtonClickListener(NumpadView.SeparatorButtonOnClickListener separatorButtonClickListener) {
			this.separatorButtonClickListener = separatorButtonClickListener;
			return this;
		}

		public Configuration setBracketButtonClickListener(ButtonClickListener bracketButtonClickListener) {
			this.bracketButtonClickListener = bracketButtonClickListener;
			return this;
		}

		public Configuration setFunctionButtonClickListener(ButtonClickListener functionButtonClickListener) {
			this.functionButtonClickListener = functionButtonClickListener;
			return this;
		}

		public Configuration setBinaryOperatorButtonClickListener(ButtonClickListener binaryOperatorButtonClickListener) {
			this.binaryOperatorButtonClickListener = binaryOperatorButtonClickListener;
			return this;
		}

		public Configuration setSuffixOperatorButtonClickListener(ButtonClickListener suffixOperatorButtonClickListener) {
			this.suffixOperatorButtonClickListener = suffixOperatorButtonClickListener;
			return this;
		}

		public Configuration setOnJustInsertButtonClickListener(ButtonClickListener onJustInsertButtonClickListener) {
			this.onJustInsertButtonClickListener = onJustInsertButtonClickListener;
			return this;
		}
	}

}
