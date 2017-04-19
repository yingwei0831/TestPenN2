package com.yingwei.testing.testpenn2.trans;

import android.util.Log;

//import com.yingwei.testing.testpenn2.trans.util.log.LogUtil;

import java.util.Locale;

import kr.neolab.sdk.ink.structure.DotType;

/**
 * Created by jiahe008_lvlanlan on 2017/3/29.
 */
public class Dot {

    private static final int TOTAL_PAGE = 64;

    public float x;
    public float y;
    public int dotType;
    public int pressure;
    public long timestamp;

    public Dot(float x, float y, int pressure, int dotType, long timestamp) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.dotType = dotType;
        this.timestamp = timestamp;
    }

    public Dot(int x, int y, int fx, int fy, int pressure, int dotType, long timestamp) {
        this((float)x + (float)((double)fx * 0.01D), (float)y + (float)((double)fy * 0.01D), pressure, dotType, timestamp);
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public int getDotType() {
        return this.dotType;
    }

    public int getPressure() {
        return this.pressure;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    private static final String TAG = "Dot";

    public interface ActionStep {
        byte START = 1;
        byte MOVE = 2;
        byte END = 3;
        byte REVOKE = 4;
        byte SERIAL = 5;
        byte CLEAR = 6;
        byte CLEAR_ACK = 7;
        byte SYNC_REQUEST = 8;
        byte SYNC = 9;
        byte SYNC_PREPARE = 10;
        byte SYNC_PREPARE_ACK = 11;
        byte LASER_PEN = 12;
        byte LASER_PEN_END = 13;
        byte Flip = 14;
    }

    private byte step = ActionStep.START;
    private int color;

    private int sectionId; //区域
    private int ownerId; //所有者
    private int noteId; //笔记本id
    private int pageId; //页码
    private int type;
    private int countTotal; //目前64页

    // 封装transaction中的uid，是画笔数据的owner ? account?
    private String uid;
    private int end;

    public Dot() {
    }

    public Dot(byte step) {
        this.step = step;
    }

    public Dot(byte step, String uid, int end) {
        this.step = step;
        this.uid = uid;
        this.end = end;
    }

    public Dot(byte step, int docId, int currentPageNum, int countTotal, int type) {//step, docId, currentPageNum, countTotal, type
        this.step = step;
        this.noteId = docId;
        this.pageId = currentPageNum;
        this.countTotal = countTotal;
        this.type = type;
    }

    public Dot(byte step, float x, float y, int pressure, int dotType, long timestamp) {
        this(x, y, pressure, dotType, timestamp);
        this.step = step;
    }

    public Dot(byte step, int x, int y, int fx, int fy, int pressure, int dotType, long timestamp) {
        this(x, y, fx, fy, pressure, dotType, timestamp);
        this.step = step;
    }

    public Dot(byte step, int x, int y, int fx, int fy, int pressure, int dotType, long timestamp, int color) {
        this(x, y, fx, fy, pressure, dotType, timestamp);
        this.color = color;
        this.step = step;
    }

    public static String packIndex(int index) {
        return String.format(Locale.getDefault(), "5:%d,0;", index);
    }

    public static String pack(Dot t) {
        Log.e(TAG, "pack step = " + t.getStep()); //123
        if (t.getStep() == ActionStep.SYNC) {
            return String.format(Locale.getDefault(), "%d:%s,%d;", t.getStep(), t.getUid(), t.getEnd());
        } else if (t.getStep() == ActionStep.CLEAR || t.getStep() == ActionStep.REVOKE
                || t.getStep() == ActionStep.CLEAR_ACK || t.getStep() == ActionStep.SYNC_REQUEST
                || t.getStep() == ActionStep.SERIAL
                || t.getStep() == ActionStep.SYNC_PREPARE || t.getStep() == ActionStep.SYNC_PREPARE_ACK) {
            return String.format(Locale.getDefault(), "%d:;", t.getStep());
        } else if (t.getStep() == ActionStep.Flip) { //翻页
            return String.format(Locale.getDefault(), "%d:%d,%d,%d,%d;",
                    t.getStep(), t.getNoteId(), t.getPageId(), TOTAL_PAGE, t.getType());
        }
//        return String.format(Locale.getDefault(), "%d:%f,%f,%d;", t.getStep(), t.getX(), t.getY(), t.getColor());
        return String.format(Locale.getDefault(), "%d:%.2f,%.2f,%d,%d,%d,%d;",
                t.getStep(), t.getX(), t.getY(), t.getPressure(), t.getType(), t.getTimestamp(), t.getColor());
    }

    public static Dot unpack(String data) {
        Log.e(TAG, "unpack data = " + data);
        int sp1 = data.indexOf(":");
        byte step;
        try {
            if (sp1 <= 0) {
                step = Byte.parseByte(data);
            } else {
                step = Byte.parseByte(data.substring(0, sp1));
            }
            Log.e(TAG, "unpack step = " + step); //235 125
            if (step == ActionStep.START || step == ActionStep.MOVE || step == ActionStep.END
                    || step == ActionStep.LASER_PEN) {
                // 画笔 起始点，移动点，结束点
                int sp2 = data.indexOf(",");
                if (sp2 <= 2) {
                    return null;
                }
                String[] dotData = data.substring(sp1+1).split(",");
                return new Dot(step,
                        Float.valueOf(dotData[0]).intValue(), Float.valueOf(dotData[1]).intValue(),
                        Float.valueOf(Float.valueOf(dotData[0]) * 100 % 100).intValue(), Float.valueOf(Float.valueOf(dotData[1]) * 100 % 100).intValue(),
                        Integer.parseInt(dotData[2]), Integer.parseInt(dotData[3]), Long.parseLong(dotData[4]), Integer.parseInt(dotData[5]));
            } else if (step == ActionStep.SYNC) {
                // 同步
                int sp2 = data.indexOf(",");
                if (sp2 <= 2) {
                    return null;
                }
                String uid = data.substring(sp1 + 1, sp2);
                int end = Integer.parseInt(data.substring(sp2 + 1));
                Log.i(TAG, "Syncing，recive sync data, account:" + uid + ", end:" + end);
                return new Dot(step, uid, end);
            } else if (step == 5) {
                // 包序号
                String id = data.substring(sp1 + 1);
                Log.i(TAG, "RECV DATA " + id);
            } else if (step == ActionStep.Flip) { //文档分享？ 翻页
                Log.i(TAG, "Receive Flip Data");
                // 翻页
//                int sp2 = data.indexOf(",");
//                String docId = data.substring(sp1 + 1, sp2);
//                int sp3 = data.indexOf(",", sp2 + 1);
//                int currentPageNum = Integer.parseInt(data.substring(sp2 + 1, sp3));
//                int sp4 = data.indexOf(",", sp3 + 1);
//                int countTotal = Integer.parseInt(data.substring(sp3 + 1, sp4));
//                int type = Integer.parseInt(data.substring(sp4 + 1));
                String[] dotData = data.substring(sp1+1).split(",");
//                t.getStep(), t.getNoteId(), t.getPageId(), TOTAL_PAGE, t.getType()
                //step, docId, currentPageNum, countTotal, type
                return new Dot(step,
                        Integer.parseInt(dotData[0]), Integer.parseInt(dotData[1]), Integer.parseInt(dotData[2]), Integer.parseInt(dotData[3]));
            } else {
                Log.i(TAG, "recieve step:" + step);
                // 其他控制指令
                return new Dot(step);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Dot makeStartTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        make(ActionStep.START, sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
        return this;
    }

    public Dot makeStartTransaction(float x, float y, int rgb) {
        make(ActionStep.START, x, y, rgb);
        return this;
    }

    public Dot makeMoveTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        make(ActionStep.MOVE, sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
        return this;
    }

    public Dot makeEndTransaction(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        make(ActionStep.END, sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
        return this;
    }

    public Dot makeRevokeTransaction() {
        make(ActionStep.REVOKE);
        return this;
    }

    public Dot makeSyncPrepareTransaction() {
        make(ActionStep.SYNC_PREPARE);
        return this;
    }

    public Dot makeClearAckTransaction() {
        make(ActionStep.CLEAR_ACK);
        return this;
    }

    public Dot makeSyncPrepareAckTransaction() {
        make(ActionStep.SYNC_PREPARE_ACK);
        return this;
    }

    public Dot makeFlipTranscation(int docId, int currentPageNum, int pageCount, int type) {
        make(ActionStep.Flip, docId, currentPageNum, pageCount, type);
        return this;
    }
    /**
     * 翻页
     *
     * @param step           命令
     * @param docId          文档id
     * @param currentPageNum 当前页数，1开始计算
     * @param pageCount      总页数
     * @param type           状态通知 0：无。1：翻页操作
     */
    private void make(byte step, int docId, int currentPageNum, int pageCount, int type) {
        this.step = step;
        this.noteId = docId;
        this.pageId = currentPageNum;
        this.countTotal = pageCount;
        this.type = type;
    }

    private void make(byte step, int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        this.step = step;
        this.x = (float)x + (float)((double)fx * 0.01D);
        this.y = (float)y + (float)((double)fy * 0.01D);

        this.sectionId = sectionId;
        this.ownerId = ownerId;
        this.noteId = noteId;
        this.pageId = pageId;
        this.pressure = pressure;
        this.timestamp = timestamp;
        this.type = type;
        this.color = color;
    }

    public Dot makeClearSelfTransaction() {
        make(ActionStep.CLEAR);
        return this;
    }

    private void make(byte step) {
        this.step = step;
    }

    public boolean isSync() {
        return step == ActionStep.SYNC;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public byte getStep() {
        return step;
    }
    public void setStep(byte step) {
        this.step = step;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
