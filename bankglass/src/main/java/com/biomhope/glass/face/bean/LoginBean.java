package com.biomhope.glass.face.bean;

/**
 * @author $USER_NAME
 * create at : 2018-10-16
 * description :
 */
public class LoginBean {
    public String userName;
    public String password;

    public LoginBean(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginBean{" +
                "userName=" + userName +
                ", password='" + password + '\'' +
                '}';
    }
}
