package com.maxsavteam.newmcalc2.entity;

import android.text.InputType;

public class PasswordGeneratorOption {

	private final String categoryId;
	private final String categoryName;
	private final String defaultCategoryCharacters;
	private final int inputType;

	public PasswordGeneratorOption(String categoryId, String categoryName, String defaultCategoryCharacters) {
		this(categoryId, categoryName, defaultCategoryCharacters, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS );
	}

	public PasswordGeneratorOption(String categoryId, String categoryName, String defaultCategoryCharacters, int inputType) {
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.defaultCategoryCharacters = defaultCategoryCharacters;
		this.inputType = inputType;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public String getDefaultCategoryCharacters() {
		return defaultCategoryCharacters;
	}

	public int getInputType() {
		return inputType;
	}
}
