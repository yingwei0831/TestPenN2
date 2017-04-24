package com.yingwei.testing.testpenn2.doodleback;

import java.util.List;


/**
 * Created by huangjun on 2015/6/24.
 */
public interface DotObserverBack {
    void onTransaction(String account, List<DotBack> transactions);
}
