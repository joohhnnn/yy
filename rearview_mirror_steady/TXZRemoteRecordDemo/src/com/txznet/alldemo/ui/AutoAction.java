package com.txznet.alldemo.ui;

public abstract class AutoAction {
	public String getName() {
		return name;
	}

	public AutoAction setName(String name) {
		this.name = name;
		return this;
	}

	private String name;

	public abstract void aciton();
}
