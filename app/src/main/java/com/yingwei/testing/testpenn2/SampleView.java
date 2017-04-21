package com.yingwei.testing.testpenn2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yingwei.testing.testpenn2.trans.DemoCache;
import com.yingwei.testing.testpenn2.trans.DotManager;
import com.yingwei.testing.testpenn2.trans.DotObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.neolab.sdk.graphic.Renderer;
import kr.neolab.sdk.ink.structure.Dot;
import kr.neolab.sdk.ink.structure.DotType;
import kr.neolab.sdk.ink.structure.Stroke;

public class SampleView extends SurfaceView implements SurfaceHolder.Callback, DotObserver {
    private static final String TAG = "SampleView";

    private SampleThread mSampleThread;

    // paper background
    private Bitmap background = null;

    // draw the strokes
    private ArrayList<Stroke> strokes = new ArrayList<Stroke>();

    private Stroke stroke = null;

    private int sectionId = 0, ownerId = 0, noteId = 0, pageId = 0;

    private float scale = 12, offsetX = 0, offsetY = 0;

    private float xZoom = 0;
    private float yZoom = 0;
    private DotManager transactionManager; // 数据发送管理器
//    private boolean isSurfaceViewCreated;

    // <account, transactions>
    private Map<String, List<com.yingwei.testing.testpenn2.trans.Dot>> userDataMap = new HashMap<>();

    /**
     * 蓝牙笔测试，换做网络通信则需要注释掉
     */
    public SampleView(Context context) {
        super(context);
        getHolder().addCallback(this);
        mSampleThread = new SampleThread(this.getHolder(), this);
    }

    /**
     * 蓝牙笔测试，换做网络通信则需要注释掉
     */
    public SampleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        mSampleThread = new SampleThread(this.getHolder(), this);
    }

    public void initTransactionManager(Context context, String sessionId, String toAccount){
        transactionManager = new DotManager(sessionId, toAccount, context);
        transactionManager.registerTransactionObserver(this);
    }

    //--------以上是一种初始化方式，以下是一种初始化方式---------

    public SampleView(Context context, String sessionId, String toAccount) {
        super(context);

        getHolder().addCallback(this);
        mSampleThread = new SampleThread(this.getHolder(), this);
        transactionManager = new DotManager(sessionId, toAccount, context);
        transactionManager.registerTransactionObserver(this);
    }

    public void sendStartTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        transactionManager.sendStartTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
    }

    public void sendMoveTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        transactionManager.sendMoveTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
    }

    public void sendEndTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        transactionManager.sendEndTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
    }

    /**
     * 清空
     */
    public void clear() {
        clearAll();
        transactionManager.sendClearSelfTransaction();
    }

    public void sendFlipData(int docId, int currentPageNum, int pageCount, int type) {
        transactionManager.sendFlipTranscation(docId, currentPageNum, pageCount, type);
        saveUserData(DemoCache.getAccount(),
                new com.yingwei.testing.testpenn2.trans.Dot().makeFlipTranscation(docId, currentPageNum, pageCount, type),
                false, false, true);
    }

    public void clearAll() {
        saveUserData(DemoCache.getAccount(), null, false, true, false);
    }

    public void setPageSize(float width, float height) {
        Log.e(TAG, "setPageSize：width = " + width + ", height = " + height);
        if (getWidth() <= 0 || getHeight() <= 0 || width <= 0 || height <= 0) {
            return;
        }

        float width_ratio = getWidth() / width;
        float height_ratio = getHeight() / height;
        Log.e(TAG, "width_ratio = " + width_ratio + ", height_ratio = " + height_ratio);
        scale = Math.min(width_ratio, height_ratio);

        int docWidth = (int) (width * scale);
        int docHeight = (int) (height * scale);

        int mw = getWidth() - docWidth;
        int mh = getHeight() - docHeight;

        offsetX = mw / 2;
        offsetY = mh / 2;

        background = Bitmap.createBitmap(docWidth, docHeight, Bitmap.Config.ARGB_8888);
        background.eraseColor(Color.parseColor("#F9F9F9"));
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(Color.WHITE); //LTGRAY

        if (background != null) {
            canvas.drawBitmap(background, offsetX, offsetY, null);
        }

        if (strokes != null && strokes.size() > 0) {
            Renderer.draw(canvas, strokes.toArray(new Stroke[0]), scale, offsetX, offsetY);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Log.e(TAG, "surfaceChanged: format = " + format + ", width = " + width + ", height = " + height);

        int x_p = Double.valueOf(width / 176 * 2.371).intValue(); //176
        int y_p = Double.valueOf(height / 250 * 2.371).intValue(); //250
        scale = Math.min(x_p, y_p);

        Log.e(TAG, "scale = " + scale + ", offsetX = " + offsetX + ", offsetY = " + offsetY); //arg1 = 4, arg2 = 1080, arg3 = 1920
        xZoom = width;
        yZoom = height;
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
//        isSurfaceViewCreated = true;
        mSampleThread = new SampleThread(getHolder(), this);
        mSampleThread.setRunning(true);
        mSampleThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
//        isSurfaceViewCreated = false;
        mSampleThread.setRunning(false);

        boolean retry = true;

        while (retry) {
            try {
                mSampleThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.getStackTrace();
            }
        }
    }

    public void addDot(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int force, long timestamp, int type, int color) {
        if (this.sectionId != sectionId || this.ownerId != ownerId || this.noteId != noteId || this.pageId != pageId) {
            strokes = new ArrayList<Stroke>();

            this.sectionId = sectionId;
            this.ownerId = ownerId;
            this.noteId = noteId;
            this.pageId = pageId;
        }

        if (DotType.isPenActionDown(type) || stroke == null || stroke.isReadOnly()) {
            stroke = new Stroke(sectionId, ownerId, noteId, pageId, color);
            strokes.add(stroke);
        }

        stroke.add(new Dot(x, y, fx, fy, force, type, timestamp));
    }

    public void addStrokes(Stroke[] strs) {
        for (Stroke stroke : strs) {
            strokes.add(stroke);
        }
    }

    public void saveUserData(String account, com.yingwei.testing.testpenn2.trans.Dot t, boolean isBack, boolean isClear, boolean isFlip) {
        List<com.yingwei.testing.testpenn2.trans.Dot> list = userDataMap.get(account);
        if (isBack) {
            while (list != null && list.size() > 0 && list.get(list.size() - 1).getStep() != com.yingwei.testing.testpenn2.trans.Dot.ActionStep.START) {
                list.remove(list.size() - 1);
            }
            if (list != null && list.size() > 0) {
                list.remove(list.size() - 1);
            }
            userDataMap.put(account, list);
        } else if (isClear) {
            userDataMap.clear();
        } else if (isFlip) {
            if (list == null) {
                list = new ArrayList<>();
                list.add(t);
            } else {
                for (com.yingwei.testing.testpenn2.trans.Dot transaction : list) {
                    if (transaction.getStep() == com.yingwei.testing.testpenn2.trans.Dot.ActionStep.Flip) {
                        list.remove(transaction);
                        break;
                    }
                }
                list.add(t);
            }
            userDataMap.put(account, list);
        } else {
            if (list == null) {
                list = new ArrayList<>();
                list.add(t);
            } else {
                list.add(t);
            }
            userDataMap.put(account, list);
        }
    }

    public class SampleThread extends Thread {
        private SurfaceHolder surfaceholder;
        private SampleView mSampleiView;
        private boolean running = false;

        public SampleThread(SurfaceHolder surfaceholder, SampleView mView) {
            this.surfaceholder = surfaceholder;
            this.mSampleiView = mView;
        }

        public void setRunning(boolean run) {
            running = run;
        }

        @Override
        public void run() {
            setName("SampleThread");

            Canvas mCanvas;

            while (running) {
                mCanvas = null;

                try {
                    mCanvas = surfaceholder.lockCanvas(); // lock canvas

                    synchronized (surfaceholder) {
                        if (mCanvas != null) {
                            mSampleiView.draw(mCanvas);
                        }
                    }
                } finally {
                    if (mCanvas != null) {
                        surfaceholder.unlockCanvasAndPost(mCanvas); // unlock
                        // canvas
                    }
                }
            }
        }
    }

    /**
     * ******************************* 回放板 ****************************
     */

    @Override
    public void onTransaction(String account, final List<com.yingwei.testing.testpenn2.trans.Dot> transactions) {
        for (com.yingwei.testing.testpenn2.trans.Dot dot : transactions) {
            DotType actionType = DotType.getPenAction(dot.getType());
            switch (actionType) {
                case PEN_ACTION_DOWN:
                    Log.e(TAG, "<<<<<<<PEN_ACTION_DOWN");
                    break;
                case PEN_ACTION_MOVE:
                    Log.e(TAG, "-----PEN_ACTION_MOVE-----");
                    break;
                case PEN_ACTION_UP:
                    Log.e(TAG, "PEN_ACTION_UP>>>>>>>");
                    break;
            }
            addDot(sectionId, dot.getOwnerId(), dot.getNoteId(), dot.getPageId(),
                    Float.valueOf(dot.getX()).intValue(), Float.valueOf(dot.getY()).intValue(),
                    Float.valueOf(dot.getX() * 100 % 100).intValue(), Float.valueOf(dot.getY() * 100 % 100).intValue(),
                    dot.getPressure(), dot.getTimestamp(), dot.getDotType(), dot.getColor());
        }
//        if (transactions.size() > 0 && transactions.get(0).isSync()
//                && transactions.get(0).getUid().equals(DemoCache.getAccount())) {
//            // 断网重连，主播同步数据，收到自己的数据
//            if (!isSurfaceViewCreated) {
//                postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        onSyncMyTransactionsDraw(transactions);
//                    }
//                }, 50);
//            } else {
//                onSyncMyTransactionsDraw(transactions);
//            }
//            isSyncAlready = true;
//            return;
//        }

//        DoodleChannel playbackChannel = initPlaybackChannel(account);
//
//        List<com.yingwei.testing.testpenn2.trans.Dot> cache = new ArrayList<>(transactions.size());
//        for (com.yingwei.testing.testpenn2.trans.Dot t : transactions) {
//            if (t == null) {
//                continue;
//            }
//            if (t.isPaint()) {
//                // 正常画笔
//                cache.add(t);
//            } else {
//                onMultiTransactionsDraw(playbackChannel, account, cache);
//                cache.clear();
//                if (t.isRevoke()) {
//                    back(account, false);
//                } else if (t.isClearSelf()) {
//                    clearAll();
//                    transactionManager.sendClearAckTransaction();
//                } else if (t.isClearAck()) {
//                    clearAll();
//                } else if (t.isSyncRequest()) {
//                    sendSyncData(account);
//                } else if (t.isSyncPrepare()) {
//                    clearAll();
//                    transactionManager.sendSyncPrepareAckTransaction();
//                } else if (t.isFlip()) {
//                    // 收到翻页消息。先清空白板，然后做翻页操作。
//                    Log.e(TAG, "receive flip msg");
//                    flipListener.onFlipPage(t);
//                }
//            }
//        }
//
//        if (cache.size() > 0) {
//            onMultiTransactionsDraw(playbackChannel, account, cache);
//            cache.clear();
//        }
    }

    /**
     * ******************************* 基础绘图封装 ****************************
     */
//    private void onSyncMyTransactionsDraw(List<com.yingwei.testing.testpenn2.trans.Dot> transactions) {
//        int tempColor = paintChannel.paintColor;
//        for (com.yingwei.testing.testpenn2.trans.Dot t : transactions) {
//            switch (t.getStep()) {
//                case com.yingwei.testing.testpenn2.trans.Dot.ActionStep.START:
//                    paintChannel.paintColor = convertRGBToARGB(t.getRgb());
//                    onActionStart(t.getX() * xZoom, t.getY() * yZoom);
//                    break;
//                case com.yingwei.testing.testpenn2.trans.Dot.ActionStep.MOVE:
//                    onActionMove(t.getX() * xZoom, t.getY() * yZoom);
//                    break;
//                case com.yingwei.testing.testpenn2.trans.Dot.ActionStep.END:
//                    onActionEnd();
//                    break;
//                default:
//                    break;
//            }
//        }
//        paintChannel.paintColor = tempColor;
//    }
}
