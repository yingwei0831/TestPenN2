package com.yingwei.testing.testpenn2.trans;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

/**
 * Created by jiahe008_lvlanlan on 2017/3/30.
 */
public class PhoneCallStateObserver extends PhoneStateListener{

    private static PhoneCallStateObserver instance;
    private int state;

    public static PhoneCallStateObserver getInstance(){
        if (instance == null){
            synchronized (PhoneCallStateObserver.class){
                if (instance == null){
                    instance = new PhoneCallStateObserver();
                }
            }
        }
        return instance;
    }

    public int getPhoneCallState() {
        return state;
    }

    private PhoneCallStateObserver() {
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        switch (state){
            case TelephonyManager.CALL_STATE_IDLE: //电话是闲置的
                this.state = 0;
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK: //电话是接起的
                this.state = 1;
                break;
            case TelephonyManager.CALL_STATE_RINGING: //电话是响起的
                this.state = 2;
                break;
        }
    }

}
