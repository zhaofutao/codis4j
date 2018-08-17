package codis4j.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import codis4j.CodisConnectionHandler;
import redis.clients.jedis.Jedis;

public abstract class CodisMSetCommand<T> {
	private int maxRetryTime = 3;
	private CodisConnectionHandler connectionHandler;

	public CodisMSetCommand(CodisConnectionHandler connectionHandler, int maxRetryTime) {
		this.connectionHandler = connectionHandler;
		this.maxRetryTime = maxRetryTime;
	}

	public abstract T execute(Jedis jedis, Set<ByteBuffer> set);

	public List<T> run(ByteBuffer... keys) {
		BatchMap batchs = new BatchMap(connectionHandler, keys);
		return runParaller(batchs);
	}

	public List<T> run(String... keys) {
		BatchMap batchs = new BatchMap(connectionHandler, keys);
		return runParaller(batchs);
	}

	public List<T> run(List<String> keys) {
		BatchMap batchs = new BatchMap(connectionHandler, keys);
		return runParaller(batchs);
	}

	private List<T> runParaller(BatchMap batchMap) {
		List<T> list = new ArrayList<T>();
		batchMap.entrySet().parallelStream().forEach(entry -> {
			synchronized (list) {
				list.add(runWithRetries(entry.getKey(), entry.getValue()));
			}
		});
		return list;
	}

	private T runWithRetries(int groupId, Set<ByteBuffer> keys) {
		Jedis jedis = null;
		int retry = 0;
		while (retry++ < this.maxRetryTime) {
			try {
				jedis = connectionHandler.getConnection(groupId, true);
				if (jedis == null) {
					System.out.println(String.format("group %d is null", groupId));
				}
				return execute(jedis, keys);
			} catch (Exception e) {
				System.out.println(String.format("CodisMSetCommand retry[%d], groupId[%d], size[%d]", retry, groupId,
						keys.size()));
				// e.printStackTrace();
			} finally {
				releaseConnection(jedis);
			}
		}
		return null;
	}

	private void releaseConnection(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
		}
	}
}
