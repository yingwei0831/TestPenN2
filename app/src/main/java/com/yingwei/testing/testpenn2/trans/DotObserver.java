package com.yingwei.testing.testpenn2.trans;

import java.util.List;


/**
 * Created by huangjun on 2015/6/24.
 */
public interface DotObserver {
    void onTransaction(String account, List<Dot> transactions);
}
