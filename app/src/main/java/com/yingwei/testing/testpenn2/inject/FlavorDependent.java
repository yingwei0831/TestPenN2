package com.yingwei.testing.testpenn2.inject;

import android.app.Activity;

import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser;

/**
 * Created by huangjun on 2016/3/15.
 */
public class FlavorDependent implements IFlavorDependent{

    @Override
    public String getFlavorName() {
        return "education";
    }

    @Override
    public Class<? extends Activity> getMainClass() {
        return null;
    }

    @Override
    public MsgAttachmentParser getMsgAttachmentParser() {
        return null;
    }

    @Override
    public void onLogout() {

    }

    public static FlavorDependent getInstance() {
        return InstanceHolder.instance;
    }

    public static class InstanceHolder {
        public final static FlavorDependent instance = new FlavorDependent();
    }

    @Override
    public void onApplicationCreate() {

    }
}
