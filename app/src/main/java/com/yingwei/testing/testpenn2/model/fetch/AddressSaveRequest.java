package com.yingwei.testing.testpenn2.model.fetch;

/**
 * Created by jiahe008_lvlanlan on 2017/4/12.
 */
public class AddressSaveRequest {

    String channelid;
    String studentMobile;
    String teacherMobile;
    String taskId;
    String type;

    public AddressSaveRequest(String channelid, String studentMobile, String teacherMobile, String taskId, String type) {
        this.channelid = channelid;
        this.studentMobile = studentMobile;
        this.teacherMobile = teacherMobile;
        this.taskId = taskId;
        this.type = type;
    }

    public String getChannelid() {
        return channelid;
    }

    public void setChannelid(String channelid) {
        this.channelid = channelid;
    }

    public String getStudentMobile() {
        return studentMobile;
    }

    public void setStudentMobile(String studentMobile) {
        this.studentMobile = studentMobile;
    }

    public String getTeacherMobile() {
        return teacherMobile;
    }

    public void setTeacherMobile(String teacherMobile) {
        this.teacherMobile = teacherMobile;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AddressSaveRequest{" +
                "channelid='" + channelid + '\'' +
                ", studentMobile='" + studentMobile + '\'' +
                ", teacherMobile='" + teacherMobile + '\'' +
                ", taskId='" + taskId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
