package com.cjyfff.election.biz;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by jiashen on 18-11-21.
 */
@Getter
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

    private ElectionBiz masterBeforeUpdateElectionFinishBiz = new NoneBiz();

    private ElectionBiz masterAfterUpdateElectionFinishBiz = new NoneBiz();

    private ElectionBiz masterBeforeUpdateElectionNotYetBiz = new NoneBiz();

    private ElectionBiz masterAfterUpdateElectionNotYetBiz = new NoneBiz();

    private ElectionBiz slaveBeforeUpdateElectionFinishBiz = new NoneBiz();

    private ElectionBiz slaveAfterUpdateElectionFinishBiz = new NoneBiz();

    private ElectionBiz slaveBeforeUpdateElectionNotYetBiz = new NoneBiz();

    private ElectionBiz slaveAfterUpdateElectionNotYetBiz = new NoneBiz();

    @Autowired
    private ApplicationContext applicationContext;

    public void setContainer() {
        String[] allBeansNames = applicationContext.getBeanDefinitionNames();

        for (String name : allBeansNames) {

            if (MBUEFB_NAME.equals(name)) {
                masterBeforeUpdateElectionFinishBiz = (ElectionBiz) applicationContext.getBean(MBUEFB_NAME);
            }

            if (MAUEFB_NAME.equals(name)) {
                masterAfterUpdateElectionFinishBiz = (ElectionBiz) applicationContext.getBean(MAUEFB_NAME);
            }

            if (MBUENYB_NAME.equals(name)) {
                masterBeforeUpdateElectionNotYetBiz = (ElectionBiz) applicationContext.getBean(MBUENYB_NAME);
            }

            if (MAUENYB_NAME.equals(name)) {
                masterAfterUpdateElectionNotYetBiz = (ElectionBiz) applicationContext.getBean(MAUENYB_NAME);
            }

            if (SBUEFB_NAME.equals(name)) {
                slaveBeforeUpdateElectionFinishBiz = (ElectionBiz) applicationContext.getBean(SBUEFB_NAME);
            }

            if (SAUEFB_NAME.equals(name)) {
                slaveAfterUpdateElectionFinishBiz = (ElectionBiz) applicationContext.getBean(SAUEFB_NAME);
            }

            if (SBUENYB_NAME.equals(name)) {
                slaveBeforeUpdateElectionNotYetBiz = (ElectionBiz) applicationContext.getBean(SBUENYB_NAME);
            }

            if (SAUENYB_NAME.equals(name)) {
                slaveAfterUpdateElectionNotYetBiz = (ElectionBiz) applicationContext.getBean(SAUENYB_NAME);
            }
        }
    }
}
