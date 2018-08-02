package com.zhaofutao.codis4j;

import java.util.Map;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyBinaryCommands;
import redis.clients.jedis.MultiKeyCommands;

public interface CodisClient extends JedisCommands, BinaryJedisCommands, MultiKeyCommands, MultiKeyBinaryCommands {
	void close();

	String mset(Map<String, String> keysvalues);
}
