package com.biomhope.glass.face.bean;

public class PersonCheckResult {

    // 返回结果码 0成功其他失败
    public String exCode;

    // 返回结果信息
    public String exMsg;

    // 返回数据内容
    public PersonCheckResultData data;

    public class PersonCheckResultData {
        // 对比的相似度 f
        public double sim;
        // 默认阀值 f
        public double defaultSim;
        // 比对成功的模板编号 f
        public String faceId;
        // 当前业务渠道编号 f
        public String channel;
        // 业务成功后，接口日志记录的现场图片路径 f
        public String img1Src;
        // 业务成功后，接口日志记录的现场图片路径 f
        public String Img2Src;

        @Override
        public String toString() {
            return "PersonCheckResultData{" +
                    "sim=" + sim +
                    ", defaultSim=" + defaultSim +
                    ", faceId='" + faceId + '\'' +
                    ", channel='" + channel + '\'' +
                    ", img1Src='" + img1Src + '\'' +
                    ", Img2Src='" + Img2Src + '\'' +
                    '}';
        }
    }


}
