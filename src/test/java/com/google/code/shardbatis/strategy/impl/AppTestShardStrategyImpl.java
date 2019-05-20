package com.google.code.shardbatis.strategy.impl;

import com.google.code.shardbatis.strategy.ShardStrategy;

/**
 * @author sean.he
 *
 */
public class AppTestShardStrategyImpl implements ShardStrategy {

	public String getTargetTableName(String baseTableName, Object params, String mapperId) {
		return baseTableName+"_0";
	}
}
