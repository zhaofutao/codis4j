package codis4j.model;

public class GroupServer {
	private String server;
	private String datacenter;
	private GroupAction action;
	private boolean replica_group;

	@Override
	public String toString() {
		return "GroupServer [server=" + server + ", datacenter=" + datacenter + ", action=" + action
				+ ", replica_group=" + replica_group + "]";
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getDatacenter() {
		return datacenter;
	}

	public void setDatacenter(String datacenter) {
		this.datacenter = datacenter;
	}

	public GroupAction getAction() {
		return action;
	}

	public void setAction(GroupAction action) {
		this.action = action;
	}

	public boolean isReplica_group() {
		return replica_group;
	}

	public void setReplica_group(boolean replica_group) {
		this.replica_group = replica_group;
	}

}
