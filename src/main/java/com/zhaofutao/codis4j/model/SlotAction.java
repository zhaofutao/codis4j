package com.zhaofutao.codis4j.model;

public class SlotAction {
	private int index;
	private String state;
	private int target_id;

	@Override
	public String toString() {
		return "SlotAction [index=" + index + ", state=" + state + ", target_id=" + target_id + "]";
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

	public int getTarget_id() {
		return target_id;
	}

	public void setTarget_id(int target_id) {
		this.target_id = target_id;
	}
}
