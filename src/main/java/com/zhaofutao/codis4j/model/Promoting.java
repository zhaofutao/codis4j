package com.zhaofutao.codis4j.model;

public class Promoting {
	private int index;
	private String state;

	@Override
	public String toString() {
		return "Promoting [index=" + index + ", state=" + state + "]";
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

}
