package com.cjyfff.election.core.info;

import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.springframework.stereotype.Component;

/**
 * Created by jiashen on 18-9-1.
 */
@Getter
@Setter
@Component
public class ElectionListener {

    private NodeCache slaveMonitorShardingInfoListener;

    private NodeCache slaveMonitorElectionStatusListener;
}
