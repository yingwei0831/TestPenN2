package com.yingwei.testing.testpenn2.doodleback;

import android.content.Context;
import android.os.Handler;

import com.yingwei.testing.testpenn2.trans.Dot;
import com.yingwei.testing.testpenn2.trans.DotCenter;
import com.yingwei.testing.testpenn2.trans.DotObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiahe008_lvlanlan on 2017/3/28.
 */
public class DotManagerBack {

    private final int TIMER_TASK_PERIOD = 30;

    private String sessionId;

    private String toAccount;

    private Handler handler;

    private List<Dot> cache = new ArrayList<>(1000);

    public DotManagerBack(String sessionId, String toAccount, Context context) {
        this.sessionId = sessionId;
        this.toAccount = toAccount;
        this.handler = new Handler(context.getMainLooper());
        this.handler.postDelayed(timerTask, TIMER_TASK_PERIOD); // 立即开启定时器
    }

    public void end() {
        this.handler.removeCallbacks(timerTask);
    }

    public void registerTransactionObserver(DotObserver o) {
        DotCenter.getInstance().registerObserver(sessionId, o);
    }

    public void sendStartTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        cache.add(new Dot().makeStartTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color));
    }

    public void sendStartTransaction(float x, float y, int rgb, int typeDiffer) {
        cache.add(new Dot().makeStartTransaction(x, y, rgb, typeDiffer));
    }

    public void sendMoveTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        cache.add(new Dot().makeMoveTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color));
    }

    public void sendMoveTransaction(float x, float y, int rgb, int typeDiffer) {
        cache.add(new Dot().makeMoveTransaction(x, y, rgb, typeDiffer));
    }


    public void sendEndTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        cache.add(new Dot().makeEndTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color));
    }

    public void sendEndTransaction(float x, float y, int rgb, int typeDiffer) {
        cache.add(new Dot().makeEndTransaction(x, y, rgb, typeDiffer));
    }


    public void sendRevokeTransaction() {
        cache.add(new Dot().makeRevokeTransaction());
    }

    public void sendSyncPrepareTransaction() {
        cache.add(new Dot().makeSyncPrepareTransaction());
    }

    public void sendClearAckTransaction() {
        cache.add(new Dot().makeClearAckTransaction());
    }

    public void sendSyncPrepareAckTransaction() {
        cache.add(new Dot().makeSyncPrepareAckTransaction());
    }

    public void sendSyncTransaction(String toAccount, String paintAccount, int end, List<Dot> transactions, int typeDiffer) {
        List<Dot> syncCache = new ArrayList<>(1000);
        // 封装transaction中的uid，是画笔数据的owner
        syncCache.add(new Dot().makeSyncTransaction(paintAccount, end, typeDiffer));
        syncCache.addAll(transactions);
        // account 是发送的对象
        DotCenter.getInstance().sendToRemote(sessionId, toAccount, syncCache);
    }

    public void sendFlipTranscation(int docId, int currentPageNum, int pageCount, int type) {
        List<Dot> flipCache = new ArrayList<>();
        flipCache.add(new Dot().makeFlipTranscation(docId, currentPageNum, pageCount, type));
        DotCenter.getInstance().sendToRemote(sessionId, toAccount, flipCache);
    }

    public void sendFlipTranscation(String docId, int currentPageNum, int pageCount, int type, int typeDiffer) {
        List<Dot> flipCache = new ArrayList<>();
        flipCache.add(new Dot().makeFlipTransaction(docId, currentPageNum, pageCount, type, typeDiffer));
        DotCenter.getInstance().sendToRemote(sessionId, toAccount, flipCache);
    }


    public void sendClearSelfTransaction() {
        cache.add(new Dot().makeClearSelfTransaction());
    }

    private Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(timerTask);
            {
                if (cache.size() > 0) {
                    sendCacheTransaction();
                }
            }
            handler.postDelayed(timerTask, TIMER_TASK_PERIOD);
        }
    };

    private void sendCacheTransaction() {
        DotCenter.getInstance().sendToRemote(sessionId, toAccount, this.cache);
        cache.clear();
    }
}
