package com.biomhope.glass.face.bean;

/**
 * @author $USER_NAME
 * create at : 2018-10-16
 * description :
 */
public class RegisterBean {
    public String userName;
    public String password;
    public String superPwd;

    public RegisterBean(String userName, String password, String superPwd) {
        this.userName = userName;
        this.password = password;
        this.superPwd = superPwd;
    }

    @Override
    public String toString() {
        return "RegisterBean{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", superPwd='" + superPwd + '\'' +
                '}';
    }
}
