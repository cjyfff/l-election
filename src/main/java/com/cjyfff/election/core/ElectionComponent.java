package com.cjyfff.election.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.cjyfff.election.info.ShardingInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by jiashen on 2018/9/8.
 */
@Component
@Slf4j
public class ElectionComponent {

    @Autowired
    private ShardingInfo shardingInfo;

    @Value("${server.port}")
    private String servicePort;

    @Value("${l_election.specified_local_ip}")
    private String localIp;

    public void updateSelfShardingInfo(ConcurrentHashMap<Integer, String> shardingMap) throws Exception {
        if (shardingMap == null) {
            shardingInfo.setShardingMap(null);
            return;
        }

        String host = getHost();

        shardingInfo.setShardingMap(shardingMap);

        Integer nodeId = null;
        for (Entry<Integer, String> node : shardingMap.entrySet()) {
            if (host.equals(node.getValue())) {
                nodeId = node.getKey();
                break;
            }
        }
        if (nodeId == null) {
            log.warn("Invalid Sharding Map, can not find self node info.");
        } else {
            shardingInfo.setNodeId(nodeId);
        }
    }

    /**
     * 获取本机的ip以及端口号组合
     * 在网络断开再恢复时，自身ip在一段时间内可能会获取不到，因此加上重试机制
     * @return
     */
    public String getHost() throws Exception {

        if (StringUtils.isEmpty(localIp)) {
            InetAddress addr = null;
            while (addr == null) {
                try {
                    addr = InetAddress.getLocalHost();
                } catch (UnknownHostException ue) {
                    log.warn("Can not get local ip info, retrying...");
                    TimeUnit.SECONDS.sleep(5);
                }

            }
            localIp = addr.getHostAddress();
        }

        return localIp + ":" + servicePort;
    }
}
