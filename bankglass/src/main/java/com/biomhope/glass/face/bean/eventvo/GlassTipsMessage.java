package com.biomhope.glass.face.bean.eventvo;

/**
 * @author $USER_NAME
 * create at : 2018-10-19
 * description : 管理眼镜比对结果提示信息bean
 */
public class GlassTipsMessage {

    public String content;
    public String viplevel;

    public GlassTipsMessage(String content, String viplevel) {
        this.content = content;
        this.viplevel = viplevel;
    }

    @Override
    public String toString() {
        return "GlassTipsMessage{" +
                "content='" + content + '\'' +
                ", viplevel='" + viplevel + '\'' +
                '}';
    }
}
