package com.cjyfff.election.core;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.cjyfff.election.biz.ElectionBizContainer;
import com.cjyfff.election.biz.NoneBiz;
import com.cjyfff.election.core.info.ElectionStatus;
import com.cjyfff.election.core.info.ElectionStatus.ElectionStatusType;
import com.cjyfff.election.core.info.SetSelfESAndRunBLProxy;
import com.cjyfff.election.core.master.MasterAction;
import com.cjyfff.election.core.slave.SlaveAction;
import com.cjyfff.election.config.ZooKeeperClient;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created by jiashen on 2018/8/18.
 */
@Component
public class Election {

    private static final String ELECTION_PATH = "/leader";

    private static final String NODE_INFO_PATH = "/node_info";

    private static final String SHARDING_INFO_PATH = "/sharding_info";

    private static final String ELECTION_STATUS_PATH = "/election_status";

    private static final String CLIENT_ID = UUID.randomUUID().toString();

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    private ZooKeeperClient zooKeeperClient;

    @Autowired
    private MasterAction masterAction;

    @Autowired
    private SlaveAction slaveAction;

    @Autowired
    private SetSelfESAndRunBLProxy setSelfESAndRunBLProxy;

    @Autowired
    private ElectionComponent electionComponent;

    @Autowired
    private ElectionBizContainer electionBizContainer;

    public void start() {
        try {
            electionBizContainer.setContainer();

            CuratorFramework client = zooKeeperClient.getClient();

            writeNodeInfo(client);

            electLeader(client);

            monitorNodeInfo(client);

            // 防止监控开始时，master还没有写入分片信息，因此循环等待
            // 同时也根据SHARDING_INFO_PATH和ELECTION_STATUS_PATH是否存在来判断master是否已经选举出来
            while (client.checkExists().forPath(SHARDING_INFO_PATH) == null ||
                   client.checkExists().forPath(ELECTION_STATUS_PATH) == null) {
                logger.info("Wait for ZK path initialization...");
                TimeUnit.SECONDS.sleep(1);
            }

            if (ElectionStatus.getLeaderLatch() == null) {
                throw new Exception("LeaderLatch in electionStatus get null.");
            }

            // 确保只有slave才执行
            if (! ElectionStatus.getLeaderLatch().hasLeadership()) {

                slaveAction.slaveMonitorShardingInfo(client);

                slaveAction.slaveMonitorElectionStatus(client);
            }

        } catch (Exception e) {
            logger.error("Starting election get error: ", e);
            System.exit(1);
       }

    }

    /**
     * 把自身ip和端口作为临时节点路径写入zk
     * @param client
     * @throws Exception
     */
    private void writeNodeInfo(CuratorFramework client) throws Exception {

        String host = electionComponent.getHost();
        String myNodeInfoPath = NODE_INFO_PATH + "/" + host;
        Stat stat = client.checkExists().forPath(myNodeInfoPath);
        if (stat == null) {
            client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL).forPath(myNodeInfoPath, "".getBytes());
        }
    }

    /**
     * 监控集群节点信息（NODE_INFO_PATH）
     * 1、在选举成功后，发生节点变更，master需要触发重新分片
     * 2、发生重连后，要再次写入分片信息，以便触发分片
     * @param client
     * @throws Exception
     */
    private void monitorNodeInfo(CuratorFramework client) throws Exception {
        PathChildrenCacheListener cacheListener = (client1, event) -> {
            logger.info("NODE_INFO_PATH listener monitor data change, event type is：" + event.getType());

            if (Lists.newArrayList(Type.CHILD_ADDED, Type.CHILD_REMOVED, Type.CHILD_UPDATED).contains(event.getType())) {

                // 在选举成功后，发生节点变更，master需要触发重新分片，在这个过程需要宣告选举没完成
                if (ElectionStatus.getLeaderLatch().hasLeadership()) {
                    if (ElectionStatusType.FINISH.equals(ElectionStatus.getElectionStatus())) {
                        logger.info("NODE_INFO_PATH change, start sharding...");

                        masterAction.masterUpdateZkAndSelfElectionStatus(client, false);
                        masterAction.masterSetShardingInfo(client);
                        masterAction.masterUpdateZkAndSelfElectionStatus(client, true);
                    }
                }
            }

            // 重新连上zk是需要再写入node info，以便触发重新分片
            if (Type.CONNECTION_RECONNECTED.equals(event.getType())) {
                writeNodeInfo(client);
            }
        };

        PathChildrenCache cache = new PathChildrenCache(client, NODE_INFO_PATH, true);
        cache.getListenable().addListener(cacheListener);
        cache.start();
        // todo: 考虑cache回收问题
    }

    /**
     * 选举master
     * @param client
     * @throws Exception
     */
    private void electLeader(CuratorFramework client) throws Exception {
        LeaderLatch leaderLatch = new LeaderLatch(client, ELECTION_PATH, "Client #" + CLIENT_ID);

        leaderLatch.addListener(new LeaderLatchListener() {

            @Override
            public void isLeader() {
                try {
                    logger.info("I am Master");

                    masterAction.masterCloseSlaveListener();

                    if (ElectionStatusType.FINISH.equals(ElectionStatus.getElectionStatus())) {
                        logger.info("Starting re-election...");
                        masterAction.masterUpdateZkAndSelfElectionStatus(client, false);

                    }

                    masterAction.masterSetShardingInfo(client);

                    masterAction.masterUpdateZkAndSelfElectionStatus(client, true);

                } catch (Exception e) {
                    logger.error("Master action get error: ", e);
                }

            }

            /**
             * 作为master，断开zk的连接后会触发本方法
             */
            @Override
            public void notLeader() {
                try {
                    logger.warn("Lose master status...");
                    setSelfESAndRunBLProxy.setNotYet(new NoneBiz(), new NoneBiz());

                    slaveAction.slaveMonitorShardingInfo(client);

                    slaveAction.slaveMonitorElectionStatus(client);
                } catch (Exception e) {
                    logger.error("notLeader method get error: ", e);
                }

            }
        });


        leaderLatch.start();
        ElectionStatus.setLeaderLatch(leaderLatch);
    }
}
