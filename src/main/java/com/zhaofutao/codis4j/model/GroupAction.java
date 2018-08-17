package com.zhaofutao.codis4j.model;

public class GroupAction {
	private String state;
	private int target_id;

	@Override
	public String toString() {
		return "GroupAction [state=" + state + ", target_id=" + target_id + "]";
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getState() {
		return state;
	}

	public int getTarget_id() {
		return target_id;
	}

	public void setTarget_id(int target_id) {
		this.target_id = target_id;
	}
}
