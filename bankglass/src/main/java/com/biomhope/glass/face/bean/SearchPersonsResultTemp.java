package com.biomhope.glass.face.bean;

import java.util.List;

public class SearchPersonsResultTemp {

    public int result;
    public String serialNumber;
    public float defaultSim;
    public List<UserInfo> userInfos;
    public String exCode;
    public String exMsg;

    public class UserInfo {
        public int faceId;
        public float sim;
        public String userId;
        public String certNo;
        public String imgSrc;
        public String userName;
//        public String userName;
//        public String imgSrc;
//        public String certNo;

        @Override
        public String toString() {
            return "UserInfo{" +
                    "faceId=" + faceId +
                    ", userId='" + userId + '\'' +
                    ", sim=" + sim +
                    ", userName='" + userName + '\'' +
                    ", imgSrc='" + imgSrc + '\'' +
                    ", certNo='" + certNo + '\'' +
                    '}';
        }
    }

}
