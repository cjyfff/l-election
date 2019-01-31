package com.cjyfff.election.core.info;

import org.apache.curator.framework.recipes.cache.NodeCache;

/**
 * Created by jiashen on 18-9-1.
 */
public class ElectionListener {

    private static NodeCache slaveMonitorShardingInfoListener;

    private static NodeCache slaveMonitorElectionStatusListener;

    public static NodeCache getSlaveMonitorShardingInfoListener() {
        return slaveMonitorShardingInfoListener;
    }

    public static void setSlaveMonitorShardingInfoListener(
        NodeCache slaveMonitorShardingInfoListener) {
        ElectionListener.slaveMonitorShardingInfoListener = slaveMonitorShardingInfoListener;
    }

    public static NodeCache getSlaveMonitorElectionStatusListener() {
        return slaveMonitorElectionStatusListener;
    }

    public static void setSlaveMonitorElectionStatusListener(
        NodeCache slaveMonitorElectionStatusListener) {
        ElectionListener.slaveMonitorElectionStatusListener = slaveMonitorElectionStatusListener;
    }
}
