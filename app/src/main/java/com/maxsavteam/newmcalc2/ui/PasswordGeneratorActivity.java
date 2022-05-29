package com.maxsavteam.newmcalc2.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.ThemeActivity;
import com.maxsavteam.newmcalc2.adapters.PasswordGeneratorOptionsAdapter;
import com.maxsavteam.newmcalc2.types.PasswordGeneratorOption;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

public class PasswordGeneratorActivity extends ThemeActivity {

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final String DEFAULT_LOWERCASE_CHARACTERS = "qwertyuiopasdfghjklzxcvbnm";
	private static final String DEFAULT_UPPERCASE_CHARACTERS = "MNBVCXZLKJHGFDSAPOIUYTREWQ";
	private static final String DEFAULT_DIGITS = "0123456789";
	private static final String DEFAULT_SPECIAL_CHARACTERS = "£$&()*+[]@#^-_!?";

	private int passwordLength = 12;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if ( item.getItemId() == android.R.id.home ) {
			onBackPressed();
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_password_generator );

		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );
		ActionBar actionBar = getSupportActionBar();
		if ( actionBar != null ) {
			actionBar.setDisplayHomeAsUpEnabled( true );
		}

		List<PasswordGeneratorOption> generatorOptions = List.of(
				new PasswordGeneratorOption( "lowercase", getString( R.string.passgen_lowercase_characters ), DEFAULT_LOWERCASE_CHARACTERS ),
				new PasswordGeneratorOption(
						"uppercase", getString( R.string.passgen_uppercase_characters ),
						DEFAULT_UPPERCASE_CHARACTERS, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
				),
				new PasswordGeneratorOption(
						"digits", getString( R.string.passgen_digits ),
						DEFAULT_DIGITS, InputType.TYPE_CLASS_NUMBER
				),
				new PasswordGeneratorOption( "special", getString( R.string.passgen_special_characters ), DEFAULT_SPECIAL_CHARACTERS )
		);

		PasswordGeneratorOptionsAdapter adapter = new PasswordGeneratorOptionsAdapter( generatorOptions );
		generateOptionsLayout( adapter );

		EditText editTextPasswordLength = findViewById( R.id.edittext_password_length );
		editTextPasswordLength.setText( String.valueOf( passwordLength ) );
		editTextPasswordLength.addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				boolean valid = false;
				int i = 4;
				try {
					i = Integer.parseInt( String.valueOf( s ) );
					if ( i >= 4 && i <= 30 ) {
						valid = true;
					}
				} catch (NumberFormatException ignored) {
				}
				if ( valid ) {
					passwordLength = i;
				} else {
					editTextPasswordLength.setError( getString( R.string.passgen_length_out_of_range ) );
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		} );

		findViewById( R.id.btn_pass_length_minus ).setOnClickListener( v->{
			if ( passwordLength > 4 ) {
				passwordLength--;
				editTextPasswordLength.setText( String.valueOf( passwordLength ) );
			}
		} );

		findViewById( R.id.btn_pass_length_plus ).setOnClickListener( v->{
			if ( passwordLength < 30 ) {
				passwordLength++;
				editTextPasswordLength.setText( String.valueOf( passwordLength ) );
			}
		} );

		findViewById( R.id.btn_generate_password ).setOnClickListener( v->performPasswordGeneration( adapter.getCharactersOfSelectedCategories() ) );

		List<CharSequence> allCategoriesCharactersList = new ArrayList<>();
		for (PasswordGeneratorOption option : generatorOptions)
			allCategoriesCharactersList.add( option.getDefaultCategoryCharacters() );

		performPasswordGeneration( allCategoriesCharactersList );

	}

	private void generateOptionsLayout(PasswordGeneratorOptionsAdapter adapter) {
		LinearLayout layout = findViewById( R.id.layout_password_generator_options );
		for (int i = 0; i < adapter.getItemCount(); i++) {
			PasswordGeneratorOptionsAdapter.ViewHolder holder =
					adapter.onCreateViewHolder( layout, adapter.getItemViewType( i ) );
			layout.addView( holder.itemView );
			adapter.onBindViewHolder( holder, i );
		}
	}

	private void performPasswordGeneration(List<CharSequence> categoriesCharacters) {
		CharSequence password = generatePassword( categoriesCharacters );
		TextView textView = findViewById( R.id.textview_generated_password );
		textView.setText( password );
	}

	// password generator supports emojis, that's why code looks so strange
	private CharSequence generatePassword(List<CharSequence> categoriesCharacters) {
		SpannableStringBuilder sb = new SpannableStringBuilder();
		for (int i = 0; i < passwordLength; i++) {
			int categoryIndex = RANDOM.nextInt( categoriesCharacters.size() );
			CharSequence characters = categoriesCharacters.get( categoryIndex );
			List<Integer> list = characters.codePoints().boxed().collect( Collectors.toList() );
			int len = list.size();
			if ( len > 0 ) {
				int index = RANDOM.nextInt( len );
				sb.append( new String( Character.toChars( list.get( index ) ) ) );
			}
		}
		return sb;
	}

}