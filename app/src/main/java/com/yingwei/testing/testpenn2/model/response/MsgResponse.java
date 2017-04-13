package com.yingwei.testing.testpenn2.model.response;

/**
 * Created by jiahe008_lvlanlan on 2017/4/11.
 */
public class MsgResponse {

    String msg;

    public MsgResponse(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "MsgResponse{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
