package com.maxsavteam.newmcalc2.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maxsavteam.newmcalc2.R;
import com.maxsavteam.newmcalc2.types.PasswordGeneratorOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PasswordGeneratorOptionsAdapter extends RecyclerView.Adapter<PasswordGeneratorOptionsAdapter.ViewHolder> {

	private final List<PasswordGeneratorOption> options;
	private final Map<String, CharSequence> categoriesCharactersMap = new HashMap<>();
	private final Set<String> selectedCategoriesSet = new HashSet<>();

	public PasswordGeneratorOptionsAdapter(List<PasswordGeneratorOption> options) {
		this.options = options;
		for(PasswordGeneratorOption option : options){
			categoriesCharactersMap.put( option.getCategoryId(), option.getDefaultCategoryCharacters() );
			selectedCategoriesSet.add( option.getCategoryId() );
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_password_generator_option, parent, false ) );
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		PasswordGeneratorOption option = options.get( position );
		holder.textViewCategoryName.setText( option.getCategoryName() );

		holder.editTextCategoryCharacters.setText( option.getDefaultCategoryCharacters() );
		holder.editTextCategoryCharacters.setInputType( option.getInputType() );
		holder.editTextCategoryCharacters.addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				categoriesCharactersMap.put( option.getCategoryId(), s );
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		} );

		holder.checkBoxGeneratorOption.setChecked( true );
		holder.checkBoxGeneratorOption.setOnCheckedChangeListener( (buttonView, isChecked)->{
			if(isChecked)
				selectedCategoriesSet.add( option.getCategoryId() );
			else
				selectedCategoriesSet.remove( option.getCategoryId() );
		} );
	}

	public List<CharSequence> getCharactersOfSelectedCategories(){
		List<CharSequence> list = new ArrayList<>();
		for(String id : selectedCategoriesSet)
			list.add( categoriesCharactersMap.get( id ) );
		return list;
	}

	@Override
	public int getItemCount() {
		return options.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textViewCategoryName;
		private final EditText editTextCategoryCharacters;
		private final CheckBox checkBoxGeneratorOption;

		public ViewHolder(@NonNull View itemView) {
			super( itemView );

			textViewCategoryName = itemView.findViewById( R.id.textview_category_name );
			editTextCategoryCharacters = itemView.findViewById( R.id.edittext_category_characters );
			checkBoxGeneratorOption = itemView.findViewById( R.id.checkbox_generator_option );
		}
	}

}
