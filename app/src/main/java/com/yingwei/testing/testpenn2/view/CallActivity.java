package com.yingwei.testing.testpenn2.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatTimeOutEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatOnlineAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatOptionalConfig;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.yingwei.testing.testpenn2.R;

import java.util.Map;

public class CallActivity extends AppCompatActivity {

    private static final String TAG = "CallActivity";

    private boolean active; //主动发起通话

    private AVChatData mChatData; //来音频通话请求，携带的数据

    boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        parseIntent();

        registerCallingObserver(true);
        registerTheOtherSideClosedObserver(true);
        registerTheOtherSideReflect(true);
        registerTimeoutObserver(true);
        registerAVChatStateObserver(true);
    }

    private void parseIntent() {
        mChatData = (AVChatData) getIntent().getSerializableExtra("data");
    }

    /**
     * 监听呼叫或接听超时通知
     * 主叫方在拨打网络通话时，超过 45 秒被叫方还未接听来电，则自动挂断。
     * 被叫方超过 45 秒未接听来听，也会自动挂断，
     * 在通话过程中网络超时 30 秒自动挂断。
     */
    private void registerTimeoutObserver(boolean register) {
        AVChatManager.getInstance().observeTimeoutNotification(timeoutObserver, register);
    }


    public void deny(View view){ //拒接
        if (connected){ //挂断
            close();
        } else { //拒接
            AVChatManager.getInstance().hangUp(avChatCallbackDeny);
        }
    }

    public void receive(View view){ //接听
        AVChatOptionalConfig config = new AVChatOptionalConfig();
        config.enableServerRecordAudio(true); //开启服务器音频录制
        AVChatManager.getInstance().accept(config, avChatCallback);
    }

    private void registerAVChatStateObserver(boolean register) {
        AVChatManager.getInstance().observeAVChatState(avChatStateObserver, register);
    }

    /**
     * 监听被叫方回应（主叫方）
     */
    private void registerTheOtherSideReflect(boolean register) {
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, register);
    }

    /**
     * 监听对方挂断（主叫方、被叫方）
     * 当被叫方收到来电时（在通话建立之前）需要监听主叫方挂断通知，当双方通话建立之后，都需要监听对方挂断通知来结束本次通话
     */
    private void registerTheOtherSideClosedObserver(boolean register) {
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, register);
    }

    /**
     * 监听该帐号其他端回应（被叫方）
     */
    private void registerCallingObserver(boolean register) {
        AVChatManager.getInstance().observeOnlineAckNotification(onlineAckObserver, register);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerCallingObserver(false);
        registerTheOtherSideClosedObserver(false);
        registerTheOtherSideReflect(false);
        registerTimeoutObserver(false);
        registerAVChatStateObserver(false);
    }

    Observer<AVChatTimeOutEvent> timeoutObserver = new Observer<AVChatTimeOutEvent>() {
        @Override
        public void onEvent(AVChatTimeOutEvent event) {
            // 超时类型
            String timeout = "";
            switch (event){
                case INCOMING_TIMEOUT:
                    timeout = "来电超时";
                    break;
                case OUTGOING_TIMEOUT:
                    timeout = "拨打超时";
                    break;
                case NET_BROKEN_TIMEOUT:
                    timeout = "网络超时";
                    break;
            }
            showToast(timeout);
            Log.e(TAG, timeout);
            finish();
        }
    };


    Observer<AVChatCalleeAckEvent> callAckObserver = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent ackInfo) {
            if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {
                // 对方正在忙
                showToast("对方正在忙");
                Log.e(TAG, "对方正在忙");
            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                // 对方拒绝接听
                showToast("对方拒绝接听");
                Log.e(TAG, "对方拒绝接听");
                finish();
            } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                // 对方同意接听
                connected = true;
                showToast("对方同意接听");
                Log.e(TAG, "对方同意接听");
                findViewById(R.id.iv_receive).setVisibility(View.INVISIBLE);
            }
        }
    };

    //拒绝接听（被叫方）
    AVChatCallback avChatCallbackDeny = new AVChatCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            showToast("我拒绝接听（被叫方）");
            Log.e(TAG, "我拒绝接听（被叫方）");
            finish();
        }

        @Override
        public void onFailed(int i) {
            Log.e(TAG, "拒接失败");
        }

        @Override
        public void onException(Throwable throwable) {
            Log.e(TAG, "拒接异常");
        }
    };

    //同意接听（被叫方）
    AVChatCallback avChatCallback = new AVChatCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            showToast("同意接听（被叫方）");
            findViewById(R.id.iv_receive).setVisibility(View.INVISIBLE);
        }

        @Override
        public void onFailed(int i) {
            Log.e(TAG, "同意接听失败");
        }

        @Override
        public void onException(Throwable throwable) {
            Log.e(TAG, "同意接听异常");
        }
    };

    Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent hangUpInfo) {
            // 结束通话
            showToast("对方结束通话");
            finish();
//            close();
        }
    };

    /**
     * 结束通话
     */
    private void close() {
        AVChatManager.getInstance().hangUp(new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                connected = false;
                showToast("发起结束通话成功");
                Log.e(TAG, "发起结束通话成功");
            }

            @Override
            public void onFailed(int i) {
                showToast("发起结束通话失败");
                Log.e(TAG, "发起结束通话失败");
            }

            @Override
            public void onException(Throwable throwable) {
                showToast("发起结束通话异常");
                Log.e(TAG, "发起结束通话异常");
            }
        });
    }

    Observer<AVChatOnlineAckEvent> onlineAckObserver = new Observer<AVChatOnlineAckEvent>() {
        @Override
        public void onEvent(AVChatOnlineAckEvent ackInfo) {
            if (ackInfo.getClientType() != ClientType.Android) {
                String client = null; // 做回应的客户端
                switch (ackInfo.getClientType()) {
                    case ClientType.Windows:
                        client = "Windows";
                        break;
                    case ClientType.Web:
                        client = "Web";
                    default:
                        break;
                }
                // your code
                if (client != null) {
                    showToast("已经在其它端处理");
                    finish();
                }
            }
        }
    };

    AVChatStateObserver avChatStateObserver = new AVChatStateObserver() {
        /**
         * 用户执行截图后会回调
         * @param account
         * @param success
         * @param file
         */
        @Override
        public void onTakeSnapshotResult(String account, boolean success, String file) {

        }

        /**
         * 本地客户端网络类型发生改变时回调，会通知当前网络类型
         * @param netType
         */
        @Override
        public void onConnectionTypeChanged(int netType) {

        }

        /**
         * 当用户录制音视频结束时回调，会通知录制的用户id和录制文件路径
         * @param account
         * @param filePath
         */
        @Override
        public void onAVRecordingCompletion(String account, String filePath) {

        }

        /**
         * 当用户录制语音结束时回调，会通知录制文件路径
         * @param filePath
         */
        @Override
        public void onAudioRecordingCompletion(String filePath) {

        }

        /**
         * 当存储空间不足时的警告回调,存储空间低于20M时开始出现警告，
         * 出现警告时请及时关闭所有的录制服务，
         * 当存储空间低于10M时会自动关闭所有的录制
         * @param availableSize
         */
        @Override
        public void onLowStorageSpaceWarning(long availableSize) {

        }

        /**
         * 当用户第一帧视频画面绘制前通知
         * @param account
         */
        @Override
        public void onFirstVideoFrameAvailable(String account) {

        }

        /**
         * 实时汇报用户的视频绘制帧率。
         * @param account
         * @param fps
         */
        @Override
        public void onVideoFpsReported(String account, int fps) {

        }

        @Override
        public void onJoinedChannel(int code, String filePath, String fileName) {
            //1.当前音视频服务器连接回调
            // 首先返回服务器连接是否成功的回调,并通过返回的 result code 做相应的处理
            // 参数 code 返回加入频道是否成功。
            // 常见错误码参考 JoinChannelCode 参数 filePath fileName 在开启服务器录制的情况下返回录制文件的保存路径。

        }

        @Override
        public void onLeaveChannel() {
            //自己成功离开频道回调
        }

        @Override
        public void onUserJoined(String account) {
            // 2.加入当前音视频频道用户帐号回调c
            // 其他用户音视频服务器连接成功后，会回调 ，可以获取当前通话的用户帐号

        }

        /**
         * @param event －1,用户超时离开  0,正常退出
         */
        @Override
        public void onUserLeave(String account, int event) {
            // 3.当前用户离开频道回
            // 调通话过程中，若有用户离开，则会回调
        }

        /**
         * @param status 0 自己版本过低  1 对方版本过低
         */
        @Override
        public void onProtocolIncompatible(int status) {
            // 版本协议不兼容回调
            // 若语音视频通话双方软件版本不兼容，则会回调

        }

        @Override
        public void onDisconnectServer() {
            //服务器断开回调
            // 通话过程中，服务器断开，会回调

        }

        /**
         * @param value 0~3 ,the less the better; 0 : best; 3 : worst
         */
        @Override
        public void onNetworkQuality(String account, int value) {
            // 当前通话网络状况回调
            // 通话过程中网络状态发生变化,会回调
        }

        @Override
        public void onCallEstablished() {
            //音视频连接成功建立回调
            //音视频连接建立，会回调 onCallEstablished。
            // 音频切换到正在通话的界面，并开始计时等处理。视频则通过为用户设置对应画布并添加到相应的 layout 上显示图像
//            if (state == AVChatTypeEnum.AUDIO.getValue()) {
//                aVChatUIManager.onCallStateChange(CallStateEnum.AUDIO);
//            } else {
//                aVChatUIManager.initSmallSurfaceView();
//                aVChatUIManager.onCallStateChange(CallStateEnum.VIDEO);
//            }
//            isCallEstablished = true;
        }

        @Override
        public void onDeviceEvent(int i, String s) {

        }

        /**
         * 当用户第一帧视频画面绘制后通知
         * @param user
         */
        @Override
        public void onFirstVideoFrameRendered(String user) {

        }

        /**
         * 当用户视频画面的分辨率改变时通知。
         * @param user
         * @param width
         * @param height
         * @param rotate
         */
        @Override
        public void onVideoFrameResolutionChanged(String user, int width, int height, int rotate) {

        }

        /**
         * 采集视频数据回调
         * 当用户开始外部视频处理后,采集到的视频数据通过次回调通知。
         * 用户可以对视频数据做相应的美颜等不同的处理。 需要通过setParameters开启视频数据处理
         * @param avChatVideoFrame
         * @return
         */
        @Override
        public boolean onVideoFrameFilter(AVChatVideoFrame avChatVideoFrame) {
            return false;
        }

        /**
         * 当用户开始外部语音处理后,采集到的语音数据通过次回调通知。
         * 用户可以对语音数据做相应的变声等不同的处理。需要通过setParameters开启语音数据处理
         * @param avChatAudioFrame
         * @return
         */
        @Override
        public boolean onAudioFrameFilter(AVChatAudioFrame avChatAudioFrame) {
            return false;
        }

        /**
         * 当用户切换扬声器或者耳机的插拔等操作时, 语音的播放设备都会发生变化通知。 语音设备参考 AVChatAudioDevice
         * @param device
         */
        @Override
        public void onAudioDeviceChanged(int device) {

        }

        /**
         * 正在说话用户的语音强度回调，包括自己和其他用户的声音强度。
         * 如果一个用户没有说话,或者说话声音小没有被参加到混音,那么这个用户的信息不会在回调中出现
         * @param speakers
         * @param mixedEnergy
         */
        @Override
        public void onReportSpeaker(Map<String, Integer> speakers, int mixedEnergy) {

        }

        @Override
        public void onStartLiveResult(int i) {

        }

        @Override
        public void onStopLiveResult(int i) {

        }

        /**
         * 当伴音出错或者结束时，通过此回调进行通知
         * @param event
         */
        @Override
        public void onAudioMixingEvent(int event) {

        }
    };

    private void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (connected){
            close();
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void startActivity(Context context, Bundle bundle){
        Intent intents = new Intent(context, CallActivity.class);
        intents.putExtra("data", bundle.getSerializable("data"));
        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intents);
    }
}
