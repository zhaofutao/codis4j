package codis4j.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import codis4j.CodisConnectionHandler;
import redis.clients.jedis.Jedis;

public abstract class CodisMGetCommand<T> {
	private int maxRetryTime = 3;
	private CodisConnectionHandler connectionHandler;

	public CodisMGetCommand(CodisConnectionHandler connectionHandler, int maxRetryTime) {
		this.connectionHandler = connectionHandler;
		this.maxRetryTime = maxRetryTime;
	}

	public abstract List<T> execute(Jedis jedis, Set<ByteBuffer> set);

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
				list.addAll(runWithRetries(entry.getKey(), entry.getValue()));
			}
		});
		return list;
	}

	private List<T> runWithRetries(int groupId, Set<ByteBuffer> keys) {
		Jedis jedis = null;
		int retry = 0;
		while (retry++ < this.maxRetryTime) {
			try {
				jedis = connectionHandler.getConnection(groupId, false);
				return execute(jedis, keys);
			} catch (Exception e) {
				System.out.println(String.format("CodisMGetCommand retry[%d], groupId[%d], size[%d]", retry, groupId,
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
