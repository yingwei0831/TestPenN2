package com.yingwei.testing.testpenn2.trans;

import android.text.TextUtils;
import android.util.Log;

import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.model.RTSTunData;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by jiahe008_lvlanlan on 2017/3/28.
 */
public class DotCenter {

    private int index = 0;

    private final String TAG = "DotCenter";
    // sessionId to TransactionObserver
    private Map<String, DotObserver> observers = new HashMap<>(2);

    // <sessionId, <account, transactions>>
    private Map<String, Map<String, List<Dot>>> syncCache = new HashMap<>();

    private boolean isDoodleViewInited = false;

    public static DotCenter getInstance() {
        return TransactionCenterHolder.instance;
    }

    private static class TransactionCenterHolder {
        public static final DotCenter instance = new DotCenter();
    }

    public void registerObserver(String sessionId, DotObserver o) {
        this.observers.put(sessionId, o);
    }

    /**
     * 数据发送
     */
    public void sendToRemote(String sessionId, String toAccount, List<Dot> transactions) {
        Log.e(TAG, "---------sendToRemote: sessionId = " + sessionId + ", toAccount = " + toAccount);
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        String data = pack(transactions);
        try {
            RTSTunData channelData = new RTSTunData(sessionId, toAccount, data.getBytes("UTF-8"), data.getBytes().length);
            boolean isSend = RTSManager.getInstance().sendData(channelData);
            Log.i(TAG, "SEND DATA = " + index + ", BYTES = " + data.getBytes().length + ", isSend=" + isSend);
        } catch (UnsupportedEncodingException e) {
            Log.e("Transaction", "send to remote, getBytes exception : " + data);
        }
    }

    /**
     * 数据接收
     */
    public void onReceive(String sessionId, String account, String data) {
        Log.e(TAG, "------onReceive: sessionId = " + sessionId + ", account = " + account);
        List<Dot> transactions = unpack(data);
        if ((transactions != null ? transactions.size() : 0) <= 0) {
            return;
        }
        int step = transactions.get(0).getStep();

        if (observers.containsKey(sessionId)) {
            // 断网重连数据同步
            if (step == Dot.ActionStep.SYNC) {
                observers.get(sessionId).onTransaction(transactions.get(0).getUid(), transactions);
            } else {
                // 接收数据
                observers.get(sessionId).onTransaction(account, transactions);
            }
        }

        if (step == Dot.ActionStep.LASER_PEN || step == Dot.ActionStep.LASER_PEN_END) {
            return;
        }
        saveCacheData(sessionId, account, transactions, step);

        RevokeOrClearCache(sessionId, account, step);
    }

    private void saveCacheData(String sessionId, String account, List<Dot> transactions, int step) {
        if (step == Dot.ActionStep.SYNC || !DotCenter.getInstance().isDoodleViewInited()) {
            String paintAccount;
            Dot t = transactions.get(0);
            if (t.getStep() == Dot.ActionStep.SYNC) {
                paintAccount = t.getUid();
            } else {
                paintAccount = account;
            }

            Map<String, List<Dot>> map = syncCache.get(sessionId);
            if (map == null) {
                map = new HashMap<>();
                map.put(paintAccount, transactions);
            } else {
                List<Dot> syncTrans = map.get(paintAccount);
                if (syncTrans == null) {
                    syncTrans = new ArrayList<>();
                }
                syncTrans.addAll(transactions);
                map.put(paintAccount, syncTrans);
            }
            syncCache.put(sessionId, map);
        }
    }


    private void RevokeOrClearCache(String sessionId, String account, int step) {
        // 白板还没有初始化，收到的信息，存储在syncCache中。若撤回或者清空，不处理，则会数据冗余
        if (!DotCenter.getInstance().isDoodleViewInited()) {
            if (step == Dot.ActionStep.CLEAR) {
                syncCache.remove(sessionId);
            } else if (step == Dot.ActionStep.REVOKE) {
                Map<String, List<Dot>> map = syncCache.get(sessionId);
                if (map != null) {
                    List<Dot> t = map.get(account);
                    for (int i = t.size() - 1; i >= 0; i--) {
                        if (t.get(i).getStep() != Dot.ActionStep.START) {
                            t.remove(i);
                            continue;
                        }
                        t.remove(i);
                        break;
                    }
                    map.put(account, t);
                    syncCache.put(sessionId, map);
                }
            }
        }
    }

    private String pack(List<Dot> transactions) {
        StringBuilder sb = new StringBuilder();

        List<Dot> tempList = new ArrayList<>();
        tempList.addAll(transactions);
        for (Dot t : tempList) {
            String data = Dot.pack(t);
            Log.e(TAG, "pack data = " + data);
            sb.append(data);
        }

        // 打入序号
        sb.append(Dot.packIndex(++index));

        return sb.toString();
    }

    private List<Dot> unpack(String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }

        List<Dot> transactions = new ArrayList<>();
        String[] pieces = data.split(";");
        for (String p : pieces) {
            Dot t = Dot.unpack(p);
            if (t != null) {
                transactions.add(t);
            }
        }

        return transactions;
    }

    public Map<String, List<Dot>> getSyncDataMap(String sessionId) {
        return syncCache.get(sessionId);
    }

    public Map<String, Map<String, List<Dot>>> getSyncCache() {
        return syncCache;
    }

    public boolean isDoodleViewInited() {
        return isDoodleViewInited;
    }

    public void setDoodleViewInited(boolean doodleViewInited) {
        isDoodleViewInited = doodleViewInited;
    }
}
