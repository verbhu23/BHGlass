package com.biomhope.glass.face.home.session;

import android.net.Uri;

public class ChatItemBean {

    // 默认为0
    public static final int RIGHT_CLIENT_SESSION = 0;
    public static final int LEFT_SERVER_SESSION = 1;

    public int type; // 界面显示类型 1:left服务端 0:right客户端
    public String msg;
    public String userIcon;
    public String voiceFilePath;
    public int voiceLength;
    public boolean isPlayed; // 服务器音频是否播放过
    public boolean isPlaying; // 记录当前是否正在播放
    public String imgPath; // 显示图片路径
    public ImageSize imageSize;

    public ChatItemBean() {
    }

    public ChatItemBean(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ChatItemBean{" +
                "type=" + type +
                ", msg='" + msg + '\'' +
                ", userIcon='" + userIcon + '\'' +
                '}';
    }

}
