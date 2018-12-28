package com.cjyfff.election.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by jiashen on 2018/8/18.
 */
@Component
public class ZooKeeperClient implements ApplicationListener<ContextClosedEvent> {

    @Value("${l_election.zk_host}")
    private String zkHost;

    @Value("${l_election.zk_session_timeout_ms}")
    private Integer zkSessionTimeoutMs;

    @Value("${l_election.zk_connection_timeout_ms}")
    private Integer zkConnectionTimeoutMs;

    @Value("${l_election.zk_base_sleep_time_ms}")
    private Integer zkBaseSleepTimeMs;

    @Value("${l_election.zk_max_retries}")
    private Integer zkMaxRetries;

    private CuratorFramework client;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public CuratorFramework getClient() {
        if (this.client == null) {
            CuratorFramework c = CuratorFrameworkFactory.newClient(zkHost, zkSessionTimeoutMs, zkConnectionTimeoutMs,
                new ExponentialBackoffRetry(zkBaseSleepTimeMs, zkMaxRetries));
            c.start();
            this.client = c;
        }
        return this.client;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent closedEvent) {
        if (! CuratorFrameworkState.STOPPED.equals(this.client.getState())) {
            this.client.close();
        }
        logger.info("Close zookeeper connection.");
    }
}
