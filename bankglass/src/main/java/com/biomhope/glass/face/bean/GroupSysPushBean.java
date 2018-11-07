package com.biomhope.glass.face.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author $USER_NAME
 * create at : 2018-10-17
 * description :
 */
public class GroupSysPushBean {

    public String exCode;
    public String exMsg;
    public ArrayList<GroupUserInfo> result;

    @Override
    public String toString() {
        return "GroupSysPushBean{" +
                "exCode='" + exCode + '\'' +
                ", exMsg='" + exMsg + '\'' +
                '}';
    }


}
