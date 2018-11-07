package com.biomhope.glass.face.bean.eventvo;

import com.biomhope.glass.face.bean.GroupUserInfo;

/**
 * @author $USER_NAME
 * create at : 2018-10-23
 * description :
 */
public class EditUserMessage {

    public int type;
    public int position;
    public GroupUserInfo editUserInfo;

    public EditUserMessage(int type, int position, GroupUserInfo editUserInfo) {
        this.type = type;
        this.position = position;
        this.editUserInfo = editUserInfo;
    }

    @Override
    public String toString() {
        return "EditUserMessage{" +
                "type=" + type +
                ", position=" + position +
                ", editUserInfo=" + editUserInfo +
                '}';
    }

}
