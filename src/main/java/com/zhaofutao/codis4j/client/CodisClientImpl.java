/**
 * 
 */
package com.zhaofutao.codis4j.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

import com.zhaofutao.codis4j.CodisClient;
import com.zhaofutao.codis4j.CodisConnectionHandler;

public class CodisClientImpl implements CodisClient {
	private CodisConnectionHandler connection;

	public CodisClientImpl(CodisConnectionHandler connection) {
		this.connection = connection;
	}

	@Override
	public void close() {
		try {
			this.connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String set(byte[] key, byte[] value) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.set(key, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String set(String key, String value) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.set(key, value);
			}

		}.run(key);
	}

	@Override
	public String set(byte[] key, byte[] value, byte[] nxxx, byte[] expx, long time) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.set(key, value, nxxx, expx, time);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String set(String key, String value, String nxxx, String expx, long time) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.set(key, value, nxxx, expx, time);
			}

		}.run(key);
	}

	@Override
	public String set(byte[] key, byte[] value, byte[] nxxx) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.set(key, value, nxxx);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String set(String key, String value, String nxxx) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.set(key, value, nxxx);
			}

		}.run(key);
	}

	@Override
	public String mset(byte[]... keysvalues) {
		ByteBuffer[] keys = new ByteBuffer[keysvalues.length / 2];
		ByteBuffer[] values = new ByteBuffer[keysvalues.length / 2];
		for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
			keys[keyIdx] = ByteBuffer.wrap(keysvalues[keyIdx * 2]);
			values[keyIdx] = ByteBuffer.wrap(keysvalues[keyIdx * 2 + 1]);
		}

		List<String> results = new CodisMSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis, Set<ByteBuffer> set) {
				byte[][] subkeysvalues = new byte[set.size() * 2][];
				int subkeyIdx = 0;
				for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
					byte[] key = keys[keyIdx].array();
					byte[] value = values[keyIdx].array();
					if (set.contains(ByteBuffer.wrap(key))) {
						subkeysvalues[subkeyIdx++] = key;
						subkeysvalues[subkeyIdx++] = value;
					}
				}
				return jedis.mset(subkeysvalues);
			}

		}.run(keys);

		for (String result : results) {
			if (!result.equalsIgnoreCase("OK")) {
				return result;
			}
		}
		return "OK";
	}

	@Override
	public String mset(String... keysvalues) {
		String[] keys = new String[keysvalues.length / 2];
		String[] values = new String[keysvalues.length / 2];
		for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
			keys[keyIdx] = keysvalues[keyIdx * 2];
			values[keyIdx] = keysvalues[keyIdx * 2 + 1];
		}

		List<String> results = new CodisMSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis, Set<ByteBuffer> set) {
				String[] subkeysvalues = new String[set.size() * 2];
				int subkeyIdx = 0;
				for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
					String key = keys[keyIdx];
					String value = values[keyIdx];
					if (set.contains(ByteBuffer.wrap(key.getBytes()))) {
						subkeysvalues[subkeyIdx++] = key;
						subkeysvalues[subkeyIdx++] = value;
					}
				}
				return jedis.mset(subkeysvalues);
			}

		}.run(keys);

		for (String result : results) {
			if (!result.equalsIgnoreCase("OK")) {
				return result;
			}
		}
		return "OK";
	}

	@Override
	public String mset(Map<String, String> keysvalues) {
		List<String> results = new CodisMSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis, Set<ByteBuffer> sub) {
				String[] subkeysvalues = new String[sub.size() * 2];
				int subkeyIdx = 0;
				for (Map.Entry<String, String> entry : keysvalues.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (sub.contains(ByteBuffer.wrap(key.getBytes()))) {
						subkeysvalues[subkeyIdx++] = key;
						subkeysvalues[subkeyIdx++] = value;
					}
				}
				return jedis.mset(subkeysvalues);
			}

		}.run(new ArrayList<String>(keysvalues.keySet()));

		for (String result : results) {
			if (!result.equalsIgnoreCase("OK")) {
				return result;
			}
		}
		return "OK";
	}

	@Override
	public Long expire(byte[] key, int seconds) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.expire(key, seconds);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long expire(String key, int seconds) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.expire(key, seconds);
			}

		}.run(key);
	}

	@Override
	public byte[] getSet(byte[] key, byte[] value) {
		return new CodisSetCommand<byte[]>(connection, 3) {

			@Override
			public byte[] execute(Jedis jedis) {
				return jedis.getSet(key, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String getSet(String key, String value) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.getSet(key, value);
			}

		}.run(key);
	}

	@Override
	public byte[] get(byte[] key) {
		return new CodisGetCommand<byte[]>(connection, 3) {

			@Override
			public byte[] execute(Jedis jedis) {
				return jedis.get(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String get(String key) {
		return new CodisGetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.get(key);
			}

		}.run(key);
	}

	@Override
	public List<byte[]> mget(byte[]... keys) {
		ByteBuffer[] keys2 = new ByteBuffer[keys.length];
		int keyIdx = 0;
		for (byte[] key : keys) {
			keys2[keyIdx++] = ByteBuffer.wrap(key);
		}
		return new CodisMGetCommand<byte[]>(connection, 3) {

			@Override
			public List<byte[]> execute(Jedis jedis, Set<ByteBuffer> set) {
				byte[][] subkeys = new byte[set.size()][];
				int keyIdx = 0;
				for (ByteBuffer key : keys2) {
					if (set.contains(key)) {
						subkeys[keyIdx++] = key.array();
					}
				}
				return jedis.mget(subkeys);
			}
		}.run(keys2);
	}

	@Override
	public List<String> mget(String... keys) {
		return new CodisMGetCommand<String>(connection, 3) {

			@Override
			public List<String> execute(Jedis jedis, Set<ByteBuffer> set) {
				String[] subkeys = new String[set.size()];
				int keyIdx = 0;
				for (String key : keys) {
					if (set.contains(ByteBuffer.wrap(key.getBytes()))) {
						subkeys[keyIdx++] = key;
					}
				}
				return jedis.mget(subkeys);
			}
		}.run(keys);
	}

	@Override
	public Boolean exists(byte[] key) {
		return new CodisGetCommand<Boolean>(connection, 3) {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.exists(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long exists(byte[]... keys) {
		ByteBuffer[] keys2 = new ByteBuffer[keys.length];
		int keyIdx = 0;
		for (byte[] key : keys) {
			keys2[keyIdx++] = ByteBuffer.wrap(key);
		}
		List<Long> results = new CodisMSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis, Set<ByteBuffer> set) {
				byte[][] subkeys = new byte[set.size()][];
				int subkeyIdx = 0;
				for (ByteBuffer key : keys2) {
					if (set.contains(key)) {
						subkeys[subkeyIdx++] = key.array();
					}
				}
				return jedis.exists(subkeys);
			}

		}.run(keys2);

		long num = 0;
		for (long l : results) {
			num += l;
		}
		return num;
	}

	@Override
	public Boolean exists(String key) {
		return new CodisGetCommand<Boolean>(connection, 3) {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.exists(key);
			}

		}.run(key);
	}

	@Override
	public Long exists(String... keys) {
		List<Long> results = new CodisMSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis, Set<ByteBuffer> set) {
				String[] subkeys = new String[set.size()];
				int subkeyIdx = 0;
				for (String key : keys) {
					if (set.contains(ByteBuffer.wrap(key.getBytes()))) {
						subkeys[subkeyIdx++] = key;
					}
				}
				return jedis.exists(subkeys);
			}

		}.run(keys);

		long num = 0;
		for (long l : results) {
			num += l;
		}
		return num;
	}

	@Override
	public Long del(byte[] key) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.del(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long del(byte[]... keys) {
		ByteBuffer[] keys2 = new ByteBuffer[keys.length];
		int keyIdx = 0;
		for (byte[] key : keys) {
			keys2[keyIdx++] = ByteBuffer.wrap(key);
		}
		List<Long> results = new CodisMSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis, Set<ByteBuffer> set) {
				byte[][] subkeys = new byte[set.size()][];
				int subkeyIdx = 0;
				for (ByteBuffer key : keys2) {
					if (set.contains(key)) {
						subkeys[subkeyIdx++] = key.array();
					}
				}
				return jedis.del(subkeys);
			}

		}.run(keys2);

		long num = 0;
		for (long l : results) {
			num += l;
		}
		return num;
	}

	@Override
	public Long del(String key) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.del(key);
			}

		}.run(key);
	}

	@Override
	public Long del(String... keys) {
		List<Long> results = new CodisMSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis, Set<ByteBuffer> set) {
				String[] subkeys = new String[set.size()];
				int subkeyIdx = 0;
				for (String key : keys) {
					if (set.contains(ByteBuffer.wrap(key.getBytes()))) {
						subkeys[subkeyIdx++] = key;
					}
				}
				return jedis.del(subkeys);
			}

		}.run(keys);

		long num = 0;
		for (long l : results) {
			num += l;
		}
		return num;
	}

	@Override
	public Long persist(String key) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.persist(key);
			}

		}.run(key);
	}

	@Override
	public String type(String key) {
		return new CodisGetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.type(key);
			}

		}.run(key);
	}

	@Override
	public Long pexpire(byte[] key, long milliseconds) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.pexpire(key, milliseconds);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Deprecated
	@Override
	public Long pexpire(String key, long milliseconds) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.pexpire(key, milliseconds);
			}

		}.run(key);
	}

	@Override
	public Long expireAt(byte[] key, long unixTime) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.expireAt(key, unixTime);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long expireAt(String key, long unixTime) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.expireAt(key, unixTime);
			}

		}.run(key);
	}

	@Override
	public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.pexpireAt(key, millisecondsTimestamp);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long pexpireAt(String key, long millisecondsTimestamp) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.pexpireAt(key, millisecondsTimestamp);
			}

		}.run(key);
	}

	@Override
	public Long ttl(byte[] key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.ttl(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long ttl(String key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.ttl(key);
			}

		}.run(key);
	}

	@Override
	public Long pttl(String key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.pttl(key);
			}

		}.run(key);
	}

	@Override
	public Boolean setbit(String key, long offset, boolean value) {
		return new CodisSetCommand<Boolean>(connection, 3) {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.setbit(key, offset, value);
			}

		}.run(key);
	}

	@Override
	public Boolean setbit(String key, long offset, String value) {
		return new CodisSetCommand<Boolean>(connection, 3) {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.setbit(key, offset, value);
			}

		}.run(key);
	}

	@Override
	public Boolean getbit(String key, long offset) {
		return new CodisGetCommand<Boolean>(connection, 3) {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.getbit(key, offset);
			}

		}.run(key);
	}

	@Override
	public Long setrange(String key, long offset, String value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.setrange(key, offset, value);
			}

		}.run(key);
	}

	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		return new CodisGetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.getrange(key, startOffset, endOffset);
			}

		}.run(key);
	}

	@Override
	public Long setnx(byte[] key, byte[] value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.setnx(key, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String setex(byte[] key, int seconds, byte[] value) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.setex(key, seconds, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long setnx(String key, String value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.setnx(key, value);
			}

		}.run(key);
	}

	@Override
	public String setex(String key, int seconds, String value) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.setex(key, seconds, value);
			}

		}.run(key);
	}

	@Override
	public String psetex(String key, long milliseconds, String value) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.psetex(key, milliseconds, value);
			}

		}.run(key);
	}

	@Override
	public Long decrBy(byte[] key, long integer) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.decrBy(key, integer);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long decrBy(String key, long integer) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.decrBy(key, integer);
			}

		}.run(key);
	}

	@Override
	public Long decr(byte[] key) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.decr(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long decr(String key) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.decr(key);
			}

		}.run(key);
	}

	@Override
	public Long incrBy(byte[] key, long integer) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.incrBy(key, integer);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long incrBy(String key, long integer) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.incrBy(key, integer);
			}

		}.run(key);
	}

	@Override
	public Double incrByFloat(byte[] key, double value) {
		return new CodisSetCommand<Double>(connection, 3) {

			@Override
			public Double execute(Jedis jedis) {
				return jedis.incrByFloat(key, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Double incrByFloat(String key, double value) {
		return new CodisSetCommand<Double>(connection, 3) {

			@Override
			public Double execute(Jedis jedis) {
				return jedis.incrByFloat(key, value);
			}

		}.run(key);
	}

	@Override
	public Long incr(byte[] key) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.incr(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long incr(String key) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.incr(key);
			}

		}.run(key);
	}

	@Override
	public Long append(String key, String value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.append(key, value);
			}

		}.run(key);
	}

	@Override
	public String substr(String key, int start, int end) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.substr(key, start, end);
			}

		}.run(key);
	}

	@Override
	public Long hset(byte[] key, byte[] field, byte[] value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hset(key, field, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long hset(String key, String field, String value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hset(key, field, value);
			}

		}.run(key);
	}

	@Override
	public byte[] hget(byte[] key, byte[] field) {
		return new CodisSetCommand<byte[]>(connection, 3) {

			@Override
			public byte[] execute(Jedis jedis) {
				return jedis.hget(key, field);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String hget(String key, String field) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.hget(key, field);
			}

		}.run(key);
	}

	@Override
	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hsetnx(key, field, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long hsetnx(String key, String field, String value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hsetnx(key, field, value);
			}

		}.run(key);
	}

	@Override
	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.hmset(key, hash);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String hmset(String key, Map<String, String> hash) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.hmset(key, hash);
			}

		}.run(key);
	}

	@Override
	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		return new CodisGetCommand<List<byte[]>>(connection, 3) {

			@Override
			public List<byte[]> execute(Jedis jedis) {
				return jedis.hmget(key, fields);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public List<String> hmget(String key, String... fields) {
		return new CodisGetCommand<List<String>>(connection, 3) {

			@Override
			public List<String> execute(Jedis jedis) {
				return jedis.hmget(key, fields);
			}

		}.run(key);
	}

	@Override
	public Long hincrBy(byte[] key, byte[] field, long value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hincrBy(key, field, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long hincrBy(String key, String field, long value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hincrBy(key, field, value);
			}

		}.run(key);
	}

	@Override
	public Double hincrByFloat(byte[] key, byte[] field, double value) {
		return new CodisSetCommand<Double>(connection, 3) {

			@Override
			public Double execute(Jedis jedis) {
				return jedis.hincrByFloat(key, field, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Double hincrByFloat(String key, String field, double value) {
		return new CodisSetCommand<Double>(connection, 3) {

			@Override
			public Double execute(Jedis jedis) {
				return jedis.hincrByFloat(key, field, value);
			}

		}.run(key);
	}

	@Override
	public Boolean hexists(byte[] key, byte[] field) {
		return new CodisGetCommand<Boolean>(connection, 3) {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.hexists(key, field);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Boolean hexists(String key, String field) {
		return new CodisGetCommand<Boolean>(connection, 3) {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.hexists(key, field);
			}

		}.run(key);
	}

	@Override
	public Long hdel(byte[] key, byte[]... field) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hdel(key, field);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long hdel(String key, String... field) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hdel(key, field);
			}

		}.run(key);
	}

	@Override
	public Long hlen(byte[] key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hlen(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long hlen(String key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.hlen(key);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> hkeys(byte[] key) {
		return new CodisGetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.hkeys(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> hkeys(String key) {
		return new CodisGetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.hkeys(key);
			}

		}.run(key);
	}

	@Override
	public Collection<byte[]> hvals(byte[] key) {
		return new CodisGetCommand<Collection<byte[]>>(connection, 3) {

			@Override
			public Collection<byte[]> execute(Jedis jedis) {
				return jedis.hvals(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public List<String> hvals(String key) {
		return new CodisGetCommand<List<String>>(connection, 3) {

			@Override
			public List<String> execute(Jedis jedis) {
				return jedis.hvals(key);
			}

		}.run(key);
	}

	@Override
	public Map<byte[], byte[]> hgetAll(byte[] key) {
		return new CodisGetCommand<Map<byte[], byte[]>>(connection, 3) {

			@Override
			public Map<byte[], byte[]> execute(Jedis jedis) {
				return jedis.hgetAll(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Map<String, String> hgetAll(String key) {
		return new CodisGetCommand<Map<String, String>>(connection, 3) {

			@Override
			public Map<String, String> execute(Jedis jedis) {
				return jedis.hgetAll(key);
			}

		}.run(key);
	}

	@Override
	public Long rpush(byte[] key, byte[]... args) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.rpush(key, args);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long rpush(String key, String... string) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.rpush(key, string);
			}

		}.run(key);
	}

	@Override
	public Long lpush(byte[] key, byte[]... args) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.lpush(key, args);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long lpush(String key, String... string) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.lpush(key, string);
			}

		}.run(key);
	}

	@Override
	public Long llen(byte[] key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.llen(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long llen(String key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.llen(key);
			}

		}.run(key);
	}

	@Override
	public List<byte[]> lrange(byte[] key, long start, long end) {
		return new CodisGetCommand<List<byte[]>>(connection, 3) {

			@Override
			public List<byte[]> execute(Jedis jedis) {
				return jedis.lrange(key, start, end);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public List<String> lrange(String key, long start, long end) {
		return new CodisGetCommand<List<String>>(connection, 3) {

			@Override
			public List<String> execute(Jedis jedis) {
				return jedis.lrange(key, start, end);
			}

		}.run(key);
	}

	@Override
	public String ltrim(byte[] key, long start, long end) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.ltrim(key, start, end);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String ltrim(String key, long start, long end) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.ltrim(key, start, end);
			}

		}.run(key);
	}

	@Override
	public byte[] lindex(byte[] key, long index) {
		return new CodisGetCommand<byte[]>(connection, 3) {

			@Override
			public byte[] execute(Jedis jedis) {
				return jedis.lindex(key, index);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String lindex(String key, long index) {
		return new CodisGetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.lindex(key, index);
			}

		}.run(key);
	}

	@Override
	public String lset(byte[] key, long index, byte[] value) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.lset(key, index, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String lset(String key, long index, String value) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.lset(key, index, value);
			}

		}.run(key);
	}

	@Override
	public Long lrem(byte[] key, long count, byte[] value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.lrem(key, count, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long lrem(String key, long count, String value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.lrem(key, count, value);
			}

		}.run(key);
	}

	@Override
	public byte[] lpop(byte[] key) {
		return new CodisSetCommand<byte[]>(connection, 3) {

			@Override
			public byte[] execute(Jedis jedis) {
				return jedis.lpop(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String lpop(String key) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.lpop(key);
			}

		}.run(key);
	}

	@Override
	public byte[] rpop(byte[] key) {
		return new CodisSetCommand<byte[]>(connection, 3) {

			@Override
			public byte[] execute(Jedis jedis) {
				return jedis.rpop(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String rpop(String key) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.rpop(key);
			}

		}.run(key);
	}

	@Override
	public Long sadd(byte[] key, byte[]... member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.sadd(key, member);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long sadd(String key, String... member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.sadd(key, member);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> smembers(byte[] key) {
		return new CodisGetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.smembers(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> smembers(String key) {
		return new CodisGetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.smembers(key);
			}

		}.run(key);
	}

	@Override
	public Long srem(byte[] key, byte[]... member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.srem(key, member);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long srem(String key, String... member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.srem(key, member);
			}

		}.run(key);
	}

	@Override
	public byte[] spop(byte[] key) {
		return new CodisSetCommand<byte[]>(connection, 3) {

			@Override
			public byte[] execute(Jedis jedis) {
				return jedis.spop(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String spop(String key) {
		return new CodisSetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.spop(key);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> spop(byte[] key, long count) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.spop(key, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> spop(String key, long count) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.spop(key, count);
			}

		}.run(key);
	}

	@Override
	public Long scard(byte[] key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.scard(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long scard(String key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.scard(key);
			}

		}.run(key);
	}

	@Override
	public Boolean sismember(byte[] key, byte[] member) {
		return new CodisGetCommand<Boolean>(connection, 3) {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.sismember(key, member);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Boolean sismember(String key, String member) {
		return new CodisGetCommand<Boolean>(connection, 3) {

			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.sismember(key, member);
			}

		}.run(key);
	}

	@Override
	public byte[] srandmember(byte[] key) {
		return new CodisGetCommand<byte[]>(connection, 3) {

			@Override
			public byte[] execute(Jedis jedis) {
				return jedis.srandmember(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public String srandmember(String key) {
		return new CodisGetCommand<String>(connection, 3) {

			@Override
			public String execute(Jedis jedis) {
				return jedis.srandmember(key);
			}

		}.run(key);
	}

	@Override
	public List<byte[]> srandmember(byte[] key, int count) {
		return new CodisGetCommand<List<byte[]>>(connection, 3) {

			@Override
			public List<byte[]> execute(Jedis jedis) {
				return jedis.srandmember(key, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public List<String> srandmember(String key, int count) {
		return new CodisGetCommand<List<String>>(connection, 3) {

			@Override
			public List<String> execute(Jedis jedis) {
				return jedis.srandmember(key, count);
			}

		}.run(key);
	}

	@Override
	public Long strlen(byte[] key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.strlen(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long strlen(String key) {
		return new CodisGetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.strlen(key);
			}

		}.run(key);
	}

	@Override
	public Long zadd(byte[] key, double score, byte[] member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zadd(key, score, member);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zadd(String key, double score, String member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zadd(key, score, member);
			}

		}.run(key);
	}

	@Override
	public Long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zadd(key, score, member, params);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zadd(String key, double score, String member, ZAddParams params) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zadd(key, score, member, params);
			}

		}.run(key);
	}

	@Override
	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zadd(key, scoreMembers);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zadd(key, scoreMembers);
			}

		}.run(key);
	}

	@Override
	public Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zadd(key, scoreMembers, params);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zadd(key, scoreMembers, params);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrange(byte[] key, long start, long end) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrange(key, start, end);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrange(String key, long start, long end) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrange(key, start, end);
			}

		}.run(key);
	}

	@Override
	public Long zrem(byte[] key, byte[]... member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zrem(key, member);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zrem(String key, String... member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zrem(key, member);
			}

		}.run(key);
	}

	@Override
	public Double zincrby(byte[] key, double score, byte[] member) {
		return new CodisSetCommand<Double>(connection, 3) {

			@Override
			public Double execute(Jedis jedis) {
				return jedis.zincrby(key, score, member);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Double zincrby(String key, double score, String member) {
		return new CodisSetCommand<Double>(connection, 3) {

			@Override
			public Double execute(Jedis jedis) {
				return jedis.zincrby(key, score, member);
			}

		}.run(key);
	}

	@Override
	public Double zincrby(byte[] key, double score, byte[] member, ZIncrByParams params) {
		return new CodisSetCommand<Double>(connection, 3) {

			@Override
			public Double execute(Jedis jedis) {
				return jedis.zincrby(key, score, member, params);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Double zincrby(String key, double score, String member, ZIncrByParams params) {
		return new CodisSetCommand<Double>(connection, 3) {

			@Override
			public Double execute(Jedis jedis) {
				return jedis.zincrby(key, score, member, params);
			}

		}.run(key);
	}

	@Override
	public Long zrank(byte[] key, byte[] member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zrank(key, member);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zrank(String key, String member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zrank(key, member);
			}

		}.run(key);
	}

	@Override
	public Long zrevrank(byte[] key, byte[] member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zrevrank(key, member);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zrevrank(String key, String member) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zrevrank(key, member);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrevrange(byte[] key, long start, long end) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrevrange(key, start, end);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrevrange(key, start, end);
			}

		}.run(key);
	}

	@Override
	public Set<Tuple> zrangeWithScores(byte[] key, long start, long end) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrangeWithScores(key, start, end);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long end) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrangeWithScores(key, start, end);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrangeWithScores(key, start, end);
			}

		}.run(key);
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrevrangeWithScores(key, start, end);
			}

		}.run(key);
	}

	@Override
	public Long zcard(byte[] key) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zcard(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zcard(String key) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zcard(key);
			}

		}.run(key);
	}

	@Override
	public Double zscore(byte[] key, byte[] member) {
		return new CodisSetCommand<Double>(connection, 3) {

			@Override
			public Double execute(Jedis jedis) {
				return jedis.zscore(key, member);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Double zscore(String key, String member) {
		return new CodisSetCommand<Double>(connection, 3) {

			@Override
			public Double execute(Jedis jedis) {
				return jedis.zscore(key, member);
			}

		}.run(key);
	}

	@Override
	public List<byte[]> sort(byte[] key) {
		return new CodisSetCommand<List<byte[]>>(connection, 3) {

			@Override
			public List<byte[]> execute(Jedis jedis) {
				return jedis.sort(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public List<String> sort(String key) {
		return new CodisSetCommand<List<String>>(connection, 3) {

			@Override
			public List<String> execute(Jedis jedis) {
				return jedis.sort(key);
			}

		}.run(key);
	}

	@Override
	public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
		return new CodisSetCommand<List<byte[]>>(connection, 3) {

			@Override
			public List<byte[]> execute(Jedis jedis) {
				return jedis.sort(key);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public List<String> sort(String key, SortingParams sortingParameters) {
		return new CodisSetCommand<List<String>>(connection, 3) {

			@Override
			public List<String> execute(Jedis jedis) {
				return jedis.sort(key, sortingParameters);
			}

		}.run(key);
	}

	@Override
	public Long zcount(byte[] key, double min, double max) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zcount(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zcount(byte[] key, byte[] min, byte[] max) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zcount(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zcount(String key, double min, double max) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zcount(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Long zcount(String key, String min, String max) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zcount(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrevrangeByScore(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrevrangeByScore(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max, offset, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max, offset, count);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrevrangeByScore(key, max, min);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrevrangeByScore(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max, offset, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max, offset, count);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrevrangeByScore(key, max, min, offset, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrevrangeByScore(key, max, min, offset, count);
			}

		}.run(key);
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrevrangeByScore(key, max, min, offset, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrevrangeByScore(key, max, min, offset, count);
			}

		}.run(key);
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
			}

		}.run(key);
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, min, max, offset, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, min, max, offset, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, min, max, offset, count);
			}

		}.run(key);
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
		return new CodisSetCommand<Set<Tuple>>(connection, 3) {

			@Override
			public Set<Tuple> execute(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, min, max, offset, count);
			}

		}.run(key);
	}

	@Override
	public Long zremrangeByRank(byte[] key, long start, long end) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zremrangeByRank(key, start, end);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zremrangeByRank(String key, long start, long end) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zremrangeByRank(key, start, end);
			}

		}.run(key);
	}

	@Override
	public Long zremrangeByScore(byte[] key, double start, double end) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zremrangeByScore(key, start, end);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zremrangeByScore(String key, double start, double end) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zremrangeByScore(key, start, end);
			}

		}.run(key);
	}

	@Override
	public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zremrangeByScore(key, start, end);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zremrangeByScore(String key, String start, String end) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zremrangeByScore(key, start, end);
			}

		}.run(key);
	}

	@Override
	public Long zlexcount(byte[] key, byte[] min, byte[] max) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zlexcount(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zlexcount(String key, String min, String max) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zlexcount(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrangeByLex(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrangeByLex(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrangeByLex(key, min, max, offset, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrangeByLex(key, min, max, offset, count);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrevrangeByLex(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrevrangeByLex(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
		return new CodisSetCommand<Set<byte[]>>(connection, 3) {

			@Override
			public Set<byte[]> execute(Jedis jedis) {
				return jedis.zrevrangeByLex(key, min, max, offset, count);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		return new CodisSetCommand<Set<String>>(connection, 3) {

			@Override
			public Set<String> execute(Jedis jedis) {
				return jedis.zrevrangeByLex(key, min, max, offset, count);
			}

		}.run(key);
	}

	@Override
	public Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zremrangeByLex(key, min, max);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long zremrangeByLex(String key, String min, String max) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.zremrangeByLex(key, min, max);
			}

		}.run(key);
	}

	@Override
	public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot, byte[] value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.linsert(key, where, pivot, value);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.linsert(key, where, pivot, value);
			}

		}.run(key);
	}

	@Override
	public Long lpushx(byte[] key, byte[]... arg) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.lpushx(key, arg);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long lpushx(String key, String... string) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.lpushx(key, string);
			}

		}.run(key);
	}

	@Override
	public Long rpushx(byte[] key, byte[]... arg) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.rpushx(key, arg);
			}

		}.run(ByteBuffer.wrap(key));
	}

	@Override
	public Long rpushx(String key, String... string) {
		return new CodisSetCommand<Long>(connection, 3) {

			@Override
			public Long execute(Jedis jedis) {
				return jedis.rpushx(key, string);
			}

		}.run(key);
	}

	@Deprecated
	@Override
	public List<byte[]> blpop(byte[] arg) {
		return null;
	}

	@Override
	public List<String> blpop(int timeout, String key) {
		return null;
	}

	@Deprecated
	@Override
	public List<byte[]> brpop(byte[] arg) {
		return null;
	}

	@Deprecated
	@Override
	public List<String> brpop(String arg) {
		return null;
	}

	@Override
	public List<String> brpop(int timeout, String key) {
		return null;
	}

	@Override
	public String echo(String string) {
		return null;
	}

	@Override
	public Long move(String key, int dbIndex) {
		return null;
	}

	@Override
	public Long bitcount(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long bitcount(String key, long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long bitpos(String key, boolean value) {
		return null;
	}

	@Override
	public Long bitpos(String key, boolean value, BitPosParams params) {
		return null;
	}

	@Deprecated
	@Override
	public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public ScanResult<String> sscan(String key, int cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public ScanResult<Tuple> zscan(String key, int cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long pfadd(String key, String... elements) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long pfcount(String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Long geoadd(String key, double longitude, double latitude, String member) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double geodist(String key, String member1, String member2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double geodist(String key, String member1, String member2, GeoUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> geohash(String key, String... members) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoCoordinate> geopos(String key, String... members) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> bitfield(String key, String... arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long persist(byte[] key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String type(byte[] key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean setbit(byte[] key, long offset, boolean value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean setbit(byte[] key, long offset, byte[] value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getbit(byte[] key, long offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long setrange(byte[] key, long offset, byte[] value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getrange(byte[] key, long startOffset, long endOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long append(byte[] key, byte[] value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] substr(byte[] key, int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] echo(byte[] arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long move(byte[] key, int dbIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long bitcount(byte[] key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long bitcount(byte[] key, long start, long end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long pfadd(byte[] key, byte[]... elements) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long pfcount(byte[] key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double geodist(byte[] key, byte[] member1, byte[] member2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<byte[]> geohash(byte[] key, byte[]... members) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<byte[]> bitfield(byte[] key, byte[]... arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> blpop(int timeout, String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> brpop(int timeout, String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> blpop(String... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> brpop(String... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> keys(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long msetnx(String... keysvalues) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String rename(String oldkey, String newkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long renamenx(String oldkey, String newkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String rpoplpush(String srckey, String dstkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> sdiff(String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long sdiffstore(String dstkey, String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> sinter(String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long sinterstore(String dstkey, String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long smove(String srckey, String dstkey, String member) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long sort(String key, SortingParams sortingParameters, String dstkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long sort(String key, String dstkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> sunion(String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long sunionstore(String dstkey, String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String watch(String... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String unwatch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zinterstore(String dstkey, String... sets) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zinterstore(String dstkey, ZParams params, String... sets) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zunionstore(String dstkey, String... sets) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zunionstore(String dstkey, ZParams params, String... sets) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String brpoplpush(String source, String destination, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long publish(String channel, String message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribe(JedisPubSub jedisPubSub, String... channels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
		// TODO Auto-generated method stub

	}

	@Override
	public String randomKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long bitop(BitOP op, String destKey, String... srcKeys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public ScanResult<String> scan(int cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<String> scan(String cursor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScanResult<String> scan(String cursor, ScanParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String pfmerge(String destkey, String... sourcekeys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long pfcount(String... keys) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<byte[]> blpop(int timeout, byte[]... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<byte[]> brpop(int timeout, byte[]... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public List<byte[]> blpop(byte[]... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<byte[]> brpop(byte[]... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<byte[]> keys(byte[] pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long msetnx(byte[]... keysvalues) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String rename(byte[] oldkey, byte[] newkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long renamenx(byte[] oldkey, byte[] newkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<byte[]> sdiff(byte[]... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long sdiffstore(byte[] dstkey, byte[]... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<byte[]> sinter(byte[]... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long sinterstore(byte[] dstkey, byte[]... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long sort(byte[] key, byte[] dstkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<byte[]> sunion(byte[]... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long sunionstore(byte[] dstkey, byte[]... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String watch(byte[]... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zinterstore(byte[] dstkey, byte[]... sets) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zunionstore(byte[] dstkey, byte[]... sets) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long publish(byte[] channel, byte[] message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] randomBinaryKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long pfcount(byte[]... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public List<String> blpop(String arg) {
		// TODO Auto-generated method stub
		return null;
	}
}
