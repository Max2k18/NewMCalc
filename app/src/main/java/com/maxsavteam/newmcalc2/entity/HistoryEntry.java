package com.maxsavteam.newmcalc2.entity;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public final class HistoryEntry {
	private String example;
	private String answer;
	private String description;

	public HistoryEntry(String example, String answer) {
		this( example, answer, null );
	}

	public HistoryEntry(String example, String answer, @Nullable String description) {
		this.example = example;
		this.answer = answer;
		if(description == null || description.isEmpty())
			this.description = null;
		else
			this.description = description;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(@Nullable String description) {
		if(description == null || description.isEmpty()){
			this.description = null;
		}else {
			this.description = description;
		}
	}

	public JSONObject getJSON() throws JSONException {
		return new JSONObject()
				.put( "example", example )
				.put( "answer", answer )
				.put( "description", description );
	}

	@Override
	public String toString() {
		return "HistoryEntry{" +
				"example='" + example + '\'' +
				", answer='" + answer + '\'' +
				", description='" + description + '\'' +
				'}';
	}

	public static HistoryEntry create(String ex, String ans) {
		return new HistoryEntry( ex, ans );
	}
}
