package com.zhaofutao.codis4j.model;

import java.util.List;

import com.zhaofutao.codis4j.util.RandomUtil;

public class Group {
	private int id;
	private List<GroupServer> servers;
	private Promoting promoting;
	private boolean out_of_sync;

	@Override
	public String toString() {
		return "Group [id=" + id + ", servers=" + servers + ", promoting=" + promoting + ", out_of_sync=" + out_of_sync
				+ "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<GroupServer> getServers() {
		return servers;
	}

	public GroupServer getOneServer() {
		if (servers.size() > 0) {
			return servers.get(RandomUtil.rand(servers.size() - 1));
		}
		return null;
	}

	public GroupServer getMasterServer() {
		if (servers.size() > 0) {
			return servers.get(0);
		}
		return null;
	}

	public void setServers(List<GroupServer> servers) {
		this.servers = servers;
	}

	public Promoting getPromoting() {
		return promoting;
	}

	public void setPromoting(Promoting promoting) {
		this.promoting = promoting;
	}

	public boolean isOut_of_sync() {
		return out_of_sync;
	}

	public void setOut_of_sync(boolean out_of_sync) {
		this.out_of_sync = out_of_sync;
	}
}
