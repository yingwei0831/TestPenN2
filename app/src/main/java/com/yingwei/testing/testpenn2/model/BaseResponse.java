package com.yingwei.testing.testpenn2.model;

/**
 * Created by jiahe008_lvlanlan on 2017/4/12.
 */
public class BaseResponse<T> {

    /**
     * result : success
     * data : {"whiteboardURL":"http://nim.nos.netease.com/NDI2MTYxNA==/bmltYV8zNTMxMTUzODhfMTkwOTczMDU4MTI3MTA1XzE0OTE5NzcxMjE5MjlfYzNkNjc3YjktZWZmYy00MTU1LTkwZDQtMDJhYTY5ZWY1NTg1?download=353115388-190973058127105.aac"}
     */

    private String result;
    private T data;

    private String errorCode;
    private String errorMsg;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "result='" + result + '\'' +
                ", data=" + data +
                ", errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
