package com.yingwei.testing.testpenn2.view;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.model.RTSTunData;
import com.yingwei.testing.testpenn2.Const;
import com.yingwei.testing.testpenn2.DeviceListActivity;
import com.yingwei.testing.testpenn2.R;
import com.yingwei.testing.testpenn2.SampleView;
import com.yingwei.testing.testpenn2.Util;
import com.yingwei.testing.testpenn2.trans.DemoCache;
import com.yingwei.testing.testpenn2.trans.Dot;
import com.yingwei.testing.testpenn2.trans.DotCenter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import kr.neolab.sdk.ink.structure.DotType;
import kr.neolab.sdk.ink.structure.Stroke;
import kr.neolab.sdk.pen.IPenCtrl;
import kr.neolab.sdk.pen.PenCtrl;
import kr.neolab.sdk.pen.offline.OfflineFileParser;
import kr.neolab.sdk.pen.penmsg.IPenMsgListener;
import kr.neolab.sdk.pen.penmsg.PenMsg;
import kr.neolab.sdk.pen.penmsg.PenMsgType;

public class AVChatActivity extends AppCompatActivity implements IPenMsgListener {

    private static final String TAG = "AVChatActivity";

    private SampleView mSampleView;
    private IPenCtrl iPenCtrl;

//    private DotManager transactionManager; // 数据发送管理器

    private String sessionId; //建立通道后的返回值，必须记住 通道id
    private String toAccount; //通道中，对面 客户id
    private long channelId;

    // Notification
    protected NotificationCompat.Builder mBuilder;
    protected NotificationManager mNotifyManager;

    private TextView tvSessionId;
    private TextView tvAccount;
    private TextView tvChannelId;

    private int pageId;
    private boolean flip; //翻页

//    private int type = 2;
//    private AVChatData mChatData; //来音频通话请求，携带的数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avchat);

        parseIntent();
//        if (type == 2){
//            return;
//        }
        // create an instance of a pen controller
        iPenCtrl = PenCtrl.getInstance();

        // start a pen controller
        iPenCtrl.startup();

        // add event listener(IPenMsgListener) to a pen controller
        iPenCtrl.setListener(this);

        //after pair, should connect
//        String mac_address = "9c7bd20001d4";
//        penCtrl.connect(mac_address);

        mSampleView = new SampleView(this, sessionId, toAccount);
//        transactionManager = new DotManager(sessionId, toAccount, getApplicationContext());

        FrameLayout view = (FrameLayout) findViewById(R.id.content_view);
        view.addView(mSampleView);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(Const.Broadcast.ACTION_PEN_UPDATE), PendingIntent.FLAG_UPDATE_CURRENT); //"firmware_update"

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setContentTitle("Update Pen");
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_n);
        mBuilder.setContentIntent(pendingIntent);

        tvSessionId = (TextView) findViewById(R.id.tv_session_id);
        tvAccount = (TextView) findViewById(R.id.tv_account);
        tvChannelId = (TextView) findViewById(R.id.tv_channel_id);
        tvSessionId.setText(sessionId);
        tvAccount.setText(toAccount);
        tvChannelId.setText(String.valueOf(channelId));

        registerObserverReceiverData(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (type == 2){
//            return;
//        }
        registerObserverReceiverData(false);
    }

    private void registerObserverReceiverData(boolean register) {
        RTSManager.getInstance().observeReceiveData(sessionId, receiveDataObserver, register);
    }

    private void parseIntent() {
//        if (getIntent().getSerializableExtra("data") != null){
//            mChatData = (AVChatData) getIntent().getSerializableExtra("data");
//            type = 2;
//            return;
//        }
        sessionId = getIntent().getStringExtra("sessionId");
        toAccount = getIntent().getStringExtra("toAccount");
        channelId = getIntent().getLongExtra("channelId", -1);
        Log.e(TAG, "sessionId = " + sessionId + ", toAccount = " + toAccount + ", channelId = " + channelId);
        showToast("sessionId = " + sessionId + ", toAccount = " + toAccount + ", channelId = " + channelId);
    }

    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceiveDot(int sectionId, int ownerId, int noteId, int pageId,
                             int x, int y, int fx, int fy,
                             int pressure, long timestamp, int type, int color) {
        Log.d(TAG, "sectionId = " + sectionId + ", " + "ownerId = " + ownerId + ", " + "noteId = " + noteId + ", " + "pageId = " + pageId +
                ", " + "x = " + x + ", " + "y = " + y + ", " + "fx = " + fx + ", " + "fy = " + fy +
                ", " + "pressure = " + pressure + ", " + "timestamp = " + timestamp + ", " + "type = " + type + ", " + "color = " + color);
        sendPenDotByBroadcast(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);

        if (this.pageId == 0){
            this.pageId = pageId;
        }else{
            if (this.pageId != pageId){
                //TODO 翻页
                flip = true;
                this.pageId = pageId;
            }else{
                flip = false;
            }
        }
        if (flip){
            mSampleView.clear();
            mSampleView.sendFlipData(noteId, pageId, 64, 1);
        }
        DotType actionType = DotType.getPenAction(type);
        switch (actionType) {
            case PEN_ACTION_DOWN:
                Log.e(TAG, "<<<<<<<PEN_ACTION_DOWN");
                onPaintActionStart(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
                break;
            case PEN_ACTION_MOVE:
                Log.e(TAG, "-----PEN_ACTION_MOVE-----");
                onPaintActionMove(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
                break;
            case PEN_ACTION_UP:
                Log.e(TAG, "PEN_ACTION_UP>>>>>>>");
                onPaintActionEnd(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
                break;
        }
    }

    private void onPaintActionStart(int sectionId, int ownerId, int noteId, int pageId,
                                    int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        mSampleView.sendStartTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
        mSampleView.saveUserData(DemoCache.getAccount(), new Dot(Dot.ActionStep.START, x, y, fx, fy, pressure, type, timestamp, color), false, false, flip);
    }

    private void onPaintActionMove(int sectionId, int ownerId, int noteId, int pageId,
                                   int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        mSampleView.sendMoveTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
        mSampleView.saveUserData(DemoCache.getAccount(), new Dot(Dot.ActionStep.MOVE, x, y, fx, fy, pressure, type, timestamp, color), false, false, flip);
    }

    private void onPaintActionEnd(int sectionId, int ownerId, int noteId, int pageId,
                                  int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        mSampleView.sendEndTransaction(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
        mSampleView.saveUserData(DemoCache.getAccount(), new Dot(Dot.ActionStep.END, x, y, fx, fy, pressure, type, timestamp, color), false, false, flip);
    }

    @Override
    public void onReceiveMessage(PenMsg penMsg) {
        // The data other than Dot data is contained in the penMsg
        switch (penMsg.penMsgType) {
            // step 1. Connected with a pen (Authentication step hasn’t processed yet)
            case PenMsgType.PEN_CONNECTION_SUCCESS:
                Log.e(TAG, "PEN_CONNECTION_SUCCESS");
                break;
            // step 2. A pen requests a password (request received right after PEN_CONNECTION_SUCCESS)
            // essential password is "0000"
            case PenMsgType.PASSWORD_REQUEST:
                Log.e(TAG, "PASSWORD_REQUEST");
                String password = "0000";
                iPenCtrl.inputPassword(password);
                break;
            // step 3. Fired when ready to use pen
            // set currently using page information here.
            //In order to get all the data, set the page information as Ctrl.reqAddUsingNoteAll()
            case PenMsgType.PEN_AUTHORIZED:
                Log.e(TAG, "PEN_AUTHORIZED");
                iPenCtrl.reqAddUsingNoteAll();
                iPenCtrl.reqOfflineDataList();
                break;
            case PenMsgType.PEN_DISCONNECTED:
                Log.e(TAG, "PEN_DISCONNECTED");
                break;
        }
        sendPenMsgByBroadcast(penMsg);
    }

    private void sendPenMsgByBroadcast(PenMsg penMsg) {
        Intent i = new Intent(Const.Broadcast.ACTION_PEN_MESSAGE);
        i.putExtra(Const.Broadcast.MESSAGE_TYPE, penMsg.getPenMsgType());
        i.putExtra(Const.Broadcast.CONTENT, penMsg.getContent());

        sendBroadcast(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_avchat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connect: //连接
                connect();
                break;
            case R.id.action_disconnect: //断开
                disconnect();
                break;
            case R.id.action_offline_list: //获取离线数据列表
                getOffLineData();
                break;
            case R.id.action_offline: //展示离线数据列表
                showOffLineData();
                break;
            case R.id.action_pen_status: //笔的状态
                viewStatus();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                String address = null;

                if ((address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)) != null) {
                    iPenCtrl.connect(address);
                }
            }
        }
    }

    public void connect() {
        startActivityForResult(new Intent(getApplicationContext(), DeviceListActivity.class), 2);
    }
    private void viewStatus() {

    }

    private void showOffLineData() {
        parseOfflineData();
    }

    private void getOffLineData() {
        iPenCtrl.reqOfflineDataList();
    }

    private void disconnect() {
        iPenCtrl.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Const.Broadcast.ACTION_PEN_MESSAGE);
        filter.addAction(Const.Broadcast.ACTION_PEN_DOT);
//        filter.addAction( "firmware_update" );
        filter.addAction(Const.Broadcast.ACTION_PEN_UPDATE);

        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void sendPenDotByBroadcast(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color) {
        Intent i = new Intent(Const.Broadcast.ACTION_PEN_DOT);
        i.putExtra(Const.Broadcast.SECTION_ID, sectionId);
        i.putExtra(Const.Broadcast.OWNER_ID, ownerId);
        i.putExtra(Const.Broadcast.NOTE_ID, noteId);
        i.putExtra(Const.Broadcast.PAGE_ID, pageId);
        i.putExtra(Const.Broadcast.X, x);
        i.putExtra(Const.Broadcast.Y, y);
        i.putExtra(Const.Broadcast.FX, fx);
        i.putExtra(Const.Broadcast.FY, fy);
        i.putExtra(Const.Broadcast.PRESSURE, pressure);
        i.putExtra(Const.Broadcast.TIMESTAMP, timestamp);
        i.putExtra(Const.Broadcast.TYPE, type);
        i.putExtra(Const.Broadcast.COLOR, color);

        sendBroadcast(i);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Const.Broadcast.ACTION_PEN_MESSAGE.equals(action)) {
                int penMsgType = intent.getIntExtra(Const.Broadcast.MESSAGE_TYPE, 0);
                String content = intent.getStringExtra(Const.Broadcast.CONTENT);

                handleMsg(penMsgType, content);
            } else if (Const.Broadcast.ACTION_PEN_DOT.equals(action)) {
                int sectionId = intent.getIntExtra(Const.Broadcast.SECTION_ID, 0);
                int ownerId = intent.getIntExtra(Const.Broadcast.OWNER_ID, 0);
                int noteId = intent.getIntExtra(Const.Broadcast.NOTE_ID, 0);
                int pageId = intent.getIntExtra(Const.Broadcast.PAGE_ID, 0);
                int x = intent.getIntExtra(Const.Broadcast.X, 0);
                int y = intent.getIntExtra(Const.Broadcast.Y, 0);
                int fx = intent.getIntExtra(Const.Broadcast.FX, 0);
                int fy = intent.getIntExtra(Const.Broadcast.FY, 0);
                int force = intent.getIntExtra(Const.Broadcast.PRESSURE, 0);
                long timestamp = intent.getLongExtra(Const.Broadcast.TIMESTAMP, 0);
                int type = intent.getIntExtra(Const.Broadcast.TYPE, 0);
                int color = intent.getIntExtra(Const.Broadcast.COLOR, 0);

                handleDot(sectionId, ownerId, noteId, pageId, x, y, fx, fy, force, timestamp, type, color);
            } else if (Const.Broadcast.ACTION_PEN_UPDATE.equals(action)) {
                iPenCtrl.suspendPenUpgrade();
            }
        }
    };

    private void handleDot(int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int force, long timestamp, int type, int color) {
        mSampleView.addDot(sectionId, ownerId, noteId, pageId, x, y, fx, fy, force, timestamp, type, color);
    }

    private void handleMsg(int penMsgType, String content) {
        Log.d(TAG, "handleMsg : " + penMsgType);

        switch (penMsgType) {
            // Message of the attempt to connect a pen
            case PenMsgType.PEN_CONNECTION_TRY:
                Util.showToast(this, "try to connect.");
                break;
            // Pens when the connection is completed (state certification process is not yet in progress)
            case PenMsgType.PEN_CONNECTION_SUCCESS:
                Util.showToast(this, "connection is successful.");
                break;
            // Message when a connection attempt is unsuccessful pen
            case PenMsgType.PEN_CONNECTION_FAILURE:
                Util.showToast(this, "connection has failed.");
                break;
            // When you are connected and disconnected from the state pen
            case PenMsgType.PEN_DISCONNECTED:
                Util.showToast(this, "connection has been terminated.");
                break;
            // Pen transmits the state when the firmware update is processed.
            case PenMsgType.PEN_FW_UPGRADE_STATUS: {
                try {
                    JSONObject job = new JSONObject(content);
                    int total = job.getInt(Const.JsonTag.INT_TOTAL_SIZE);
                    int sent = job.getInt(Const.JsonTag.INT_SENT_SIZE);
                    this.onUpgrading(total, sent);
                    Log.d(TAG, "pen fw upgrade status => total : " + total + ", progress : " + sent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            // Pen firmware update is complete
            case PenMsgType.PEN_FW_UPGRADE_SUCCESS:
                this.onUpgradeSuccess();
                Util.showToast(this, "file transfer is complete.");
                break;
            // Pen Firmware Update Fails
            case PenMsgType.PEN_FW_UPGRADE_FAILURE:
                this.onUpgradeFailure(false);
                Util.showToast(this, "file transfer has failed.");
                break;
            // When the pen stops randomly during the firmware update
            case PenMsgType.PEN_FW_UPGRADE_SUSPEND:
                this.onUpgradeFailure(true);
                Util.showToast(this, "file transfer is suspended.");
                break;
            // Offline Data List response of the pen
            case PenMsgType.OFFLINE_DATA_NOTE_LIST:
                try {
                    JSONArray list = new JSONArray(content);
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject jobj = list.getJSONObject(i);

                        int sectionId = jobj.getInt(Const.JsonTag.INT_SECTION_ID);
                        int ownerId = jobj.getInt(Const.JsonTag.INT_OWNER_ID);
                        int noteId = jobj.getInt(Const.JsonTag.INT_NOTE_ID);

                        Log.d(TAG, "offline(" + (i + 1) + ") note => sectionId : " + sectionId + ", ownerId : " + ownerId + ", noteId : " + noteId);
                        iPenCtrl.reqOfflineData(sectionId, ownerId, noteId); //2017.3.23 add by lanlan, note below
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // if you want to get offline data of pen, use this function.
                // you can call this function, after complete download.
                // iPenCtrl.reqOfflineData( sectionId, ownerId, noteId );
                Util.showToast(this, "offline data list is received.");
                break;
            // Messages for offline data transfer begins
            case PenMsgType.OFFLINE_DATA_SEND_START:
                break;
            // Offline data transfer completion
            case PenMsgType.OFFLINE_DATA_SEND_SUCCESS:
                break;
            // Offline data transfer failure
            case PenMsgType.OFFLINE_DATA_SEND_FAILURE:
                break;
            // Progress of the data transfer process offline
            case PenMsgType.OFFLINE_DATA_SEND_STATUS: {
                try {
                    JSONObject job = new JSONObject(content);
                    int total = job.getInt(Const.JsonTag.INT_TOTAL_SIZE);
                    int received = job.getInt(Const.JsonTag.INT_RECEIVED_SIZE);
                    Log.d(TAG, "offline data send status => total : " + total + ", progress : " + received);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            // When the file transfer process of the download offline
            case PenMsgType.OFFLINE_DATA_FILE_CREATED: {
                try {
                    JSONObject job = new JSONObject(content);
                    int sectionId = job.getInt(Const.JsonTag.INT_SECTION_ID);
                    int ownerId = job.getInt(Const.JsonTag.INT_OWNER_ID);
                    int noteId = job.getInt(Const.JsonTag.INT_NOTE_ID);
                    int pageId = job.getInt(Const.JsonTag.INT_PAGE_ID);
                    String filePath = job.getString(Const.JsonTag.STRING_FILE_PATH);
                    Log.d(TAG, "offline data file created => sectionId : " + sectionId + ", ownerId : " + ownerId + ", noteId : " + noteId + ", pageId : " + pageId + " filePath : " + filePath);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            // Ask for your password in a message comes when the pen
            case PenMsgType.PASSWORD_REQUEST: {
                int retryCount = -1, resetCount = -1;
                try {
                    JSONObject job = new JSONObject(content);
                    retryCount = job.getInt(Const.JsonTag.INT_PASSWORD_RETRY_COUNT);
                    resetCount = job.getInt(Const.JsonTag.INT_PASSWORD_RESET_COUNT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                inputPassDialog = new InputPasswordDialog( this, this, retryCount, resetCount ); //暂时先不考虑输入密码的情况
//                inputPassDialog.show();
            }
            break;
        }
    }

    private void parseOfflineData() {
        // obtain saved offline data file list
        String[] files = OfflineFileParser.getOfflineFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (String file : files) {
            try {
                // create offline file parser instance
                OfflineFileParser parser = new OfflineFileParser(file);
                // parser return array of strokes
                Stroke[] strokes = parser.parse();
                if (strokes != null) {
                    mSampleView.addStrokes(strokes);
                }
                // delete data file
                parser.delete();
                parser = null;
                Log.e(TAG, "parse file finished.");
            } catch (Exception e) {
                Log.e(TAG, "parse file exeption occured.", e);
            }
        }
    }

    private void onUpgrading(int total, int progress) {
        mBuilder.setContentText("Sending").setProgress(total, progress, false);
        mNotifyManager.notify(0, mBuilder.build());
    }

    private void onUpgradeFailure(boolean isSuspend) {
        if (isSuspend) {
            mBuilder.setContentText("file transfer is suspended.").setProgress(0, 0, false);
        } else {
            mBuilder.setContentText("file transfer has failed.").setProgress(0, 0, false);
        }
        mNotifyManager.notify(0, mBuilder.build());
    }

    private void onUpgradeSuccess() {
        mBuilder.setContentText("The file transfer is complete.").setProgress(0, 0, false);
        mNotifyManager.notify(0, mBuilder.build());
    }

    /**
     * 监听收到对方发送的通道数据
     */
    private Observer<RTSTunData> receiveDataObserver = new Observer<RTSTunData>() {
        @Override
        public void onEvent(RTSTunData rtsTunData) {
            Log.e(TAG, "receive data");
            String data = "[parse bytes error]";
            try {
                data = new String(rtsTunData.getData(), 0, rtsTunData.getLength(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            DotCenter.getInstance().onReceive(sessionId, rtsTunData.getAccount(), data);
        }
    };

//    private void registerRTSObservers(String sessionName, boolean register) {
//        this.sessionName = sessionName;
//        RTSManager2.getInstance().observeChannelState(sessionName, channelStateObserver, register);
//        RTSManager2.getInstance().observeReceiveData(sessionName, receiveDataObserver, register);
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "onNewIntent");
    }

    public static void startActivity(Context context, Intent intent){
        Intent intents = new Intent(context, AVChatActivity.class);
//        if (intent.getSerializableExtra("data") != null){
//            intents.putExtra("data", intent.getSerializableExtra("data"));
//        }else {
            intents.putExtra("sessionId", intent.getStringExtra("sessionId"));
            intents.putExtra("toAccount", intent.getStringExtra("account"));
            intents.putExtra("channelId", intent.getLongExtra("channelId", 0));
//        }
        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intents);
    }

}
