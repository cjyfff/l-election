package com.cjyfff.election.biz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by jiashen on 18-11-21.
 */

@Component
public class ElectionBizContainer {

    private static final String MBUEFB_NAME = "masterBeforeUpdateElectionFinishBiz";

    private static final String MAUEFB_NAME = "masterAfterUpdateElectionFinishBiz";

    private static final String MBUENYB_NAME = "masterBeforeUpdateElectionNotYetBiz";

    private static final String MAUENYB_NAME = "masterAfterUpdateElectionNotYetBiz";

    private static final String SBUEFB_NAME = "slaveBeforeUpdateElectionFinishBiz";

    private static final String SAUEFB_NAME = "slaveAfterUpdateElectionFinishBiz";

    private static final String SBUENYB_NAME = "slaveBeforeUpdateElectionNotYetBiz";

    private static final String SAUENYB_NAME = "slaveAfterUpdateElectionNotYetBiz";

    private Map<String, ElectionBiz> containerMap = new HashMap<>();

    public ElectionBizContainer() {
        List<String> nameList = Lists.newArrayList(MBUEFB_NAME, MAUEFB_NAME, MBUENYB_NAME,
            MAUENYB_NAME, SBUEFB_NAME, SAUEFB_NAME, SBUENYB_NAME, SAUENYB_NAME);

        for (String name : nameList) {
            this.containerMap.put(name, new NoneBiz());
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    public void setContainer() {
        String[] allBeansNames = applicationContext.getBeanDefinitionNames();

        for (String name : allBeansNames) {

            if (containerMap.get(name) != null) {
                containerMap.put(name, (ElectionBiz) applicationContext.getBean(name));
            }
        }
    }

    public ElectionBiz getMasterBeforeUpdateElectionFinishBiz() {
        return containerMap.get(MBUEFB_NAME);
    }

    public ElectionBiz getMasterAfterUpdateElectionFinishBiz() {
        return containerMap.get(MAUEFB_NAME);
    }

    public ElectionBiz getMasterBeforeUpdateElectionNotYetBiz() {
        return containerMap.get(MBUENYB_NAME);
    }

    public ElectionBiz getMasterAfterUpdateElectionNotYetBiz() {
        return containerMap.get(MAUENYB_NAME);
    }

    public ElectionBiz getSlaveBeforeUpdateElectionFinishBiz() {
        return containerMap.get(SBUEFB_NAME);
    }

    public ElectionBiz getSlaveAfterUpdateElectionFinishBiz() {
        return containerMap.get(SAUEFB_NAME);
    }

    public ElectionBiz getSlaveBeforeUpdateElectionNotYetBiz() {
        return containerMap.get(SBUENYB_NAME);
    }

    public ElectionBiz getSlaveAfterUpdateElectionNotYetBiz() {
        return containerMap.get(SAUENYB_NAME);
    }
}
