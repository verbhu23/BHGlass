package com.biomhope.glass.face.bean;

public class PersonCheckBean {

    // 客户标示 t
    private String id;
    // 图片获取方式 f
    private String pathflag;
    // 人脸照片 t
    private String img1;
    // 业务渠道编号 t
    private String channel;
    // 是否hack检测  hack传1时进行hack攻击检测，不传不进行检测 f
    private String hack;
    // 客户端IP号 f
    private String ip;
    // 客户端端口号 f
    private String port;
    // 调用者ID号 f
    private String caller;
    // 调用者机构号 f
    private String callOrg;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPathflag() {
        return pathflag;
    }

    public void setPathflag(String pathflag) {
        this.pathflag = pathflag;
    }

    public String getImg1() {
        return img1;
    }

    public void setImg1(String img1) {
        this.img1 = img1;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getHack() {
        return hack;
    }

    public void setHack(String hack) {
        this.hack = hack;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCallOrg() {
        return callOrg;
    }

    public void setCallOrg(String callOrg) {
        this.callOrg = callOrg;
    }

    @Override
    public String toString() {
        return "PersonCheckBean{" +
                "id='" + id + '\'' +
                ", pathflag='" + pathflag + '\'' +
                ", img1='" + img1 + '\'' +
                ", channel='" + channel + '\'' +
                ", hack='" + hack + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", caller='" + caller + '\'' +
                ", callOrg='" + callOrg + '\'' +
                '}';
    }

}
