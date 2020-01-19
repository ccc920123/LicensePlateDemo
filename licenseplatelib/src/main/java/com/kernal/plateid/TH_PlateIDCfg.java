
package com.kernal.plateid;
/**
 * 描述：
 * 编写人：陈渝金-pc:chenyujin
 * 时间： 2019/12/13 10:36
 * 修改人：
 * 修改时间：
 */
public class TH_PlateIDCfg {
    public int nMinPlateWidth = 80;
    public int nMaxPlateWidth = 400;
    public int bVertCompress = 0;
    public int bIsFieldImage = 0;
    public int bOutputSingleFrame = 1;
    public int bMovingImage = 0;
    public int bIsNight = 0;
    public int nImageFormat = 1;
    public int nLastError = 0;
    public int nErrorModelSN = 0;
    public String reserved = "";
    public int bRotate = 0;
    public int left;
    public int right;
    public int top;
    public int bottom;
    //add
    public int scale = 1;
    public TH_PlateIDCfg() {
    }
}
