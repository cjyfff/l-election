package com.cjyfff.election.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import com.cjyfff.election.exception.ElectionException;
import com.cjyfff.election.core.info.ShardingInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by jiashen on 2018/9/8.
 */
@Component
@Slf4j
public class ElectionComponent {

    @Value("${l_election.specified_port}")
    private String servicePort;

    @Value("${l_election.specified_local_ip}")
    private String localIp;

    public void updateSelfShardingInfo(ConcurrentSkipListMap<Integer, String> shardingMap) throws Exception {
        if (shardingMap == null) {
            ShardingInfo.setShardingMap(null);
            return;
        }

        String host = getHost();

        ShardingInfo.setShardingMap(shardingMap);

        Integer shardingId = null;
        for (Entry<Integer, String> node : shardingMap.entrySet()) {
            if (host.equals(node.getValue())) {
                shardingId = node.getKey();
                break;
            }
        }
        if (shardingId == null) {
            log.warn("Invalid Sharding Map, can not find self node info.");
        } else {
            ShardingInfo.setShardingId(shardingId);
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

        if (StringUtils.isEmpty(servicePort)) {
            throw new ElectionException("servicePort can not be null!");
        }

        return localIp + ":" + servicePort;
    }
}
