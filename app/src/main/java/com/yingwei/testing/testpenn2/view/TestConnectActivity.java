package com.yingwei.testing.testpenn2.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.AVChatOptionalConfig;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.netease.nimlib.sdk.media.player.AudioPlayer;
import com.netease.nimlib.sdk.media.player.OnPlayListener;
import com.netease.nimlib.sdk.rts.RTSCallback;
import com.netease.nimlib.sdk.rts.RTSChannelStateObserver;
import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.constant.RTSEventType;
import com.netease.nimlib.sdk.rts.constant.RTSTimeOutEvent;
import com.netease.nimlib.sdk.rts.constant.RTSTunnelType;
import com.netease.nimlib.sdk.rts.model.RTSCalleeAckEvent;
import com.netease.nimlib.sdk.rts.model.RTSControlEvent;
import com.netease.nimlib.sdk.rts.model.RTSData;
import com.netease.nimlib.sdk.rts.model.RTSNotifyOption;
import com.netease.nimlib.sdk.rts.model.RTSOptions;
import com.netease.nimlib.sdk.rts.model.RTSTunData;
import com.yingwei.testing.testpenn2.Const;
import com.yingwei.testing.testpenn2.DeviceListActivity;
import com.yingwei.testing.testpenn2.R;
import com.yingwei.testing.testpenn2.SampleView;
import com.yingwei.testing.testpenn2.Util;
import com.yingwei.testing.testpenn2.doodle.ActionTypeEnum;
import com.yingwei.testing.testpenn2.doodle.DoodleView;
import com.yingwei.testing.testpenn2.doodle.SupportActionType;
import com.yingwei.testing.testpenn2.doodle.Transaction;
import com.yingwei.testing.testpenn2.doodle.action.MyPath;
import com.yingwei.testing.testpenn2.im.business.LogoutHelper;
import com.yingwei.testing.testpenn2.im.confit.AuthPreferences;
import com.yingwei.testing.testpenn2.model.BaseResponse;
import com.yingwei.testing.testpenn2.model.fetch.AddressDownLoadRequest;
import com.yingwei.testing.testpenn2.model.fetch.AddressSaveRequest;
import com.yingwei.testing.testpenn2.model.response.AddressDownLoad;
import com.yingwei.testing.testpenn2.model.response.MsgResponse;
import com.yingwei.testing.testpenn2.retrofitutil.RetrofitWrapper;
import com.yingwei.testing.testpenn2.retrofitutil.intf.IApiService;
import com.yingwei.testing.testpenn2.trans.DemoCache;
import com.yingwei.testing.testpenn2.trans.Dot;
import com.yingwei.testing.testpenn2.trans.PhoneCallStateObserver;
import com.yingwei.testing.testpenn2.trans.DotCenter;
import com.yingwei.testing.testpenn2.trans.DotManager;
import com.yingwei.testing.testpenn2.trans.util.ScreenUtil;
import com.yingwei.testing.testpenn2.trans.util.log.LogUtil;
import com.yingwei.testing.testpenn2.util.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.neolab.sdk.ink.structure.DotType;
import kr.neolab.sdk.ink.structure.Stroke;
import kr.neolab.sdk.pen.IPenCtrl;
import kr.neolab.sdk.pen.PenCtrl;
import kr.neolab.sdk.pen.offline.OfflineFileParser;
import kr.neolab.sdk.pen.penmsg.IPenMsgListener;
import kr.neolab.sdk.pen.penmsg.PenMsg;
import kr.neolab.sdk.pen.penmsg.PenMsgType;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Looper.getMainLooper;

public class TestConnectActivity extends AppCompatActivity implements IPenMsgListener, DoodleView.FlipListener {

    private static final String TAG = "TestConnect";

    /**
     * IPenCtrl:        Connect with a pen by Bluetooth
     * IPenMsgListener: Receive data from a pen
     * PenMsg:          Data structure(it consists of Type and String)
     * PenMsgType:      The type of Data received from a pen
     * Const/JsonTag:   The description of Data received from a pen
     * Renderer:        It draws a Stroke on either Bitmap or Canvas
     */
    private IPenCtrl iPenCtrl;
    private SampleView mSampleView;

//    private DotManager transactionManager; // 数据发送管理器

    private String sessionId; //建立通道后的返回值，必须记住 通道id
    private String toAccount; //通道中，对面 客户id
    private String account; //自己

    // Notification
    protected NotificationCompat.Builder mBuilder;
    protected NotificationManager mNotifyManager;

    //    public InputPasswordDialog inputPassDialog;
    private AVChatData mChatData; //来音频通话请求，携带的数据

    private DoodleView doodleView;
    private Bitmap mDoodleViewBmp; //surfaceview保存的bitmap

    private String filePath;
    private String fileName;
//    private long pausedPosition;

    private int pageId;
    private boolean flip; //翻页

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_connect);

        account = AuthPreferences.getUserAccount();

        // create an instance of a pen controller
        iPenCtrl = PenCtrl.getInstance();

        // start a pen controller
        iPenCtrl.startup();

        // add event listener(IPenMsgListener) to a pen controller
        iPenCtrl.setListener(this);


        //after pair, should connect
//        String mac_address = "9c7bd20001d4";
//        penCtrl.connect(mac_address);

//        mSampleView = new SampleView(this);
        mSampleView = (SampleView) findViewById(R.id.sample_view_dot);
//        transactionManager = new DotManager(sessionId, account, getApplicationContext());

//        setContentView(mSampleView);

        doodleView = (DoodleView) findViewById(R.id.doodle_view_transaction);
        doodleView.setEnableView(true);
        doodleView.setZOrderOnTop(true);
        doodleView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(Const.Broadcast.ACTION_PEN_UPDATE), PendingIntent.FLAG_UPDATE_CURRENT); //"firmware_update"

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setContentTitle("Update Pen");
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_n);
        mBuilder.setContentIntent(pendingIntent);

        registerObservers(true);
        registerAVChatIncomingCallObserver(true); //监听来电
        registerRTSIncomingCallObserver(true);
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, true); //监听对方结束会话（主叫方、被叫方）
        registerAVChatStateObserver(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (doodleView != null) {
            doodleView.end();
        }
        registerObservers(false);
        registerAVChatIncomingCallObserver(false);
        registerRTSIncomingCallObserver(false);
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, false);
        registerAVChatStateObserver(false);
        registerReceiverData(false);

        registerObserverReceiverData(false);
    }

    private void registerAVChatIncomingCallObserver(final boolean register) {
        AVChatManager.getInstance().observeIncomingCall(new Observer<AVChatData>() {
            @Override
            public void onEvent(AVChatData chatData) {
                //监听其它端的回应
//                observeOnlineAckNotification(true);
                Toast.makeText(getApplicationContext(), "监听到来电", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onEvent 监听到来电：" + chatData.getAccount() + ", " + chatData.getChatId() + ", " + chatData.getExtra() + ", " + chatData.getChatType() + ", " + chatData.getTimeTag() + ", " + chatData.getPushSound());
                mChatData = chatData;
                dealIncomingCall();
            }
        }, register);
    }

    //处理音频来电
    private void dealIncomingCall() {
        if (PhoneCallStateObserver.getInstance().getPhoneCallState() != 0) { //忙线状态
            AVChatManager.getInstance().sendControlCommand(AVChatControlCommand.BUSY, null);
            interruptCall();
            return;
        }
        goToCallView(mChatData);
//        showDialog(2, "来电提醒", "是否接收新的来电", "确定", "拒接");
    }

    private void interruptCall() { //拒绝接听
        AVChatManager.getInstance().hangUp(new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("拒接成功");
            }

            @Override
            public void onFailed(int i) {
                showToast("拒接失败");
            }

            @Override
            public void onException(Throwable throwable) {
                showToast("拒接出现异常");
            }
        });
    }

    private void acceptCall() { //同意接听
        AVChatOptionalConfig config = new AVChatOptionalConfig();
        config.enableServerRecordAudio(true);
        AVChatManager.getInstance().accept(config, new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("对方同意接听成功");
                Log.e(TAG, "对方同意接听成功");
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("data", mChatData);
//                CallActivity.startActivityForResult(TestConnectActivity.this, bundle); //进入音频通话页面
            }

            @Override
            public void onFailed(int i) {
                showToast("对方同意接听失败");
                Log.e(TAG, "对方同意接听失败");
            }

            @Override
            public void onException(Throwable throwable) {
                showToast("对方同意接听异常");
                Log.e(TAG, "对方同意接听异常");
            }
        });
    }

    /**
     * 通话状态监听
     * 实现 AVChatStateObserver 监听通话过程中状态变化。
     * 被叫方同意来电请求后，SDK 自动进行音视频服务器连接，并返回相应信息供上层应用使用
     */
    private void registerAVChatStateObserver(boolean register) {
        AVChatManager.getInstance().observeAVChatState(avChatStateObserver, register);
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
    }

    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            if (statusCode.wontAutoLogin()) {
                // 被踢出、账号被禁用、密码错误等情况，自动登录失败，需要返回到登录界面进行重新登录操作
                LogoutHelper.logout(TestConnectActivity.this, true);
                finish();
            }
        }
    };

    // 监听对方挂断（主叫方、被叫方）
    Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent hangUpInfo) {
            // 结束通话
            showToast("对方结束通话");
        }
    };

    public void connect() {
        startActivityForResult(new Intent(getApplicationContext(), DeviceListActivity.class), 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "------------onActivityResult------------");
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                String address = null;

                if ((address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)) != null) {
                    iPenCtrl.connect(address);
                }
            }
        } else if (requestCode == 880) { //TODO 注册

        } else if (requestCode == 881) { //TODO 登录
            account = AuthPreferences.getUserAccount();
        } else if (requestCode == 11) { //录制了音视频
//            if (data != null) {
//                filePath = data.getStringExtra("filePath");
//                fileName = data.getStringExtra("fileName");
//            }
            //请求通话音频录制地址
//            saveAddress();
            requestAddress();
        }
    }

    static int taskId = 6;

    //请求通话音频录制地址
    private void requestAddress() {
        Log.e(TAG, "account = " + account +", toAccount = " + toAccount +", channelId = " + channelId);
        if (!account.equals(user0)){
            return;
        }
//        channelid = "190982202757249" taskId = "11" studentMobile = "13260398606" teacherMobile = "13260398607"
        AddressDownLoadRequest request = new AddressDownLoadRequest("13260398606", "13260398607", "11");
        Log.e(TAG, "request = " + request);
        RetrofitWrapper ins = RetrofitWrapper.getInstance();
        IApiService ser = ins.create(IApiService.class);
        Call<BaseResponse<AddressDownLoad>> str = ser.addressDownLoad(request);
        str.enqueue(new Callback<BaseResponse<AddressDownLoad>>() {
            @Override
            public void onResponse(Call<BaseResponse<AddressDownLoad>> call, Response<BaseResponse<AddressDownLoad>> response) {
                Log.e(TAG, "onResponse: " + response.body());
                String loadAddress = response.body().getData().getWhiteboardURL();
//                downloadFile(loadAddress);
            }

            @Override
            public void onFailure(Call<BaseResponse<AddressDownLoad>> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private File downloadFile;
    private String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AUDIO/taskManager";
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 112:
                    playRecord();
                    break;
            }
        }
    };
    /**
     * 子线程执行
     */
    private void downloadFile(String loadAddress) {
        final File file = new File(savePath);
        if (!file.exists()) {
            boolean b = file.mkdirs();
            Log.e(TAG, "result = " + b);
        }
        fileName = loadAddress.substring(loadAddress.indexOf("-")+1, loadAddress.lastIndexOf("-")) + ".aac";
        Log.e(TAG, "loadAddress = " + loadAddress);
        Log.e(TAG, "fileName = " + fileName);
        downloadFile = new File(file, fileName);

        RetrofitWrapper.getInstance().create(IApiService.class).downloadFileWithDynamicUrlSync(loadAddress).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    BufferedSink sink = null;
                    //下载文件到本地
                    try {
                        sink = Okio.buffer(Okio.sink(downloadFile));
                        sink.writeAll(response.body().source());
                    } catch (Exception e) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                    } finally {
                        try {
                            if (sink != null) sink.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("DownloadActivity", "下载成功");

                    handler.sendEmptyMessage(112);

                }else{
                    Log.e("DownloadActivity", "==responseCode=="+response.code() + "");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("下载失败", t.getMessage());
            }
        });
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
                onPaintActionStart(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
                break;
            case PEN_ACTION_MOVE:
                onPaintActionMove(sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color);
                break;
            case PEN_ACTION_UP:
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play_record:
//                playRecord();
                downloadFile("http://nim.nos.netease.com/NDI2MTYxNA==/bmltYV8wXzE5MDk4MjIwMjc1NzI0OV8xNDkyMDQ4NjYzNTg4XzlhZWI2ZWM4LWY3OTgtNGY0Ny05YjEyLTE1MGI3YTVhZGE1Nw==?download=0-\n" +
                        "190982202757249-mix.aac");
                break;
            case R.id.action_create: //注册
                register(880);
                break;
            case R.id.action_join: //登录
                register(881);
                break;
            case R.id.connect_account: //联系某一用户（音频）
                callAccount();
                break;
            case R.id.connect_band: //联系某一用户（白板）
                callAccountBin();
                break;
            case R.id.connect_status: //检验用户在线状态
                checkAccountStatus();
                break;
            case R.id.account_logout: //注销
                LogoutHelper.logout(TestConnectActivity.this, false);
                break;
            case R.id.action_save_bitmap: //保存图片到本地
                mDoodleViewBmp = convertViewToBitmap(doodleView.getRootView());
                Log.e(TAG, "mDoodleViewBmp = " + mDoodleViewBmp);
                break;
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

    /**
     * 播放录音
     */
    private void playRecord() {
//        startActivity(new Intent( getApplicationContext(), CallActivity.class));
//        Log.e(TAG, "fileName = " + fileName + ", filePath = " + filePath);
        // 定义一个播放进程回调类
        OnPlayListener listener = new OnPlayListener() {

            // 音频转码解码完成，会马上开始播放了
            public void onPrepared() {
                Log.e(TAG, "onPrepared: ");
            }

            // 播放结束
            public void onCompletion() {
                Log.e(TAG, "onCompletion: " );
            }

            // 播放被中断了
            public void onInterrupt() {
                Log.e(TAG, " onInterrupt: ");
            }

            // 播放过程中出错。参数为出错原因描述
            public void onError(String error) {
                Log.e(TAG, " onError: " + error);
            }

            // 播放进度报告，每隔 500ms 会回调一次，告诉当前进度。 参数为当前进度，单位为毫秒，可用于更新 UI
            public void onPlaying(long curPosition) {
                Log.e(TAG, "onPlaying: " + curPosition);
            }
        };
//        filePath = "http://nim.nos.netease.com/NDI2MTYxNA==/bmltYV8wXzE5MDk3MTAxOTM5ODUyOV8xNDkxOTYxMjgxMzU1XzFjNTY3MWNiLWU3ZWQtNDEwNC04MjRiLWVkMmZjMTM3ZmI5ZQ==?download=0-190971019398529-mix.aac";
//        fileName = "0-190971019398529-mix.aac";

        // 构造播放器对象
        String path = downloadFile.getAbsolutePath();
        Log.e(TAG, "path = " + path );
        AudioPlayer player = new AudioPlayer(getApplicationContext(), path, listener);

        // 开始播放。需要传入一个 Stream Type 参数，表示是用听筒播放还是扬声器。取值可参见
        // android.media.AudioManager#STREAM_***
        // AudioManager.STREAM_VOICE_CALL 表示使用听筒模式
        // AudioManager.STREAM_MUSIC 表示使用扬声器模式
        player.start(AudioManager.STREAM_VOICE_CALL); //streamType

        // 如果中途切换播放设备，重新调用 start，传入指定的 streamType 即可。player 会自动停止播放，然后再以新的 streamType 重新开始播放。
        // 如果需要从中断的地方继续播放，需要外面自己记住已经播放过的位置，然后在 onPrepared 回调中调用 seekTo
//        player.seekTo(pausedPosition);

        // 主动停止播放
//        player.stop();
    }

    private void callAccountBin() { //发起白板会话通道
        if (account == null) {
            showToast("请登录");
            return;
        }
        List<RTSTunnelType> types = new ArrayList<>(1);
//        types.add(RTSTunnelType.AUDIO);
        types.add(RTSTunnelType.DATA);

        String pushContent = account + "发起一个会话";
        String extra = "extra_data";
        RTSOptions options = new RTSOptions(); //.setRecordAudioTun(true).setRecordDataTun(true);
        RTSNotifyOption notifyOption = new RTSNotifyOption();

        registerTimeOut(true); //监听（发起）创建新通道或接受新通道超时通知
        sessionId = RTSManager.getInstance().start(account.equals("13260398606") ? "13260398607" : "13260398606", types, options, notifyOption, new RTSCallback<RTSData>() {
            @Override
            public void onSuccess(RTSData rtsData) {
                showToast("发起通话通道成功：" + rtsData.getAccount() + ", " + rtsData.getChannelId() + ", " + rtsData.getExtra() + ", " + rtsData.getLocalSessionId());
                Log.e(TAG, "发起通话通道成功：" + rtsData.getAccount() + ", " + rtsData.getChannelId() + ", " + rtsData.getExtra() + ", " + rtsData.getLocalSessionId());

                observerAccountReturn(true); //监听被叫方回应
                channelId = rtsData.getChannelId();
            }

            @Override
            public void onFailed(int i) {
                showToast("发起通话通道失败: ErrorCode = " + i);
                Log.e(TAG, "发起通话通道失败: ErrorCode = " + i);
            }

            @Override
            public void onException(Throwable throwable) {
                showToast("发起通话通道异常");
                Log.e(TAG, "发起通话通道异常：" + throwable.getMessage());
            }
        });

        if (sessionId == null) {
            // 发起会话失败,音频通道同时只能有一个会话开启
            showToast("发起会话失败,音频通道同时只能有一个会话开启");
        }
        Log.e(TAG, "发起通信请求：sessionId = " + sessionId);
    }

    //监听被叫方回应
    private void observerAccountReturn(boolean register) {
        RTSManager.getInstance().observeCalleeAckNotification(sessionId, calleeAckEventObserver, register);
    }

    private void goChatRoom(String sessionId, String account, long channelId) {
        Intent intent = new Intent();
        intent.putExtra("sessionId", sessionId);
        intent.putExtra("account", account);
        intent.putExtra("channelId", channelId);
        AVChatActivity.startActivity(getApplicationContext(), intent);
    }

    private void registerTimeOut(boolean register) {
        RTSManager.getInstance().observeTimeoutNotification("监听超时", timeoutObserver, register);
    }

    private Observer<RTSCalleeAckEvent> calleeAckEventObserver = new Observer<RTSCalleeAckEvent>() {
        @Override
        public void onEvent(RTSCalleeAckEvent rtsCalleeAckEvent) {
            if (rtsCalleeAckEvent.getEvent() == RTSEventType.CALLEE_ACK_AGREE) {
                // 判断SDK自动开启通道是否成功
                if (!rtsCalleeAckEvent.isTunReady()) {
                    return;
                }
                showToast("通道请求被接受");
                channelId = rtsCalleeAckEvent.getChannelId();
                Log.e(TAG, "通道请求被接受: " + rtsCalleeAckEvent.getAccount() + ", " + channelId + ", " + rtsCalleeAckEvent.getLocalSessionId());
                RTSManager.getInstance().observeControlNotification(sessionId, controlObserver, true); //双方会话建立之后，需要监听会话控制通知。
                registerReceiverData(true); //发起会话（对方接受后），或者接受了会话请求后，需要立即注册对数据通道状态的监听。

                // TODO 进入会话界面
                toAccount = rtsCalleeAckEvent.getAccount();
                mSampleView.initTransactionManager(getApplicationContext(), sessionId, toAccount);
//                goChatRoom(sessionId, rtsCalleeAckEvent.getAccount(), channelId);
                initDoodleView(toAccount);
                registerObserverReceiverData(true);
            } else if (rtsCalleeAckEvent.getEvent() == RTSEventType.CALLEE_ACK_REJECT) {
                // 被拒绝，结束会话
                showToast("通道请求被拒绝");
                Log.e(TAG, "通道请求被拒绝");
                close();
            }
        }
    };

    Observer<RTSControlEvent> controlObserver = new Observer<RTSControlEvent>() {
        @Override
        public void onEvent(RTSControlEvent rtsControlEvent) {
            // your code
            showToast("会话控制监听: " + rtsControlEvent.getEvent());
        }
    };

    private void close() {
        RTSManager.getInstance().close(sessionId, new RTSCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("结束会话成功");
                toAccount = null;
            }

            @Override
            public void onFailed(int i) {
                showToast("结束会话失败");
            }

            @Override
            public void onException(Throwable throwable) {
                showToast("结束会话异常");
            }
        });
    }

    long channelId;

    //监听会话请求/数据通道请求（被叫方）
    private void registerRTSIncomingCallObserver(boolean register) {
        RTSManager.getInstance().observeIncomingSession(new Observer<RTSData>() {
            @Override
            public void onEvent(RTSData rtsData) {
                // 启动会话界面
                channelId = rtsData.getChannelId();
                showToast("收到数据通道请求，选择拒绝或接受 " + rtsData.getAccount() + ", " + channelId + ", " + rtsData.getExtra() + ", " + rtsData.getLocalSessionId());
                Log.e(TAG, "收到数据通道请求,选择拒绝或接受:" + rtsData.getAccount() + ", " + channelId + ", " + rtsData.getExtra() + ", " + rtsData.getLocalSessionId());
                //查看当前手机状态，如果忙，则拒接，如果闲，则弹出选择是否接收页面
                int state = PhoneCallStateObserver.getInstance().getPhoneCallState();
                if (0 == state) {
                    sessionId = rtsData.getLocalSessionId();
                    toAccount = rtsData.getAccount();
                    showToast("已接受通信请求");
                    Log.e(TAG, "已接受通信请求");
                    acceptTunData(toAccount, channelId);
                } else {
                    showToast("已拒绝通信请求");
                    Log.e(TAG, "已拒绝通信请求");
                    denyTunData();
                }
            }
        }, register);
    }

    //拒绝白板通道请求（被叫方）
    private void denyTunData() {
        RTSManager.getInstance().close(sessionId, new RTSCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showToast("已拒绝请求:");
            }

            @Override
            public void onFailed(int i) {
                showToast("拒绝请求失败：ErrorCode = " + i);
                Log.e(TAG, "拒绝请求失败：ErrorCode = " + i);
            }

            @Override
            public void onException(Throwable throwable) {
                showToast("拒绝请求异常" + throwable.getMessage());
            }
        });
    }

    //接受白板通道请求(被叫方)
    private void acceptTunData(final String account, final long channelId) {
        RTSOptions options = new RTSOptions(); //.setRecordAudioTun(true).setRecordDataTun(true);
        RTSManager.getInstance().accept(sessionId, options, new RTSCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                showToast("已接受请求: " + aBoolean.booleanValue());
                toAccount = account;
                mSampleView.initTransactionManager(getApplicationContext(), sessionId, toAccount);
                initDoodleView(toAccount);
//                goChatRoom(sessionId, account, channelId);
                registerReceiverData(true); //发起会话（对方接受后），或者接受了会话请求后，需要立即注册对数据通道状态的监听。
                registerObserverReceiverData(true);
            }

            @Override
            public void onFailed(int i) {
                showToast("接受请求失败：ErrorCode = " + i);
            }

            @Override
            public void onException(Throwable throwable) {
                showToast("接受请求异常" + throwable.getMessage());
            }
        });
    }

    /**
     * 监听数据通道的状态
     */
    private void registerReceiverData(boolean register) {
        RTSManager.getInstance().observeChannelState(sessionId, channelStateObserver, register);
    }

    RTSChannelStateObserver channelStateObserver = new RTSChannelStateObserver() {

        @Override
        public void onConnectResult(String localSessionId, RTSTunnelType tunType, long channelId, int code, String recordFile) {
            // 与服务器连接结果通知，成功返回 200, 同时返回服务器录制文件的地址
            showToast("与服务器连接结果通知: " + code);
            Log.e(TAG, "localSessionId = " + localSessionId + ", channelId = " + channelId + ", code = " + code + ", recordFile = " + recordFile);
        }

        @Override
        public void onChannelEstablished(String localSessionId, RTSTunnelType tunType) {
            // 双方通道连接建立(对方用户已加入)
            showToast("双方通道连接建立(对方用户已加入)");
        }

        @Override
        public void onUserJoin(String localSessionId, RTSTunnelType tunType, String account) {
            // 用户加入
            showToast("用户加入");
        }

        @Override
        public void onUserLeave(String localSessionId, RTSTunnelType tunType, String account, int event) {
            // 用户离开
            showToast("用户离开");
            close();
        }

        @Override
        public void onDisconnectServer(String localSessionId, RTSTunnelType tunType) {
            // 与服务器断开连接
            showToast("与服务器断开连接");
        }

        @Override
        public void onError(String localSessionId, RTSTunnelType tunType, int error) {
            // 通道发生错误
            showToast("通道发生错误");
        }

        @Override
        public void onNetworkStatusChange(String localSessionId, RTSTunnelType channelType, int value) {
            // 网络信号强弱
            showToast("网络信号强弱");
        }
    };

    Observer<RTSTimeOutEvent> timeoutObserver = new Observer<RTSTimeOutEvent>() {
        @Override
        public void onEvent(RTSTimeOutEvent rtsTimeOutEvent) {
            // 超时，结束会话
            showToast("超时，结束会话");
            close();
        }
    };

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    String user0 = "13260398606";
    String user1 = "13260398607";

    private void callAccount() { //请求语音通话
        if (account == null) {
            showToast("请登录");
            return;
        }
        //TODO 返回缺失的权限
//        List<String> permissions = AVChatManager.checkPermission(getApplicationContext());
//        if (permissions.size() != 0){
//            //进行权限请求
//            return;
//        }
//        RTSOptions options = new RTSOptions();
//        options.setRecordAudioTun(true).setRecordDataTun(true);

        toAccount = account.equals(user0) ? user1 : user0;
        AVChatOptionalConfig configs = new AVChatOptionalConfig();
        configs.enableServerRecordAudio(true);  //是否打开服务器录制音频,服务器录制需要开通相关业务。
        AVChatNotifyOption notifyOption = new AVChatNotifyOption();
        AVChatManager.getInstance().call(toAccount,
                AVChatType.AUDIO, configs, notifyOption, new AVChatCallback<AVChatData>() {
                    @Override
                    public void onSuccess(AVChatData avChatData) {
                        showToast("发起呼叫成功");
                        Log.e(TAG, "onSuccess: " + avChatData.getAccount() + ", " + avChatData.getChatId() + ", " + avChatData.getTimeTag() + ", " +
                                avChatData.getPushSound() + ", " + avChatData.getExtra() + ", " + avChatData.getChatType());
                        goToCallView(avChatData);
                    }

                    @Override
                    public void onFailed(int i) { //403:应用被封禁
                        showToast("发起呼叫失败");
                        Log.e(TAG, "onFailed: ERROR CODE = " + i);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        showToast("发起呼叫异常");
                        Log.e(TAG, "onException: " + throwable.getMessage());
                    }
                });
    }

    private void goToCallView(AVChatData avChatData) {
        channelId = avChatData.getChatId();
        Intent intents = new Intent(getApplicationContext(), CallActivity.class);
        intents.putExtra("data", avChatData);
        startActivityForResult(intents, 11);
    }

    private void register(int type) {
        startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), type);
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
        if (doodleView != null) {
            doodleView.onResume();
        }
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

    private void registerObserverReceiverData(boolean register) {
        Log.e(TAG, "sessionId = " + sessionId);
        RTSManager.getInstance().observeReceiveData(sessionId, receiveDataObserver, register);
    }

    /**
     * 监听收到对方发送的通道数据
     */
    private Observer<RTSTunData> receiveDataObserver = new Observer<RTSTunData>() {
        @Override
        public void onEvent(RTSTunData rtsTunData) {
            LogUtil.i(TAG, "receive data");
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

    private void checkAccountStatus() {
        StatusCode status = NIMClient.getStatus();
        String msg = "";
        switch (status) {
            case INVALID:
                msg = "未定义";
                break;
            case UNLOGIN:
                msg = "未登录/登录失败";
                break;
            case NET_BROKEN:
                msg = "网络连接已断开";
                break;
            case CONNECTING:
                msg = "正在连接服务器";
                break;
            case LOGINING:
                msg = "正在登录中";
                break;
            case SYNCING:
                msg = "正在同步数据";
                break;
            case LOGINED:
                msg = "已成功登录";
                break;
            case KICKOUT:
                msg = "被其他端的登录踢掉";
                break;
            case KICK_BY_OTHER_CLIENT:
                msg = "被同时在线的其他端主动踢掉";
                break;
            case FORBIDDEN:
                msg = "被服务器禁止登录";
                break;
            case VER_ERROR:
                msg = "客户端版本错误";
                break;
            case PWD_ERROR:
                msg = "用户名或密码错误";
                break;
        }
        Log.e(TAG, "用户状态：" + msg);
        Toast.makeText(getApplicationContext(), msg+": " +AuthPreferences.getUserAccount(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (toAccount != null) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                showDialog(1, "提示", "是否结束", "是", "否");
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showDialog(final int type, String title, String message, String positiveMsg, String negativeMsg) {
        // 构造对话框
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TestConnectActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        // 更新
        builder.setPositiveButton(positiveMsg, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                action(type);
            }
        });
        builder.setNegativeButton(negativeMsg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                actionCancel(type);
            }
        });
        builder.setCancelable(true);

        Dialog noticeDialog = builder.create();
        if (this.isFinishing()) {
            return;
        }
        noticeDialog.show();

    }

    private void actionCancel(int type) {
        switch (type) {
            case 2:
                interruptCall();
                break;
        }
    }

    private void action(int type) {
        switch (type) {
            case 1: //关闭白板通话
                close();
                break;
            case 2: //开启音频通话
                acceptCall();
                break;
        }
    }

    /**
     * ***************************** 画板 ***********************************
     */
    private void initDoodleView(String account) {
        Toast.makeText(getApplicationContext(), "init doodle success", Toast.LENGTH_SHORT).show();
        // add support ActionType
        SupportActionType.getInstance().addSupportActionType(ActionTypeEnum.Path.getValue(), MyPath.class);

        doodleView.init(sessionId, account, DoodleView.Mode.BOTH, Color.TRANSPARENT, Color.BLACK, getApplicationContext(), this);

        doodleView.setPaintSize(3);
        doodleView.setPaintType(ActionTypeEnum.Path.getValue());

        // adjust paint offset
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Rect frame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                int statusBarHeight = frame.top;
                Log.i("Doodle", "statusBarHeight = " + statusBarHeight);

                int marginTop = doodleView.getTop();
                Log.i("Doodle", "doodleView marginTop = " + marginTop);

                int marginLeft = doodleView.getLeft();
                Log.i("Doodle", "doodleView marginLeft = " + marginLeft);

                //TODO toolBar的高度
                int toolBarHeight = 0;
//                if (getSupportActionBar() != null) {
                toolBarHeight = getSupportActionBar().getHeight();
//                }
                Log.e("Doodle", "toolBarHeight = " + toolBarHeight);
                float offsetX = marginLeft;
                float offsetY = statusBarHeight + marginTop + toolBarHeight; // + ScreenUtil.dip2px(220) + ScreenUtil.dip2px(40);

                doodleView.setPaintOffset(offsetX, offsetY);
                Log.i("Doodle", "client1 offsetX = " + offsetX + ", offsetY = " + offsetY);
            }
        }, 50);
    }

    public static Bitmap convertViewToBitmap(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        return view.getDrawingCache(true);
    }

    AVChatStateObserver avChatStateObserver = new AVChatStateObserver() {
        @Override
        public void onTakeSnapshotResult(String account, boolean success, String file) {
            //用户执行截图后会回调

        }

        @Override
        public void onConnectionTypeChanged(int i) {

        }

        @Override
        public void onAVRecordingCompletion(String s, String s1) {

        }

        @Override
        public void onAudioRecordingCompletion(String s) {

        }

        @Override
        public void onLowStorageSpaceWarning(long l) {

        }

        @Override
        public void onFirstVideoFrameAvailable(String s) {

        }

        @Override
        public void onVideoFpsReported(String s, int i) {

        }

        @Override
        public void onJoinedChannel(int i, String s, String s1) {

        }

        @Override
        public void onLeaveChannel() {

        }

        @Override
        public void onUserJoined(String s) {

        }

        @Override
        public void onUserLeave(String s, int i) {

        }

        @Override
        public void onProtocolIncompatible(int i) {

        }

        @Override
        public void onDisconnectServer() {

        }

        @Override
        public void onNetworkQuality(String s, int i) {

        }

        @Override
        public void onCallEstablished() {

        }

        @Override
        public void onDeviceEvent(int i, String s) {


        }

        @Override
        public void onFirstVideoFrameRendered(String s) {

        }

        @Override
        public void onVideoFrameResolutionChanged(String s, int i, int i1, int i2) {

        }

        @Override
        public boolean onVideoFrameFilter(AVChatVideoFrame avChatVideoFrame) {
            return false;
        }

        @Override
        public boolean onAudioFrameFilter(AVChatAudioFrame avChatAudioFrame) {
            return false;
        }

        @Override
        public void onAudioDeviceChanged(int i) {

        }

        @Override
        public void onReportSpeaker(Map<String, Integer> map, int i) {

        }

        @Override
        public void onStartLiveResult(int i) {

        }

        @Override
        public void onStopLiveResult(int i) {

        }

        @Override
        public void onAudioMixingEvent(int i) {

        }
    };

    /**
     * 翻页 FlipListener
     *
     * @param transaction
     */
    @Override
    public void onFlipPage(Transaction transaction) {
//        pageFlip(transaction);
    }
}
