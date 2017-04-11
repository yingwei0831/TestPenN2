package com.yingwei.testing.testpenn2.view;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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
import com.netease.nimlib.sdk.media.player.AudioPlayer;
import com.netease.nimlib.sdk.media.player.OnPlayListener;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.yingwei.testing.testpenn2.R;
import com.yingwei.testing.testpenn2.retrofitutil.RetrofitWrapper;
import com.yingwei.testing.testpenn2.retrofitutil.intf.IApiService;

import java.io.File;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallActivity extends AppCompatActivity {

    private static final String TAG = "CallActivity";

    private boolean active; //主动发起通话

    private AVChatData mChatData; //来音频通话请求，携带的数据

    boolean connected;

    String filepath;
    String filename;

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


    public void deny(View view) { //拒接
        if (connected) { //挂断
            close();
        } else { //拒接
            AVChatManager.getInstance().hangUp(avChatCallbackDeny);
        }
    }

    public void receive(View view) { //接听
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
            switch (event) {
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
            Intent data = new Intent();
            data.putExtra("filePath", filepath);
            data.putExtra("fileName", filename);
            setResult(RESULT_OK, data);
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
                Intent data = new Intent();
                data.putExtra("filePath", filepath);
                data.putExtra("fileName", filename);
                setResult(RESULT_OK, data);
                finish();
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
            Log.e(TAG, "onTakeSnapshotResult");
        }

        /**
         * 本地客户端网络类型发生改变时回调，会通知当前网络类型
         * @param netType
         */
        @Override
        public void onConnectionTypeChanged(int netType) {
            Log.e(TAG, "onConnectionTypeChanged");
        }

        /**
         * 当用户录制音视频结束时回调，会通知录制的用户id和录制文件路径
         * @param account
         * @param filePath
         */
        @Override
        public void onAVRecordingCompletion(String account, String filePath) {
            Log.e(TAG, "录制音视频结束：account = " + account + ", filePath = " + filePath);
        }

        /**
         * 当用户录制语音结束时回调，会通知录制文件路径
         * @param filePath
         */
        @Override
        public void onAudioRecordingCompletion(String filePath) {
            Log.e(TAG, "录制语音结束：filePath = " + filePath);
        }

        /**
         * 当存储空间不足时的警告回调,存储空间低于20M时开始出现警告，
         * 出现警告时请及时关闭所有的录制服务，
         * 当存储空间低于10M时会自动关闭所有的录制
         * @param availableSize
         */
        @Override
        public void onLowStorageSpaceWarning(long availableSize) {
            Log.e(TAG, "onLowStorageSpaceWarning");
        }

        /**
         * 当用户第一帧视频画面绘制前通知
         * @param account
         */
        @Override
        public void onFirstVideoFrameAvailable(String account) {
            Log.e(TAG, "onFirstVideoFrameAvailable");
        }

        /**
         * 实时汇报用户的视频绘制帧率。
         * @param account
         * @param fps
         */
        @Override
        public void onVideoFpsReported(String account, int fps) {
            Log.e(TAG, "onVideoFpsReported");
        }

        @Override
        public void onJoinedChannel(int code, String filePath, String fileName) {
            //1.当前音视频服务器连接回调
            // 首先返回服务器连接是否成功的回调,并通过返回的 result code 做相应的处理
            // 参数 code 返回加入频道是否成功。
            // 常见错误码参考 JoinChannelCode 参数 filePath fileName 在开启服务器录制的情况下返回录制文件的保存路径。
            Log.e(TAG, "音视频服务器连接回调：code = " + code + ", filePath = " + filePath + ", fileName = " + fileName);
            //code = 200, filePath = 335393470-190949284218369.mp4, fileName = 335393470-190949284218369.aac
            filepath = filePath;
            filename = fileName;
        }

        @Override
        public void onLeaveChannel() {
            //自己成功离开频道回调
            Log.e(TAG, "onLeaveChannel");
        }

        @Override
        public void onUserJoined(String account) {
            // 2.加入当前音视频频道用户帐号回调c
            // 其他用户音视频服务器连接成功后，会回调 ，可以获取当前通话的用户帐号
            Log.e(TAG, "onUserJoined");
        }

        /**
         * @param event －1,用户超时离开  0,正常退出
         */
        @Override
        public void onUserLeave(String account, int event) {
            // 3.当前用户离开频道回
            // 调通话过程中，若有用户离开，则会回调
            Log.e(TAG, "onUserLeave");
        }

        /**
         * @param status 0 自己版本过低  1 对方版本过低
         */
        @Override
        public void onProtocolIncompatible(int status) {
            // 版本协议不兼容回调
            // 若语音视频通话双方软件版本不兼容，则会回调
            Log.e(TAG, "onProtocolIncompatible");
        }

        @Override
        public void onDisconnectServer() {
            //服务器断开回调
            // 通话过程中，服务器断开，会回调
            Log.e(TAG, "onDisconnectServer");
        }

        /**
         * @param value 0~3 ,the less the better; 0 : best; 3 : worst
         */
        @Override
        public void onNetworkQuality(String account, int value) {
            // 当前通话网络状况回调
            // 通话过程中网络状态发生变化,会回调
            Log.e(TAG, "onNetworkQuality");
        }

        @Override
        public void onCallEstablished() {
            Log.e(TAG, "onCallEstablished");
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
            Log.e(TAG, "onDeviceEvent");
        }

        /**
         * 当用户第一帧视频画面绘制后通知
         * @param user
         */
        @Override
        public void onFirstVideoFrameRendered(String user) {
            Log.e(TAG, "onFirstVideoFrameRendered");
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
            Log.e(TAG, "onVideoFrameResolutionChanged");
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
            Log.e(TAG, "onVideoFrameFilter");
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
            Log.e(TAG, "onAudioFrameFilter");
            return false;
        }

        /**
         * 当用户切换扬声器或者耳机的插拔等操作时, 语音的播放设备都会发生变化通知。 语音设备参考 AVChatAudioDevice
         * @param device
         */
        @Override
        public void onAudioDeviceChanged(int device) {
            Log.e(TAG, "onAudioDeviceChanged");
        }

        /**
         * 正在说话用户的语音强度回调，包括自己和其他用户的声音强度。
         * 如果一个用户没有说话,或者说话声音小没有被参加到混音,那么这个用户的信息不会在回调中出现
         * @param speakers
         * @param mixedEnergy
         */
        @Override
        public void onReportSpeaker(Map<String, Integer> speakers, int mixedEnergy) {
            Log.e(TAG, "onReportSpeaker");
        }

        @Override
        public void onStartLiveResult(int i) {
            Log.e(TAG, "onStartLiveResult");
        }

        @Override
        public void onStopLiveResult(int i) {
            Log.e(TAG, "onStopLiveResult");
        }

        /**
         * 当伴音出错或者结束时，通过此回调进行通知
         * @param event
         */
        @Override
        public void onAudioMixingEvent(int event) {
            Log.e(TAG, "onAudioMixingEvent");
        }
    };

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (connected) {
            close();
        }
        return super.onKeyDown(keyCode, event);
    }

//    public static void startActivityForResult(AppCompatActivity context, Bundle bundle){
//        Intent intents = new Intent(context, CallActivity.class);
//        intents.putExtra("data", bundle.getSerializable("data"));
////        intents.setFlags(Intent.flagactivity);
//        context.startActivityForResult(intents, 11);
//    }

    boolean recording;
    AudioRecorder recorder;
    File audioFileRecord;

    public void record(View view) {
        if (recorder == null) {
            // 初始化recorder
            recorder = new AudioRecorder(
                    getApplicationContext(),
                    RecordType.AAC, // 录制音频类型（aac/amr)
                    120, // 最长录音时长，到该长度后，会自动停止录音, 默认120s
                    callback // 录音过程回调
            );
        }
        if (recording) {
            // 结束录音, 正常结束true，或者取消录音false
//            recorder.completeRecord(true);
            recorder.handleEndRecord(true, recorder.getCurrentRecordMaxAmplitude());
            recording = false;
        } else {
            // 开始录音
            recorder.startRecord();
            if (!recorder.isRecording()) {
                // 开启录音失败。
                Log.e(TAG, "开启录音失败");
            }
            recording = true;
        }
    }

    public void play(View view) {
        // 定义一个播放进程回调类
        OnPlayListener listener = new OnPlayListener() {

            // 音频转码解码完成，会马上开始播放了
            public void onPrepared() {}

            // 播放结束
            public void onCompletion() {}

            // 播放被中断了
            public void onInterrupt() {}

            // 播放过程中出错。参数为出错原因描述
            public void onError(String error){}

            // 播放进度报告，每隔 500ms 会回调一次，告诉当前进度。 参数为当前进度，单位为毫秒，可用于更新 UI
            public void onPlaying(long curPosition) {}
        };

//        // 构造播放器对象
        AudioPlayer player = new AudioPlayer(getApplicationContext(), audioFileRecord.getAbsolutePath(), listener);

//        // 开始播放。需要传入一个 Stream Type 参数，表示是用听筒播放还是扬声器。取值可参见
//        // android.media.AudioManager#STREAM_***
//        // AudioManager.STREAM_VOICE_CALL 表示使用听筒模式
//        // AudioManager.STREAM_MUSIC 表示使用扬声器模式
        player.start(AudioManager.STREAM_VOICE_CALL);

//        // 如果中途切换播放设备，重新调用 start，传入指定的 streamType 即可。player 会自动停止播放，然后再以新的 streamType 重新开始播放。
//        // 如果需要从中断的地方继续播放，需要外面自己记住已经播放过的位置，然后在 onPrepared 回调中调用 seekTo
//        player.seekTo(pausedPosition);

//        // 主动停止播放
//        player.stop();
    }

    // 定义录音过程回调对象
    IAudioRecordCallback callback = new IAudioRecordCallback() {
        /**
         * 录音器已就绪，提供此接口用于在录音前关闭本地音视频播放（可选）
         */
        @Override
        public void onRecordReady() {
            Log.e(TAG, "onRecordReady: ");
        }

        /**
         * 开始录音回调
         *
         * @param audioFile  录音文件
         * @param recordType 文件类型
         */
        @Override
        public void onRecordStart(File audioFile, RecordType recordType) {
            Log.e(TAG, "onRecordStart: "+audioFile.getAbsolutePath() + ", "+audioFile.getName() + ", " +audioFile.getUsableSpace() +", "+ audioFile.getPath());
        }

        /**
         * 录音结束，成功
         *
         * @param audioFile   录音文件
         * @param audioLength 录音时间长度
         * @param recordType  文件类型
         */
        @Override
        public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
            Log.e(TAG, "onRecordSuccess: "+audioFile.getAbsolutePath() + ", "+audioFile.getName() + ", " +audioFile.getUsableSpace() +", "+ audioFile.getPath() + ", audioLength = " + audioLength);
            audioFileRecord = audioFile;
        }

        /**
         * 录音结束，出错
         */
        @Override
        public void onRecordFail() {
            Log.e(TAG, "onRecordFail: ");
        }

        /**
         * 录音结束， 用户主动取消录音
         */
        @Override
        public void onRecordCancel() {
            Log.e(TAG, "onRecordCancel: ");
        }

        /**
         * 到达指定的最长录音时间
         *
         * @param maxTime 录音文件时间长度限制
         */
        @Override
        public void onRecordReachedMaxTime(int maxTime) {
            Log.e(TAG, "onRecordReachedMaxTime: maxTime = " + maxTime);
        }
    };

    public void remoteRecord(View view){
        RetrofitWrapper ins = RetrofitWrapper.getInstance();
        IApiService ser = ins.create(IApiService.class);
        Call<String> str = ser.urlAddress();
        str.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.e(TAG, "onResponse: code = " + response.code());
                if (response.isSuccessful()) {
                    showToast("成功");
                    Log.e(TAG, "body = " + response.body());
                } else {
                    showToast("失败");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
