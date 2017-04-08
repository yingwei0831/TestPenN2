package com.yingwei.testing.testpenn2.trans;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiahe008_lvlanlan on 2017/3/28.
 */
public class DotManager {

    private final int TIMER_TASK_PERIOD = 30;

    private String sessionId;

    private String toAccount;

    private Handler handler;

    private List<Dot> cache = new ArrayList<>(1000);

    public DotManager(String sessionId, String toAccount, Context context) {
        this.sessionId = sessionId;
        this.toAccount = toAccount;
        this.handler = new Handler(context.getMainLooper());
        this.handler.postDelayed(timerTask, TIMER_TASK_PERIOD); // 立即开启定时器
    }

    public void registerTransactionObserver(DotObserver o) {
        DotCenter.getInstance().registerObserver(sessionId, o);
    }

    public void sendStartTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        cache.add(new Dot().makeStartTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color));
    }

    public void sendMoveTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        cache.add(new Dot().makeMoveTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color));
    }

    public void sendEndTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        cache.add(new Dot().makeEndTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color));
    }

    public void sendFlipTransaction(int docId, int currentPageNum, int pageCount, int type) {
        List<Dot> flipCache = new ArrayList<>();
        flipCache.add(new Dot().makeFlipTranscation(docId, currentPageNum, pageCount, type));
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
