package com.biomhope.glass.face.bean;

import java.util.ArrayList;

/**
 * @author $USER_NAME
 * create at : 2018-10-12
 * description :
 */
public class GroupMyItemBean {

    private String groupName;
    private int members;
    private ArrayList<GroupUserInfo> list;

    public GroupMyItemBean(String groupName, int members) {
        this.groupName = groupName;
        this.members = members;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public ArrayList<GroupUserInfo> getList() {
        return list;
    }

    public void setList(ArrayList<GroupUserInfo> list) {
        this.list = list;
        if (list != null && !list.isEmpty()) {
            this.members = list.size();
        }else {
            this.members = 0;
        }
    }

    public void addNewRegister(GroupUserInfo newInfo) {
        if (list != null) {
            list.add(0, newInfo);
            this.members = list.size();
        }
    }

    @Override
    public String toString() {
        return "GroupMyItemBean{" +
                "groupName='" + groupName + '\'' +
                ", members=" + members +
                '}';
    }
}
