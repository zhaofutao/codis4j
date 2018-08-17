package codis4j.client;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import codis4j.CodisConnectionHandler;

import java.util.Set;

public class BatchMap {
	private Map<Integer, Set<ByteBuffer>> batchs = new HashMap<Integer, Set<ByteBuffer>>();

	public BatchMap(CodisConnectionHandler connection, List<String> keys) {
		for (String key : keys) {
			int groupId = connection.getGroupId(key);
			if (groupId == -1) {
				continue;
			}
			batchs.computeIfAbsent(groupId, k -> merge(k)).add(ByteBuffer.wrap(key.getBytes()));
		}
	}

	public BatchMap(CodisConnectionHandler connection, String[] keys) {
		for (String key : keys) {
			int groupId = connection.getGroupId(key);
			if (groupId == -1) {
				continue;
			}
			batchs.computeIfAbsent(groupId, k -> merge(k)).add(ByteBuffer.wrap(key.getBytes()));
		}
	}

	public BatchMap(CodisConnectionHandler connection, ByteBuffer[] keys) {
		for (ByteBuffer key : keys) {
			int groupId = connection.getGroupId(key);
			if (groupId == -1) {
				continue;
			}
			batchs.computeIfAbsent(groupId, k -> merge(k)).add(key);
		}
	}

	private Set<ByteBuffer> merge(int k) {
		return new HashSet<ByteBuffer>();
	}

	public Set<Entry<Integer, Set<ByteBuffer>>> entrySet() {
		return batchs.entrySet();
	}
}
