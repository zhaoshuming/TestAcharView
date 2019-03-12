package com.howbuy.taoliang.testacharview;

/**
 * Created by tao.liang on 2016/10/17.
 */

public class DataEntity {
    private String oritentationVal;
    private String verticalVal;

    public DataEntity(String oritentationVal, String verticalVal) {
        this.oritentationVal = oritentationVal;
        this.verticalVal = verticalVal;
    }

    public String getOritentationVal() {
        return oritentationVal;
    }

    public void setOritentationVal(String oritentationVal) {
        this.oritentationVal = oritentationVal;
    }

    public String getVerticalVal() {
        return verticalVal;
    }

    public void setVerticalVal(String verticalVal) {
        this.verticalVal = verticalVal;
    }
}
