package com.cjyfff.election.core.info;

import org.apache.curator.framework.recipes.leader.LeaderLatch;

/**
 * Created by jiashen on 18-8-25.
 */
public class ElectionStatus {

    private static volatile ElectionStatusType electionStatus = ElectionStatusType.NOT_YET;

    private static volatile LeaderLatch leaderLatch;

    public static ElectionStatusType getElectionStatus() {
        return electionStatus;
    }

    static void setElectionStatus(ElectionStatusType electionStatus) {
        ElectionStatus.electionStatus = electionStatus;
    }

    public static LeaderLatch getLeaderLatch() {
        return leaderLatch;
    }

    public static void setLeaderLatch(LeaderLatch leaderLatch) {
        ElectionStatus.leaderLatch = leaderLatch;
    }

    public enum ElectionStatusType {

        /**
         * 未完成
         */
        NOT_YET("NOT YET", 0),

        /**
         * 已完成
         */
        FINISH("FINISH", 1);

        private String desc;

        private Integer value;

        ElectionStatusType(String desc, Integer value) {
            this.desc = desc;
            this.value = value;
        }

        public static String getDesc(Integer value) {
            for (ElectionStatusType electionStatusType : ElectionStatusType.values()) {
                if (electionStatusType.getValue().equals(value)) {
                    return electionStatusType.getDesc();
                }
            }
            return null;
        }

        public static Integer getValue(String desc) {
            for (ElectionStatusType electionStatusType : ElectionStatusType.values()) {
                if (electionStatusType.getDesc().equals(desc)) {
                    return electionStatusType.getValue();
                }
            }
            return null;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }
}
