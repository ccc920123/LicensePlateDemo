
package com.kernal.plateid;
/**
 * 描述：
 * 编写人：陈渝金-pc:chenyujin
 * 时间： 2019/12/13 10:36
 * 修改人：
 * 修改时间：
 */
public class PlateRecognitionParameter {
    public byte[] picByte;
    public String pic;
    public int width;
    public int height;
    public String userData;
    public String devCode = "";
    public boolean isCheckDevType = false;
    public String dataFile = "";
    public String versionfile = "";
    public TH_PlateIDCfg plateIDCfg = new TH_PlateIDCfg();

    public PlateRecognitionParameter() {
    }
}
