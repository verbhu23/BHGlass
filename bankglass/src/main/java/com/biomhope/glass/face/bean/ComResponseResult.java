package com.biomhope.glass.face.bean;

/**
 * @author $USER_NAME
 * create at : 2018-10-23
 * description :
 */
public class ComResponseResult {

    public String exCode;
    public String exMsg;

    public ComResponseResult(String exCode, String exMsg) {
        this.exCode = exCode;
        this.exMsg = exMsg;
    }

    @Override
    public String toString() {
        return "ComResponseResult{" +
                "exCode='" + exCode + '\'' +
                ", exMsg='" + exMsg + '\'' +
                '}';
    }
}
