package com.zhaofutao.codis4j.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.SafeEncoder;

import com.zhaofutao.codis4j.CodisConnectionHandler;
import com.zhaofutao.codis4j.model.Group;
import com.zhaofutao.codis4j.model.GroupServer;
import com.zhaofutao.codis4j.model.Slot;
import com.zhaofutao.codis4j.util.CRC32Util;

public class DirectCodisConnectionHandleImpl implements CodisConnectionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(DirectCodisConnectionHandleImpl.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final int SLOT_SIZE = 1024;

	private static final String ZK_DIR_OF_GROUP = "/group";
	private static final String ZK_DIR_OF_SLOTS = "/slots";

	private final Map<Integer, Group> groups = new HashMap<Integer, Group>();
	private final Slot[] slots = new Slot[SLOT_SIZE];
	private Map<String, JedisPool> pools = new HashMap<String, JedisPool>();

	private CuratorFramework curatorClient;
	private String zkDir;
	private JedisPoolConfig poolConfig;
	private int connectionTimeoutMs;
	private int soTimeoutMs;
	@SuppressWarnings("unused")
	private String productName;
	private String productAuth;
	private int db;
	private String clientName;

	private PathChildrenCache group_watcher;
	private PathChildrenCache slots_watcher;

	public DirectCodisConnectionHandleImpl(CuratorFramework curatorClient, String zkDir, JedisPoolConfig poolConfig,
			int connectionTimeoutMs, int soTimeoutMs, int db, String productName, String productAuth,
			String clientName) {
		this.curatorClient = curatorClient;
		this.zkDir = zkDir;
		this.poolConfig = poolConfig;
		this.connectionTimeoutMs = connectionTimeoutMs;
		this.soTimeoutMs = soTimeoutMs;
		this.db = db;
		this.productName = productName;
		this.productAuth = productAuth;
		this.clientName = clientName;

		init();
	}

	@Override
	public Jedis getConnection(int groupId, boolean master) {
		int retry = 0;
		Jedis jedis = null;
		do {
			Group group = groups.get(groupId);
			if (group == null) {
				resetGroup();
				continue;
			}

			GroupServer server = master ? group.getMasterServer() : group.getOneServer();
			if (server == null) {
				resetGroup();
				continue;
			}

			JedisPool pool = pools.get(server.getServer());
			if (pool == null || pool.isClosed()) {
				resetPool(server.getServer());
				continue;
			}
			try {
				jedis = pool.getResource();
			} catch (Exception e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
				continue;
			}
			if (jedis == null) {
				resetPool(server.getServer());
				continue;
			}

			String pong = jedis.ping();
			if (pong == null || !pong.equalsIgnoreCase("pong")) {
				resetPool(server.getServer());
				continue;
			}
			return jedis;

		} while (jedis == null && retry++ < 10);

		return null;
	}

	@Override
	public int getGroupId(ByteBuffer key) {
		int slotNum = (int) (CRC32Util.crc32(SafeEncoder.encode(key.array())) % SLOT_SIZE);
		Slot slot = slots[slotNum];
		if (slot == null) {
			return -1;
		}
		return slot.getGroup_id();
	}

	@Override
	public int getGroupId(String key) {
		int slotNum = (int) (CRC32Util.crc32(key) % SLOT_SIZE);
		Slot slot = slots[slotNum];
		if (slot == null) {
			return -1;
		}
		return slot.getGroup_id();
	}

	@Override
	public void close() throws IOException {
		try {
			group_watcher.close();
			slots_watcher.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		curatorClient.close();
		for (Entry<String, JedisPool> entry : this.pools.entrySet()) {
			entry.getValue().close();
		}
		this.pools.clear();
	}

	private void init() {
		this.group_watcher = new PathChildrenCache(curatorClient, zkDir + ZK_DIR_OF_GROUP, true);
		this.group_watcher.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				logEvent(event);
				resetGroup();
				// group更新，需要重置pools;
				resetPools();
			}
		});

		try {
			this.group_watcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.slots_watcher = new PathChildrenCache(curatorClient, zkDir + ZK_DIR_OF_SLOTS, true);
		this.slots_watcher.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				logEvent(event);
				resetSlots();
				// slots不需要更新pools
				// resetPools();
			}
		});

		try {
			this.slots_watcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		resetGroup();
		resetSlots();
		resetPools();
	}

	private void logEvent(PathChildrenCacheEvent event) {
		StringBuilder msg = new StringBuilder("Receive child event: ");
		msg.append("type=").append(event.getType());
		ChildData data = event.getData();
		if (data != null) {
			msg.append(", path=").append(data.getPath());
			msg.append(", stat=").append(data.getStat());
			if (data.getData() != null) {
				msg.append(", bytes length=").append(data.getData().length);
			} else {
				msg.append(", no bytes");
			}
		} else {
			msg.append(", no data");
		}
		LOG.info(msg.toString());
	}

	private synchronized void resetGroup() {
		groups.clear();
		for (ChildData childData : group_watcher.getCurrentData()) {
			try {
				Group group = MAPPER.readValue(childData.getData(), Group.class);
				if (group.getServers() != null) {
					groups.put(group.getId(), group);
				}
			} catch (IOException e) {
				LOG.warn("group:" + childData.toString());
				LOG.warn(e.getMessage());
				// e.printStackTrace();
			}
		}
	}

	private synchronized void resetSlots() {
		for (int i = 0; i < slots.length; ++i) {
			slots[i] = null;
		}
		for (ChildData childData : slots_watcher.getCurrentData()) {
			try {
				Slot slot = MAPPER.readValue(childData.getData(), Slot.class);
				slots[slot.getId()] = slot;
			} catch (IOException e) {
				LOG.warn("slot:" + childData.toString());
				LOG.warn(e.getMessage());
				// e.printStackTrace();
			}
		}
	}

	private synchronized void resetPool(String server) {
		JedisPool pool = pools.get(server);
		if (pool != null) {
			pool.close();
		}
		LOG.info("Reset pool: " + server);
		String[] hostAndPort = server.split(":");
		String host = hostAndPort[0];
		int port = Integer.parseInt(hostAndPort[1]);
		pool = new JedisPool(poolConfig, host, port, connectionTimeoutMs, soTimeoutMs, productAuth, db, clientName,
				false, null, null, null);
		pools.put(server, pool);
	}

	private synchronized void resetPools() {
		Set<String> servers = new HashSet<String>();
		for (Group group : groups.values()) {
			for (GroupServer server : group.getServers()) {
				servers.add(server.getServer());
			}
		}

		Iterator<Map.Entry<String, JedisPool>> iterator = pools.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, JedisPool> entry = iterator.next();
			if (!servers.contains(entry.getKey())) {
				LOG.info("Remove pool: " + entry.getKey());
				entry.getValue().close();
				iterator.remove();
			}
		}

		for (String server : servers) {
			if (pools.containsKey(server) && !pools.get(server).isClosed()) {
				continue;
			}
			LOG.info("Add new pool: " + server);
			String[] hostAndPort = server.split(":");
			String host = hostAndPort[0];
			int port = Integer.parseInt(hostAndPort[1]);
			JedisPool pool = new JedisPool(poolConfig, host, port, connectionTimeoutMs, soTimeoutMs, productAuth, db,
					clientName, false, null, null, null);
			pools.put(server, pool);
		}
	}
}
