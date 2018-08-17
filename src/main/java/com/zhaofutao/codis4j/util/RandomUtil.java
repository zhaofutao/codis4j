package com.zhaofutao.codis4j.util;

import java.util.Random;

public class RandomUtil {
	private static Random r = new Random();

	public static int rand(int range) {
		return rand(0, range);
	}

	public static int rand(int start, int end) {
		if (end < start) {
			return -1;
		}
		return r.nextInt(end - start + 1) + start;
	}
}
