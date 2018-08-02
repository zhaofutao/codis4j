package com.zhaofutao.codis4j.model;

public class Slot {
	private int id;
	private int group_id;
	private SlotAction action;

	@Override
	public String toString() {
		return "Slot [id=" + id + ", group_id=" + group_id + ", action=" + action + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGroup_id() {
		return group_id;
	}

	public void setGroup_id(int group_id) {
		this.group_id = group_id;
	}

	public SlotAction getAction() {
		return action;
	}

	public void setAction(SlotAction action) {
		this.action = action;
	}
}
