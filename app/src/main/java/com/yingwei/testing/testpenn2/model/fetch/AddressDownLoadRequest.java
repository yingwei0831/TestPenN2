package com.yingwei.testing.testpenn2.model.fetch;

/**
 * Created by jiahe008_lvlanlan on 2017/4/12.
 */
public class AddressDownLoadRequest {

//    studentMobile(String,学生手机号)、teacherMobile(String,教师手机号)、taskId(String,作业Id)

    String studentMobile;
    String teacherMobile;
    String taskId;

    public AddressDownLoadRequest(String studentMobile, String teacherMobile, String taskId) {
        this.studentMobile = studentMobile;
        this.teacherMobile = teacherMobile;
        this.taskId = taskId;
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

    @Override
    public String toString() {
        return "AddressDownLoadRequest{" +
                "studentMobile='" + studentMobile + '\'' +
                ", teacherMobile='" + teacherMobile + '\'' +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}
