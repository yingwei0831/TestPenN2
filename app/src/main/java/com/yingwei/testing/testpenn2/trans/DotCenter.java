package com.yingwei.testing.testpenn2.trans;

import android.nfc.Tag;
import android.text.TextUtils;
import android.util.Log;

import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.model.RTSTunData;
import com.yingwei.testing.testpenn2.view.ReplayActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;


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
//        String string1 = "1:12.37,12.24,34,17,1492150044850,-16777216;2:12.38,12.22,97,18,1492150044858,-16777216;2:12.37,12.22,128,18,1492150044866,-16777216;2:12.38,12.24,138,18,1492150044875,-16777216;2:12.38,12.28,159,18,1492150044883,-16777216;2:12.38,12.35,169,18,1492150044891,-16777216;2:12.37,12.45,177,18,1492150044900,-16777216;2:12.36,12.54,182,18,1492150044908,-16777216;2:12.33,12.61,185,18,1492150044916,-16777216;5:1,0;";
//        String string11 = "1:22.37,22.24,34,17,1492150045850,-16777216;2:22.38,22.22,97,18,1492150045858,-16777216;2:22.37,22.22,128,18,1492150045866,-16777216;2:22.38,22.24,138,18,1492150045875,-16777216;2:22.38,22.28,159,18,1492150045883,-16777216;2:22.38,22.35,169,18,1492150045891,-16777216;2:22.37,22.45,177,18,1492150045900,-16777216;2:22.36,22.54,182,18,1492150045908,-16777216;2:22.33,22.61,185,18,1492150045916,-16777216;5:1,0;";
//        String string2 = "2:12.30,12.70,187,18,1492150044925,-16777216;2:12.24,12.83,189,18,1492150044933,-16777216;2:12.13,13.04,189,18,1492150044941,-16777216;2:12.00,13.33,189,18,1492150044950,-16777216;2:11.85,13.62,189,18,1492150044958,-16777216;2:11.74,13.84,181,18,1492150044966,-16777216;2:11.65,14.03,171,18,1492150044975,-16777216;2:11.60,14.13,121,18,1492150044983,-16777216;2:11.59,14.19,68,18,1492150044991,-16777216;2:11.59,14.16,48,18,1492150045000,-16777216;2:11.59,14.04,10,18,1492150045008,-16777216;3:11.59,14.04,10,20,1492150045009,-16777216;1:12.50,13.13,135,17,1492150045083,-16777216;2:12.49,13.21,168,18,1492150045091,-16777216;2:12.43,13.39,175,18,1492150045100,-16777216;2:12.39,13.59,185,18,1492150045108,-16777216;2:12.36,13.81,188,18,1492150045116,-16777216;2:12.31,14.03,191,18,1492150045125,-16777216;2:12.28,14.29,191,18,1492150045133,-16777216;2:12.25,14.55,192,18,1492150045141,-16777216;5:1,0;";
//        String string21 = "2:22.30,22.70,187,18,1492150045925,-16777216;2:22.24,22.83,189,18,1492150045933,-16777216;2:22.13,23.04,189,18,1492150045941,-16777216;2:22.00,23.33,189,18,1492150045950,-16777216;2:21.85,23.62,189,18,1492150045958,-16777216;2:21.74,23.84,181,18,1492150045966,-16777216;2:21.65,24.03,171,18,1492150045975,-16777216;2:21.60,24.13,121,18,1492150045983,-16777216;2:21.59,24.19,68,18,1492150045991,-16777216;2:21.59,24.16,48,18,1492150046000,-16777216;2:21.59,24.04,10,18,1492150046008,-16777216;3:21.59,24.04,10,20,1492150046009,-16777216;1:22.50,23.13,135,17,1492150046083,-16777216;2:22.49,23.21,168,18,1492150046091,-16777216;2:22.43,23.39,175,18,1492150046100,-16777216;2:22.39,23.59,185,18,1492150046108,-16777216;2:22.36,23.81,188,18,1492150046116,-16777216;2:22.31,24.03,191,18,1492150046125,-16777216;2:22.28,24.29,191,18,1492150046133,-16777216;2:22.25,24.55,192,18,1492150046141,-16777216;5:1,0;";
//        ArrayList<String> dataList = new ArrayList<>();
//        dataList.add(string1);
//        dataList.add(string11);
//        dataList.add(string2);
//        dataList.add(string21);
//        for (String data : dataList) {

        try {
            byte byteData[] = data.getBytes("UTF-8");
            saveOutFile(sessionId, byteData, byteData.length);
            RTSTunData channelData = new RTSTunData(sessionId, toAccount, byteData, byteData.length);
            boolean isSend = RTSManager.getInstance().sendData(channelData);
            Log.e(TAG, "SEND DATA = " + index + ", BYTES = " + data.getBytes().length + ", isSend=" + isSend);
        } catch (UnsupportedEncodingException e) {
            Log.e("Transaction", "send to remote, getBytes exception : " + data);
        }
    }

    private void saveOutFile(String sessionId, byte[] byteData, int length) {
        GZIPOutputStream gos = null;
        try {
            File folder = new File(ReplayActivity.savePath);
            if (!folder.exists()){
                folder.mkdirs();
            }

            File f = new File(folder, sessionId + ".gz");
            if (!f.exists()) {
                boolean create = f.createNewFile();
                Log.e(TAG, "create = " + create);
            }
            boolean read = f.setReadable(true);
            boolean write = f.setWritable(true);
            Log.e(TAG, "read: " + read + ", write: " + write);
            Log.e(TAG, "read: " + f.canRead() + ", write: " + f.canWrite());

            gos = new GZIPOutputStream(new FileOutputStream(f, true));

            byte[] lengthB = intToByteArray(length + 8);
            gos.write(lengthB, 0, 4);

            byte[] timeB = longToByteArray(System.currentTimeMillis());
            gos.write(timeB, 0, 4);

            gos.write(byteData);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gos != null) {
                try {
                    gos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] longToByteArray(long data) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data >> 8) & 0xff);
        bytes[2] = (byte) ((data >> 16) & 0xff);
        bytes[3] = (byte) ((data >> 24) & 0xff);
        bytes[4] = (byte) ((data >> 32) & 0xff);
        bytes[5] = (byte) ((data >> 40) & 0xff);
        bytes[6] = (byte) ((data >> 48) & 0xff);
        bytes[7] = (byte) ((data >> 56) & 0xff);
        return bytes;
    }

    private byte[] intToByteArray(int data) {
        byte[] result = new byte[4];
//        result[0] = (byte) ((i >> 24) & 0xFF);
//        result[1] = (byte) ((i >> 16) & 0xFF);
//        result[2] = (byte) ((i >> 8) & 0xFF);
//        result[3] = (byte) (i & 0xFF);

        result[0] = (byte) (data & 0xff);
        result[1] = (byte) ((data & 0xff00) >> 8);
        result[2] = (byte) ((data & 0xff0000) >> 16);
        result[3] = (byte) ((data & 0xff000000) >> 24);
        return result;
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
