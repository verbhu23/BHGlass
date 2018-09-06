package com.biomhope.glass.face.bean;

import java.util.List;

public class SearchPersonsResult {

    public String exCode;
    public String exMsg;
    public SearchPersonsResultData data;

    @Override
    public String toString() {
        return "SearchPersonsResult{" +
                "exCode='" + exCode + '\'' +
                ", exMsg='" + exMsg + '\'' +
                ", data=" + data +
                '}';
    }

    public class SearchPersonsResultData {
        public List<UserInfo> userInfos;

        public float defaultSim;

        @Override
        public String toString() {
            return "SearchPersonsResultData{" +
                    "userinfos=" + userInfos +
                    ", defaultSim=" + defaultSim +
                    '}';
        }
    }

    public class UserInfo {
        public int faceId;
        public float sim;
        public String userId;

//        public String userName;
//        public String imgSrc;
//        public String certNo;

        @Override
        public String toString() {
            return "UserInfo{" +
                    "faceId=" + faceId +
                    ", userId='" + userId + '\'' +
                    ", sim=" + sim +
//                    ", userName='" + userName + '\'' +
//                    ", imgSrc='" + imgSrc + '\'' +
//                    ", certNo='" + certNo + '\'' +
                    '}';
        }
    }

}
