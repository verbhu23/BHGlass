package com.biomhope.glass.face.bean;

public class SearchPersonsBean {

    private String img1;
    private String channel;

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

    public SearchPersonsBean(String img1, String channel) {
        this.img1 = img1;
        this.channel = channel;
    }

    public SearchPersonsBean(String img1) {
        this.img1 = img1;
    }

    @Override
    public String toString() {
        return "SearchPersonsBean{" +
                "img1='" + img1 + '\'' +
                ", channel='" + channel + '\'' +
                '}';
    }
}
