package com.cjyfff.election.core.info;

import java.util.concurrent.ConcurrentSkipListMap;

/**
 * ShardingInfo，全局分片信息
 */
public class ShardingInfo {
    /**
     * 集群所有机器的分片信息，key为node id，value为机器ip，
     * 为避免多线程写入时信息丢失，不能用HashMap而要使用线程安全的Map
     */
    public static ConcurrentSkipListMap<Byte, String> shardingMap;

    /**
     * 本机 node id
     */
    public static Byte nodeId;

    public static ConcurrentSkipListMap<Byte, String> getShardingMap() {
        return shardingMap;
    }

    public static void setShardingMap(ConcurrentSkipListMap<Byte, String> shardingMap) {
        ShardingInfo.shardingMap = shardingMap;
    }

    public static Byte getNodeId() {
        return nodeId;
    }

    public static void setNodeId(Byte nodeId) {
        ShardingInfo.nodeId = nodeId;
    }
}
