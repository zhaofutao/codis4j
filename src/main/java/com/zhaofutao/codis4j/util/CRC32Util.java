package com.zhaofutao.codis4j.util;

import java.util.zip.CRC32;

public class CRC32Util {
	public static long crc32(String input) {
		CRC32 crc = new CRC32();
		crc.update(input.getBytes());
		return crc.getValue();
	}
}
