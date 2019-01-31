package com.cjyfff.election.core.info;

import com.cjyfff.election.biz.ElectionBiz;
import com.cjyfff.election.exception.ElectionException;
import com.cjyfff.election.core.info.ElectionStatus.ElectionStatusType;
import org.springframework.stereotype.Component;

/**
 * 设置本机选举状态，并运行相关业务逻辑代理类
 * Created by jiashen on 2018/9/9.
 */
@Component
public class SetSelfESAndRunBLProxy {

    public void setFinish(ElectionBiz before, ElectionBiz after) throws Exception {
        checkBeforeAndAfterMethod(before, after);

        before.run();
        ElectionStatus.setElectionStatus(ElectionStatusType.FINISH);
        after.run();
    }

    public void setNotYet(ElectionBiz before, ElectionBiz after) throws Exception {
        checkBeforeAndAfterMethod(before, after);

        before.run();
        ElectionStatus.setElectionStatus(ElectionStatusType.NOT_YET);
        after.run();
    }

    private void checkBeforeAndAfterMethod(ElectionBiz before, ElectionBiz after) throws ElectionException {
        if (before == null || after == null) {
            throw new ElectionException("Before Method or After Method can neither be null.");
        }
    }
}
