package com.yingwei.testing.testpenn2;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.logging.Logger;

import kr.neolab.sdk.ink.structure.Stroke;
import kr.neolab.sdk.pen.IPenCtrl;
import kr.neolab.sdk.pen.PenCtrl;
import kr.neolab.sdk.pen.offline.OfflineFileParser;
import kr.neolab.sdk.pen.penmsg.IPenMsgListener;
import kr.neolab.sdk.pen.penmsg.PenMsg;
import kr.neolab.sdk.pen.penmsg.PenMsgType;

public class TestConnectActivity extends AppCompatActivity implements IPenMsgListener {

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

    // Notification
    protected NotificationCompat.Builder mBuilder;
    protected NotificationManager mNotifyManager;

//    public InputPasswordDialog inputPassDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_connect);

        // create an instance of a pen controller
        iPenCtrl = PenCtrl.getInstance();

        // start a pen controller
        iPenCtrl.startup();

        // add event listener(IPenMsgListener) to a pen controller
        iPenCtrl.setListener(this);


        //after pair, should connect
//        String mac_address = "9c7bd20001d4";
//        penCtrl.connect(mac_address);

        mSampleView = new SampleView( this );

        setContentView( mSampleView );

//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int viewWidth  = dm.widthPixels;
//        int viewHeight  = dm.heightPixels;
//        ViewGroup.LayoutParams params = mSampleView.getLayoutParams();
//        params.width = viewWidth;
//        params.height = viewHeight;
//        mSampleView.setLayoutParams(params);

        PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 0, new Intent(Const.Broadcast.ACTION_PEN_UPDATE), PendingIntent.FLAG_UPDATE_CURRENT ); //"firmware_update"

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setContentTitle( "Update Pen" );
        mBuilder.setSmallIcon( R.mipmap.ic_launcher_n );
        mBuilder.setContentIntent( pendingIntent );
    }

    public void connect() {
        startActivityForResult(new Intent(getApplicationContext(), DeviceListActivity.class), 2);
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

    @Override
    public void onReceiveDot(int sectionId, int ownerId, int noteId, int pageId,
                             int x, int y, int fx, int fy,
                             int pressure, long timestamp, int type, int color) {
        Log.d(TAG, "sectionId = " + sectionId + ", " + "ownerId = " + ownerId + ", " + "noteId = " + noteId + ", " + "pageId = " + pageId +
                ", " + "x = " + x + ", " + "y = " + y + ", " + "fx = " + fx + ", " + "fy = " + fy +
                ", " + "pressure = " + pressure + ", " + "timestamp = " + timestamp + ", " + "type = " + type + ", " + "color = " + color );
        sendPenDotByBroadcast( sectionId, ownerId, noteId, pageId, x, y, fx, fy, pressure, timestamp, type, color );
        //将dot转换为屏幕上的坐标
//        resizePoint(x, y, fx, fy);
    }

//    private void resizePoint(int x, int y, int fx, int fy) {
//        float x_d = x + 0.01f * fx;
//        float y_d = y + 0.01f * fy;
//        float x_p = x_d / 2.371f;
//        float y_p = y_d / 2.371f;
//        Log.d(TAG, "x_p = " + x_p +", y_p = " + y_p);
//    }

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
        sendPenMsgByBroadcast( penMsg );
    }

    private void sendPenMsgByBroadcast( PenMsg penMsg )
    {
        Intent i = new Intent( Const.Broadcast.ACTION_PEN_MESSAGE );
        i.putExtra( Const.Broadcast.MESSAGE_TYPE, penMsg.getPenMsgType() );
        i.putExtra( Const.Broadcast.CONTENT, penMsg.getContent() );

        sendBroadcast( i );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
        IntentFilter filter = new IntentFilter( Const.Broadcast.ACTION_PEN_MESSAGE );
        filter.addAction( Const.Broadcast.ACTION_PEN_DOT );
//        filter.addAction( "firmware_update" );
        filter.addAction( Const.Broadcast.ACTION_PEN_UPDATE );

        registerReceiver( mBroadcastReceiver, filter );
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver( mBroadcastReceiver );
    }

    private void sendPenDotByBroadcast( int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int pressure, long timestamp, int type, int color )
    {
        Intent i = new Intent( Const.Broadcast.ACTION_PEN_DOT );
        i.putExtra( Const.Broadcast.SECTION_ID, sectionId );
        i.putExtra( Const.Broadcast.OWNER_ID, ownerId );
        i.putExtra( Const.Broadcast.NOTE_ID, noteId );
        i.putExtra( Const.Broadcast.PAGE_ID, pageId );
        i.putExtra( Const.Broadcast.X, x );
        i.putExtra( Const.Broadcast.Y, y );
        i.putExtra( Const.Broadcast.FX, fx );
        i.putExtra( Const.Broadcast.FY, fy );
        i.putExtra( Const.Broadcast.PRESSURE, pressure );
        i.putExtra( Const.Broadcast.TIMESTAMP, timestamp );
        i.putExtra( Const.Broadcast.TYPE, type );
        i.putExtra( Const.Broadcast.COLOR, color );

        sendBroadcast( i );
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent )
        {
            String action = intent.getAction();

            if ( Const.Broadcast.ACTION_PEN_MESSAGE.equals( action ) )
            {
                int penMsgType = intent.getIntExtra( Const.Broadcast.MESSAGE_TYPE, 0 );
                String content = intent.getStringExtra( Const.Broadcast.CONTENT );

                handleMsg( penMsgType, content );
            }
            else if ( Const.Broadcast.ACTION_PEN_DOT.equals( action ) )
            {
                int sectionId = intent.getIntExtra( Const.Broadcast.SECTION_ID, 0 );
                int ownerId = intent.getIntExtra( Const.Broadcast.OWNER_ID, 0 );
                int noteId = intent.getIntExtra( Const.Broadcast.NOTE_ID, 0 );
                int pageId = intent.getIntExtra( Const.Broadcast.PAGE_ID, 0 );
                int x = intent.getIntExtra( Const.Broadcast.X, 0 );
                int y = intent.getIntExtra( Const.Broadcast.Y, 0 );
                int fx = intent.getIntExtra( Const.Broadcast.FX, 0 );
                int fy = intent.getIntExtra( Const.Broadcast.FY, 0 );
                int force = intent.getIntExtra( Const.Broadcast.PRESSURE, 0 );
                long timestamp = intent.getLongExtra( Const.Broadcast.TIMESTAMP, 0 );
                int type = intent.getIntExtra( Const.Broadcast.TYPE, 0 );
                int color = intent.getIntExtra( Const.Broadcast.COLOR, 0 );

                handleDot( sectionId, ownerId, noteId, pageId, x, y, fx, fy, force, timestamp, type, color );
            }
            else if ( Const.Broadcast.ACTION_PEN_UPDATE.equals( action ))
            {
                iPenCtrl.suspendPenUpgrade();
            }
        }
    };

    private void handleDot( int sectionId, int ownerId, int noteId, int pageId, int x, int y, int fx, int fy, int force, long timestamp, int type, int color )
    {
        mSampleView.addDot( sectionId, ownerId, noteId, pageId, x, y, fx, fy, force, timestamp, type, color );
    }

    private void handleMsg( int penMsgType, String content )
    {
        Log.d( TAG, "handleMsg : " + penMsgType );

        switch ( penMsgType )
        {
            // Message of the attempt to connect a pen
            case PenMsgType.PEN_CONNECTION_TRY:

                Util.showToast( this, "try to connect." );

                break;

            // Pens when the connection is completed (state certification process is not yet in progress)
            case PenMsgType.PEN_CONNECTION_SUCCESS:

                Util.showToast( this, "connection is successful." );

                break;

            // Message when a connection attempt is unsuccessful pen
            case PenMsgType.PEN_CONNECTION_FAILURE:

                Util.showToast( this, "connection has failed." );

                break;

            // When you are connected and disconnected from the state pen
            case PenMsgType.PEN_DISCONNECTED:

                Util.showToast( this, "connection has been terminated." );

                break;

            // Pen transmits the state when the firmware update is processed.
            case PenMsgType.PEN_FW_UPGRADE_STATUS:
            {
                try
                {
                    JSONObject job = new JSONObject( content );

                    int total = job.getInt( Const.JsonTag.INT_TOTAL_SIZE );
                    int sent = job.getInt( Const.JsonTag.INT_SENT_SIZE );

                    this.onUpgrading( total, sent );

                    Log.d( TAG, "pen fw upgrade status => total : " + total + ", progress : " + sent );
                }
                catch ( JSONException e )
                {
                    e.printStackTrace();
                }
            }
            break;

            // Pen firmware update is complete
            case PenMsgType.PEN_FW_UPGRADE_SUCCESS:

                this.onUpgradeSuccess();

                Util.showToast( this, "file transfer is complete." );

                break;

            // Pen Firmware Update Fails
            case PenMsgType.PEN_FW_UPGRADE_FAILURE:

                this.onUpgradeFailure( false );

                Util.showToast( this, "file transfer has failed." );

                break;

            // When the pen stops randomly during the firmware update
            case PenMsgType.PEN_FW_UPGRADE_SUSPEND:

                this.onUpgradeFailure( true );

                Util.showToast( this, "file transfer is suspended." );

                break;

            // Offline Data List response of the pen
            case PenMsgType.OFFLINE_DATA_NOTE_LIST:

                try
                {
                    JSONArray list = new JSONArray( content );

                    for ( int i = 0; i < list.length(); i++ )
                    {
                        JSONObject jobj = list.getJSONObject( i );

                        int sectionId = jobj.getInt( Const.JsonTag.INT_SECTION_ID );
                        int ownerId = jobj.getInt( Const.JsonTag.INT_OWNER_ID );
                        int noteId = jobj.getInt( Const.JsonTag.INT_NOTE_ID );

                        Log.d( TAG, "offline(" + (i + 1) + ") note => sectionId : " + sectionId + ", ownerId : " + ownerId + ", noteId : " + noteId );
                        iPenCtrl.reqOfflineData(sectionId, ownerId, noteId); //2017.3.23 add by lanlan, note below
                    }
                }
                catch ( JSONException e )
                {
                    e.printStackTrace();
                }

                // if you want to get offline data of pen, use this function.
                // you can call this function, after complete download.
                //
                // iPenCtrl.reqOfflineData( sectionId, ownerId, noteId );

                Util.showToast( this, "offline data list is received." );

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
            case PenMsgType.OFFLINE_DATA_SEND_STATUS:
            {
                try
                {
                    JSONObject job = new JSONObject( content );

                    int total = job.getInt( Const.JsonTag.INT_TOTAL_SIZE );
                    int received = job.getInt( Const.JsonTag.INT_RECEIVED_SIZE );

                    Log.d( TAG, "offline data send status => total : " + total + ", progress : " + received );
                }
                catch ( JSONException e )
                {
                    e.printStackTrace();
                }
            }
            break;

            // When the file transfer process of the download offline
            case PenMsgType.OFFLINE_DATA_FILE_CREATED:
            {
                try
                {
                    JSONObject job = new JSONObject( content );

                    int sectionId = job.getInt( Const.JsonTag.INT_SECTION_ID );
                    int ownerId = job.getInt( Const.JsonTag.INT_OWNER_ID );
                    int noteId = job.getInt( Const.JsonTag.INT_NOTE_ID );
                    int pageId = job.getInt( Const.JsonTag.INT_PAGE_ID );

                    String filePath = job.getString( Const.JsonTag.STRING_FILE_PATH );

                    Log.d( TAG, "offline data file created => sectionId : " + sectionId + ", ownerId : " + ownerId + ", noteId : " + noteId + ", pageId : " + pageId + " filePath : " + filePath );
                }
                catch ( JSONException e )
                {
                    e.printStackTrace();
                }
            }
            break;

            // Ask for your password in a message comes when the pen
            case PenMsgType.PASSWORD_REQUEST:
            {
                int retryCount = -1, resetCount = -1;

                try
                {
                    JSONObject job = new JSONObject( content );

                    retryCount = job.getInt( Const.JsonTag.INT_PASSWORD_RETRY_COUNT );
                    resetCount = job.getInt( Const.JsonTag.INT_PASSWORD_RESET_COUNT );
                }
                catch ( JSONException e )
                {
                    e.printStackTrace();
                }

//                inputPassDialog = new InputPasswordDialog( this, this, retryCount, resetCount ); //暂时先不考虑输入密码的情况
//                inputPassDialog.show();
            }
            break;
        }
    }

    private void parseOfflineData()
    {
        // obtain saved offline data file list
        String[] files = OfflineFileParser.getOfflineFiles();

        if ( files == null || files.length == 0 )
        {
            return;
        }

        for ( String file : files )
        {
            try
            {
                // create offline file parser instance
                OfflineFileParser parser = new OfflineFileParser( file );

                // parser return array of strokes
                Stroke[] strokes = parser.parse();

                if ( strokes != null )
                {
                    mSampleView.addStrokes( strokes );
                }

                // delete data file
                parser.delete();
                parser = null;
                Log.e( TAG, "parse file finished.");
            }
            catch ( Exception e )
            {
                Log.e( TAG, "parse file exeption occured.", e );
            }
        }
    }

    private void onUpgrading( int total, int progress )
    {
        mBuilder.setContentText( "Sending" ).setProgress( total, progress, false );
        mNotifyManager.notify( 0, mBuilder.build() );
    }

    private void onUpgradeFailure( boolean isSuspend )
    {
        if ( isSuspend )
        {
            mBuilder.setContentText( "file transfer is suspended." ).setProgress( 0, 0, false );
        }
        else
        {
            mBuilder.setContentText( "file transfer has failed." ).setProgress( 0, 0, false );
        }
        mNotifyManager.notify( 0, mBuilder.build() );
    }

    private void onUpgradeSuccess()
    {
        mBuilder.setContentText( "The file transfer is complete." ).setProgress( 0, 0, false );
        mNotifyManager.notify( 0, mBuilder.build() );
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
