package com.cjyfff.election.core.info;

import java.util.concurrent.ConcurrentSkipListMap;

/**
 * ShardingInfo，全局分片信息
 */
public class ShardingInfo {
    /**
     * 集群所有机器的分片信息，key为sharding id，value为机器ip，
     * 为避免多线程写入时信息丢失，不能用HashMap而要使用线程安全的Map
     */
    private static ConcurrentSkipListMap<Integer, String> shardingMap;

    /**
     * 本机 sharding id
     */
    private static Integer shardingId;

    public static ConcurrentSkipListMap<Integer, String> getShardingMap() {
        return shardingMap;
    }

    public static void setShardingMap(ConcurrentSkipListMap<Integer, String> shardingMap) {
        ShardingInfo.shardingMap = shardingMap;
    }

    public static Integer getShardingId() {
        return shardingId;
    }

    public static void setShardingId(Integer shardingId) {
        ShardingInfo.shardingId = shardingId;
    }
}
