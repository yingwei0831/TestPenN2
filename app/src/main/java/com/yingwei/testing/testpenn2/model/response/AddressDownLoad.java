package com.yingwei.testing.testpenn2.model.response;

/**
 * Created by jiahe008_lvlanlan on 2017/4/12.
 */
public class AddressDownLoad {

    /**
     * whiteboardURL : http://nim.nos.netease.com/NDI2MTYxNA==/bmltYV8zNTMxMTUzODhfMTkwOTczMDU4MTI3MTA1XzE0OTE5NzcxMjE5MjlfYzNkNjc3YjktZWZmYy00MTU1LTkwZDQtMDJhYTY5ZWY1NTg1?download=353115388-190973058127105.aac
     */

//    private String whiteboardURL;
    private String aacFileUrl;
    private String gzFileUrl;

    public String getAacFileUrl() {
        return aacFileUrl;
    }

    public void setAacFileUrl(String aacFileUrl) {
        this.aacFileUrl = aacFileUrl;
    }

    public String getGzFileUrl() {
        return gzFileUrl;
    }

    public void setGzFileUrl(String gzFileUrl) {
        this.gzFileUrl = gzFileUrl;
    }

    @Override
    public String toString() {
        return "AddressDownLoad{" +
                "aacFileUrl='" + aacFileUrl + '\'' +
                ", gzFileUrl='" + gzFileUrl + '\'' +
                '}';
    }
}
