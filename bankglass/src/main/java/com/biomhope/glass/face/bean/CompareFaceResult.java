package com.biomhope.glass.face.bean;

/**
 * @author $USER_NAME
 * create at : 2018-10-18
 * description :
 */
public class CompareFaceResult {
//    public int result;
//    public float sim;
//    public String serialNumber;
//    public float defaultSim;

    public String exCode;
    public String exMsg;
    public Compare data;

    @Override
    public String toString() {
        return "CompareFaceResult{" +
                "exCode='" + exCode + '\'' +
                ", exMsg='" + exMsg + '\'' +
                ", data=" + data.toString() +
                '}';
    }

    public class Compare {
        public String sim;
        public String defaultSim;

        @Override
        public String toString() {
            return "Compare{" +
                    "sim='" + sim + '\'' +
                    ", defaultSim='" + defaultSim + '\'' +
                    '}';
        }
    }

}
