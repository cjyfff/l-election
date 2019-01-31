package com.cjyfff.election.core.master;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.alibaba.fastjson.JSON;

import com.cjyfff.election.biz.ElectionBizContainer;
import com.cjyfff.election.core.info.ElectionListener;
import com.cjyfff.election.core.info.ElectionStatus.ElectionStatusType;
import com.cjyfff.election.core.ElectionComponent;
import com.cjyfff.election.core.info.SetSelfESAndRunBLProxy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jiashen on 18-8-25.
 */
@Component
public class MasterAction {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String NODE_INFO_PATH = "/node_info";

    private static final String SHARDING_INFO_PATH = "/sharding_info";

    private static final String ELECTION_STATUS_PATH = "/election_status";

    @Autowired
    private ElectionComponent electionComponent;

    @Autowired
    private SetSelfESAndRunBLProxy setSelfESAndRunBLProxy;

    @Autowired
    private ElectionBizContainer electionBizContainer;

    /**
     * master统计节点，分配node id，写入zk
     * 同时更新本机节点信息
     * @param client
     */
    public void masterSetShardingInfo(CuratorFramework client) throws Exception {
        List<String> nodeHostList = client.getChildren().forPath(NODE_INFO_PATH);
        Byte nodeId = 0;
        ConcurrentSkipListMap<Byte, String> shardingMap = new ConcurrentSkipListMap<>();
        for (String nodeHost : nodeHostList) {
            shardingMap.put(nodeId, nodeHost);
            logger.info("Host: " + nodeHost + ", get nodeId: " + nodeId);
            nodeId ++;
        }

        Stat stat = client.checkExists().forPath(SHARDING_INFO_PATH);
        String shardingInfo = JSON.toJSONString(shardingMap);
        if (stat == null) {
            // SHARDING_INFO_PATH设为临时节点
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .forPath(SHARDING_INFO_PATH, shardingInfo.getBytes());
        } else {
            client.setData().forPath(SHARDING_INFO_PATH, shardingInfo.getBytes());
        }

        electionComponent.updateSelfShardingInfo(shardingMap);
    }


    /**
     * master把选举状态信息写入zk
     * @param client
     * @Param isFinish 是否选举完成
     * @throws Exception
     */
    public void masterClaimElectionStatus(CuratorFramework client, boolean isFinish) throws Exception {
        ElectionStatusType electionStatusType;
        if (isFinish) {
            electionStatusType = ElectionStatusType.FINISH;
        } else {
            electionStatusType = ElectionStatusType.NOT_YET;
        }

        Stat stat = client.checkExists().forPath(ELECTION_STATUS_PATH);
        if (stat == null) {
            // ELECTION_STATUS_PATH设为临时节点
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(ELECTION_STATUS_PATH,
                electionStatusType.getValue().toString().getBytes());
        } else {
            client.setData().forPath(ELECTION_STATUS_PATH,
                electionStatusType.getValue().toString().getBytes());
        }
    }

    /**
     * master更新本机选举状态
     * @Param isFinish 是否选举完成
     */
    public void masterUpdateSelfStatus(boolean isFinish) throws Exception {

        if (isFinish) {
            logger.info("*** Election finish. I am master. ***");
            setSelfESAndRunBLProxy.setFinish(
                electionBizContainer.getMasterBeforeUpdateElectionFinishBiz(),
                electionBizContainer.getMasterAfterUpdateElectionFinishBiz());
        } else {
            setSelfESAndRunBLProxy.setNotYet(
                electionBizContainer.getMasterBeforeUpdateElectionNotYetBiz(),
                electionBizContainer.getMasterAfterUpdateElectionNotYetBiz());
        }

    }

    /**
     * 成为master后，可能master是由slave转变而成的
     * 一部分zk目录是由master创建，master无需再监控这些节点，所以需要关闭之前作为slave
     * 而创建的listener
     */
    public void masterCloseSlaveListener() throws Exception {
        if (ElectionListener.getSlaveMonitorShardingInfoListener() != null) {
            ElectionListener.getSlaveMonitorShardingInfoListener().close();
        }

        if (ElectionListener.getSlaveMonitorElectionStatusListener() != null) {
            ElectionListener.getSlaveMonitorElectionStatusListener().close();
        }
    }

    /**
     * master 更新 zk 与自身的选举状态
     * @throws Exception
     */
    public void masterUpdateZkAndSelfElectionStatus(CuratorFramework client, boolean isFinish) throws Exception {
        if (isFinish) {
            // todo: 处理本机设置选举成功后，node info change listener才回调导致2次分片的问题
            masterUpdateSelfStatus(true);

            try {
                masterClaimElectionStatus(client, true);
            } catch (Exception e) {
                // 更新远端zk选举状态失败时，把自身选举状态置为未完成
                masterUpdateSelfStatus(false);
                throw e;
            }

        } else {
            masterUpdateSelfStatus(false);

            masterClaimElectionStatus(client, false);
        }
    }
}
