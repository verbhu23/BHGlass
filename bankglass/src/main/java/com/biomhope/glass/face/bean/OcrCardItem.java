package com.biomhope.glass.face.bean;

/**
 * @author $USER_NAME
 * create at : 2018-10-26
 * description :
 */
public class OcrCardItem {

    public int cardId;
    public String cardName;

    public OcrCardItem(int cardId, String cardName) {
        this.cardId = cardId;
        this.cardName = cardName;
    }

    @Override
    public String toString() {
        return "OcrCardItem{" +
                "cardId=" + cardId +
                ", cardName='" + cardName + '\'' +
                '}';
    }
}
