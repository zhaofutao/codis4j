package com.zhaofutao.codis4j;

import java.io.Closeable;
import java.nio.ByteBuffer;

import redis.clients.jedis.Jedis;

public interface CodisConnectionHandler extends Closeable {

	Jedis getConnection(int groupId, boolean master);

	int getGroupId(ByteBuffer key);

	int getGroupId(String key);
}
