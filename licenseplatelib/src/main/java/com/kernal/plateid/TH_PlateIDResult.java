
package com.kernal.plateid;
/**
 * 描述：
 * 编写人：陈渝金-pc:chenyujin
 * 时间： 2019/12/13 10:36
 * 修改人：
 * 修改时间：
 */
public class TH_PlateIDResult {
    public String license = "";
    public String color = "";
    public int nColor;
    public int nType;
    public int nConfidence;
    public int nBright;
    public int nDirection;
    public int left;
    public int top;
    public int right;
    public int bottom;
    public byte[] pbyBits;
    public int nTime;
    public int nCarBright = 0;
    public int nCarColor = 0;
    public String reserved = "";

    public TH_PlateIDResult() {
    }
}
