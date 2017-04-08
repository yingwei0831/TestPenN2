package com.yingwei.testing.testpenn2.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.yingwei.testing.testpenn2.R;
import com.yingwei.testing.testpenn2.im.confit.AuthPreferences;
import com.yingwei.testing.testpenn2.trans.util.log.LogUtil;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    private static final String KICK_OUT = "KICK_OUT";

    private boolean isKickOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        parseIntent();
    }

    private void parseIntent() {
        isKickOut = getIntent().getBooleanExtra(KICK_OUT, false);
        if (isKickOut) {
            Toast.makeText(getApplicationContext(), "你的账号被踢出下线，请确定张号信息安全！", Toast.LENGTH_SHORT).show();
        }
    }

    public static void start(Context context, boolean kickOut) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
        context.startActivity(intent);
    }

    public void login(View view){
        final String account = ((EditText)findViewById(R.id.et_name)).getText().toString();
        final String token = ((EditText)findViewById(R.id.et_pwd)).getText().toString();
        LoginInfo info = new LoginInfo(account, token); // config...
        RequestCallback<LoginInfo> callback =
                new RequestCallback<LoginInfo>() {
                    @Override
                    public void onSuccess(LoginInfo loginInfo) {
                        // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用
                        Log.e(TAG, "onSuccess: account = " + loginInfo.getAccount() + ", token = " + loginInfo.getToken() + ", appKey = " + loginInfo.getAppKey());
                        saveLoginInfo(account, token);
                        if (isKickOut){
                            startActivity(new Intent(LoginActivity.this, TestConnectActivity.class));
                        }else {
                            setResult(RESULT_OK);
                        }
                        finish();
                    }

                    @Override
                    public void onFailed(int i) {
                        Log.e(TAG, "onFailed: errorCode = " + i );
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        Log.e(TAG, throwable.getMessage());
                    }
                };
        NIMClient.getService(AuthService.class).login(info)
                .setCallback(callback);
    }

    private void saveLoginInfo(final String account, final String token) {
        AuthPreferences.saveUserAccount(account);
        AuthPreferences.saveUserToken(token);
    }

}
