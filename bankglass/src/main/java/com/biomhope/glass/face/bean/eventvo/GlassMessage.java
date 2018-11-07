package com.biomhope.glass.face.bean.eventvo;

/**
 * @author $USER_NAME
 * create at : 2018-10-09
 * description :
 */
public class GlassMessage {
    public String imgCropPath; // 当前人脸截图存储位置
    public int position; // 当前页面索引

    public GlassMessage(int position, String imgCropPath) {
        this.position = position;
        this.imgCropPath = imgCropPath;
    }

    @Override
    public String toString() {
        return "GlassMessage{" +
                "imgCropPath='" + imgCropPath + '\'' +
                ", position=" + position +
                '}';
    }
}
