package com.maxsavteam.newmcalc2.variables;

public class Variable {

	private String name;
	private String value;
	private final int tag;

	public Variable(String name, String value, int tag) {
		this.name = name;
		this.value = value;
		this.tag = tag;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getTag() {
		return tag;
	}
}
