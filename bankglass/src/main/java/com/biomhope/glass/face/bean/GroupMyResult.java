package com.biomhope.glass.face.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author $USER_NAME
 * create at : 2018-10-18
 * description :
 */
public class GroupMyResult implements Serializable {

    public String exCode;
    public String exMsg;
    public GroupRuslt result;

    public class GroupRuslt implements Serializable {
        public ArrayList<GroupUserInfo> viplist;
        public ArrayList<GroupUserInfo> commonlist;
        public ArrayList<GroupUserInfo> blacklist;
    }

}
