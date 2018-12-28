package com.cjyfff.election.info;

import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * ShardingInfo，全局分片信息
 * 这些全局信息其实可以写成类的静态属性，这样来保证多线程读取到同一份数据，
 * 不一定要使用单例bean
 */
@Getter
@Setter
@Component
public class ShardingInfo {
    /**
     * 集群所有机器的分片信息，key为node id，value为机器ip，
     * 为避免多线程写入时信息丢失，不能用HashMap而要使用线程安全的Map
     */
    private ConcurrentHashMap<Integer, String> shardingMap;

    /**
     * 本机 node id
     */
    private Integer nodeId;
}
